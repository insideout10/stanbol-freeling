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
package io.insideout.stanbol.enhancer.nlp.freeling;

import java.util.List;

public interface LanguageIdentifier {

    public List<Language> identifyLanguage(String text);

    public List<Language> identifyLanguage(String text, String languages);
    
    public static class Language {

        private String twoLetterCode;
        private double prob;

        public Language(String twoLetterCode, double rank) {
            this.twoLetterCode = twoLetterCode;
            this.prob = rank;
        }

        public String getLang() {
            return twoLetterCode;
        }

        public double getProb() {
            return prob;
        }
        
        @Override
        public String toString() {
            return new StringBuilder(twoLetterCode).append('@').append(Math.round(prob*1000)/1000f).toString();
        }

        @Override
        public int hashCode() {
            return twoLetterCode.hashCode();
        }
        @Override
        public boolean equals(Object obj) {
            return obj instanceof Language && 
                    twoLetterCode.equals(((Language)obj).twoLetterCode) &&
                    prob == ((Language)obj).prob;
        }
    }
}