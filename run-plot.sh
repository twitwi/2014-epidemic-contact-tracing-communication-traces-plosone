#!/bin/bash

# this is an example of code to plot the graphs (among so many)... we would probably do it differently today (python and numpy?)

export LANG=US

function differentiate() {
    awk 'BEGIN {prev=0} {print $1-prev; prev = $1;}'
}
function transpose {
    awk '{for (f = 1; f <= NF; f++) a[NR, f] = $f } NF > nf { nf = NF } END {for (f = 1; f <= nf; f++) for (r = 1; r <= NR; r++) printf "%f%s", a[r, f], (r==NR ? RS : FS) }'
}


function plotAllFiles() {
    setup="$1"
    shift
    plots=
    sep=plot
    for i in "$@" ; do plots="${plots} ${sep} '$i' ${setup}" ; sep="," ; done
    echo "set term svg enhanced; set output ',,.svg'; ${plots} ;" | gnuplot
    echo inkview ,,.svg
    inkview ,,.svg
}



function plotAllFilesWithLabel() {
    configuration="$1"
    shift
    setup="$1"
    shift
    sedit="$1"
    shift
    plots=
    sep=plot
    for i in "$@" ; do plots="${plots} ${sep} '$i' ${setup} ti '"$(echo $i | sed "${sedit}")"' " ; sep="," ; done
    echo "set term svg size 700,500 enhanced fname 'FreeSerif' ; set output ',,.svg' ; ${configuration} ; ${plots} ;" | gnuplot
    echo inkview ,,.svg
    #inkview ,,.svg
}


# ONE EXAMPLE

#### generating 1 kind of report
for i in ./r-* ; do
    p=$(cat $i | grep parameters | sed 's@parameters: @@g')
    max=$(cat $i | grep average-infected | transpose | sed 1,3d | sort -g|tail -1)
    if [ "$WIP" != "" -a "$max" = "" ] ; then
        # nicely skip still-computing files
        continue
    fi
    tmax=$(cat $i | grep average-infected | awk '{for (i=4; i<NF; i++) if ($i == '${max}') print ((i-4) * $2 / ($3-1))}' | head -1)
    echo $p $max $tmax
done > ,,report

#### generating plots of avg number of infected people over time
# 1 graph per overlap (with equal stuff)
# 1 curve per beta t
for gamma in .5 ; do
for Z in $(cat ,,report | awk '$7==$8 {print $7}'|sort -g|uniq) ; do


overlap=$(awk 'BEGIN{z='$Z'/10 ; print 100*(1-z)/(1+z) }')
allordered=
for bt in $(cat ,,report | awk '{print $3}'|sort -g|uniq) ; do
    o=report4-$bt-$gamma-$overlap
    cat r-.1-.02-$bt-$gamma-1000-10-$Z-$Z-50-20 | grep average-infected | awk '{for (i=4; i<NF; i++) print $i " " ((i-4) * $2 / ($3-1))}' > $o
    allordered="${allordered} $o"
done
plotAllFilesWithLabel 'set key top right ; set xrange [0:300] ; set yrange [1:1000] ; set ylabel "average infected" font "FreeSerif,16" ; set xlabel "time" font "FreeSerif,16" ;' 'u 2:1 w lines' 's@report4-\(.*\)-\(.*\)-\([^.]*\).*@βt=\1   γ=\2   overlap=\3%@g' $allordered
obase=avg-infected-against-time-$gamma-Z-$Z-overlap-$overlap
cp ,,.svg $obase.svg
inkscape --export-pdf=$obase.pdf $obase.svg
plotAllFilesWithLabel 'set key top right ; set xrange [0:300] ; set yrange [1:1000] ; set ylabel "average infected" font "FreeSerif,16" ; set xlabel "time" font "FreeSerif,16" ; set logscale y ;' 'u 2:1 w lines' 's@report4-\(.*\)-\(.*\)-\([^.]*\).*@βt=\1   γ=\2   overlap=\3%@g' $allordered
obase=avg-infected-log-against-time-$gamma-Z-$Z-overlap-$overlap
cp ,,.svg $obase.svg
inkscape --export-pdf=$obase.pdf $obase.svg

done
done
