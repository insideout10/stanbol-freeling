Freeling Server
=============

This provides a embedded Jetty running the RESTful services of the [Freeling](http://nlp.lsi.upc.edu/freeling/) [Stanbol NLP processing](http://stanbol.apache.org/docs/trunk/components/enhancer/nlp/) integration .

Building the Server
-------------------

First you need to assembly the runable Jar

    mvm clean install
    mvn assembly:single
    
to build the jar with all the dependencies.

If the build succeeds go to the /target directory and copy the

    io.insideout.stanbol.enhancer.nlp.freeling.server-*-jar-with-dependencies.jar

to the directory you would like to run the server.

Running the Server
------------------

__NOTE__: To use this server you MUST HAVE [Freeling](http://nlp.lsi.upc.edu/freeling/) 3.0 installed on your local System. The README of the parent provides information on how to correctly install and configure Freeling.

The Server uses the following environment parameters

* `$FREELINGSHARE`: If present used as default for {freeling-shared} - the Freeling shared folder 

The Server uses the following Java system properties

* `freeling.shared`: The directory of {freeling-shared} - the Freeling shared folder. Will overwrite `$FREELINGSHARE` if present
* `freeling.config`: The directory of {freeling-config}. Will override the default `{freeling-shared}/config` if present.

The server supports the following command line parameters

* `-h --help` : Prints an help screen similar to this documentation
* `-p --port {port}`: the port (default 8080)
* `-s --shared {freeling-shared}`: The Freeling shared folder. If the `$FREELINGSHARE` nor `freeling.shared` is present this parameter is required
* `-c --config {freeling-config}`: The Freeling config folder (default: `{freeling-shared}/config`
* `-l --native-lib {freeling-native-lib}` : The Freeling native library loaded to the JVM (default: `./lib/{lib-name}` and `{freeling-shared}/lib/{lib-name}` as fallback, `{lib-name}` depends on the OS and is `libfreeling_javaAPI.so` on linux and `libfreeling_javaAPI.jnilib` on MAC
* `-w --max-wait-time {max-wait-time}`: The maximum time in ms to wait for a Freeling Resource to become available (default `30*1000`ms)
* `-m --max-pool-size {max-pool-size}` : The maximum number of Analyzers created for a supported language. This defines how manny texts of a single language can be processed concurrently (default: 10).
* `-q --min-queue-size {min-queue-size}`: If the pool of available Analyzers for a language becomes less that the configured value a new Analyzer is created. The initial size of the Analyzers pools is `{min-queue-size}+1` (default : 1)
* `-i --init-threads {init-threads}`: The size of the thread-pool used to initialize Freeling Analyzers. Increasing this number allows to faster create additional Analyzers. Note that concurrent creating of Analyzers may cause JVM crashes on some systems (default : 1)

