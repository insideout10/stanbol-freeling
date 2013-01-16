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
package org.apache.stanbol.enhancer.nlp.freeling;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.stanbol.enhancer.nlp.freeling.impl.AnalyzerFactory;
import org.apache.stanbol.enhancer.nlp.freeling.impl.LangIdFactory;
import org.apache.stanbol.enhancer.nlp.freeling.pool.ResourcePool;
import org.apache.stanbol.enhancer.nlp.freeling.pool.ResourcePool.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper class that provides easy access to the Freeling Analyzers and Language
 * Identification
 * 
 * TODO: has still a dependency to the impl package :(
 * 
 * @author Rupert Westenthaler
 *
 */
public class Freeling {
    
    private final Logger log = LoggerFactory.getLogger(Freeling.class);
    
    public final static String DEFAULT_FREELING_LIB_PATH = 
            System.getProperty("os.name").toLowerCase().indexOf("mac os x") >=0 ?
                    "lib/libfreeling_javaAPI.jnilib" : 
                        "lib/libfreeling_javaAPI.so";
    public final static String DEFAULT_RELATIVE_CONFIGURATION_PATH = "config";
    public final static String DEFAULT_CONFIGURATION_FILENAME_SUFFIX = ".cfg";
    //public final static String DEFAULT_FREELING_SHARE_PATH = "/usr/local/Cellar/freeling/3.0/share/freeling";
    /**
     * By default only a single thread is used to initialize Freeling Analyzers
     * as multi-threaded initialisation may cause crashes during the initialization.
     */
    public final static int DEFAULT_CONCURRENT_THREADS = 1;
    public final static String DEFAULT_FREELING_LOCALE = "default";

    private static final int DEFAULT_ANALYZER_POOL_SIZE = 10;
    private static final int DEFAULT_MIN_ANALYZER_QUEUE_SIZE = 1;

    /**
     * Map holding the ResourcePools for the supported languages. does have the
     * same keys as the {@link #supportedLanguages}.
     */
    private final Map<String,ResourcePool<Analyzer>> analyzerPools = 
            Collections.synchronizedMap(new HashMap<String,ResourcePool<Analyzer>>());

    protected final ExecutorService freelingInitThreadPool;

    private ResourcePool<LanguageIdentifier> langIdPool;

    private boolean closed;
    
    
    /**
     * Creates a Freeling Analyzer Factory for the parsed Freeling shared path
     * directory.
     * @param freelingSharePath
     */
    public Freeling(String freelingSharePath, int poolSize){
        this(freelingSharePath,poolSize,DEFAULT_MIN_ANALYZER_QUEUE_SIZE);
    }
    /**
     * Create a Freeling instance for the parsed shared resource directory,
     * Analyzer pool size and minimum Analyzer queue size. 
     * @param freelingSharePath the shared resource path
     * @param poolSize the maximum number of Analyzers instantiated for a language
     * @param minQueueSize defines how many instances are created at startup and
     * also when additional Analyzer instances are created. Set to <code>0</code>
     * to deactivate this feature (default=1 ... Two instances at startup and
     * creates additional instances if only 1 instance is left in the queue)
     */
    public Freeling(String freelingSharePath, int poolSize, int minQueueSize){
        this(FilenameUtils.concat(freelingSharePath, DEFAULT_RELATIVE_CONFIGURATION_PATH),
            DEFAULT_CONFIGURATION_FILENAME_SUFFIX, freelingSharePath,
            DEFAULT_FREELING_LIB_PATH, DEFAULT_FREELING_LOCALE, 
            DEFAULT_CONCURRENT_THREADS,
            poolSize <= 0 ? DEFAULT_ANALYZER_POOL_SIZE : poolSize,
            minQueueSize < 0 ? DEFAULT_MIN_ANALYZER_QUEUE_SIZE : minQueueSize);
    }
    
    @SuppressWarnings("unchecked")
    public Freeling(final String configurationPath,
            final String configurationFilenameSuffix,
            final String freelingSharePath, final String freelingLibPath, 
            final String locale, final int maxInitThreads, 
            final int poolSize, final int minQueueSize) {
        //determine the supported languages
       File configDir = new File(configurationPath);
       if(!configDir.isDirectory()){
           throw new IllegalArgumentException("The parsed configDirectory '"
               +configDir+"' is not a directory!");
       }
       log.info("Reading Freeling Configuration from Directory: {}",configDir);
       Map<String,File> supportedLanguages = new HashMap<String,File>();
       String langIdConfigFile = null;
       if(configDir.isDirectory()){
           for(File confFile : (Collection<File>)FileUtils.listFiles(configDir, 
               new SuffixFileFilter(configurationFilenameSuffix), null)){
               Properties prop = new Properties();
               InputStream in = null;
               try {
                   in = new FileInputStream(confFile);
                   prop.load(in);
                   String lang = prop.getProperty("Lang");
                   String langIdentFileName = prop.getProperty("LangIdentFile");
                   if(lang != null){ //not a Analyzer config
                       File existing = supportedLanguages.get(lang);
                       if(existing == null){
                           log.info(" ... adding language '{}' with config {}",
                               lang, confFile);
                           supportedLanguages.put(lang, confFile);
                       } else { //two configs for the same language
                           //take the one that is more similar to the language name
                           int eld = StringUtils.getLevenshteinDistance(
                               lang, FilenameUtils.getBaseName(existing.getName()));
                           int cld = StringUtils.getLevenshteinDistance(
                               lang, FilenameUtils.getBaseName(confFile.getName()));
                           if(cld < eld){
                               log.info(" ... setting language '{}' to config {}",
                                   lang, confFile);
                               supportedLanguages.put(lang, confFile);
                           }
                       }
                   } else if(langIdentFileName != null){
                       if(langIdentFileName.startsWith("$FREELING")){
                           langIdentFileName = FilenameUtils.concat(freelingSharePath,
                               langIdentFileName.substring(
                                   langIdentFileName.indexOf(File.separatorChar)+1));
                       }
                       if(langIdConfigFile != null){
                           log.warn("Multiple LanguageIdentification configuration files. "
                           		+ "Keep using '{}' and ignore '{}'!",langIdConfigFile,langIdentFileName);
                       } else {
                           log.info(" ... setting language identification config to '{}'",
                               langIdentFileName);
                           langIdConfigFile = langIdentFileName;
                       }
                   }
               } catch (IOException e) {
                   log.error("Unable to read configuration file "+confFile,e);
               } finally {
                   IOUtils.closeQuietly(in);
               }
           }
       }
       //init the ThreadPool used to create Freeling components
       //this is mainly needed for beeing able to ensure that only one Freeling
       //component is created at a time. This may be necessary in some
       //environment to avoid random crashes.
       freelingInitThreadPool = Executors.newFixedThreadPool(
           maxInitThreads <= 0 ? DEFAULT_CONCURRENT_THREADS : maxInitThreads);
       //Init the Analyzers
       if(supportedLanguages.isEmpty()){
           log.warn("The parsed configDirectory '{}' does not contain any valid "
               + "language configuration (*.{}) files!",configDir,
               configurationFilenameSuffix);
       } else {
           
           AnalyzerFactory analyzerFactory = new AnalyzerFactory(
               freelingLibPath, freelingSharePath, locale, 
               freelingInitThreadPool);
           //now init the ResourcePool(s)
           log.info("init ResourcePools (size: "+poolSize+")");
           for(Entry<String,File> supported : supportedLanguages.entrySet()){
               Map<String,Object> context = new HashMap<String,Object>();
               context.put(AnalyzerFactory.PROPERTY_LANGUAGE, supported.getKey());
               context.put(AnalyzerFactory.PROPERTY_CONFIG_FILE, supported.getValue());
               log.debug(" ... create ResourcePool for {}",context);
               analyzerPools.put(supported.getKey(), new ResourcePool<Analyzer>(
                       poolSize,minQueueSize, analyzerFactory, context));
           }
       }
       if(langIdConfigFile == null){
           log.warn("The parsed configDirectory '{}' does not contain the "
                   + "Language Identification Component configuration (a *.{}) file! "
                   + "Language Identification Service will not ba available.",
                   configDir, configurationFilenameSuffix);
           langIdPool = null;
       } else {
           LangIdFactory langIdFactory = new LangIdFactory(
               freelingLibPath, langIdConfigFile, locale, freelingInitThreadPool);
           //Finally init the language identifier resource pool
           langIdPool = new ResourcePool<LanguageIdentifier>(
                   poolSize, minQueueSize,langIdFactory, null);
       }
    }
    /**
     * Getter for the read-only set with the supported languages
     * @return
     */
    public Set<String> getSupportedLanguages(){
        return Collections.unmodifiableSet(analyzerPools.keySet());
    }
    
    /**
     * Checks if the parsed language is supported by the Freeling instance
     * @param language the language
     * @return <code>true</code> if supported. Otherwise <code>false</code>.
     */
    public boolean isLanguageSupported(String language){
        return analyzerPools.containsKey(language);
    }
    /**
     * Getter for the {@link ResourcePool} for the parsed language.
     * ResourcePools can be used to obtain {@link Analyzer}s. <p>
     * <b>Usage Example:</b>
     * <code><pre>
     *    Analyzer analyzer = pool.getResource(30*1000);
     *    if(analyzer != null){
     *        try {
     *            return analyzer.analyse(in, mediaType);
     *        } finally {
     *            //we need to return the resource to the pool
     *            pool.returnResource(analyzer);
     *        }
     *    } else {
     *        //else do some Error Handling
     *    }
     * </pre></code>
     * @param language
     * @return
     */
    public ResourcePool<Analyzer> getAnalyzerPool(String language){
        return analyzerPools.get(language);
    }
    
    /*
     * This is more a demo of how to use te API
     * This class should return ResourcePools instead
     */
//    public AnalysedText analyzer(String language, InputStream in, MediaType mediaType) throws IOException {
//        ResourcePool<Analyzer,RuntimeException> pool = analyzerPools.get(language);
//        if(pool == null){ //language not supported
//            throw new IllegalArgumentException("Language '"+language+"' is not supported"
//                +"(supported: "+analyzerPools.keySet()+")!");
//        }
//        //TODO: make the maximum wait time configurable
//        Analyzer analyzer = pool.getResource(30*1000);
//        if(analyzer != null){
//            try {
//                return analyzer.analyse(in, mediaType);
//            } finally {
//                pool.returnResource(analyzer);
//            }
//        } else {
//            throw new IllegalStateException("Unable to get Analyzer for supported language '"
//                +language+"'!");
//        }
//    }
    
    /**
     * Getter for the state of the {@link LanguageIdentifier} service
     * @return <code>true</code> if the {@link LanguageIdentifier} service is
     * available
     */
    public boolean isLanguageIdentificationSupported(){
        return langIdPool != null;
    }
    /**
     * Getter for the {@link ResourcePool} for the {@link LanguageIdentifier}
     * service.<p>
     * <b>Usage Example:</b>
     * <code><pre>
     *    LanguageIdentifier langId = pool.getResource(30*1000);
     *    if(langId != null){
     *        try {
     *            return langId.identifyLanguage(text);
     *        } finally {
     *            //we need to return the resource to the pool
     *            pool.returnResource(langId);
     *        }
     *    } else {
     *        //else do some Error Handling
     *    }
     * </pre></code>
     * @return the {@link ResourcePool} or <code>null</code> if 
     * {@link #isLanguageIdentificationSupported()} is <code>false</code>
     */
    public ResourcePool<LanguageIdentifier> getLangIdPool(){
        return langIdPool;
    }
    /**
     * If this Freeling instance was already {@link #closed()}
     * @return <code>true</code> if {@link #close()} was already called on this
     * Freeling instance.
     */
    public final boolean isClosed() {
        return closed;
    }
    /**
     * Closes this Freeling instance by closing all resources in the different
     * {@link ResourcePool}s.
     */
    public final void close(){
        closed = true;
        ResourcePool<LanguageIdentifier> langIdPool = this.langIdPool;
        this.langIdPool = null; //set first to null
        langIdPool.close(); //than close
        //create a copy of the analyzerPools
        Collection<ResourcePool<Analyzer>> analyzerPools = new HashSet<ResourcePool<Analyzer>>(this.analyzerPools.values());
        this.analyzerPools.clear(); //clean the member variable
        for(ResourcePool<Analyzer> pool : analyzerPools){
            pool.close();
        }
    }

}
