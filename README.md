Stopcock
========

Introduction
------------
Stopcock is designed to be a hand over point between production network equipment and research/experiment OpenFlow
controllers. It aims to be past and future protocol compatible with graceful handling of unrecognised versions.

Design Brief
------------
* Acts as a TCP proxy for OpenFlow connections.
* Connections to proxy must not be acknowledged until onward connection is completed (end of TCP 3-way).
* Monitor response times to OpenFlow transactions between Switch and Controller.
* Monitor availability of Switch and Controller (using echo requests), disconnecting both parties in case of unresponsive behaviour.
* Conversion of transaction IDs to permit proxy to send messages.
* Logging of queries and table modifications.