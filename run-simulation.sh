#!/bin/bash

# This was a script to run simulations on a cluster so it might be overcomplicated for the simple case.

function build() {
    mvn -Dmaven.test.skip=true install dependency:copy-dependencies
}


function parameters() {
# alpha betaRandom      betaTraced gamma netSize    avgNeighbor    avgAdd    avgRemove nNets nInits
echo .1 .00     0 .5 1000      10     0     0 50 20
for bT in 0 .1 $(seq 0.25 .25 2.5); do
for add in 0 10 ; do #$(seq 0 10); do
for rem in $add ; do #$(seq 0 10); do
echo .1 .02   $bT .5 1000      10  $add  $rem 50 20
done;done;done
}


function waitEndOfAllMyJobs() {
    # will just crash and do nothing if qstat does not exist
    while test 0 -ne $(qstat | wc -l)
    do
        printf "."
        sleep 2
    done
    echo " done"
}




function processparameters() {
    while read i; do
        NAME=$(echo $i | tr ' ' '-')
        OUT=running/r-$NAME
        ( echo "parameters: $i" && java -Xmx500m -cp target/NetworkPropagation-1.0-SNAPSHOT.jar:target/dependency/\* com.heeere.networkpropagation.AppTwoNetworks $i ) > "$OUT"
    done
}
#SETSHELL grid
#function processparameters() {
#    while read i; do
#        NAME=$(echo $i | tr ' ' '-')
#        OUT=/idiap/temp/remonet/socinfo/r-$NAME
#        quicksub1d ---open echo "parameters: $i" --- java -Xmx500m -cp NetworkPropagation/target/NetworkPropagation-1.0-SNAPSHOT.jar:NetworkPropagation/target/dependency/\* com.heeere.networkpropagation.AppTwoNetworks $i ---close ---into "$OUT"
#    done
#}


mkdir -p running/
parameters  | processparameters
waitEndOfAllMyJobs


