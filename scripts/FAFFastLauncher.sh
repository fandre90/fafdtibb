#!/bin/bash

#to run me, you first need to export .jar from Eclipse and put them in $JAR_DIR

HEAD="java -jar "
#répertoires
JAR_DIR="../jar/"
NAMES_DIR="../names/"
DATA_DIR="../data/"
XML_DIR="../"
OUTPUT_DIR="../output/"
#FAF jar
BUILD=$JAR_DIR"fafbuild1.0.jar"
QUERY=$JAR_DIR"fafquery1.0.jar"
UTILS=$JAR_DIR"fafutils1.0.jar"
#usage
BUILD_USAGE="--build <filename of .names> <filename of .data>"
QUERY_USAGE="--query <filename of .xml> <\"question\">"
UTILSPNG_USAGE="--utilspng <filename of .png>"
UTILSDOT_USAGE="--utilsdot <filename of .dot>"

#creation des répertoires de sortie s'ils n'existent pas encore
if [ ! -d $OUTPUT_DIR ] ; then
mkdir $OUTPUT_DIR
echo $OUTPUT_DIR" created"
fi

if [ ! -d $XML_DIR ] ; then
mkdir $XML_DIR
echo $XML_DIR" created"
fi

ARGC=$#
#vérification qu'on a au moins une option (pour le mode)
if [ $ARGC -eq 0 ] ; then
echo "[usage] :$BUILD_USAGE
	|$QUERY_USAGE
	|$UTILSPNG_USAGE
	|$UTILSDOT_USAGE"
exit -1
fi

#build mode
if [ $1 == "--build" ] ; then

if [ $ARGC -ne 3 ] ; then
echo "[usage] :$BUILD_USAGE"
exit -1
fi

echo "##BUILD MODE"
$HEAD$BUILD -n $NAMES_DIR$2 -d $DATA_DIR$3 -o $XML_DIR$3
exit 0

fi

#query mode
if [ $1 == "--query" ] ; then

if [ $ARGC -ne 3 ] ; then
echo "[usage] :$QUERY_USAGE"
exit -1
fi

echo "##QUERY MODE"
$HEAD$QUERY -i $XML_DIR$2 -q $3
exit 0

fi

#utils png mode
if [ $1 == "--utilspng" ] ; then

if [ $ARGC -ne 2 ] ; then
echo "[usage] :$UTILSPNG_USAGE"
exit -1
fi

echo "##UTILS PNG MODE"
$HEAD$UTILS -p -i $XML_DIR$2 -o $OUTPUT_DIR$2 -D
exit 0

fi

#utils dot mode
if [ $1 == "--utilsdot" ] ; then

if [ $ARGC -ne 2 ] ; then
echo "[usage] :$UTILSDOT_USAGE"
exit -1
fi

echo "##UTILS DOT MODE"
$HEAD$UTILS -d -i $XML_DIR$2 -o $OUTPUT_DIR$2
exit 0

fi

