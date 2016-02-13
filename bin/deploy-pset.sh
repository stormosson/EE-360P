#!/usr/bin/env bash
# Written by Eric Crosson
# 2016-02-02

# $1 - pset (in english) to deploy -- will be name of package to deploy
target_pset=$1; shift

source=../assignments/src/pset/${target_pset}
tmpsource=/tmp/ee360p-deploy
rm -rf ${tmpsource}
mkdir -p ${tmpsource}
for i in ${source}/**/*.java ${source}/*.java; do
    # Ignore files decompiled from the course administrators
    [[ ! -e $i ]] && continue
    [[ $(grep "\* Decompiled" ${i}) ]] && continue
    sed '/^package*/d' ${i} > ${tmpsource}/$(basename ${i})
done


destination=$(dirname $(readlink -e $0))/../deploy/${target_pset}
zipfile=${destination}/esc625_wsm443.zip
mkdir -p ${destination}
rm ${zipfile}

cd ${tmpsource}
zip ${zipfile} *.java

rm -rf ${tmpsource}
