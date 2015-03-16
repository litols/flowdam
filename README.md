Stopcock
========

Introduction
------------
Stopcock is designed to be a hand over point between production network equipment and research/experiment OpenFlow
controllers. It aims to be past and future protocol compatible with graceful handling of unrecognised versions.

License
-------
Copyright 2014 University of Lancaster

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Configuration
-------------
See included config.yml.

Copy log4j2.xml into the same directory as config.yml for logging.

stopcock.sh is provided as a sample start up script for rc.d style operating systems.

Developers
----------
Peter Wood <p.wood@lancaster.ac.uk>

Design Brief
------------
* Acts as a TCP proxy for OpenFlow connections.
* Monitor availability of Switch and Controller (using echo requests), disconnecting both parties in case of unresponsive behaviour.
* Logging of queries and table modifications.