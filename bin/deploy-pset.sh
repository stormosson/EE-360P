#!/usr/bin/env bash
# Written by Eric Crosson
# 2016-02-02

# $1 - pset (in english) to deploy -- will be name of package to deploy
target_pset=$1; shift

source=../assignments/src/pset/${target_pset}
tmpsource=/tmp/ee360p-deploy
rm -rf ${tmpsource}
mkdir -p ${tmpsource}
for i in ${source}/*.java; do
    sed '/^package*/d' ${i} > ${tmpsource}/$(basename ${i})
done


destination=../deploy
zipfile=${destination}/${target_pset}.zip
mkdir -p ${destination}
rm ${zipfile}


zip ${zipfile} ${tmpsource}/*.java

rm -rf ${tmpsource}
