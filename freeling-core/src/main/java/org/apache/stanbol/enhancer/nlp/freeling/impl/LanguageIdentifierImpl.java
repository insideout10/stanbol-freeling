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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.stanbol.enhancer.nlp.freeling.LanguageIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.upc.freeling.LangIdent;
import edu.upc.freeling.PairDoubleString;
import edu.upc.freeling.SWIGTYPE_p_std__setT_std__wstring_t;
import edu.upc.freeling.Util;
import edu.upc.freeling.VectorPairDoubleString;

public class LanguageIdentifierImpl implements LanguageIdentifier {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final double DEFAULT_MIN_PROBABILITY = 0.25;
    public static final int DEFAULT_MAX_SUGGESTED_LANGUAGES = 3;
    
    private final double minProb = DEFAULT_MIN_PROBABILITY;
    private final int maxSuggestions = DEFAULT_MAX_SUGGESTED_LANGUAGES;
    
    /**
     * Sorts {@link Language} instances in decreasing order based on their
     * {@link Language#getProb()}
     */
    private static final Comparator<Language> LANGUAGE_RANk_COMPARATOR = new Comparator<LanguageIdentifier.Language>() {
        @Override
        public int compare(Language o1, Language o2) {
            return o2.getProb() < o1.getProb() ? -1 : o1.getProb() < o2.getProb() ? 1 : 0;
        }
        
    };

    private LangIdent languageIdentifier = null;

    public LanguageIdentifierImpl(String configurationPath) {
        languageIdentifier = new LangIdent(configurationPath);
    }
    /*
     * (non-Javadoc)
     * @see org.apache.stanbol.enhancer.nlp.freeling.LanguageIdentifier#identifyLanguage(java.lang.String)
     */
    @Override
    public List<Language> identifyLanguage(String text) {
        return identifyLanguage(text, "");
    }

    /*
     * (non-Javadoc)
     * @see org.apache.stanbol.enhancer.nlp.freeling.LanguageIdentifier#identifyLanguage(java.lang.String, java.lang.String)
     */
    @Override
    public List<Language> identifyLanguage(String text, String languages) {

        // TODO: check for support of languages like Japanese, Chinese, Russian, Bulgarian, Hindi, ...

        logger.trace("Identifying language.");

        SWIGTYPE_p_std__setT_std__wstring_t allowedLanguages = Util.wstring2set(languages, ",");

        List<Language> languageSet = identifyMultipleLanguages(text, allowedLanguages);

        // return the found languages
        return languageSet;
    }

    @SuppressWarnings("unused")
    private String identifyOneLanguage(String text, SWIGTYPE_p_std__setT_std__wstring_t languages) {
        return languageIdentifier.identifyLanguage(text, languages);
    }

    private List<Language> identifyMultipleLanguages(String text, SWIGTYPE_p_std__setT_std__wstring_t languages) {
        if(languageIdentifier == null){
            throw new IllegalStateException("The language identifier was already closed!");
        }
        VectorPairDoubleString languageRanks = new VectorPairDoubleString();
        languageIdentifier.rankLanguages(languageRanks, text, languages);

        int size = (int) languageRanks.size();

        List<Language> detectedLangs = new ArrayList<Language>(size);

        for (long i = 0; i < size; i++) {
            PairDoubleString pair = languageRanks.get((int) i);
            Double rank = pair.getFirst();
            String language = pair.getSecond();
            if(rank >= minProb) {
                logger.trace("The language [{}][rank :: {}] has been identified for "
                        + "the provided text.",language, rank);
                detectedLangs.add(new Language(language, rank));
            }
        }
        //sort based on prob
        Collections.sort(detectedLangs, LANGUAGE_RANk_COMPARATOR);
        return detectedLangs.size() > maxSuggestions ?
                detectedLangs.subList(0, maxSuggestions) : detectedLangs;
    }
    
    public void close(){
        LangIdent langIdent = languageIdentifier;
        languageIdentifier = null;
        langIdent.delete();
    }
}
