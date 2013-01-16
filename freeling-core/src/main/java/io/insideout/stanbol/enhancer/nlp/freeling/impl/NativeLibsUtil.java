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

import java.io.File;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility used to ensure the Freeling native library is loaded.
 * @author Rupert Westenthaler
 *
 */
public class NativeLibsUtil {

    private static final Logger log = LoggerFactory.getLogger(NativeLibsUtil.class);
    
    /**
     * Ensures that the parsed native library is loaded to the JVM. <p>
     * This first
     * checks of the parsed libName is (1) a file (2) a File accessible via
     * the classpath (Resources in JARs do not work!). If (1) and (2) do fail
     * it assumes that the parsed lib is in a "lib directory" of the system.
     * <p>
     * As next step is is checked if the resloved lib is already loaded in the
     * System. This is done by accessing the values of the private field 
     * '<code>loadedLibraryNames</code>' in the {@link ClassLoader} (NOTE that
     * this will likely fail if a {@link SecurityManager} is in place).
     * <p>
     * If neither the resolved lib (case (1) or (2)) nor the parsed lib is 
     * present than the native lib is loaded. In case of (1) or (2) 
     * {@link System#load(String)} will be used. In case of (3) 
     * {@link System#loadLibrary(String)} is used. In the later case a 
     * {@link UnsatisfiedLinkError} will be thrown if the parsed lib string is
     * not a valid path relative to one of the systems lib directories.
     * 
     * @param libName the native lib to check/load
     */
    @SuppressWarnings("unchecked")
    public static void ensureNativeLib(String libName){
        Field nativeLibs;
        ClassLoader cl = NativeLibsUtil.class.getClassLoader();
        try {
            nativeLibs = ClassLoader.class.getDeclaredField("loadedLibraryNames");
            nativeLibs.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Unable to check native libs because "
                + "Field 'loadedLibraryNames' is missing in Classloader impl "
                + cl.getClass()+"!",e);
        }
        String nativLibPath = getNativeLibPath(libName);
        Collection<String> libraries;
        try {
            libraries = (Collection<String>) nativeLibs.get(cl);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to check native libs because "
                    + "Field 'loadedLibraryNames' is not accessible for classloader impl "
                    + cl.getClass()+"!",e);
        }
        cl.getResource(libName);
        if(!libraries.contains(nativLibPath) && !libraries.contains(libName)){
            try {
                System.load(nativLibPath);
            } catch (UnsatisfiedLinkError e) {
                System.loadLibrary(libName);
            }
        }
    }
    
    private static String getNativeLibPath(String libName){
        File nativeLibFile = new File(libName);
        if(!nativeLibFile.isFile()){
            URL libUrl = AnalyzerFactory.class.getClassLoader().getResource(libName);
            if(libUrl != null){
                try {
                    nativeLibFile = new File(libUrl.toURI());
                  } catch(URISyntaxException e) {
                    nativeLibFile = new File(libUrl.getPath());
                  }
            }
            if(libUrl == null || !nativeLibFile.isFile()){
                log.info(" The parsed Freeling native library '" + libName
                    + "' does not exist (not a file and not in classpath)."
                    + " It might still by loaded if placed in a lib directory"
                    + "of the system.");
                return libName; //
            }
        }
        return nativeLibFile.getPath();
    }
}
