package org.apache.stanbol.enhancer.nlp.freeling;

import static org.apache.stanbol.enhancer.nlp.NlpAnnotations.NER_ANNOTATION;
import static org.apache.stanbol.enhancer.nlp.NlpAnnotations.PHRASE_ANNOTATION;
import static org.apache.stanbol.enhancer.nlp.NlpAnnotations.POS_ANNOTATION;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.stanbol.enhancer.nlp.freeling.Analyzer;
import org.apache.stanbol.enhancer.nlp.freeling.Freeling;
import org.apache.stanbol.enhancer.nlp.freeling.LanguageIdentifier.Language;
import org.apache.stanbol.enhancer.nlp.freeling.pool.PoolTimeoutException;
import org.apache.stanbol.enhancer.nlp.freeling.pool.ResourcePool;
import org.apache.stanbol.enhancer.nlp.model.AnalysedText;
import org.apache.stanbol.enhancer.nlp.model.AnalysedTextUtils;
import org.apache.stanbol.enhancer.nlp.model.Chunk;
import org.apache.stanbol.enhancer.nlp.model.Span;
import org.apache.stanbol.enhancer.nlp.model.Span.SpanTypeEnum;
import org.apache.stanbol.enhancer.nlp.model.Token;
import org.apache.stanbol.enhancer.nlp.model.annotation.Value;
import org.apache.stanbol.enhancer.nlp.ner.NerTag;
import org.apache.stanbol.enhancer.nlp.phrase.PhraseTag;
import org.apache.stanbol.enhancer.nlp.pos.PosTag;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestFreelingAnalysis {


    private static final ClassLoader cl = TestFreelingAnalysis.class.getClassLoader();
    
    private final static Logger log = LoggerFactory.getLogger(TestFreelingAnalysis.class);
    
    private static final String freelingSharePath = "/usr/local/Cellar/freeling/3.0/share/freeling";
//    private static final String[] languages = {"en"};//, "as", "cy", "it", "ru", "es", "ca", "gl", "pt"};
    //   "pt", 

    private static final Charset UTF8 = Charset.forName("UTF-8");
    
    private static Freeling freeling;
    
        
    @BeforeClass
    public static void initPosTagger() {
        freeling = new Freeling(freelingSharePath, 5, 0);
    }


    @Test
    public void testEn() throws IOException, PoolTimeoutException {
        String resourceName = "en.txt";
        InputStream in = cl.getResourceAsStream(resourceName);
        Assert.assertNotNull("unable to load resource "+resourceName, in);
        ResourcePool<Analyzer> analyzerPool = freeling.getAnalyzerPool("en");
        Assert.assertNotNull(analyzerPool);
        Analyzer analyzer = analyzerPool.getResource(30*1000);
        Assert.assertNotNull(analyzer);
        AnalysedText at;
        try {
            at = analyzer.analyse(in, UTF8);
        } finally{
            analyzerPool.returnResource(analyzer);
        }
        IOUtils.closeQuietly(in);
        validateAnalysedText(IOUtils.toString(cl.getResourceAsStream(resourceName), 
            UTF8.name()),at);
        IOUtils.closeQuietly(in);
	}
    @Test
    public void testEs() throws IOException, PoolTimeoutException {
        String resourceName = "es.txt";
        InputStream in = cl.getResourceAsStream(resourceName);
        Assert.assertNotNull("unable to load resource "+resourceName, in);
        ResourcePool<Analyzer> analyzerPool = freeling.getAnalyzerPool("es");
        Assert.assertNotNull(analyzerPool);
        Analyzer analyzer = analyzerPool.getResource(30*1000);
        Assert.assertNotNull(analyzer);
        AnalysedText at;
        try {
            at = analyzer.analyse(in, UTF8);
        } finally{
            analyzerPool.returnResource(analyzer);
        }
        IOUtils.closeQuietly(in);
        validateAnalysedText(IOUtils.toString(cl.getResourceAsStream(resourceName), 
            UTF8.name()),at);
        IOUtils.closeQuietly(in);
    }
    @Test
    public void testIt() throws IOException, PoolTimeoutException {
        String resourceName = "it.txt";
        InputStream in = cl.getResourceAsStream(resourceName);
        Assert.assertNotNull("unable to load resource "+resourceName, in);
        ResourcePool<Analyzer> analyzerPool = freeling.getAnalyzerPool("it");
        Assert.assertNotNull(analyzerPool);
        Analyzer analyzer = analyzerPool.getResource(30*1000);
        Assert.assertNotNull(analyzer);
        AnalysedText at;
        try {
            at = analyzer.analyse(in, UTF8);
        } finally{
            analyzerPool.returnResource(analyzer);
        }
        IOUtils.closeQuietly(in);
        validateAnalysedText(IOUtils.toString(cl.getResourceAsStream(resourceName), 
            UTF8.name()),at);
        IOUtils.closeQuietly(in);
    }
    @Test
    public void testCa() throws IOException, PoolTimeoutException {
        String resourceName = "ca.txt";
        InputStream in = cl.getResourceAsStream(resourceName);
        Assert.assertNotNull("unable to load resource "+resourceName, in);
        ResourcePool<Analyzer> analyzerPool = freeling.getAnalyzerPool("ca");
        Assert.assertNotNull(analyzerPool);
        Analyzer analyzer = analyzerPool.getResource(30*1000);
        Assert.assertNotNull(analyzer);
        AnalysedText at;
        try {
            at = analyzer.analyse(in, UTF8);
        } finally{
            analyzerPool.returnResource(analyzer);
        }
        IOUtils.closeQuietly(in);
        validateAnalysedText(IOUtils.toString(cl.getResourceAsStream(resourceName), 
            UTF8.name()),at);
        IOUtils.closeQuietly(in);
    }
    @Test
    public void testRu() throws IOException, PoolTimeoutException {
        String resourceName = "ru.txt";
        InputStream in = cl.getResourceAsStream(resourceName);
        Assert.assertNotNull("unable to load resource "+resourceName, in);
        ResourcePool<Analyzer> analyzerPool = freeling.getAnalyzerPool("ru");
        Assert.assertNotNull(analyzerPool);
        Analyzer analyzer = analyzerPool.getResource(30*1000);
        Assert.assertNotNull(analyzer);
        AnalysedText at;
        try {
            at = analyzer.analyse(in, UTF8);
        } finally{
            analyzerPool.returnResource(analyzer);
        }
        IOUtils.closeQuietly(in);
        validateAnalysedText(IOUtils.toString(cl.getResourceAsStream(resourceName), 
            UTF8.name()),at);
        IOUtils.closeQuietly(in);
    }
    @Test
    public void testPt() throws IOException, PoolTimeoutException {
        String resourceName = "pt.txt";
        InputStream in = cl.getResourceAsStream(resourceName);
        Assert.assertNotNull("unable to load resource "+resourceName, in);
        ResourcePool<Analyzer> analyzerPool = freeling.getAnalyzerPool("pt");
        Assert.assertNotNull(analyzerPool);
        Analyzer analyzer = analyzerPool.getResource(30*1000);
        Assert.assertNotNull(analyzer);
        AnalysedText at;
        try {
            at = analyzer.analyse(in, UTF8);
        } finally{
            analyzerPool.returnResource(analyzer);
        }
        IOUtils.closeQuietly(in);
        validateAnalysedText(IOUtils.toString(cl.getResourceAsStream(resourceName), 
            UTF8.name()),at);
        IOUtils.closeQuietly(in);
    }
    /**
     * Does not work because Sentence detection is not working!
     * @throws IOException
     * @throws PoolTimeoutException 
     */
    //@Test
    public void testAs() throws IOException, PoolTimeoutException {
        String resourceName = "as.txt";
        InputStream in = cl.getResourceAsStream(resourceName);
        Assert.assertNotNull("unable to load resource "+resourceName, in);
        ResourcePool<Analyzer> analyzerPool = freeling.getAnalyzerPool("as");
        Assert.assertNotNull(analyzerPool);
        Analyzer analyzer = analyzerPool.getResource(30*1000);
        Assert.assertNotNull(analyzer);
        AnalysedText at;
        try {
            at = analyzer.analyse(in, UTF8);
        } finally{
            analyzerPool.returnResource(analyzer);
        }
        IOUtils.closeQuietly(in);
        validateAnalysedText(IOUtils.toString(cl.getResourceAsStream(resourceName), 
            UTF8.name()),at);
        IOUtils.closeQuietly(in);
    }

    @Test
    public void testCy() throws IOException, PoolTimeoutException {
        String resourceName = "cy.txt";
        InputStream in = cl.getResourceAsStream(resourceName);
        Assert.assertNotNull("unable to load resource "+resourceName, in);
        ResourcePool<Analyzer> analyzerPool = freeling.getAnalyzerPool("cy");
        Assert.assertNotNull(analyzerPool);
        Analyzer analyzer = analyzerPool.getResource(30*1000);
        Assert.assertNotNull(analyzer);
        AnalysedText at;
        try {
            at = analyzer.analyse(in, UTF8);
        } finally{
            analyzerPool.returnResource(analyzer);
        }
        IOUtils.closeQuietly(in);
        validateAnalysedText(IOUtils.toString(cl.getResourceAsStream(resourceName), 
            UTF8.name()),at);
        IOUtils.closeQuietly(in);
    }
    @Test
    public void testGl() throws IOException, PoolTimeoutException {
        String resourceName = "gl.txt";
        InputStream in = cl.getResourceAsStream(resourceName);
        Assert.assertNotNull("unable to load resource "+resourceName, in);
        ResourcePool<Analyzer> analyzerPool = freeling.getAnalyzerPool("gl");
        Assert.assertNotNull(analyzerPool);
        Analyzer analyzer = analyzerPool.getResource(30*1000);
        Assert.assertNotNull(analyzer);
        AnalysedText at;
        try {
            at = analyzer.analyse(in, UTF8);
        } finally{
            analyzerPool.returnResource(analyzer);
        }
        IOUtils.closeQuietly(in);
        validateAnalysedText(IOUtils.toString(cl.getResourceAsStream(resourceName), 
            UTF8.name()),at);
        IOUtils.closeQuietly(in);
    }
    
    private void validateAnalysedText(String text, AnalysedText at){
        Assert.assertNotNull(text);
        Assert.assertNotNull(at);
        //Assert the AnalysedText
        Assert.assertEquals(0, at.getStart());
        Assert.assertEquals(text.length(), at.getEnd());
        Iterator<Span> it = at.getEnclosed(EnumSet.allOf(SpanTypeEnum.class));
        while(it.hasNext()){
            //validate that the span|start|end corresponds with the Text
            Span span = it.next();
            Assert.assertNotNull(span);
            Assert.assertEquals(text.substring(span.getStart(), span.getEnd()), 
                span.getSpan());
            switch (span.getType()) {
                case Token:
                    double prevProb = -1;
                    List<Value<PosTag>> posTags = span.getAnnotations(POS_ANNOTATION);
                    Assert.assertTrue("All Tokens need to have a PosTag (missing for "
                        + span+ ")", posTags != null && !posTags.isEmpty());
                    for(Value<PosTag> posTag : posTags){
                        //assert Mapped PosTags
                        Assert.assertTrue("PosTag "+posTag+" used by "+span+" is not mapped",
                            posTag.value().isMapped());
                        //assert declining probabilities
                        Assert.assertTrue("Wrong order in "+posTags+" of "+span+"!",
                            prevProb < 0 || posTag.probability() <= prevProb);
                        prevProb = posTag.probability();
                    }
                    Assert.assertNull("Tokens MUST NOT have Phrase annotations!",
                        span.getAnnotation(PHRASE_ANNOTATION));
                    Assert.assertNull("Tokens MUST NOT have NER annotations!",
                        span.getAnnotation(NER_ANNOTATION));
                    break;
                case Chunk:
                    Assert.assertNull("Chunks MUST NOT have POS annotations!",
                        span.getAnnotation(POS_ANNOTATION));
                    List<Token> tokens = AnalysedTextUtils.asList(((Chunk)span).getTokens());
                    prevProb = -1;
                    List<Value<PhraseTag>> phraseTags = span.getAnnotations(PHRASE_ANNOTATION);
                    boolean hasPhraseTag = (phraseTags != null && !phraseTags.isEmpty());
                    List<Value<NerTag>> nerTags = span.getAnnotations(NER_ANNOTATION);
                    boolean hasNerTag = (nerTags != null && !nerTags.isEmpty());
                    Assert.assertTrue("All Chunks with several words need to have a PhraseTag (missing for "
                            + span+ ")",  hasPhraseTag || tokens.size() < 2);
                    Assert.assertTrue("All Chunks with a single word need to have a NerTag (missing for"
                            + span +")", hasNerTag || tokens.size() > 1);
                    for(Value<PhraseTag> phraseTag : phraseTags){
                        //assert Mapped PosTags
                        Assert.assertNotNull("PhraseTag "+phraseTag+" is not mapped",
                            phraseTag.value().getCategory());
                        //assert declining probabilities
                        Assert.assertTrue(prevProb < 0 || phraseTag.probability() < prevProb);
                        prevProb = phraseTag.probability();
                    }
                    for(Value<NerTag> nerTag : nerTags){
                        Assert.assertTrue("NER Tags need to have a probability",
                            nerTag.probability() > 0);
                    }
                    break;
                default:
                    Assert.assertNull(span.getType()+" type Spans MUST NOT have POS annotations!",
                        span.getAnnotation(POS_ANNOTATION));
                    Assert.assertNull(span.getType()+" type Spans MUST NOT have Phrase annotations!",
                        span.getAnnotation(PHRASE_ANNOTATION));
                    Assert.assertNull(span.getType()+" type Spans MUST NOT have NER annotations!",
                        span.getAnnotation(NER_ANNOTATION));
                    break;
            }
        }
    }
    
    @Test
    public void testLanguageDetection() throws IOException, PoolTimeoutException {
        Assert.assertTrue(freeling.isLanguageIdentificationSupported());
        ResourcePool<LanguageIdentifier> langIdPool = freeling.getLangIdPool();
        String[] languages = new String[]{ 
                "en","es","it","de","pt","bg","cs","fr","hi","ja","ru","sl","zh",
                "ca","gl","hr","sk","sr"};
        for(String lang : languages){
            String resourceName = lang+".txt";
            InputStream in = cl.getResourceAsStream(resourceName);
            Assert.assertNotNull("unable to load resource "+resourceName, in);
            String langText = IOUtils.toString(in);
            LanguageIdentifier langId = langIdPool.getResource(30*1000);
            Assert.assertNotNull(langId);
            List<Language> detected;
            try {
                detected = langId.identifyLanguage(langText);
            } finally {
                langIdPool.returnResource(langId);
            }
            log.info(" {} detected for {}",detected,lang);
            Assert.assertNotNull(detected);
            if(detected.isEmpty()){
                log.warn("No Language detected for '{}' - Language not supported ?",lang);
            } else {
                Assert.assertEquals("Wrong Language detected for "+lang,
                    lang,detected.get(0).getLang());
                Assert.assertTrue("Missing Probability for "+lang,
                    detected.get(0).getProb() >= 0);
            }
        }
    }
    
    @AfterClass
    public static final void cleanUp(){
        freeling.close();
        Assert.assertTrue(freeling.isClosed());
        Assert.assertTrue(freeling.getSupportedLanguages().isEmpty());
        Assert.assertFalse(freeling.isLanguageIdentificationSupported());
        try {
            Object o = new Object();
            synchronized (o) {
                o.wait(10000);
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
