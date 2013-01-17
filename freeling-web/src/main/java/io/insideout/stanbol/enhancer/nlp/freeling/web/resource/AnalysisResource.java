package io.insideout.stanbol.enhancer.nlp.freeling.web.resource;

import static io.insideout.stanbol.enhancer.nlp.freeling.Constants.DEFAULT_RESOURCE_WAIT_TIME;
import static io.insideout.stanbol.enhancer.nlp.freeling.Constants.SERVLET_ATTRIBUTE_FREELING;
import static io.insideout.stanbol.enhancer.nlp.freeling.Constants.SERVLET_ATTRIBUTE_MAX_RESOURCE_WAIT_TIEM;
import static javax.ws.rs.core.HttpHeaders.CONTENT_LANGUAGE;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
import io.insideout.stanbol.enhancer.nlp.freeling.Analyzer;
import io.insideout.stanbol.enhancer.nlp.freeling.Freeling;
import io.insideout.stanbol.enhancer.nlp.freeling.LanguageIdentifier;
import io.insideout.stanbol.enhancer.nlp.freeling.LanguageIdentifier.Language;
import io.insideout.stanbol.enhancer.nlp.freeling.pool.PoolTimeoutException;
import io.insideout.stanbol.enhancer.nlp.freeling.pool.ResourcePool;
import io.insideout.stanbol.enhancer.nlp.freeling.util.Utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.stanbol.enhancer.nlp.model.AnalysedText;
import org.apache.stanbol.enhancer.servicesapi.Blob;

@Path("/analysis")
public class AnalysisResource {

    private static final Charset UTF8 = Charset.forName("UTF-8");
    
    @Context
    ServletContext servletContext;


    private Long maxWaitTime;
    private Freeling freeling;
    

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<String> supported(){
        return getFreeling().getSupportedLanguages();
    }
    
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response analyse(Blob blob, @Context HttpHeaders headers){
        Freeling freeling = getFreeling();
        Long maxWaitTime = getMaxWaitTime();
        Charset cs = blob.getParameter().get("charset") != null ?
                Charset.forName(blob.getParameter().get("charset")) : UTF8;
        List<String> contentLanguages = headers.getRequestHeader(HttpHeaders.CONTENT_LANGUAGE);
        //Now retrieve/detect the language of the text
        final Language contentLanguage;
        if(contentLanguages == null || contentLanguages.isEmpty()){
            //try to detect the language
            if(!freeling.isLanguageIdentificationSupported()){
                return Response.status(BAD_REQUEST).entity("Language Identification"
                        + "is not supported. Please explicitly parse the "
                        + "Language by setting the '"+HttpHeaders.CONTENT_LANGUAGE
                        + "' in the Request").build();
            }
            try {
                contentLanguage = detectLanguage(IOUtils.toString(blob.getStream(), cs.name()));
            } catch (PoolTimeoutException e) {
                return Response.status(SERVICE_UNAVAILABLE)
                        .entity("Unable to obtain LanguageIdentifier resource after "
                            + "waiting for "+(maxWaitTime/1000d)+"sec").build();
            } catch (IOException e) {
                throw new WebApplicationException(e);
            }
            if(contentLanguage == null){
                return Response.status(BAD_REQUEST).entity("Unable to detect "
                        + "Language for parsed Text. Please explicitly parse the "
                        + "Language by setting the '"+CONTENT_LANGUAGE
                        + "' in the Request").build();
            }
        } else if(contentLanguages.size() > 1){
            return Response.status(BAD_REQUEST).entity("The " + CONTENT_LANGUAGE
                + "Header MUST only have a single value (parsed: "+
                    contentLanguages.toString()+")!").build();
        } else {
            String clString = contentLanguages.get(0);
            if(clString.length() != 2){
                return Response.status(BAD_REQUEST).entity("The " + CONTENT_LANGUAGE
                    + "Header MUST use two digit (ISO 639-1) language codes (parsed: "+
                    clString+")!").build();
            }
            contentLanguage = new Language(clString, 1.0);
        }
        //analyse the text
        if(!freeling.isLanguageSupported(contentLanguage.getLang())){
            return Response.status(BAD_REQUEST).entity("The language '" 
                    + contentLanguage.getLang()
                    + "' of the parsed text is not supported (supported: "
                    + freeling.getSupportedLanguages()+")")
                .header(HttpHeaders.CONTENT_LANGUAGE, contentLanguage.getLang())
                .build();
        }
        ResourcePool<Analyzer> analyzerPool = freeling.getAnalyzerPool(contentLanguage.getLang());
        Analyzer analyzer;
        try {
            analyzer = analyzerPool.getResource(maxWaitTime);
        } catch (PoolTimeoutException e) {
            return Response.status(SERVICE_UNAVAILABLE)
                    .entity("Unable to obtain Analyzer instance for language '"
                            + contentLanguage.getLang() + "after waiting for "
                            +(maxWaitTime/1000d)+"sec").build();
        }
        AnalysedText at; 
        try {
            try {
                at = analyzer.analyse(blob);
            } catch (IOException e) {
                throw new WebApplicationException(e);
            }
        } finally {
            analyzerPool.returnResource(analyzer);
        }
        return Response.ok(at)
                .header(HttpHeaders.CONTENT_LANGUAGE, contentLanguage.getLang())
                .build();
    }

    private Language detectLanguage(String text) throws PoolTimeoutException, IOException{
        ResourcePool<LanguageIdentifier> langIdPool = freeling.getLangIdPool();
        List<Language> detected;
        LanguageIdentifier langident = langIdPool.getResource(maxWaitTime);
        try {
            detected = langident.identifyLanguage(text);
        } finally {
            langIdPool.returnResource(langident);
        }
        return detected.isEmpty() ? null : detected.get(0);
    }
    
    private Freeling getFreeling(){
        if(freeling == null){
            freeling = Utils.getResource(Freeling.class, servletContext, SERVLET_ATTRIBUTE_FREELING);
        }
        return freeling;
    }
    
    private Long getMaxWaitTime() {
        if(maxWaitTime == null){
            maxWaitTime = Utils.getResource(Number.class, servletContext, 
                SERVLET_ATTRIBUTE_MAX_RESOURCE_WAIT_TIEM, DEFAULT_RESOURCE_WAIT_TIME).longValue();
        }
        return maxWaitTime;
    }
    
}
