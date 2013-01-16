package org.apache.stanbol.enhancer.nlp.freeling.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FreelingBundleActivator implements BundleActivator {

    private final static Logger logger = LoggerFactory.getLogger(FreelingBundleActivator.class);

    static {
        try {
            System.loadLibrary("freeling_javaAPI");
        } catch (Throwable t) {
            logger.error("Error loading the Freeling APIs.", t);
        }
    }

    @Override
    public void start(BundleContext context) throws Exception {
        // TODO Auto-generated method stub
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        // TODO Auto-generated method stub

    }

}
