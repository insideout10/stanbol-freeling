Apache Stanbol Freeling integration
================

[Freeling](http://nlp.lsi.upc.edu/freeling/) is a [GPL](http://www.fsf.org/licenses/gpl.html) licensed language analysis tool suite. This project aims to provide a standalone server providing a RESTful API that can than be used by Apache Stanbol for NLP processing of texts.


## Install Freeling

_NOTE:_ This section is copied over from [here](https://github.com/insideout10/wordlift-stanbol/blob/master/README.md#install-freeling)

Reference:
http://nlp.lsi.upc.edu/freeling/index.php?option=com_content&task=view&id=25&Itemid=62

### Install on Ubuntu 12.04

Download **Freeling 3.X** from `http://devel.cpl.upc.edu/freeling/downloads?order=time&desc=1`:
```sh
curl -o freeling-3.0.tar.gz http://devel.cpl.upc.edu/freeling/downloads/21
```

Then extract the archive and follow the instructions at this address to compile and install Freeling:
`http://nlp.lsi.upc.edu/freeling/doc/userman/userman.pdf`

### Install on Mac OS X

Use brew, http://mxcl.github.com/homebrew/.

Install:
```sh
brew install icu4c
brew install boost --with-icu
brew install https://raw.github.com/mxcl/homebrew/0d8d92bfcd00f42d6af777ba8bf548cbd5502638/Library/Formula/swig.rb
brew install https://raw.github.com/gist/4060323/74e4e36dfe6dee43d604e70ce281157db7ecf668/freeling.rb
```

**Note**:

* there might be issues according on which version of **boost** gets installed and its location (open an issue, we'll try to help).

*Solution 1*

Sometimes installing 1.49 instead of 1.50+ works:
To do that, follow these steps:
 1. ensure boost is not installed: `brew uninstall boost`
 2. install from this formula: 

```sh
brew install https://github.com/manphiz/homebrew/blob/e40bc41d84e32902d73d8c3868843470a269a449/Library/Formula/boost.rb --with-icu
```

*Solution 2*

```sh
brew uninstall icu4c
git checkout c25fd2f `brew --prefix`/Library/Formula/icu4c.rb
brew install icu4c
brew install --with-icu boost
```

* the `install-sh` file might not have the required permissions (change with `chmod 755 install-sh`)

### Freeling Java APIs

#### Install Java APIs

At least JDK 6.0 is required. JDK 7.x has been tested and is also valid

Change to `freeling-3.0/APIs/java`.

Fix the `freeling-HEAD/APIs/java/Makefile` file by setting the correct parameters:
```
FREELINGDIR=/usr/local
SWIGDIR=/usr/share/swig2.0
JAVADIR=/usr/lib/jvm/jdk1.6.0_32

...

java -> $(JAVADIR)/bin/java
jar -> $(JAVADIR)/bin/jar
```

Then run
```sh
make
```

### Load the Java APIs in the local Maven cache (optional)

```sh
mvn install:install-file \
    -Dfile=freeling.jar \
    -DgroupId=edu.upc.freeling \
    -DartifactId=edu.upc.freeling \
    -Dversion=3.0 \
    -Dpackaging=jar
```

## Freeling Configuration

Create the following folders:
```sh
 /opt/freeling
  - etc
```

In the *etc* folder, create a *symbolic link* to `/usr/local/share/freeling` (or where the share/freeling folder is located, e.g. `/usr/local/Cellar/freeling/3.0/share/freeling` on a Mac OS X install using *brew*). In the following this folder is referenced by `$FREELINGSHARE`

Please note that this project also provides a default configuration for Freeling. Users that want to give it a try can replace the `$FREELINGSHARE/config`folder with the default configuration provided by [freeling-config/config](stanbol-freeling/tree/master/freeling-config/config).


License:
-------

All modules are provided under [GNU AFFERO GENERAL PUBLIC LICENSE](LICENSE), in order to comply with Freeling's [license requirements](https://github.com/insideout10/stanbol-freeling/issues/8).

Also take a look at Freeling's [copying](http://devel.cpl.upc.edu/freeling/svn/trunk/COPYING) and [license](http://devel.cpl.upc.edu/freeling/svn/trunk/LICENSES) information. 