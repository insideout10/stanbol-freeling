package io.insideout.stanbol.enhancer.nlp.freeling.web.writer;

import io.insideout.stanbol.enhancer.nlp.freeling.LanguageIdentifier.Language;
import io.insideout.stanbol.enhancer.nlp.freeling.web.util.Utils;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class DetectedLanguageWriter implements MessageBodyWriter<Collection<Language>>{

    private JsonFactory jsonFactory;
    
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Utils.testParameterizedType(Collection.class, new Class<?>[]{Language.class}, genericType) &&
                MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType);
    }

    @Override
    public long getSize(Collection<Language> t, Class<?> type, Type genericType, Annotation[] annotations,
            MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(Collection<Language> detected, Class<?> type, Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String,Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {
        JsonGenerator jg = getJsonFactory().createJsonGenerator(entityStream);
        jg.writeStartArray();
        for(Language lang : detected){
            jg.writeStartObject();
            jg.writeStringField("lang", lang.getLang());
            if(lang.getProb() > 0){
                jg.writeNumberField("prob", lang.getProb());
            }
            jg.writeEndObject();
        }
        jg.writeEndArray();
        jg.close();
    }
    
    public JsonFactory getJsonFactory() {
        if(jsonFactory == null){
            jsonFactory = new JsonFactory();
        }
        return jsonFactory;
    }

}
