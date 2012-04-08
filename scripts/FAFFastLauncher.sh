#!/bin/bash

#to run me, first you need to export .jar from Eclipse and put them in $JAR_DIR

HEAD="java -jar "
#directories
JAR_DIR="../jar/"
NAMES_DIR="../../FaF/res/examples/"
DATA_DIR="../../FaF/res/examples/"
XML_DIR="../xml/"
HTML_DIR="../html/"
OUTPUT_DIR="../output/"	#dot and png
#FAF jar
BUILD=$JAR_DIR"fafbuild1.0.jar"
QUERY=$JAR_DIR"fafquery1.0.jar"
UTILS=$JAR_DIR"fafutils1.0.jar"
#options
BUILD_OPT="--build"
QUERY_OPT="--query"
QUERYSTAT_OPT="--querystat"
UTILSPNG_OPT="--utilspng"
UTILSDOT_OPT="--utilsdot"
#usage
BUILD_USAGE=$BUILD_OPT" <filename of .names> <filename of .data>"
QUERY_USAGE=$QUERY_OPT" <filename of .xml> <\"question\">"
QUERYSTAT_USAGE=$QUERYSTAT_OPT" <filename of .xml> <filename of .data>"
UTILSPNG_USAGE=$UTILSPNG_OPT" <filename of .png> <tree number>"
UTILSDOT_USAGE=$UTILSDOT_OPT" <filename of .dot> <tree number>"

#output directories creation if they don't exist already
if [ ! -d $OUTPUT_DIR ] ; then
mkdir $OUTPUT_DIR
echo $OUTPUT_DIR" created"
fi

if [ ! -d $XML_DIR ] ; then
mkdir $XML_DIR
echo $XML_DIR" created"
fi

if [ ! -d $HTML_DIR ] ; then
mkdir $HTML_DIR
echo $HTML_DIR" created"
fi

#display help function, then exit script
display_help(){
	echo "## FAFFastLauncher :: use it to customize your configurations and processing preferences ##
## for Fast And Furious Decision Tree Induction : Bagging Begins Command Line Interface ##
	"
	echo "[usage] :$BUILD_USAGE
	|$QUERY_USAGE
	|$QUERYSTAT_USAGE
	|$UTILSPNG_USAGE
	|$UTILSDOT_USAGE"
	echo "Infos :
	- Always use <filename> without extension
	- In <\"question\">, features must be in order and seperated by ','"
	exit -1
}
#check number of parameters function
#if not equal, display help, so exit script
check_nb_param(){
	if [ $ARGC -ne $1 ] ; then
	display_help
	fi
}

ARGC=$#
#check there is at least 1 argument (for mode)
if [ $ARGC -eq 0 ] ; then
display_help
fi

#build mode
if [ $1 == $BUILD_OPT ] ; then
check_nb_param 3
echo "##BUILD MODE"
#clean log.log
echo "" > log.log
#get current date used for suffixed output
dat=$(date +%s)
$HEAD$BUILD -n $NAMES_DIR$2 -d $DATA_DIR$3 -o $XML_DIR$3$dat -b 4 -t 2 -c entropy -m 1 -M 10 -g 0.1 -w ../../workdir/$dat

#query mode
elif [ $1 == $QUERY_OPT ] ; then
check_nb_param 3
echo "##QUERY MODE"
$HEAD$QUERY -i $XML_DIR$2 -q $3

#query stats campaign mode
elif [ $1 == $QUERYSTAT_OPT ] ; then
check_nb_param 3
echo "##QUERY STATS CAMPAIGN MODE"
$HEAD$QUERY -i $XML_DIR$2 -o $HTML_DIR$2 < $DATA_DIR$3".test"

#utils png mode
elif [ $1 == $UTILSPNG_OPT ] ; then
check_nb_param 3
echo "##UTILS PNG MODE"
$HEAD$UTILS -p -i $XML_DIR$2 -o $OUTPUT_DIR$2$3 -D -I $3

#utils dot mode
elif [ $1 == $UTILSDOT_OPT ] ; then
check_nb_param 3
echo "##UTILS DOT MODE"
$HEAD$UTILS -d -i $XML_DIR$2 -o $OUTPUT_DIR$2$3 -I $3

else
display_help

fi
exit 0
