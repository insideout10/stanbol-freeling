Freeling Core
=============

This provides the integration of [Freeling](http://nlp.lsi.upc.edu/freeling/) with the [Stanbol NLP processing](http://stanbol.apache.org/docs/trunk/components/enhancer/nlp/) module.

In addition it cares about the initialization based on the Freeling shared directory and provides ResourcePools that manage Freeling Analyzers. The later is important to concurrently process texts as Freeling Analyzers are not thread save.