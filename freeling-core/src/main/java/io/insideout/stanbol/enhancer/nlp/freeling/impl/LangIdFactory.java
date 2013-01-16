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
package io.insideout.stanbol.enhancer.nlp.freeling.impl;

import io.insideout.stanbol.enhancer.nlp.freeling.pool.ResourcePool.ResourceFactory;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.upc.freeling.Util;

public class LangIdFactory implements ResourceFactory<LanguageIdentifierImpl> {

    protected final Logger log = LoggerFactory.getLogger(LangIdFactory.class);
    private final ExecutorService executorService;
    private final String configFile;
    
    public LangIdFactory(final String freelingLibPath, String configFile, 
            String locale, ExecutorService factoryThreadPool){
        if(freelingLibPath == null){
            throw new IllegalArgumentException("The path to the Freeling native "
                + "lib MUST NOT be NULL!");
        }
        if(configFile == null){
            throw new IllegalArgumentException("The parsed configuration file for the"
                + "Freeling Lanugge Identification component MUST NOT be NULL!");
        }
        if(locale == null){
            throw new IllegalArgumentException("The parsed Freeling Locale"
                + "MUST NOT be NULL!");
        }
        if(factoryThreadPool == null){
            throw new IllegalArgumentException("The parsed ExecutorService"
                + "MUST NOT be NULL!");
        }
        this.executorService = factoryThreadPool;
        this.configFile = configFile;
        //check for the native freeling lib
        NativeLibsUtil.ensureNativeLib(freelingLibPath);
        log.info("Setting locale [{}].", locale);
        Util.initLocale(locale);
    }
    
    @Override
    public Future<LanguageIdentifierImpl> createResource(Map<String,Object> context) {
        log.info("Request to create Language Identification Resource");
        final long request = System.currentTimeMillis();
        return executorService.submit(new Callable<LanguageIdentifierImpl>() {

            @Override
            public LanguageIdentifierImpl call() throws Exception {
                long start = System.currentTimeMillis();
                log.info("createing Language Identification Resourc ({}ms after request)",start-request);
                try {
                    return new LanguageIdentifierImpl(configFile);
                } finally {
                    long created = System.currentTimeMillis();
                    log.info("  ... create in {}ms ({}ms after request)",created-start,created-request);
                }
            }
            
        });
    }
    
    @Override
    public void closeResource(final Object resource, Map<String,Object> context) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                LanguageIdentifierImpl langIdent = LanguageIdentifierImpl.class.cast(resource);
                log.info("close Language Identification resource");
                langIdent.close();
            }
        });
    }
}
