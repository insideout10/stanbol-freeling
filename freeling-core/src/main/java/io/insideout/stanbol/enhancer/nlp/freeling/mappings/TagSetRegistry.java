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
package io.insideout.stanbol.enhancer.nlp.freeling.mappings;

import java.util.HashMap;
import java.util.Map;

import org.apache.stanbol.enhancer.nlp.model.tag.TagSet;
import org.apache.stanbol.enhancer.nlp.ner.NerTag;
import org.apache.stanbol.enhancer.nlp.phrase.PhraseTag;
import org.apache.stanbol.enhancer.nlp.pos.LexicalCategory;
import org.apache.stanbol.enhancer.nlp.pos.Pos;
import org.apache.stanbol.enhancer.nlp.pos.PosTag;
import org.apache.stanbol.enhancer.nlp.pos.olia.English;
import org.apache.stanbol.enhancer.nlp.pos.olia.Spanish;
import org.apache.stanbol.enhancer.servicesapi.rdf.OntologicalClasses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TagSetRegistry {

    private final Logger log = LoggerFactory.getLogger(TagSetRegistry.class);
    
    private static TagSetRegistry instance = new TagSetRegistry();
    
    private TagSetRegistry(){}
    
    private final Map<String, TagSet<PosTag>> posModels = new HashMap<String,TagSet<PosTag>>();
    private final Map<String, TagSet<PhraseTag>> phraseModels = new HashMap<String,TagSet<PhraseTag>>();
    private final Map<String, TagSet<NerTag>> nerModels = new HashMap<String,TagSet<NerTag>>();
    /**
     * Adhoc {@link PosTag}s created for string tags missing in the {@link #posModels}
     */
    private Map<String,Map<String,PosTag>> adhocPosTagMap = new HashMap<String,Map<String,PosTag>>();
    /**
     * Adhoc {@link PhraseTag}s created for string tags missing in the {@link #phraseModels}
     */
    private Map<String,Map<String,PhraseTag>> adhocPhraseTagMap = new HashMap<String,Map<String,PhraseTag>>();
    /**
     * Adhoc {@link NerTag}s created for string tags missing in the {@link #nerModels}
     */
    private Map<String,Map<String,NerTag>> adhocNerTagMap = new HashMap<String,Map<String,NerTag>>();
    
    private Map<String,TagMapper> posTagMappers = new HashMap<String,TagMapper>();
    
    public static TagSetRegistry getInstance(){
        return instance;
    }
    
    private void addPosTagSet(TagSet<PosTag> model) {
        for(String lang : model.getLanguages()){
            if(posModels.put(lang, model) != null){
                throw new IllegalStateException("Multiple Pos Models for Language '"
                    + lang+"'! This is an error in the static confituration of "
                    + "this class!");
            }
        }
    }
    private void addPosTagMapper(TagMapper tagMapper, String...langs) {
        for(String lang : langs){
            if(posTagMappers.put(lang, tagMapper) != null){
                throw new IllegalStateException("Multiple Pos TagMapper for Language '"
                        + lang+"'! This is an error in the static confituration of "
                        + "this class!");
            }
        }
        
    }

    
    private void addPhraseTagSet(TagSet<PhraseTag> model) {
        for(String lang : model.getLanguages()){
            if(phraseModels.put(lang, model) != null){
                throw new IllegalStateException("Multiple Phrase Models for Language '"
                    + lang+"'! This is an error in the static confituration of "
                    + "this class!");
            }
        }
    }
    private void addNerTagSet(TagSet<NerTag> model) {
        for(String lang : model.getLanguages()){
            if(nerModels.put(lang, model) != null){
                throw new IllegalStateException("Multiple Ner Models for Language '"
                    + lang+"'! This is an error in the static confituration of "
                    + "this class. Please report this to the stanbol-dev mailing"
                    + "list!");
            }
        }
    }
    /**
     * Getter for the {@link PosTag} {@link TagSet} by language. If no {@link TagSet}
     * is available for an Language this will return <code>null</code>
     * @param language the language
     * @return the AnnotationModel or <code>null</code> if non is defined
     */
    public TagSet<PosTag> getPosTagSet(String language){
        return posModels.get(language);
    }
    
    /**
     * Getter for the {@link TagMapper} for {@link PosTag}s
     * @param language the language
     * @return the {@link TagMapper} or <code>null</code> if none
     */
    public TagMapper getPosTagMapper(String language){
        return posTagMappers.get(language);
    }
    
    /**
     * Getter for the map holding the adhoc {@link PosTag} for the given language
     * @param language the language
     * @return the map with the adhoc {@link PosTag}s
     */
    public Map<String,PosTag> getAdhocPosTagMap(String language){
        Map<String,PosTag> adhocMap =  adhocPosTagMap.get(language);
        if(adhocMap == null){
            adhocMap = new HashMap<String,PosTag>();
            adhocPosTagMap.put(language, adhocMap);
        }
        return adhocMap;
    }
    
    /**
     * Getter for the {@link PhraseTag} {@link TagSet} by language. If no {@link TagSet}
     * is available for an Language this will return <code>null</code>
     * @param language the language
     * @return the AnnotationModel or <code>null</code> if non is defined
     */
    public TagSet<PhraseTag> getPhraseTagSet(String language){
        return phraseModels.get(language);
    }
    
    /**
     * Getter for the map holding the adhoc {@link PhraseTag} for the given language
     * @param language the language
     * @return the map with the adhoc {@link PhraseTag}s
     */
    public Map<String,PhraseTag> getAdhocPhraseTagMap(String language){
        Map<String,PhraseTag> adhocMap =  adhocPhraseTagMap.get(language);
        if(adhocMap == null){
            adhocMap = new HashMap<String,PhraseTag>();
            adhocPhraseTagMap.put(language, adhocMap);
        }
        return adhocMap;
    }

    /**
     * Getter for the {@link NerTag} {@link TagSet} by language. If no {@link TagSet}
     * is available for an Language this will return <code>null</code>
     * @param language the language
     * @return the AnnotationModel or <code>null</code> if non is defined
     */
    public TagSet<NerTag> getNerTagSet(String language){
        return nerModels.get(language);
    }
    /**
     * Getter for the map holding the adhoc {@link NerTag} for the given language
     * @param language the language
     * @return the map with the adhoc {@link NerTag}s
     */
    public Map<String,NerTag> getAdhocNerTagMap(String language){
        Map<String,NerTag> adhocMap =  adhocNerTagMap.get(language);
        if(adhocMap == null){
            adhocMap = new HashMap<String,NerTag>();
            adhocNerTagMap.put(language, adhocMap);
        }
        return adhocMap;
    }    
        
    /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
     * PHRASE TAG SET DEFINITIONS
     * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
     */

    //NOTE: Added support to automatically create PhraseTags based on the
    //      PosTag. It uses the PosTagMapper to determine the Tag as well a
    //      the LexicalCategoriy of the PosTag as Category for the PhraseTag
    //      Because of that this definitions are no longer needed
//    /**
//     * Based on the documentation only a single Phrase tag "np" is used for
//     * multi words named entities
//     */
//    private static final TagSet<PhraseTag> FREELING_COMMON_PHRASE = new TagSet<PhraseTag>("Freeling Phrase Tagset", 
//            "en", "as", "cy", "it", "ru", "ca", "gl", "pt");
//    private static final TagSet<PhraseTag> FREELING_ES_PHRASE = new TagSet<PhraseTag>("Freeling Phrase Tagset", "es");
//    
//    static {
//        FREELING_COMMON_PHRASE.addTag(new PhraseTag("NP",LexicalCategory.Noun));
//        FREELING_COMMON_PHRASE.addTag(new PhraseTag("NP00000", LexicalCategory.Noun));
//        getInstance().addPhraseTagSet(FREELING_COMMON_PHRASE);
//        FREELING_ES_PHRASE.addTag(new PhraseTag("NP",LexicalCategory.Noun));
//        FREELING_ES_PHRASE.addTag(new PhraseTag("NP00000", LexicalCategory.Noun));
//        FREELING_ES_PHRASE.addTag(new PhraseTag("W", LexicalCategory.Residual)); //date
//        getInstance().addPhraseTagSet(FREELING_ES_PHRASE);
//    }

    /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
     * NER TAG SET DEFINITIONS
     * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
     */
    private static final TagSet<NerTag> FREELING_NER = new TagSet<NerTag>("Freeling NER Tagset", 
            "en", "as", "cy", "it", "ru", "es", "ca", "gl", "pt");
    
    static {
        FREELING_NER.addTag(new NerTag("NP00000")); //unclassified Named Entities
        FREELING_NER.addTag(new NerTag("NP00SP0", OntologicalClasses.DBPEDIA_PERSON));
        FREELING_NER.addTag(new NerTag("NP00O00", OntologicalClasses.DBPEDIA_ORGANISATION));
        FREELING_NER.addTag(new NerTag("NP00G00", OntologicalClasses.DBPEDIA_PLACE));
        FREELING_NER.addTag(new NerTag("NP00V00", OntologicalClasses.SKOS_CONCEPT)); //Other
        getInstance().addNerTagSet(FREELING_NER);
    }
    //TODO
    
    /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
     * POS TAG SET DEFINITIONS
     * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
     */
    
    // (1) GENERIC POS TAG MAPPERS
    /**
     * This mapper convert NP*** tags to NP. All other Tags are returned
     * unchanged.
     */
    private static final TagMapper NER_POS_TAG_MAPPER = new TagMapper() {
        
        @Override
        public String map(String tag) {
            return tag.startsWith("NP") ? "NP" : tag;
        }
    };
    static {
        getInstance().addPosTagMapper(NER_POS_TAG_MAPPER, "en", "as");
    }
    
    /**
     * Basically {@link English#PENN_TREEBANK} but with some derivations and
     * other tags for punctations. The Freeling documetnation refers to
     * <a href="http://acl.ldc.upenn.edu/J/J93/J93-2004.pdf">this version</a> of
     * the Penn Treebank set. However the Tags mentioned in <code>en/tagger.dat</code>
     * do not 100% fit with the one listed in the cited paper.<p>
     * This TagSet follows all Tags defined in <code>en/tagger.dat</code> and
     * maps them based on {@link English#PENN_TREEBANK}
     */
    private static final TagSet<PosTag> FREELING_TREEBANK = new TagSet<PosTag>(
            "Freeling Treebank POS TagSet", "en");
    static {
        //Tags taken from the <Tag></Tag> section in /en/tagger.dat
        //Most 
        FREELING_TREEBANK.addTag(new PosTag("CC", Pos.CoordinatingConjunction));
        FREELING_TREEBANK.addTag(new PosTag("CD",Pos.CardinalNumber));
        FREELING_TREEBANK.addTag(new PosTag("DT",Pos.Determiner));
        FREELING_TREEBANK.addTag(new PosTag("EX",Pos.ExistentialParticle)); //TODO: unsure mapping
        FREELING_TREEBANK.addTag(new PosTag("FW",Pos.Foreign));
        FREELING_TREEBANK.addTag(new PosTag("IN",Pos.Preposition, Pos.SubordinatingConjunction));
        FREELING_TREEBANK.addTag(new PosTag("JJ",LexicalCategory.Adjective));
        FREELING_TREEBANK.addTag(new PosTag("JJR",LexicalCategory.Adjective, Pos.ComparativeParticle));
        FREELING_TREEBANK.addTag(new PosTag("JJS",LexicalCategory.Adjective, Pos.SuperlativeParticle));
        FREELING_TREEBANK.addTag(new PosTag("LS",Pos.ListMarker));
        FREELING_TREEBANK.addTag(new PosTag("MD",Pos.ModalVerb));
        FREELING_TREEBANK.addTag(new PosTag("NN",Pos.CommonNoun, Pos.SingularQuantifier));
        FREELING_TREEBANK.addTag(new PosTag("NNP",Pos.ProperNoun, Pos.SingularQuantifier));
        FREELING_TREEBANK.addTag(new PosTag("NNPS",Pos.ProperNoun, Pos.PluralQuantifier));
        FREELING_TREEBANK.addTag(new PosTag("NNS",Pos.CommonNoun, Pos.PluralQuantifier));
        FREELING_TREEBANK.addTag(new PosTag("PDT",Pos.Determiner)); //TODO should be Pre-Determiner
        FREELING_TREEBANK.addTag(new PosTag("POS",LexicalCategory.Residual)); //Possessive Ending (e.g., Nouns ending in 's)
        FREELING_TREEBANK.addTag(new PosTag("PRP",Pos.PersonalPronoun));
        FREELING_TREEBANK.addTag(new PosTag("PRP$",Pos.PossessivePronoun));
        FREELING_TREEBANK.addTag(new PosTag("RB",LexicalCategory.Adverb));
        FREELING_TREEBANK.addTag(new PosTag("RG", LexicalCategory.Adverb)); // G stands for "general"
        FREELING_TREEBANK.addTag(new PosTag("RBR",LexicalCategory.Adverb,Pos.ComparativeParticle));
        FREELING_TREEBANK.addTag(new PosTag("RBS",LexicalCategory.Adverb,Pos.SuperlativeParticle));
        FREELING_TREEBANK.addTag(new PosTag("RP",Pos.Participle));
        FREELING_TREEBANK.addTag(new PosTag("SYM",Pos.Symbol));
        FREELING_TREEBANK.addTag(new PosTag("TO",LexicalCategory.Adposition));
        FREELING_TREEBANK.addTag(new PosTag("UH",LexicalCategory.Interjection));
        FREELING_TREEBANK.addTag(new PosTag("VB",Pos.Infinitive)); //TODO check a Verb in the base form should be Pos.Infinitive
        FREELING_TREEBANK.addTag(new PosTag("VBD",Pos.PastParticiple)); //TODO check
        FREELING_TREEBANK.addTag(new PosTag("VBG",Pos.PresentParticiple,Pos.Gerund));
        FREELING_TREEBANK.addTag(new PosTag("VBN",Pos.PastParticiple));
        FREELING_TREEBANK.addTag(new PosTag("VBP",Pos.PresentParticiple));
        FREELING_TREEBANK.addTag(new PosTag("VBZ",Pos.PresentParticiple));
        FREELING_TREEBANK.addTag(new PosTag("WDT",Pos.WHDeterminer));
        FREELING_TREEBANK.addTag(new PosTag("WP",Pos.WHPronoun));
        FREELING_TREEBANK.addTag(new PosTag("WP$",Pos.PossessivePronoun, Pos.WHPronoun));
        FREELING_TREEBANK.addTag(new PosTag("WRB",Pos.WHTypeAdverbs));
        //add tags for punctations
        addCommonPunctationTags(FREELING_TREEBANK);
        //additional punctations mentioned
        addCommonNumberTags(FREELING_TREEBANK);
        addCommonDateTags(FREELING_TREEBANK);
        addCommonNerTags(FREELING_TREEBANK); //add the Tags used by NER
        getInstance().addPosTagSet(FREELING_TREEBANK);
    }
    /**
     * Languages that use the {@link #FREELING_PAROLE} {@link TagSet}. Also used
     * for the {@link #FREELING_PAROLE_POS_TAG_MAPPER}.
     */
    private static final String[] FREELING_PAROLE_LANGUAGES = new String [] {
        "es", "pt", "ca", "it", "gl"
    };
    /**
     * Freeling uses <a href="http://www.ilc.cnr.it/EAGLES96/annotate/annotate.html/">EAGLES</a> for
     * <a href="http://nlp.lsi.upc.edu/freeling/doc/tagsets/tagset-ca.html">Catalan</a>, 
     * <a href="http://nlp.lsi.upc.edu/freeling/doc/tagsets/tagset-es.html">Spanish</a>, and 
     * <a href="http://nlp.lsi.upc.edu/freeling/doc/tagsets/tagset-ru.html">Russian</a>.
     * <p>
     * This TagSet corresponds very nicely to the {@link Spanish#PAROLE}.
     * Only "DN", "PN", "VAG" are missing and "Z" tag is used for Numerals instead 
     * of images as defined by {@link Spanish#PAROLE}. <p>
     * Generally this TagSet follows all Tags defined in <code>es/tagger.dat</code> and
     * maps them based on {@link Spanish#PAROLE}<p>
     * <b>NOTE</b> that his also uses the {@link #FREELING_PAROLE_POS_TAG_MAPPER}
     * to convert long POS tags to the core categories as defined in this set.
     */
    private static final TagSet<PosTag> FREELING_PAROLE = new TagSet<PosTag>(
            "Freeling PAROLE POS TagSet", FREELING_PAROLE_LANGUAGES);
    /**
     * {@link PosTag} {@link TagMapper} for {@link #FREELING_PAROLE}
     */
    private static final TagMapper FREELING_PAROLE_POS_TAG_MAPPER;
    static {
        FREELING_PAROLE.addTag(new PosTag("AO", LexicalCategory.Adjective));
        FREELING_PAROLE.addTag(new PosTag("AQ", Pos.QualifierAdjective));
        FREELING_PAROLE.addTag(new PosTag("CC", Pos.CoordinatingConjunction));
        FREELING_PAROLE.addTag(new PosTag("CS", Pos.SubordinatingConjunction));
        FREELING_PAROLE.addTag(new PosTag("DA", Pos.Article));
        FREELING_PAROLE.addTag(new PosTag("DD", Pos.DemonstrativeDeterminer));
        FREELING_PAROLE.addTag(new PosTag("DE", Pos.ExclamatoryDeterminer));
        FREELING_PAROLE.addTag(new PosTag("DI", Pos.IndefiniteDeterminer));
        //FREELING_ES.addTag(new PosTag("DN", Pos.Numeral,Pos.Determiner));
        FREELING_PAROLE.addTag(new PosTag("DP", Pos.PossessiveDeterminer));
        FREELING_PAROLE.addTag(new PosTag("DT", Pos.InterrogativeDeterminer));
        FREELING_PAROLE.addTag(new PosTag("I", LexicalCategory.Interjection));
        FREELING_PAROLE.addTag(new PosTag("NC", Pos.CommonNoun));
        //added by the addCommonNerTags
        //FREELING_ES.addTag(new PosTag("NP", Pos.ProperNoun));
        FREELING_PAROLE.addTag(new PosTag("P0", Pos.Pronoun)); //TODO: CliticPronoun is missing
        FREELING_PAROLE.addTag(new PosTag("PD", Pos.DemonstrativePronoun));
        FREELING_PAROLE.addTag(new PosTag("PE", Pos.ExclamatoryPronoun));
        FREELING_PAROLE.addTag(new PosTag("PI", Pos.IndefinitePronoun));
//        PAROLE.addTag(new PosTag("PN", Pos.Pronoun)); //TODO: NumeralPronoun is missing
        FREELING_PAROLE.addTag(new PosTag("PP", Pos.PersonalPronoun));
        FREELING_PAROLE.addTag(new PosTag("PR", Pos.RelativePronoun));
        FREELING_PAROLE.addTag(new PosTag("PT", Pos.InterrogativePronoun));
        FREELING_PAROLE.addTag(new PosTag("PX", Pos.PossessivePronoun));
        FREELING_PAROLE.addTag(new PosTag("RG", LexicalCategory.Adverb));
        FREELING_PAROLE.addTag(new PosTag("RN", Pos.NegativeAdverb));
        FREELING_PAROLE.addTag(new PosTag("SP", Pos.Preposition));
        //PAROLE.addTag(new PosTag("VAG", Pos.StrictAuxiliaryVerb, Pos.Gerund));
        FREELING_PAROLE.addTag(new PosTag("VAI", Pos.StrictAuxiliaryVerb, Pos.IndicativeVerb));
        FREELING_PAROLE.addTag(new PosTag("VAM", Pos.StrictAuxiliaryVerb, Pos.ImperativeVerb));
        FREELING_PAROLE.addTag(new PosTag("VAN", Pos.StrictAuxiliaryVerb, Pos.Infinitive));
        FREELING_PAROLE.addTag(new PosTag("VAP", Pos.StrictAuxiliaryVerb, Pos.Participle));
        FREELING_PAROLE.addTag(new PosTag("VAS", Pos.StrictAuxiliaryVerb, Pos.SubjunctiveVerb));
        FREELING_PAROLE.addTag(new PosTag("VMG", Pos.MainVerb, Pos.Gerund));
        FREELING_PAROLE.addTag(new PosTag("VMI", Pos.MainVerb, Pos.IndicativeVerb));
        FREELING_PAROLE.addTag(new PosTag("VMM", Pos.MainVerb, Pos.ImperativeVerb));
        FREELING_PAROLE.addTag(new PosTag("VMN", Pos.MainVerb, Pos.Infinitive));
        FREELING_PAROLE.addTag(new PosTag("VMP", Pos.MainVerb, Pos.Participle));
        FREELING_PAROLE.addTag(new PosTag("VMS", Pos.MainVerb, Pos.SubjunctiveVerb));
        FREELING_PAROLE.addTag(new PosTag("VSG", Pos.ModalVerb, Pos.Gerund));
        FREELING_PAROLE.addTag(new PosTag("VSI", Pos.ModalVerb, Pos.IndicativeVerb));
        FREELING_PAROLE.addTag(new PosTag("VSM", Pos.ModalVerb, Pos.ImperativeVerb));
        FREELING_PAROLE.addTag(new PosTag("VSN", Pos.ModalVerb, Pos.Infinitive));
        FREELING_PAROLE.addTag(new PosTag("VSP", Pos.ModalVerb, Pos.Participle));
        FREELING_PAROLE.addTag(new PosTag("VSS", Pos.ModalVerb, Pos.SubjunctiveVerb));
        //added by addCommonDateTags(..)
        //FREELING_PAROLE.addTag(new PosTag("W", Pos.Date)); //date times
        FREELING_PAROLE.addTag(new PosTag("X")); //unknown
        //PAROLE.addTag(new PosTag("Y", Pos.Abbreviation)); //abbreviation
        //NOTE: Freeling uses Z for numbers!!
        //PAROLE.addTag(new PosTag("Z", Pos.Image)); //Figures
        //PAROLE.addTag(new PosTag("Zm", Pos.Symbol)); //currency
        //PAROLE.addTag(new PosTag("Zp", Pos.Symbol)); //percentage
        //add tags for punctations
        addCommonPunctationTags(FREELING_PAROLE);
        //additional punctations mentioned
        addCommonNumberTags(FREELING_PAROLE);
        addCommonDateTags(FREELING_PAROLE);
        addCommonNerTags(FREELING_PAROLE); //add the Tags used by NER
        getInstance().addPosTagSet(FREELING_PAROLE);
        //we need to have a TagMapper
        FREELING_PAROLE_POS_TAG_MAPPER = new TagMapper() {
            
            @Override
            public String map(String tag) {
                if(tag == null) {
                    return tag;
                }
                if(!tag.isEmpty()){
                    switch (tag.charAt(0)) {
                        case 'A':
                        case 'C':
                        case 'D':
                        case 'N':
                        case 'P':
                        case 'R':
                        case 'S':
                            return tag.length() > 2 ? tag.substring(0, 2) : tag;
                        case 'V':
                            return tag.length() > 3 ? tag.substring(0,3) : tag;
                        case 'I':
                        case 'W':
                        case 'X':
                            return tag.length() > 1 ? tag.substring(0,1) : tag;
                        default:
                    }
                } else {
                    return "X"; //unknown
                }
                return tag;
            }
        };
        getInstance().addPosTagMapper(FREELING_PAROLE_POS_TAG_MAPPER, FREELING_PAROLE_LANGUAGES);

    }
    /**
     * <a herf="http://nlp.lsi.upc.edu/freeling/doc/tagsets/tagset-ru-tradeng.html">
     * Russian PAROLE version</a>
     */
    private static final TagSet<PosTag> FREELING_RU = new TagSet<PosTag>(
            "Freeling PAROLE POS TagSet for Russian", "ru");
    /**
     * {@link TagMapper} used for Russian {@link PosTag}s
     */
    private static final TagMapper FREELING_RU_POS_TAG_MAPPER;
    static {
        FREELING_RU.addTag(new PosTag("A", LexicalCategory.Adjective));
        FREELING_RU.addTag(new PosTag("D", LexicalCategory.Adverb));
        FREELING_RU.addTag(new PosTag("P", Pos.PronominalAdverb));
        FREELING_RU.addTag(new PosTag("Y", Pos.OrdinalNumber));
        FREELING_RU.addTag(new PosTag("R", LexicalCategory.Adjective)); //Pronominal
        FREELING_RU.addTag(new PosTag("C", LexicalCategory.Conjuction));
        FREELING_RU.addTag(new PosTag("J", LexicalCategory.Interjection));
        FREELING_RU.addTag(new PosTag("T", Pos.Particle));
        FREELING_RU.addTag(new PosTag("B", Pos.Preposition));
        FREELING_RU.addTag(new PosTag("NC", Pos.CommonNoun));
        //added by the addCommonNerTags
        //FREELING_ES.addTag(new PosTag("NP", Pos.ProperNoun));
        FREELING_RU.addTag(new PosTag("E", Pos.Pronoun));
        
        FREELING_RU.addTag(new PosTag("VG", Pos.Gerund));
        FREELING_RU.addTag(new PosTag("VI", Pos.Infinitive));
        FREELING_RU.addTag(new PosTag("VN", LexicalCategory.Verb)); //used but not in docu
        FREELING_RU.addTag(new PosTag("VD", Pos.IndicativeVerb));
        FREELING_RU.addTag(new PosTag("VM", Pos.ImperativeVerb));
        FREELING_RU.addTag(new PosTag("Q0", Pos.Participle));
        FREELING_RU.addTag(new PosTag("QF", Pos.Participle)); //used but not in docu
        FREELING_RU.addTag(new PosTag("QO", Pos.Participle)); //used but not in docu
        FREELING_RU.addTag(new PosTag("QG", Pos.Participle,Pos.Gerund));
        FREELING_RU.addTag(new PosTag("QN", Pos.Participle,Pos.Infinitive));
        FREELING_RU.addTag(new PosTag("QD", Pos.Participle,Pos.IndicativeVerb));
        FREELING_RU.addTag(new PosTag("QM", Pos.Participle,Pos.ImperativeVerb));
        
        FREELING_RU.addTag(new PosTag("X")); //unknown
        //PAROLE.addTag(new PosTag("Y", Pos.Abbreviation)); //abbreviation
        //NOTE: Freeling uses Z for numbers!!
        //PAROLE.addTag(new PosTag("Z", Pos.Image)); //Figures
        //PAROLE.addTag(new PosTag("Zm", Pos.Symbol)); //currency
        //PAROLE.addTag(new PosTag("Zp", Pos.Symbol)); //percentage
        //add tags for punctations
        addCommonPunctationTags(FREELING_RU);
        //additional punctations mentioned
        addCommonNumberTags(FREELING_RU);
        addCommonDateTags(FREELING_RU);
        addCommonNerTags(FREELING_RU); //add the Tags used by NER
        getInstance().addPosTagSet(FREELING_RU);
        //we can use the same POS_TAG mapper as for other languages
        FREELING_RU_POS_TAG_MAPPER = new TagMapper() {
            
            @Override
            public String map(String tag) {
                if(tag == null) {
                    return tag;
                }
                if(!tag.isEmpty()){
                    switch (tag.charAt(0)) {
                        case 'N':
                        case 'V':
                        case 'Q':
                        case 'Z':
                            return tag.length() > 2 ? tag.substring(0, 2) : tag;
                        case 'A':
                        case 'B':
                        case 'C':
                        case 'D':
                        case 'E':
                        case 'P':
                        case 'J':
                        case 'R':
                        case 'T':
                        case 'X':
                        case 'Y':
                            return tag.length() > 1 ? tag.substring(0,1) : tag;
                        default:
                    }
                } else {
                    return "X"; //unknown
                }
                return tag;
            }
        };        
        getInstance().addPosTagMapper(FREELING_RU_POS_TAG_MAPPER, "ru");
    }   
    
    private static final TagSet<PosTag> FREELING_CY = new TagSet<PosTag>(
            "Freeling PAROLE POS TagSet for Cymraeg(Welsh)", "cy");
    /**
     * {@link TagMapper} used for Welsh {@link PosTag}s.
     * Tags missing in PAROLE are mapped based on the information found 
     * <a herf="http://apertium.svn.sourceforge.net/viewvc/apertium/trunk/apertium-tools/freeling/cy-tags.parole.txt">
     * here</a> in combination with the docu of the mapped tags that can be found
     * <a href="http://wiki.apertium.org/wiki/Breton"> here</a>
     */
    private static final TagMapper FREELING_CY_POS_TAG_MAPPER;
    static {
        //FREELING_CY.addTag(new PosTag("AO", LexicalCategory.Adjective));
        FREELING_CY.addTag(new PosTag("AQ", Pos.QualifierAdjective));
        FREELING_CY.addTag(new PosTag("CC", Pos.CoordinatingConjunction));
        FREELING_CY.addTag(new PosTag("CS", Pos.SubordinatingConjunction));
        FREELING_CY.addTag(new PosTag("DA", Pos.Article));
        FREELING_CY.addTag(new PosTag("DD", Pos.DemonstrativeDeterminer));
        //FREELING_CY.addTag(new PosTag("DE", Pos.ExclamatoryDeterminer));
        FREELING_CY.addTag(new PosTag("DI", Pos.IndefiniteDeterminer));
        //FREELING_CY.addTag(new PosTag("DN", Pos.Numeral,Pos.Determiner));
        //NOTE: mapping based on "DO0CN0    <det><ord><sp>" in
        //  http://apertium.svn.sourceforge.net/viewvc/apertium/trunk/apertium-tools/freeling/cy-tags.parole.txt
        //  and "http://wiki.apertium.org/wiki/Breton" defining <ord> as "Ordinal"
        FREELING_CY.addTag(new PosTag("DO", Pos.Determiner, Pos.OrdinalNumber)); //Not found in docu
        FREELING_CY.addTag(new PosTag("DP", Pos.PossessiveDeterminer));
        //NOTE mapping based on "DQ0CP0     <det><qnt><pl>" in
        //  http://apertium.svn.sourceforge.net/viewvc/apertium/trunk/apertium-tools/freeling/cy-tags.parole.txt
        //  assuming that <qnt> stands for "Quantifier"
        FREELING_CY.addTag(new PosTag("DQ", LexicalCategory.Quantifier, Pos.Determiner));
        FREELING_CY.addTag(new PosTag("DT", Pos.InterrogativeDeterminer));
        //NOTE: THE G* tags are mapped based on the mappings defined in
        //  http://apertium.svn.sourceforge.net/viewvc/apertium/trunk/apertium-tools/freeling/cy-tags.parole.txt
        FREELING_CY.addTag(new PosTag("GA", Pos.VerbalParticle, Pos.AffirmativeParticle));
        FREELING_CY.addTag(new PosTag("GN", Pos.VerbalParticle, Pos.NegativeParticle));
        FREELING_CY.addTag(new PosTag("GV", Pos.VerbalParticle, Pos.InterrogativeParticle));
        FREELING_CY.addTag(new PosTag("GI", Pos.VerbalParticle)); //Imperative

//        FREELING_CY.addTag(new PosTag("I", LexicalCategory.Interjection));
        FREELING_CY.addTag(new PosTag("NC", Pos.CommonNoun));
        //added by the addCommonNerTags
        //FREELING_CY.addTag(new PosTag("NP", Pos.ProperNoun));
//        FREELING_CY.addTag(new PosTag("P0", Pos.Pronoun)); //TODO: CliticPronoun is missing
        FREELING_CY.addTag(new PosTag("PD", Pos.DemonstrativePronoun));
//        FREELING_CY.addTag(new PosTag("PE", Pos.ExclamatoryPronoun));
//        FREELING_CY.addTag(new PosTag("PI", Pos.IndefinitePronoun));
//        FREELING_CY.addTag(new PosTag("PN", Pos.Pronoun)); //TODO: NumeralPronoun is missing
        FREELING_CY.addTag(new PosTag("PP", Pos.PersonalPronoun));
        FREELING_CY.addTag(new PosTag("PR", Pos.RelativePronoun));
        FREELING_CY.addTag(new PosTag("PT", Pos.InterrogativePronoun));
//        FREELING_CY.addTag(new PosTag("PX", Pos.PossessivePronoun));
        //based on "RG     <adv>" mapping in
        //  http://apertium.svn.sourceforge.net/viewvc/apertium/trunk/apertium-tools/freeling/cy-tags.parole.txt
        FREELING_CY.addTag(new PosTag("RG", LexicalCategory.Adverb));
        //based on "RG     <adv><itg>" mapping in
        //  http://apertium.svn.sourceforge.net/viewvc/apertium/trunk/apertium-tools/freeling/cy-tags.parole.txt
        FREELING_CY.addTag(new PosTag("RV", Pos.InterrogativeAdverb));
//        FREELING_CY.addTag(new PosTag("RN", Pos.NegativeAdverb));
        FREELING_CY.addTag(new PosTag("SP", Pos.Preposition));
        //FREELING_CY.addTag(new PosTag("VAG", Pos.StrictAuxiliaryVerb, Pos.Gerund));
        FREELING_CY.addTag(new PosTag("VAI", Pos.StrictAuxiliaryVerb, Pos.IndicativeVerb));
        FREELING_CY.addTag(new PosTag("VAM", Pos.StrictAuxiliaryVerb, Pos.ImperativeVerb));
//        FREELING_CY.addTag(new PosTag("VAN", Pos.StrictAuxiliaryVerb, Pos.Infinitive));
//        FREELING_CY.addTag(new PosTag("VAP", Pos.StrictAuxiliaryVerb, Pos.Participle));
//        FREELING_CY.addTag(new PosTag("VAS", Pos.StrictAuxiliaryVerb, Pos.SubjunctiveVerb));
//        FREELING_CY.addTag(new PosTag("VMG", Pos.MainVerb, Pos.Gerund));
        FREELING_CY.addTag(new PosTag("VMI", Pos.MainVerb, Pos.IndicativeVerb));
        FREELING_CY.addTag(new PosTag("VMM", Pos.MainVerb, Pos.ImperativeVerb));
        FREELING_CY.addTag(new PosTag("VMN", Pos.MainVerb, Pos.Infinitive));
//        FREELING_CY.addTag(new PosTag("VMP", Pos.MainVerb, Pos.Participle));
        FREELING_CY.addTag(new PosTag("VMS", Pos.MainVerb, Pos.SubjunctiveVerb));
        //NOTE: Mapping based on "VSC****   <vbser><cni>**" mapping(s) in
        //  http://apertium.svn.sourceforge.net/viewvc/apertium/trunk/apertium-tools/freeling/cy-tags.parole.txt
        //  assuming VS stand for ModalVerb (as in PAROLE) and
        //  <cni> for "conditional" as defined by http://wiki.apertium.org/wiki/Breton
        FREELING_CY.addTag(new PosTag("VSC", Pos.ModalVerb, Pos.ConditionalVerb));
//        FREELING_CY.addTag(new PosTag("VSG", Pos.ModalVerb, Pos.Gerund));
        FREELING_CY.addTag(new PosTag("VSI", Pos.ModalVerb, Pos.IndicativeVerb));
        FREELING_CY.addTag(new PosTag("VSM", Pos.ModalVerb, Pos.ImperativeVerb));
        FREELING_CY.addTag(new PosTag("VSN", Pos.ModalVerb, Pos.Infinitive));
//        FREELING_CY.addTag(new PosTag("VSP", Pos.ModalVerb, Pos.Participle));
        FREELING_CY.addTag(new PosTag("VSS", Pos.ModalVerb, Pos.SubjunctiveVerb));
        // added by addCommonDateTags
        //FREELING_CY.addTag(new PosTag("W", Pos.Date)); //date times
        FREELING_CY.addTag(new PosTag("x")); //unknown
        //add tags for punctations
        addCommonPunctationTags(FREELING_CY);
        //additional punctations mentioned
        addCommonNumberTags(FREELING_CY);
        addCommonDateTags(FREELING_CY);
        addCommonNerTags(FREELING_CY); //add the Tags used by NER
        getInstance().addPosTagSet(FREELING_CY);
        //we need to have a TagMapper that knows about the tags starting with "R"
        FREELING_CY_POS_TAG_MAPPER = new TagMapper() {
            
            @Override
            public String map(String tag) {
                if(tag == null) {
                    return tag;
                }
                if(!tag.isEmpty()){
                    switch (tag.charAt(0)) {
                        case 'A':
                        case 'C':
                        case 'D':
                        case 'N':
                        case 'P':
                        case 'R':
                        case 'G':
                        case 'S':
                            return tag.length() > 2 ? tag.substring(0, 2) : tag;
                        case 'V':
                            return tag.length() > 3 ? tag.substring(0,3) : tag;
                        case 'I':
                        case 'W':
                        case 'X':
                            return tag.length() > 1 ? tag.substring(0,1) : tag;
                        default:
                    }
                } else {
                    return "X"; //unknown
                }
                return tag;
            }
        };
        getInstance().addPosTagMapper(FREELING_CY_POS_TAG_MAPPER, "cy");
    }   
    
    /**
     * Freeling defines common POS tags for Punctiations in the the 
     * <code>common/punct.dat</code> file.<p>
     * Those are used for all/most lanugages. This method adds those mappings
     * to the parsed tagset.
     * @param tagset the {@link TagSet} to add the punctation POS mappings
     */
    private static void addCommonPunctationTags(TagSet<PosTag> tagset){
        tagset.addTag(new PosTag("Fp",Pos.Point));
        tagset.addTag(new PosTag("Fs",Pos.SuspensionPoints));
        tagset.addTag(new PosTag("Fd",Pos.Colon));
        tagset.addTag(new PosTag("Fx",Pos.SemiColon));
        tagset.addTag(new PosTag("Ft",Pos.SecondaryPunctuation)); //%
        tagset.addTag(new PosTag("Fg",Pos.Hyphen));
        tagset.addTag(new PosTag("Fe",Pos.Quote));
        tagset.addTag(new PosTag("Fh",Pos.Slash));
        tagset.addTag(new PosTag("Fpa",Pos.OpenBracket));
        tagset.addTag(new PosTag("Fpt",Pos.CloseBracket));
        tagset.addTag(new PosTag("Fia",Pos.InterrogativeQuantifier));
        tagset.addTag(new PosTag("Fit",Pos.QuestionMark));
        tagset.addTag(new PosTag("Faa",Pos.InterrogativeQuantifier));
        tagset.addTag(new PosTag("Fat",Pos.ExclamativePoint));
        tagset.addTag(new PosTag("Fc", Pos.ParentheticalPunctuation));
        tagset.addTag(new PosTag("Fca",Pos.OpenSquareBracket));
        tagset.addTag(new PosTag("Fct",Pos.CloseSquareBracket));
        tagset.addTag(new PosTag("Fla",Pos.OpenCurlyBracket));
        tagset.addTag(new PosTag("Flt",Pos.CloseSquareBracket));
        tagset.addTag(new PosTag("Fra",Pos.OpenAngleBracket));
        tagset.addTag(new PosTag("Frc",Pos.CloseAngleBracket));
        tagset.addTag(new PosTag("Fz",Pos.SecondaryPunctuation)); //other
    }

    /**
     * Tags assigned by the Quantity Recognition Module
     * @param tagset
     */
    private static void addCommonNumberTags(TagSet<PosTag> tagset){
        tagset.addTag(new PosTag("Z", Pos.CardinalNumber));
        //workaround for cutting the first to letters from Z00000 (same as Z)
        tagset.addTag(new PosTag("Z0", Pos.CardinalNumber));
        tagset.addTag(new PosTag("Zu", Pos.CardinalNumber));
        tagset.addTag(new PosTag("Zm", Pos.CardinalNumber));
        tagset.addTag(new PosTag("Zp", Pos.CardinalNumber));
        tagset.addTag(new PosTag("Zd", Pos.CardinalNumber));
    }
    
    private static void addCommonNerTags(TagSet<PosTag> tagset) {
        tagset.addTag(new PosTag("NP", Pos.ProperNoun));
    }
    private static void addCommonDateTags(TagSet<PosTag> tagset){
        tagset.addTag(new PosTag("W", Pos.Date));
    }
    

}
