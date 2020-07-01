# 2014-epidemic-contact-tracing-communication-traces-plosone
Sharing the (old-ish) code for "Epidemic Contact Tracing via Communication Traces" by Katayoun Farrahi, Rémi Emonet and Manuel Cebrian


## Instructions
(these are written a few years later, so they might be imprecise)

First build the jar with (the maven build tool should be present on your machine).

~~~
mvn install
~~~

Make some simulations (the file is an example)... this is long

~~~
./run-simulation.sh
~~~

Plot some graphs

~~~
cd running/
../run-plot.sh
ls -1 *.pdf
~~~
