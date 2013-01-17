package io.insideout.stanbol.enhancer.nlp.freeling;

import org.apache.stanbol.enhancer.servicesapi.ContentItemFactory;

public interface Constants {

    public static final String SERVLET_ATTRIBUTE_FREELING = Freeling.class.getName();
    public static final String SERVLET_ATTRIBUTE_MAX_RESOURCE_WAIT_TIEM = 
            Constants.class.getPackage().getName()+".maxResrouceWaitTime";
    public static final String SERVLET_ATTRIBUTE_CONTENT_ITEM_FACTORY = ContentItemFactory.class.getName();
    
    public static final Long DEFAULT_RESOURCE_WAIT_TIME = Long.valueOf(30*1000);
    
}
