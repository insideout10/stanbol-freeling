package org.apache.stanbol.enhancer.nlp.freeling;

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