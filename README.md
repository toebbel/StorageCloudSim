StorageCloudSim
===============

This is an extension to to existing toolkit [CloudSim](http://code.google.com/p/cloudsim/). Features for modeling and simulation of Storage as a Service (STaaS) Clouds were added. The currently available documentation can be found in the source code ([java doc](http://downloads.tobiassturm.de/projects/storagecloudsim/doc/index.html)) and in the bachelor thesis, that will be linked in the near future [here](http://tobiassturm.de/projects/StorageCloudSim.html).

Status
------
More work to be done ... To be continued

Requirements
------------
* Java 7 Update 21
* CloudSim (tested with [version 3.0.3](http://code.google.com/p/cloudsim/downloads/list)) (patched)
* [Simple 2.7.1](http://simple.sourceforge.net/download.php)
* [Apache Commons CLI](http://commons.apache.org/proper/commons-cli/index.html)
* and [commons-math3-3.2](http://commons.apache.org/proper/commons-math/download_math.cgi)

Patched Cloudsim
----------------
Since the original Cloudsim 3.0.3 misses a method for retrieving bandwidth information from the network topology, we patched the original version and included it for convenience. The actual diff is inside the cloudsim source directory.
