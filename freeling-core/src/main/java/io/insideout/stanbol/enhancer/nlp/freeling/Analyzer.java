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
