/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.stanbol.enhancer.nlp.freeling.impl;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.felix.scr.annotations.Reference;
import org.apache.stanbol.enhancer.nlp.freeling.Analyzer;
import org.apache.stanbol.enhancer.nlp.freeling.pool.ResourcePool.ResourceFactory;
import org.apache.stanbol.enhancer.nlp.model.AnalysedText;
import org.apache.stanbol.enhancer.nlp.model.AnalysedTextFactory;
import org.apache.stanbol.enhancer.servicesapi.ContentItemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.upc.freeling.ChartParser;
import edu.upc.freeling.DepTxala;
import edu.upc.freeling.HmmTagger;
import edu.upc.freeling.Maco;
import edu.upc.freeling.MacoOptions;
import edu.upc.freeling.Nec;
import edu.upc.freeling.Senses;
import edu.upc.freeling.Splitter;
import edu.upc.freeling.Tokenizer;
import edu.upc.freeling.UkbWrap;
import edu.upc.freeling.Util;


/**
 * Creates Freeling {@link AnalyzerImpl} by using a configureable thread pool.
 * @author Rupert Westenthaler
 * @author David Riccitelli
 */
public class AnalyzerFactory implements ResourceFactory<AnalyzerImpl> {

    protected final Logger log = LoggerFactory.getLogger(AnalyzerFactory.class);
    
    public static final String PROPERTY_LANGUAGE = "language";
    /**
     * Expects the {@link File} with the Freeling configuration as value
     */
    public static final String PROPERTY_CONFIG_FILE = "config";

    private final String freelingSharePath;
    private final ExecutorService executorService;
    private final String locale;

    @Reference
    private AnalysedTextFactory _analysedTextFactory;
    
    @Reference
    private ContentItemFactory _contentItemFactory;

    /**
     * Creates a Freeling Analyzer Factory for the parsed parameter
     * @param freelingSharePath the Freeling shared resources path
     * @param factoryThreadPool The parsed {@link ExecutorService} is used
     * to create {@link Analyzer} instances.
     */
    public AnalyzerFactory(final String freelingLibPath, 
            final String freelingSharePath, String locale, 
            ExecutorService factoryThreadPool) {
        //set the freeling locale
        if(freelingLibPath == null){
            throw new IllegalArgumentException("The path to the Freeling native "
                + "lib MUST NOT be NULL!");
        }
        if(freelingSharePath == null){
            throw new IllegalArgumentException("The parsed Freeling Shared Directory"
                + "MUST NOT be NULL!");
        }
        if(locale == null){
            throw new IllegalArgumentException("The parsed Freeling Locale"
                + "MUST NOT be NULL!");
        }
        if(factoryThreadPool == null){
            throw new IllegalArgumentException("The parsed ExecutorService"
                + "MUST NOT be NULL!");
        }
        this.locale = locale;
        this.freelingSharePath = freelingSharePath;
        this.executorService = factoryThreadPool;
        //check for the native freeling lib
        NativeLibsUtil.ensureNativeLib(freelingLibPath);
        log.debug("Setting locale [{}].", locale);
        Util.initLocale(locale);
    }
    
    /**
     * Getter for the {@link AnalysedTextFactory}. Returns the 
     * {@link AnalysedTextFactory#getDefaultInstance() default instance} if no
     * OSGI service ia available.
     * @return the {@link AnalysedText}
     */
    private AnalysedTextFactory getAnalysedTextFactory(){
        if(_analysedTextFactory == null){
            return AnalysedTextFactory.getDefaultInstance();
        }
        return _analysedTextFactory;
    }
    private ContentItemFactory getContentItemFactory(){
        if(_contentItemFactory == null){
            ServiceLoader<ContentItemFactory> loader = ServiceLoader.load(ContentItemFactory.class);
            Iterator<ContentItemFactory> cifIt = loader.iterator();
            if(cifIt.hasNext()){
                _contentItemFactory = cifIt.next();
            } else {
                throw new IllegalStateException("No ContentItemFactory Implementation available!");
            }
        }
        return _contentItemFactory;
    }
    
    @Override
    public Future<AnalyzerImpl> createResource(Map<String,Object> context) {
        final String language = (String)context.get(PROPERTY_LANGUAGE);
        if(language == null){
            throw new IllegalArgumentException("The property '"+PROPERTY_LANGUAGE 
                + "is missing in the parsed Context "+context);
        }
        final File configFile = (File)context.get(PROPERTY_CONFIG_FILE);
        if(configFile == null || !configFile.isFile()){
            throw new IllegalArgumentException("The property '"+PROPERTY_CONFIG_FILE 
                +"' MUST BE present AND must be set to a File that exists (value: "
                +configFile+")!");
        }
        log.info("Request to create Analyzer for language {}",language);
        final long request = System.currentTimeMillis();
        return executorService.submit(new Callable<AnalyzerImpl>() {

            @Override
            public AnalyzerImpl call() throws Exception {
                long start = System.currentTimeMillis();
                log.info("createing Analyzer for language {} ({}ms after request)",
                    language, start-request);
                try {
                    return createAnalyzer(configFile, language);
                } finally {
                    long created = System.currentTimeMillis();
                    log.info("  ... create in {}ms ({}ms after request)",created-start,created-request);
                }
            }
            
        });
    }
    
    @Override
    public void closeResource(final Object resource, final Map<String,Object> context) {
        final AnalyzerImpl analyzer = AnalyzerImpl.class.cast(resource);
        log.info("request to close Analyzer for language {}",analyzer.getLanguage());
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                log.info("close Analyzer for language {}",analyzer.getLanguage());
                analyzer.close();
           }
        });
     }
    
    private AnalyzerImpl createAnalyzer(final File configFile, final String language) {
        log.info("... creating Freeling Analyzer for language '{}' based on config [{}]",
            language, configFile);

        final FreelingProperties properties = new FreelingProperties(
            configFile.getPath(), freelingSharePath);
        String configLang = properties.getLanguage();
        if(!language.equals(configLang)){
            throw new IllegalStateException("The language of the config '"+configLang
                + "' does not match the configured one '"+language+" (config: "
                + configFile+")!");
        }
        String configLocale = properties.getLocale();
        if(!locale.equals(configLocale)){ //maybe this should be an exception
            log.warn("The locale '{}' of the config for language '{}' does not match "
                + "the locale '{}' set to Freeling. This might be problematic if "
                + "the used Charsets do not match (default := en_US.UTF8)", 
                new Object[]{ configLocale, language, locale});
        }
        final MacoOptions macoOptions = new MacoOptions(
                properties.getLanguage());

        log.debug("Setting MACO options [{}][{}][{}][{}][{}][{}][{}][{}][{}][{}][{}].",
                new Object[] { false, properties.isAffixAnalysis(),
                        properties.isMultiwordsDetection(),
                        properties.isNumbersDetection(),
                        properties.isPunctuationDetection(),
                        properties.isDatesDetection(),
                        properties.isQuantitiesDetection(),
                        properties.isDictionarySearch(),
                        properties.isProbabilityAssignment(),
                        properties.isNeRecognition(),
                        properties.isOrtographicCorrection() });

        macoOptions.setActiveModules(false, properties.isAffixAnalysis(),
                properties.isMultiwordsDetection(),
                properties.isNumbersDetection(),
                properties.isPunctuationDetection(),
                properties.isDatesDetection(),
                properties.isQuantitiesDetection(),
                properties.isDictionarySearch(),
                properties.isProbabilityAssignment(),
                properties.isNeRecognition(),
                properties.isOrtographicCorrection());

        log.debug("Setting MACO data files [{}][{}][{}][{}][{}][{}][{}][{}][{}].",
                new Object[] { "", properties.getLocutionsFile(),
                        properties.getQuantitiesFile(),
                        properties.getAffixFile(),
                        properties.getProbabilityFile(),
                        properties.getDictionaryFile(),
                        properties.getNpDataFile(),
                        properties.getPunctuationFile(),
                        properties.getCorrectorFile() });

        macoOptions.setDataFiles("", properties.getLocutionsFile(),
                properties.getQuantitiesFile(), properties.getAffixFile(),
                properties.getProbabilityFile(),
                properties.getDictionaryFile(), properties.getNpDataFile(),
                properties.getPunctuationFile(), properties.getCorrectorFile());

        log.debug("Creating the tokenizer [{}].",
                properties.getTokenizerFile());

        // Create analyzers.
        final Tokenizer tokenizer = new Tokenizer(properties.getTokenizerFile());

        log.debug("Creating the splitter [{}].", properties.getSplitterFile());
        final Splitter splitter = new Splitter(properties.getSplitterFile());

        AnalyzerImpl analyzer = new AnalyzerImpl(
            getContentItemFactory(),getAnalysedTextFactory(),
            language,tokenizer, splitter, properties.isAlwaysFlush());
        
        log.debug("Creating the MACO analyzer.");
        analyzer.setMaco(new Maco(macoOptions));

        log.debug("Creating the tagger.");
        analyzer.setHmmTagger(new HmmTagger(properties.getLanguage(),
                properties.getTaggerHMMFile(), properties.isTaggerRetokenize(),
                properties.getTaggerForceSelect()));

        ChartParser chartParser = null;
        final File grammarFile = new File(properties.getGrammarFile());
        if (grammarFile.exists() && !grammarFile.isDirectory()) {
            log.debug("Creating the chart parser.");
            chartParser = new ChartParser(properties.getGrammarFile());
            analyzer.setChartParser(chartParser);
        }

        final File depTxalaFile = new File(properties.getDepTxalaFile());
        if (null != chartParser && depTxalaFile.exists()
                && !depTxalaFile.isDirectory()) {
            log.debug("Creating the dependencies analyzer.");
            analyzer.setDepTxala(new DepTxala(properties.getDepTxalaFile(),
                    chartParser.getStartSymbol()));
        }

        if (properties.isNeClassification()) {
            File necFile = new File(properties.getNecFile());
            if (necFile.exists() && !necFile.isDirectory()) {
                log.debug("Creating the named entity classification.");
                analyzer.setNec(new Nec(properties.getNecFile()));
            }
        }

        // Instead of "UkbWrap", you can use a "Senses" object, that simply
        // gives all possible WN senses, sorted by frequency.
        final File senseConfigFile = new File(properties.getSenseConfigFile());
        if (senseConfigFile.exists() && senseConfigFile.isFile()) {
            log.debug("Creating the senses tool.");
            analyzer.setSenses(new Senses(properties.getSenseConfigFile()));
        }

        final File ukbConfigFile = new File(properties.getUkbConfigFile());
        if (ukbConfigFile.exists() && ukbConfigFile.isFile()) {
            log.debug("Creating the disambiguation tool.");
            analyzer.setUkbWrap(new UkbWrap(properties.getUkbConfigFile()));
        }
        
        return analyzer;
    }
}
