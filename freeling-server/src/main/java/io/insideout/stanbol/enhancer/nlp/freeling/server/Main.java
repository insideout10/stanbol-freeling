package io.insideout.stanbol.enhancer.nlp.freeling.server;

import io.insideout.stanbol.enhancer.nlp.freeling.Freeling;
import io.insideout.stanbol.enhancer.nlp.freeling.web.Constants;
import io.insideout.stanbol.enhancer.nlp.freeling.web.FreelingApplication;

import java.io.File;
import java.util.Iterator;
import java.util.ServiceLoader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FilenameUtils;
import org.apache.stanbol.enhancer.servicesapi.ContentItemFactory;
import org.apache.wink.server.internal.servlet.RestServlet;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;


public class Main {

    private static final String ENV_FREELING_SHARED_FOLDER = "FREELINGSHARED";
    private static final String PROPERTY_FREELING_SHARED_FOLDER = "freeling.shared";
    private static final String PROPERTY_FREELING_CONFIG_FOLDER = "freeling.config";
    
    private static final int DEFAULT_PORT = 8080;
    private static final int DEFAULT_MAX_POOL_SIZE = 10;
    private static final int DEFAULT_MIN_QUEUE_SIZE = 1;
    private static final int DEFAULT_INIT_THREADS = 1;
    
    private static final Options options;
    static {
        options = new Options();
        options.addOption("h", "help", false, "display this help and exit");
        options.addOption("p","port",true, 
            "The port for the server (default: "+DEFAULT_PORT+")");
        options.addOption("s", "shared", true,
            "The Freeling shared folder. If the `$FREELINGSHARE` nor " +
            "`freeling.shared` is present this parameter is required");
        options.addOption("c","config",true,
            "The Freeling config folder (default: `{freeling-shared}/config`");
        options.addOption("l","native-lib",true,
            "The native library file (defaults: `" + Freeling.DEFAULT_FREELING_LIB_PATH
            + "` with fallback `{freeling-shared}/" + Freeling.DEFAULT_FREELING_LIB_PATH+"`)");
        options.addOption("w","max-wait-time",true,
            "The maximum time in ms to wait for a Freeling Resource to become available"
            + "(default: "+Constants.DEFAULT_RESOURCE_WAIT_TIME+").");
        options.addOption("m","max-pool-size",true,
            "The maximum number of Analyzers created for a supported language. "
            + "This defines how manny texts of a single language can be processed "
            + "concurrently (default: "+DEFAULT_MAX_POOL_SIZE+")");
        options.addOption("q","min-queue-size",true,
            "If the pool of available Analyzers for a language becomes less that "
            + "the configured value a new Analyzer is created. The initial size "
            + "of the Analyzers pools is `{min-queue-size}+1` (default : " 
            + DEFAULT_MIN_QUEUE_SIZE+")");
        options.addOption("i","init-threads",true,
            "The size of the thread-pool used to initialize Freeling Analyzers. "
            + "Increasing this number allows to faster create additional Analyzers. "
            + "Note that concurrent creating of Analyzers may cause JVM crashes "
            + "on some systems (default : "+DEFAULT_INIT_THREADS+")");
    }
    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        CommandLineParser parser = new PosixParser();
        CommandLine line = parser.parse(options, args);
        args = line.getArgs();
        if(line.hasOption('h')){
            printHelp();
            System.exit(0);
        }
        //Parse the Freeling parameter and init the Freeling instance
        String sharedFolder = System.getenv(ENV_FREELING_SHARED_FOLDER);
        sharedFolder = System.getProperty(PROPERTY_FREELING_SHARED_FOLDER, sharedFolder);
        sharedFolder = line.getOptionValue('s', sharedFolder);
        if(sharedFolder == null){
            System.err.println("The Freeling shared Folder MUST BE set! \n");
            printHelp();
            System.exit(0);
        }
        File shared = new File(sharedFolder);
        if(!shared.isDirectory()){
            System.err.println("The configured Freeling shared folder '"
                + sharedFolder + "' is not a directory!\n");
            System.exit(1);
        }
        String configFolder = FilenameUtils.concat(sharedFolder, "config");
        configFolder = System.getProperty(PROPERTY_FREELING_CONFIG_FOLDER, configFolder);
        configFolder = line.getOptionValue('c', configFolder);
        File config = new File(configFolder);
        if(!config.isDirectory()){
            System.err.println("The configured Freeling config folder '"
                + configFolder + "' is not a directory!\n");
            System.exit(1);
        }
        String nativeLib = FilenameUtils.concat(sharedFolder, Freeling.DEFAULT_FREELING_LIB_PATH);
        if(new File(Freeling.DEFAULT_FREELING_LIB_PATH).isFile()){
            nativeLib = Freeling.DEFAULT_FREELING_LIB_PATH;
        }
        nativeLib = line.getOptionValue('l', nativeLib);
        if(!new File(nativeLib).isFile()){
            System.err.println("The configured Freeling native lib '"
                    + nativeLib + "' is not a file!\n");
            System.exit(1);
        }
        Freeling freeling = new Freeling(
            config.getPath(), Freeling.DEFAULT_CONFIGURATION_FILENAME_SUFFIX, 
            shared.getPath(), nativeLib, Freeling.DEFAULT_FREELING_LOCALE, 
            getInt(line, 'i', DEFAULT_INIT_THREADS), 
            getInt(line, 'm', DEFAULT_MAX_POOL_SIZE), 
            getInt(line, 'q', DEFAULT_MIN_QUEUE_SIZE));
        
        
        //init the Jetty Server
        Server server = new Server();
        Connector con = new SelectChannelConnector();
        //we need the port
        con.setPort(getInt(line,'p',DEFAULT_PORT));
        server.addConnector(con);

        //init the Servlet and the ServletContext
        Context context = new Context(server, "/", Context.SESSIONS);
        ServletHolder holder = new ServletHolder(RestServlet.class);
        holder.setInitParameter("javax.ws.rs.Application", FreelingApplication.class.getName());
        context.addServlet(holder, "/*");
        
        //now initialise the servlet context
        context.setAttribute(Constants.SERVLET_ATTRIBUTE_CONTENT_ITEM_FACTORY, 
            lookupService(ContentItemFactory.class));
        context.setAttribute(Constants.SERVLET_ATTRIBUTE_FREELING, freeling);
        context.setAttribute(Constants.SERVLET_ATTRIBUTE_MAX_RESOURCE_WAIT_TIEM, 
            getLong(line,'w',Constants.DEFAULT_RESOURCE_WAIT_TIME));
        //Freeling
        
        server.start();
        try {
            server.join();
        }catch (InterruptedException e) {
        }
        System.err.println("Shutting down Freeling");
        freeling.close();
    }
    
    private static int getInt(CommandLine line, char option, int defaultValue){
        String value = line.getOptionValue(option);
        if(value != null){
            return Integer.parseInt(value);
        } else {
            return defaultValue;
        }
    }
    private static long getLong(CommandLine line, char option, long defaultValue){
        String value = line.getOptionValue(option);
        if(value != null){
            return Long.parseLong(value);
        } else {
            return defaultValue;
        }
    }
    
    private static <T> T lookupService(Class<T> clazz){
        ServiceLoader<T> loader = ServiceLoader.load(clazz);
        Iterator<T> services = loader.iterator();
        if(services.hasNext()){
            return services.next();
        } else {
            throw new IllegalStateException("Unable to find implemetnation for service "+clazz);
        }
    }
    
    /**
     * 
     */
    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(
            "java -Xmx{size} -jar io.insideout.stanbol.enhancer.nlp.freeling.server-*" +
            "-jar-with-dependencies.jar [options]",
            "Indexing Commandline Utility: \n"+
            "  size:  Heap requirements depend on the dataset and the configuration.\n"+
            "         1024m should be a reasonable default.\n",
            options,
            null);
    }

}
