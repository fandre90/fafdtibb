#!/bin/bash

#to run me, first you need to export .jar from Eclipse and put them in $JAR_DIR

HEAD="java -jar "
#directories
JAR_DIR="../jar/"
NAMES_DIR="../names/"
DATA_DIR="../data/"
XML_DIR="../"
OUTPUT_DIR="../output/"
#FAF jar
BUILD=$JAR_DIR"fafbuild1.0.jar"
QUERY=$JAR_DIR"fafquery1.0.jar"
UTILS=$JAR_DIR"fafutils1.0.jar"
#options
BUILD_OPT="--build"
QUERY_OPT="--query"
UTILSPNG_OPT="--utilspng"
UTILSDOT_OPT="--utilsdot"
#usage
BUILD_USAGE=$BUILD_OPT" <filename of .names> <filename of .data>"
QUERY_USAGE=$QUERY_OPT" <filename of .xml> <\"question\">"
UTILSPNG_USAGE=$UTILSPNG_OPT" <filename of .png>"
UTILSDOT_USAGE=$UTILSDOT_OPT" <filename of .dot>"

#output directories creation if they don't exist already
if [ ! -d $OUTPUT_DIR ] ; then
mkdir $OUTPUT_DIR
echo $OUTPUT_DIR" created"
fi

if [ ! -d $XML_DIR ] ; then
mkdir $XML_DIR
echo $XML_DIR" created"
fi

#display help function, then exit script
display_help(){
	echo "## FAFFastLauncher :: use it to customize your configurations and processing preferences ##
## for Fast And Furious Decision Tree Induction : Bagging Begins Command Line Interface ##
	"
	echo "[usage] :$BUILD_USAGE
	|$QUERY_USAGE
	|$UTILSPNG_USAGE
	|$UTILSDOT_USAGE"
	echo "Infos :
	- Always use <filename> without extension
	- In <\"question\">, features must be in order and seperated by ';'"
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
$HEAD$BUILD -n $NAMES_DIR$2 -d $DATA_DIR$3 -o $XML_DIR$3

#query mode
elif [ $1 == $QUERY_OPT ] ; then
check_nb_param 3
echo "##QUERY MODE"
$HEAD$QUERY -i $XML_DIR$2 -q $3

#utils png mode
elif [ $1 == $UTILSPNG_OPT ] ; then
check_nb_param 2
echo "##UTILS PNG MODE"
$HEAD$UTILS -p -i $XML_DIR$2 -o $OUTPUT_DIR$2 -D

#utils dot mode
elif [ $1 == $UTILSDOT_OPT ] ; then
check_nb_param 2
echo "##UTILS DOT MODE"
$HEAD$UTILS -d -i $XML_DIR$2 -o $OUTPUT_DIR$2

else
display_help

fi
exit 0
