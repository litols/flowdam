Stopcock
========

Introduction
------------
Stopcock is designed to be a hand over point between production network equipment and research/experiment OpenFlow
controllers. It aims to be past and future protocol compatible with graceful handling of unrecognised versions.

License
-------
Main Project:
  All rights reserved, unauthorised distribution of source code, compiled binary or usage is expressly prohibited.

Configuration System (uk.ac.lancs.stopcock.configuration):
  Under license to Lancaster University for academic and non commercial use in the Stopcock project from 
  Peter Wood <peter@alastria.net>, any distribution in source or binary form outside Lancaster University
  is expressly prohibited. Configuration system was written solely outside Lancaster University, all rights
  are reserved.

Design Brief
------------
* Acts as a TCP proxy for OpenFlow connections.
* Monitor availability of Switch and Controller (using echo requests), disconnecting both parties in case of unresponsive behaviour.
* Logging of queries and table modifications.