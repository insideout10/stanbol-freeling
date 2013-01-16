package org.apache.stanbol.enhancer.nlp.freeling;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.stanbol.enhancer.nlp.model.AnalysedText;

/**
 * Interface used to analyse text using Freeling.
 */
public interface Analyzer {

    /**
     * Analysis the text read from the parsed InputStream and returns the
     * analysis results as {@link AnalysedText}.
     * @param in the {@link InputStream} to read the data from. The stream MUST
     * NOT be closed by implementations.
     * @param mediaType the charset used by the parsed input stream. UTF-8 if
     * <code>null</code> is parsed.
     * @return the AnalyzedText
     * @throws IOException on any error while reading from the stream.
     */
    AnalysedText analyse(InputStream in,  Charset charset) throws IOException;
    
}
