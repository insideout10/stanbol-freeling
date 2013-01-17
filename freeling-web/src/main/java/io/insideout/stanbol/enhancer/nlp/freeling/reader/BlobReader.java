package io.insideout.stanbol.enhancer.nlp.freeling.reader;

import static io.insideout.stanbol.enhancer.nlp.freeling.Constants.SERVLET_ATTRIBUTE_CONTENT_ITEM_FACTORY;
import io.insideout.stanbol.enhancer.nlp.freeling.Constants;
import io.insideout.stanbol.enhancer.nlp.freeling.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.servlet.ServletContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.apache.stanbol.enhancer.servicesapi.Blob;
import org.apache.stanbol.enhancer.servicesapi.ContentItemFactory;
import org.apache.stanbol.enhancer.servicesapi.impl.StreamSource;

@Provider
public class BlobReader implements MessageBodyReader<Blob>{

    @Context
    protected ServletContext servletContext;

    private ContentItemFactory contentItemFactory;
    
    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.isAssignableFrom(Blob.class);
    }

    @Override
    public Blob readFrom(Class<Blob> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String,String> httpHeaders, InputStream entityStream) throws IOException,
            WebApplicationException {
        ContentItemFactory cif = getContentItemFactory();
        return cif.createBlob(new StreamSource(entityStream,mediaType.toString()));
    }
    
    private ContentItemFactory getContentItemFactory(){
        if(contentItemFactory == null){
            contentItemFactory = Utils.getResource(ContentItemFactory.class, 
                servletContext, SERVLET_ATTRIBUTE_CONTENT_ITEM_FACTORY);
        }
        return contentItemFactory;
        
    }

}
