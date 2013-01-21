package io.insideout.stanbol.enhancer.nlp.freeling.web.resource;

import static io.insideout.stanbol.enhancer.nlp.freeling.web.Constants.DEFAULT_RESOURCE_WAIT_TIME;
import static io.insideout.stanbol.enhancer.nlp.freeling.web.Constants.SERVLET_ATTRIBUTE_FREELING;
import static io.insideout.stanbol.enhancer.nlp.freeling.web.Constants.SERVLET_ATTRIBUTE_MAX_RESOURCE_WAIT_TIEM;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
import io.insideout.stanbol.enhancer.nlp.freeling.Freeling;
import io.insideout.stanbol.enhancer.nlp.freeling.LanguageIdentifier;
import io.insideout.stanbol.enhancer.nlp.freeling.LanguageIdentifier.Language;
import io.insideout.stanbol.enhancer.nlp.freeling.pool.PoolTimeoutException;
import io.insideout.stanbol.enhancer.nlp.freeling.pool.ResourcePool;
import io.insideout.stanbol.enhancer.nlp.freeling.web.util.Utils;

import java.util.Collection;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/langident")
public class LangIdentResource {

    private Freeling freeling;
    
    @Context
    ServletContext servletContext;

    private Long maxWaitTime;
    
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response detectLanguages(String text){
        Freeling freeling = getFreeling();
        if(!freeling.isLanguageIdentificationSupported()){
            return Response.serverError().entity("Language Identification is not "
                + "supported by the configure Freeling instance!").build();
        }
        ResourcePool<LanguageIdentifier> langIdentPool = freeling.getLangIdPool();
        LanguageIdentifier langidnet;
        try {
            langidnet = langIdentPool.getResource(getMaxWaitTime());
        } catch (PoolTimeoutException e) {
            return Response.status(SERVICE_UNAVAILABLE)
                    .entity("Unable to obtain LanguageIdentifier resource after "
                        + "waiting for "+(maxWaitTime/1000d)+"sec").build();
        }
        try {
            return Response.ok(langidnet.identifyLanguage(text)).build();
        } finally {
            langIdentPool.returnResource(langidnet);
        }
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
