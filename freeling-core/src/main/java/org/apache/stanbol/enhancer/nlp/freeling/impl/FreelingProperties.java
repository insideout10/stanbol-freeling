package org.apache.stanbol.enhancer.nlp.freeling.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FreelingProperties {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private String freelingSharePath;
    private String propertiesFilePath;

    private String language;
    private String locale;

    private int traceLevel;
    private String traceModule;

    private String inputFormat;
    private String outputFormat;

    private boolean alwaysFlush;

    private String tokenizerFile;

    private String splitterFile;

    private boolean affixAnalysis;
    private boolean multiwordsDetection;
    private boolean numbersDetection;
    private boolean punctuationDetection;
    private boolean datesDetection;
    private boolean quantitiesDetection;
    private boolean dictionarySearch;
    private boolean probabilityAssignment;
    private boolean ortographicCorrection;
    private String decimalPoint;
    private String thousandPoint;
    private String locutionsFile;
    private String quantitiesFile;
    private String affixFile;
    private String probabilityFile;
    private String npDataFile;
    private String punctuationFile;
    private Double probabilityThreshold;

    private boolean neRecognition;
    private String dictionaryFile;

    private String correctorFile;

    private boolean phonetics;
    private String phoneticsFile;

    private boolean neClassification;
    private String necFile;

    private String senseAnnotation;
    private String senseConfigFile;
    private String ukbConfigFile;

    private String tagger;
    private String taggerHMMFile;
    private String taggerRelaxFile;
    private int taggerRelaxMaxIter;
    private Double taggerRelaxScaleFactor;
    private Double taggerRelaxEpsilon;
    private boolean taggerRetokenize;
    private int taggerForceSelect;

    private String grammarFile;

    private String depTxalaFile;

    private boolean coreferenceResolution;
    private String corefFile;

    public FreelingProperties(String propertiesFilePath, String freelingSharePath) {
        this.propertiesFilePath = propertiesFilePath;
        this.freelingSharePath = freelingSharePath;

        readProperties();
    }

    public void readProperties() {
        // Read properties file.
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(propertiesFilePath));
        } catch (IOException e) {}

        language = getProperty(properties, "Lang", "none");
        locale = getProperty(properties, "Locale", "default");

        traceLevel = getProperty(properties, "TraceLevel", 3);
        traceModule = getProperty(properties, "TraceModule", "0x0000");

        inputFormat = getProperty(properties, "InputFormat", "plain");
        outputFormat = getProperty(properties, "OutputFormat", "tagged");

        alwaysFlush = ("yes".equals(getProperty(properties, "locale", "no")));

        tokenizerFile = getProperty(properties, "TokenizerFile", "");

        splitterFile = getProperty(properties, "SplitterFile", "");

        affixAnalysis = ("yes".equals(getProperty(properties, "AffixAnalysis", "no")));
        multiwordsDetection = ("yes".equals(getProperty(properties, "MultiwordsDetection", "no")));
        numbersDetection = ("yes".equals(getProperty(properties, "NumbersDetection", "no")));
        punctuationDetection = ("yes".equals(getProperty(properties, "PunctuationDetection", "no")));
        datesDetection = ("yes".equals(getProperty(properties, "DatesDetection", "no")));
        quantitiesDetection = ("yes".equals(getProperty(properties, "QuantitiesDetection", "no")));
        dictionarySearch = ("yes".equals(getProperty(properties, "DictionarySearch", "no")));
        probabilityAssignment = ("yes".equals(getProperty(properties, "ProbabilityAssignment", "no")));
        ortographicCorrection = ("yes".equals(getProperty(properties, "OrtographicCorrection", "no")));
        decimalPoint = getProperty(properties, "DecimalPoint", ",");
        thousandPoint = getProperty(properties, "ThousandPoint", ".");
        locutionsFile = getProperty(properties, "LocutionsFile", "").trim();
        quantitiesFile = getProperty(properties, "QuantitiesFile", "").trim();
        affixFile = getProperty(properties, "AffixFile", "").trim();
        probabilityFile = getProperty(properties, "ProbabilityFile", "").trim();
        npDataFile = getProperty(properties, "NPDataFile", "").trim();
        punctuationFile = getProperty(properties, "PunctuationFile", "").trim();
        probabilityThreshold = getProperty(properties, "ProbabilityThreshold", (Double) 0.001);

        neRecognition = ("yes".equals(getProperty(properties, "NERecognition", "no")));
        dictionaryFile = getProperty(properties, "DictionaryFile", "").trim();

        correctorFile = getProperty(properties, "CorrectorFile", "").trim();

        phonetics = ("yes".equals(getProperty(properties, "Phonetics", "")));
        phoneticsFile = getProperty(properties, "PhoneticsFile", "");

        neClassification = ("yes".equals(getProperty(properties, "NEClassification", "no")));
        necFile = getProperty(properties, "NECFile", "");

        senseAnnotation = getProperty(properties, "SenseAnnotation", "none");
        senseConfigFile = getProperty(properties, "SenseConfigFile", "");
        ukbConfigFile = getProperty(properties, "UKBConfigFile", "");

        tagger = getProperty(properties, "Tagger", "hmm");
        taggerHMMFile = getProperty(properties, "TaggerHMMFile", "");
        taggerRelaxFile = getProperty(properties, "TaggerRelaxFile", "");
        taggerRelaxMaxIter = getProperty(properties, "TaggerRelaxMaxIter", (int) 500);
        taggerRelaxScaleFactor = getProperty(properties, "TaggerRelaxScaleFactor", (Double) 670.0);
        taggerRelaxEpsilon = getProperty(properties, "TaggerRelaxEpsilon", (Double) 0.001);
        taggerRetokenize = ("yes".equals(getProperty(properties, "TaggerRetokenize", "no")));

        String taggerForceSelectStringValue = getProperty(properties, "TaggerForceSelect", "tagger");

        if ("retokenize".equals(taggerForceSelectStringValue)) {
            taggerForceSelect = 2;
        } else if ("tagger".equals(taggerForceSelectStringValue)) {
            taggerForceSelect = 1;
        } else {
            taggerForceSelect = 0;
        }

        grammarFile = getProperty(properties, "GrammarFile", "");

        depTxalaFile = getProperty(properties, "DepTxalaFile", "");

        coreferenceResolution = ("yes".equals(getProperty(properties, "CoreferenceResolution", "no")));
        corefFile = getProperty(properties, "CorefFile", "");

    }

    @SuppressWarnings("unchecked")
    private <T> T getProperty(Properties properties, String propertyName, T defaultValue) {

        // get the value.
        String value = properties.getProperty(propertyName);

        // set the default value, if the value is empty.
        if (null == value || "".equals(value)) return defaultValue;

        // replace the freeling share placeholder.
        value = value.replace("$FREELINGSHARE", freelingSharePath);

        // return an integer.
        if (Integer.class.equals(defaultValue.getClass())) return (T) Integer.valueOf(value, 10);

        // return a double.
        if (Double.class.equals(defaultValue.getClass())) return (T) Double.valueOf(value);

        // return a boolean.
        if (Boolean.class.equals(defaultValue.getClass())) return (T) Boolean.valueOf(value);

        // return a string equivalent.
        return (T) value;
    }

    public Logger getLogger() {
        return logger;
    }

    public String getPropertiesFilePath() {
        return propertiesFilePath;
    }

    public String getLanguage() {
        return language;
    }

    public String getLocale() {
        return locale;
    }

    public int getTraceLevel() {
        return traceLevel;
    }

    public String getTraceModule() {
        return traceModule;
    }

    public String getInputFormat() {
        return inputFormat;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public boolean isAlwaysFlush() {
        return alwaysFlush;
    }

    public String getTokenizerFile() {
        return tokenizerFile;
    }

    public String getSplitterFile() {
        return splitterFile;
    }

    public boolean isAffixAnalysis() {
        return affixAnalysis;
    }

    public boolean isMultiwordsDetection() {
        return multiwordsDetection;
    }

    public boolean isNumbersDetection() {
        return numbersDetection;
    }

    public boolean isPunctuationDetection() {
        return punctuationDetection;
    }

    public boolean isDatesDetection() {
        return datesDetection;
    }

    public boolean isQuantitiesDetection() {
        return quantitiesDetection;
    }

    public boolean isDictionarySearch() {
        return dictionarySearch;
    }

    public boolean isProbabilityAssignment() {
        return probabilityAssignment;
    }

    public boolean isOrtographicCorrection() {
        return ortographicCorrection;
    }

    public String getDecimalPoint() {
        return decimalPoint;
    }

    public String getThousandPoint() {
        return thousandPoint;
    }

    public String getLocutionsFile() {
        return locutionsFile;
    }

    public String getQuantitiesFile() {
        return quantitiesFile;
    }

    public String getAffixFile() {
        return affixFile;
    }

    public String getProbabilityFile() {
        return probabilityFile;
    }

    public String getNpDataFile() {
        return npDataFile;
    }

    public String getPunctuationFile() {
        return punctuationFile;
    }

    public Double getProbabilityThreshold() {
        return probabilityThreshold;
    }

    public boolean isNeRecognition() {
        return neRecognition;
    }

    public String getDictionaryFile() {
        return dictionaryFile;
    }

    public String getCorrectorFile() {
        return correctorFile;
    }

    public boolean isPhonetics() {
        return phonetics;
    }

    public String getPhoneticsFile() {
        return phoneticsFile;
    }

    public boolean isNeClassification() {
        return neClassification;
    }

    public String getNecFile() {
        return necFile;
    }

    public String getSenseAnnotation() {
        return senseAnnotation;
    }

    public String getSenseConfigFile() {
        return senseConfigFile;
    }

    public String getUkbConfigFile() {
        return ukbConfigFile;
    }

    public String getTagger() {
        return tagger;
    }

    public String getTaggerHMMFile() {
        return taggerHMMFile;
    }

    public String getTaggerRelaxFile() {
        return taggerRelaxFile;
    }

    public int getTaggerRelaxMaxIter() {
        return taggerRelaxMaxIter;
    }

    public Double getTaggerRelaxScaleFactor() {
        return taggerRelaxScaleFactor;
    }

    public Double getTaggerRelaxEpsilon() {
        return taggerRelaxEpsilon;
    }

    public boolean isTaggerRetokenize() {
        return taggerRetokenize;
    }

    public int getTaggerForceSelect() {
        return taggerForceSelect;
    }

    public String getGrammarFile() {
        return grammarFile;
    }

    public String getDepTxalaFile() {
        return depTxalaFile;
    }

    public boolean isCoreferenceResolution() {
        return coreferenceResolution;
    }

    public String getCorefFile() {
        return corefFile;
    }

}
