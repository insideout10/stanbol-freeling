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

import static org.apache.stanbol.enhancer.nlp.NlpAnnotations.MORPHO_ANNOTATION;
import static org.apache.stanbol.enhancer.nlp.NlpAnnotations.NER_ANNOTATION;
import static org.apache.stanbol.enhancer.nlp.NlpAnnotations.PHRASE_ANNOTATION;
import static org.apache.stanbol.enhancer.nlp.NlpAnnotations.POS_ANNOTATION;

import io.insideout.stanbol.enhancer.nlp.freeling.Analyzer;
import io.insideout.stanbol.enhancer.nlp.freeling.mappings.TagMapper;
import io.insideout.stanbol.enhancer.nlp.freeling.mappings.TagSetRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.stanbol.enhancer.nlp.NlpAnnotations;
import org.apache.stanbol.enhancer.nlp.model.AnalysedText;
import org.apache.stanbol.enhancer.nlp.model.AnalysedTextFactory;
import org.apache.stanbol.enhancer.nlp.model.Chunk;
import org.apache.stanbol.enhancer.nlp.model.Span;
import org.apache.stanbol.enhancer.nlp.model.Token;
import org.apache.stanbol.enhancer.nlp.model.annotation.Value;
import org.apache.stanbol.enhancer.nlp.model.tag.TagSet;
import org.apache.stanbol.enhancer.nlp.morpho.MorphoFeatures;
import org.apache.stanbol.enhancer.nlp.ner.NerTag;
import org.apache.stanbol.enhancer.nlp.phrase.PhraseTag;
import org.apache.stanbol.enhancer.nlp.pos.PosTag;
import org.apache.stanbol.enhancer.servicesapi.Blob;
import org.apache.stanbol.enhancer.servicesapi.ContentItemFactory;
import org.apache.stanbol.enhancer.servicesapi.impl.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.upc.freeling.Analysis;
import edu.upc.freeling.ChartParser;
import edu.upc.freeling.DepTxala;
import edu.upc.freeling.HmmTagger;
import edu.upc.freeling.ListAnalysis;
import edu.upc.freeling.ListSentence;
import edu.upc.freeling.ListWord;
import edu.upc.freeling.Maco;
import edu.upc.freeling.Nec;
import edu.upc.freeling.Senses;
import edu.upc.freeling.Splitter;
import edu.upc.freeling.Tokenizer;
import edu.upc.freeling.UkbWrap;
import edu.upc.freeling.Word;

public class AnalyzerImpl implements Analyzer{
    
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private final Logger log = LoggerFactory.getLogger(AnalyzerImpl.class);
    
    private static final double DEFAULT_MIN_PROBABILITY = 0.25;

    private final ContentItemFactory cif;
    private final AnalysedTextFactory atf;
    private final TagSetRegistry tsr = TagSetRegistry.getInstance();
    
    private final TagSet<PosTag> posTagSet;
    private final Map<String,PosTag> adhocPosTags;
    private final TagMapper posTagMapper;
    private final TagSet<PhraseTag> phraseTagSet;
    private final Map<String,PhraseTag> adhocPhraseTags;
    private final TagSet<NerTag> nerTagSet;
    private final Map<String,NerTag> adhocNerTags;
    private final String language;
	private final Tokenizer tokenizer;
	private final Splitter splitter;
    private final boolean alwaysFlush;
    private Maco maco;
	private HmmTagger hmmTagger;
	private ChartParser chartParser;
    private DepTxala depTxala;
	private Nec nec;
	private Senses senses;
	private UkbWrap ukbWrap;

	private double minProb = DEFAULT_MIN_PROBABILITY;

    private boolean closed = false;

	AnalyzerImpl(final ContentItemFactory cif, AnalysedTextFactory atf,
	    final String language, final Tokenizer tokenizer,
			final Splitter splitter, final boolean alwaysFlush) {
	    if(cif == null){
	        throw new IllegalArgumentException("The parsed ContentItemFactory MUST NOT be NULL!");
	    }
        this.cif = cif;
	    if(atf == null){
	        throw new IllegalArgumentException("The parsed AnalysedTextFactory MUST NOT be NULL!");
	    }
	    this.atf = atf;
	    if(language == null || language.isEmpty()){
	        throw new IllegalArgumentException("The parsed language MUST NOT be NULL nor an empty String");
	    }
	    this.language = language;
	    if(tokenizer == null){
	        throw new IllegalArgumentException("The parsed Tokenizer MUST NOT be NULL!");
	    }
        this.tokenizer = tokenizer;
        if(splitter == null){
            throw new IllegalArgumentException("The parsed Sentnece Splitter MUST NOT be NULL!");
        }
        this.splitter = splitter;
        this.alwaysFlush = alwaysFlush;
        //the rest of the stuff is optional. See setter methods
        //init the TagSets
        posTagSet = tsr.getPosTagSet(language);
        adhocPosTags = tsr.getAdhocPosTagMap(language);
        phraseTagSet = tsr.getPhraseTagSet(language);
        adhocPhraseTags = tsr.getAdhocPhraseTagMap(language);
        nerTagSet = tsr.getNerTagSet(language);
        adhocNerTags = tsr.getAdhocNerTagMap(language);
        posTagMapper = tsr.getPosTagMapper(language);
        
	}
    /**
     * @return the language
     */
    public final String getLanguage() {
        return language;
    }

    /**
     * @return the tokenizer
     */
    protected final Tokenizer getTokenizer() {
        if(closed){
            throw new IllegalStateException("This Analyzer was already closed!");
        } else {
            return tokenizer;
        }
    }

    /**
     * @return the splitter
     */
    protected final Splitter getSplitter() {
        if(closed){
            throw new IllegalStateException("This Analyzer was already closed!");
        } else {
            return splitter;
        }
    }

    /**
     * @return the maco
     */
    protected final Maco getMaco() {
        return closed ? null : maco;
    }

    /**
     * @return the hmmTagger
     */
    protected final HmmTagger getHmmTagger() {
        return closed ? null : hmmTagger;
    }

    /**
     * @return the chartParser
     */
    protected final ChartParser getChartParser() {
        return closed ? null : chartParser;
    }

    /**
     * @return the depTxala
     */
    protected final DepTxala getDepTxala() {
        return closed ? null : depTxala;
    }

    /**
     * @return the nec
     */
    protected final Nec getNec() {
        return closed ? null : nec;
    }

    /**
     * @return the senses
     */
    protected final Senses getSenses() {
        return closed ? null : senses;
    }

    /**
     * @return the ukbWrap
     */
    protected final UkbWrap getUkbWrap() {
        return closed ? null : ukbWrap;
    }
	
    /**
     * @param maco the maco to set
     */
    final void setMaco(Maco maco) {
        this.maco = maco;
    }


    /**
     * @param hmmTagger the hmmTagger to set
     */
    final void setHmmTagger(HmmTagger hmmTagger) {
        this.hmmTagger = hmmTagger;
    }


    /**
     * @param chartParser the chartParser to set
     */
    final void setChartParser(ChartParser chartParser) {
        this.chartParser = chartParser;
    }


    /**
     * @param depTxala the depTxala to set
     */
    final void setDepTxala(DepTxala depTxala) {
        this.depTxala = depTxala;
    }


    /**
     * @param nec the nec to set
     */
    final void setNec(Nec nec) {
        this.nec = nec;
    }


    /**
     * @param senses the senses to set
     */
    final void setSenses(Senses senses) {
        this.senses = senses;
    }


    /**
     * @param ukbWrap the ukbWrap to set
     */
    final void setUkbWrap(UkbWrap ukbWrap) {
        this.ukbWrap = ukbWrap;
    }

    final public void close(){
        closed = true;
        tokenizer.delete();
        splitter.delete();
        if(maco != null){
            maco.delete();
        }
        if(hmmTagger != null){
            hmmTagger.delete();
        }
        if(chartParser != null){
            chartParser.delete();
        }
        if(depTxala != null){
            depTxala.delete();
        }
        if(nec != null){
            nec.delete();
        }
        if(senses != null){
            senses = null;
        }
        if(ukbWrap != null){
            ukbWrap = null;
        }
        
    }


    public AnalysedText analyse(InputStream in,  Charset charset) throws IOException {
        //init the AnalysedText
        return analyse(cif.createBlob(new StreamSource(in, "text/plain; charset="
            + (charset == null ? UTF8 : charset).name())));
    }
    public AnalysedText analyse(Blob blob) throws IOException {
        if(blob == null){
            throw new NullPointerException("The parsed Blob MUST NOT be NULL!");
        }
        if(!blob.getMimeType().startsWith("text/")){
            throw new IllegalArgumentException("The MediaType of the parsed Blob "
                + " MUST be a text type (start with 'text/')!");
        }
        AnalysedText at = atf.createAnalysedText(blob);
        //perform the freeling analysis
        //1. tokenize
        long analysisStart = System.currentTimeMillis();
        long stepStart = analysisStart;
        final ListWord listWord = tokenizer.tokenize(at.getSpan());

        long timeStamp = System.currentTimeMillis();
        log.debug("Tokenized {} words ({}ms)",listWord.size(),timeStamp-stepStart);
        stepStart = timeStamp;
        //2. sentence detection
        final ListSentence listSentence = splitter.split(
            listWord, alwaysFlush);
        timeStamp = System.currentTimeMillis();
        log.debug("Splitted {} sentences ({}ms)", listSentence.size(),timeStamp-stepStart);
        stepStart = timeStamp;
        //3. morphological analysis
        Maco maco = getMaco();
        if(maco != null){
            maco.analyze(listSentence);
            timeStamp = System.currentTimeMillis();
            log.debug(" ... Maco ({}ms)",timeStamp-stepStart);
            stepStart = timeStamp;
        }
        //4. Part-of-Speech (POS) Tagging.
        HmmTagger hmmTagger = getHmmTagger();
        if(hmmTagger != null){
            hmmTagger.analyze(listSentence);
            timeStamp = System.currentTimeMillis();
            log.debug(" ... HmmTagger ({}ms)",timeStamp-stepStart);
            stepStart = timeStamp;
        }
        //5. Named Entity (NE) Classificiation.
        Nec nec = getNec();
        if(nec != null){
            nec.analyze(listSentence);
            timeStamp = System.currentTimeMillis();
            log.debug(" ... NEC ({}ms)",timeStamp-stepStart);
            stepStart = timeStamp;
        }
        //6. Word Sense Disambiguation
        UkbWrap ukbWrap = getUkbWrap();
        if(ukbWrap != null){
            ukbWrap.analyze(listSentence);
            timeStamp = System.currentTimeMillis();
            log.debug(" ... UkbWra ({}ms)",timeStamp-stepStart);
            stepStart = timeStamp;
        }
        //7. Sense Labelling
        Senses senses = getSenses();
        if(senses != null){
            senses.analyze(listSentence);
            timeStamp = System.currentTimeMillis();
            log.debug(" ... Senses ({}ms)",timeStamp-stepStart);
            stepStart = timeStamp;
        }
        //8. Chunk parser
        ChartParser chartParser = getChartParser();
        if(chartParser != null) {
            chartParser.analyze(listSentence);
            timeStamp = System.currentTimeMillis();
            log.debug(" ... ChartParser ({}ms)",timeStamp-stepStart);
            stepStart = timeStamp;
        }
        //9. Dependency parser
        DepTxala depTxala = getDepTxala();
        if(depTxala != null) {
            depTxala.analyze(listSentence);
            timeStamp = System.currentTimeMillis();
            log.debug(" ... DepTxala ({}ms)",timeStamp-stepStart);
            stepStart = timeStamp;
        }
        timeStamp = System.currentTimeMillis();
        log.info(" ... processed {} '{}' Words in in {}ms",
            new Object[]{listWord.size(),language,timeStamp-analysisStart});
        stepStart = timeStamp;
        //get the Data for the language
        for(int i=0;i<listSentence.size();i++){
            edu.upc.freeling.Sentence sent = listSentence.get(i);
            at.addSentence((int)sent.get(0).getSpanStart(), 
                (int)sent.get((int)sent.size()-1).getSpanFinish());
            for (int j = 0; j < sent.size(); j++) {
                Word word = sent.get(j);
                long start = word.getSpanStart();
                long end = word.getSpanFinish();
                double prob = word.getAnalysis().size() > 0 ? word.getAnalysis().get(0).getProb() : -1;
                //Not all words are words. Some of them are chunks.
                ListWord enclosedWords = word.getWordsMw();
                if(enclosedWords.size() > 1){
                    for(int k = 0; k < enclosedWords.size(); k++){
                        Word enclosedWord = enclosedWords.get(k);
                        //add Tokens
                        Token token = at.addToken((int)enclosedWord.getSpanStart(), 
                            (int)enclosedWord.getSpanFinish());
                        log.trace("word (enclosed) :: {} (from: {}, lc-form: {}, inDict: {})",
                            new Object[]{token, enclosedWord.getForm(),enclosedWord.getLcForm(),
                                enclosedWord.foundInDict()});
                        //sometimes enclosed words do not have Analysis. In this
                        //case we use the information provided by the parent word
                        boolean fromParent = enclosedWord.getAnalysis().size() < 1;
                        ListAnalysis analysisList = fromParent ? word.getAnalysis() :
                                enclosedWord.getAnalysis();
                        log.trace(" > {} analysis {}: ",analysisList.size(),
                            enclosedWord.getAnalysis().size() < 1 ? "(from parent)" : "");
                        processAnalysis(token, analysisList, fromParent);
                    }
                    Chunk phrase = at.addChunk((int)start, (int)end);
                    Value<PhraseTag> phraseTag = addPhraseTag(phrase, word.getTag(), prob);
                    log.trace("chunk            :: {}{} (tag: {}, prop {})",
                        new Object[]{phrase,phrase.getSpan(),word.getTag(),prob});
                    log.trace(" > form         :: {} ",word.getForm());
                    log.trace(" > phraseTag    :: {} ",phraseTag);
                } else { //only a single word
                    Token token = at.addToken((int)start,(int)end);
                    log.trace("word            :: {} (from: {}, lc-form: {}, inDict: {}",
                        new Object[]{token, word.getForm(),word.getLcForm(),
                            word.foundInDict()});
                    log.trace(" > {} analysis: ",word.getAnalysis().size());
                    processAnalysis(token, word.getAnalysis(), false);
                }
                //check for NamedEntities
                if(word.getTag() != null && word.getTag().startsWith("NP")){
                    Chunk chunk = at.addChunk((int)start, (int)end);
                    Value<NerTag> nerTag = addNerTag(chunk, word.getTag(), prob);
                    log.trace("Named Entity      :: {}{} (form:{})", 
                        new Object[]{chunk,chunk.getSpan(),word.getForm()});
                    log.trace(" > NER            :: {}", nerTag);
                }
            }
        }
        return at;
    }


    /**
     * For some languages (e.g. "ru") Freeling provides 100+ Analysis objects.
     * Most of them do map to the same PosType but only differ in some other
     * attributes. to get proper confidences for PosTypes we need to collect
     * the probabilities of different {@link Analysis} objects mapping to
     * the same {@link PosTag}. The same is done with {@link MorphoFeatures}.
     * @param token the Token to add the annotations
     * @param analysisList the list of {@link Analysis}
     * @param fromParent if Analysis objects are taken from a parent {@link Word}
     * for {@link Word#getWordsMw()} (enclosed) words.
     */
    private void processAnalysis(Token token, ListAnalysis analysisList, boolean fromParent) {
        Map<PosTag,double[]> posMap = new LinkedHashMap<PosTag,double[]>();
        Map<MorphoFeatures,double[]> mfMap = new LinkedHashMap<MorphoFeatures,double[]>();
        for(int a = 0;a<analysisList.size();a++){
            Analysis analysis = analysisList.get(a);
            PosTag posTag = getPostTag(analysis.getTag());
            log.trace("   {}. POS       :: {} ", a, posTag);
            double[] prob = posMap.get(posTag);
            if(prob == null){
                posMap.put(posTag, new double[]{analysis.getProb()});
            } else {
                prob[0] = prob[0]+analysis.getProb();
            }
            if(!fromParent){ //ignore lemma if from parent
                MorphoFeatures mf = getMorphoFeatures(token, analysis, posTag);
                if(mf != null){
                    prob = mfMap.get(posTag);
                    if(prob == null){
                        mfMap.put(mf, new double[]{analysis.getProb()});
                    } else {
                        prob[0] = prob[0]+analysis.getProb();
                    }
                }
            }
        }
        List<Value<PosTag>> posValues = new ArrayList<Value<PosTag>>(posMap.size());
        for(Entry<PosTag,double[]> posEntry : posMap.entrySet()){
            Value<PosTag> posValue;
//            log.info("Entry: {}:{}",posEntry.getKey(),posEntry.getValue()[0]);
            double prob = Math.round(posEntry.getValue()[0]*10000)/10000d;
            if(prob >= minProb || prob < 0){
                //Round to avoid rounding errors generating values > 1
                posValue = prob < 0 ? Value.value(posEntry.getKey()) :  
                    Value.value(posEntry.getKey(), prob);
                log.trace("   > POS       :: {} ", posValue);
                posValues.add(posValue);
            } // else ignore annotations with less probability
        }
        token.addAnnotations(POS_ANNOTATION, posValues);
        List<Value<MorphoFeatures>> mfValues = new ArrayList<Value<MorphoFeatures>>(mfMap.size());
        for(Entry<MorphoFeatures,double[]> mfEntry : mfMap.entrySet()){
            Value<MorphoFeatures> mfValue = null;
            double prob = Math.round(mfEntry.getValue()[0]*10000)/10000d;
            if(prob >= minProb || prob < 0){
                //Round to avoid rounding errors generating values > 1
                mfValue = prob < 0 ? Value.value(mfEntry.getKey()) : 
                    Value.value(mfEntry.getKey(), prob);
                log.trace("   > Morpho    :: {} ", mfValue);
                mfValues.add(mfValue);
            } // else ignore annotations with less probability
        }
        token.addAnnotations(MORPHO_ANNOTATION, mfValues);
    }
    /**
     * Adds an {@link NlpAnnotations#POS_ANNOTATION} for the parsed tag and
     * probability to the {@link Span}.
     * @param span the span
     * @param language the language
     * @param tag the string tag
     * @param prob the probability
     * @return the {@link Value#value()} of the added {@link NlpAnnotations#POS_ANNOTATION}
     */
    private Value<PosTag> addPosTag(Span span, String parsedTag, double prob){
        PosTag posTag = getPostTag(parsedTag);
        if(prob >= minProb || prob < 0){
            Value<PosTag> value = prob < 0 ? Value.value(posTag) :  
                    Value.value(posTag, prob);
            span.addAnnotation(POS_ANNOTATION, value);
            return value;
        } else {
            return null;
        }
    }


    /**
     * @param parsedTag
     * @return
     */
    private PosTag getPostTag(String parsedTag) {
        String tag = posTagMapper != null ? posTagMapper.map(parsedTag) : parsedTag;
        PosTag posTag = posTagSet != null ? posTagSet.getTag(tag) : null;
        if(posTag == null){
            posTag = adhocPosTags.get(tag);
            if(posTag == null) {
                log.warn("Unmapped POS tag '{}' (unmapped: {}) for language '{}' and Tagset '{}'",
                    new Object[]{tag, parsedTag, language, posTagSet != null ? posTagSet.getName() : "<<none>>"});
                posTag = new PosTag(tag);
                adhocPosTags.put(tag, posTag);
            }
        }
        return posTag;
    }
    
    /**
     * Adds an {@link NlpAnnotations#PHRASE_ANNOTATION} for the parsed tag and
     * probability to the {@link Span}.
     * @param span the span
     * @param language the language
     * @param tag the string tag
     * @param prob the probability
     * @return the {@link Value#value()} of the added {@link NlpAnnotations#PHRASE_ANNOTATION}
     */
    private Value<PhraseTag> addPhraseTag(Span span, String tag, double prob){
        PhraseTag phraseTag = phraseTagSet != null ? phraseTagSet.getTag(tag) : null;
        if(phraseTag == null){
            phraseTag = adhocPhraseTags.get(tag);
            if(phraseTag == null) {
                //try to create phrase tag based on the PosTag for the parsed tag
                PosTag posTag = getPostTag(tag);
                if(posTag != null && !posTag.getCategories().isEmpty()){
                    phraseTag = new PhraseTag(posTag.getTag(),posTag.getCategories().iterator().next());
                } else {
                    log.warn("Unmapped Phrase tag '{}' for language '{}' and Tagset '{}'",
                        new Object[]{tag, language, phraseTagSet != null ? phraseTagSet.getName() : "<<none>>"});
                    phraseTag = new PhraseTag(tag);
                }
                adhocPhraseTags.put(tag, phraseTag);
            }
        }
        if(prob >= minProb || prob < 0){
            Value<PhraseTag> value = prob < 0 ? Value.value(phraseTag) : 
                    Value.value(phraseTag, prob);
            span.addAnnotation(PHRASE_ANNOTATION, value);
            return value;
        } else {
            return null;
        }
    }
    /**
     * Adds an {@link NlpAnnotations#NER_ANNOTATION} for the parsed tag and
     * probability to the {@link Span}.
     * @param span the span
     * @param tag the string tag
     * @param prob the probability
     * @return the {@link Value#value()} of the added {@link NlpAnnotations#NER_ANNOTATION}
     */
    private Value<NerTag> addNerTag(Span span, String tag, double prob){
        NerTag nerTag = nerTagSet != null ? nerTagSet.getTag(tag) : null;
        if(nerTag == null){
            nerTag = adhocNerTags.get(tag);
            if(nerTag == null) {
                log.warn("Unmapped NER tag '{}' for language '{}' and Tagset '{}'",
                    new Object[]{tag, language, nerTagSet != null ? nerTagSet.getName() : "<<none>>"});
                nerTag = new NerTag(tag);
                adhocNerTags.put(tag, nerTag);
            }
        }
        if(prob >= minProb || prob < 0){
            Value<NerTag> value = prob < 0 ? Value.value(nerTag) :
                Value.value(nerTag, prob);
            span.addAnnotation(NER_ANNOTATION, value);
            return value;
        } else {
            return null;
        }
    }
    
    private MorphoFeatures getMorphoFeatures(Span span, Analysis analysis, PosTag posTag){
        if(analysis.getLemma() != null && !span.getSpan().equalsIgnoreCase(analysis.getLemma())){
            MorphoFeatures mf = new MorphoFeatures(analysis.getLemma());
            if(posTag == null){
                posTag = getPostTag(analysis.getTag());
            }
            if(posTag != null){
                mf.addPos(posTag); //note that this morpho is for this pos
            }
            return mf;
        } else {
            return null;
        }
    }
    
    /**
     * @param span
     * @param posTag
     * @param lemma
     * @param prob
     */
    private Value<MorphoFeatures> addLemmaAnnotation(Span span, Analysis analysis, PosTag posTag) {
        MorphoFeatures mf = getMorphoFeatures(span,analysis,posTag);
        double prob = analysis.getProb();
        if(mf != null && (prob >= minProb || prob < 0)){
            Value<MorphoFeatures> value = prob < 0 ? Value.value(mf) :
                Value.value(mf, prob);
            span.addAnnotation(MORPHO_ANNOTATION, value);
            return value;
        } else {
            return null;
        }
    }
}
