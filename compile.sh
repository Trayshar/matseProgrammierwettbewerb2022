cd src

## unfortunately find is not supported by ci image
# SRC=`find . -name "*.java"`
## so we have to missuse jar as a workaround
jar cf temp.jar .
SRC=`jar tf temp.jar | grep "\.java"`
rm temp.jar
## end workaround

javac -cp ../library/commons-cli-1.5.0.jar $SRC

## unfortunately find is not supported by ci image
# CLASS=`find . -name "*.class"`
## so we have to missuse jar as a workaround
jar cf temp.jar .
CLASS=`jar tf temp.jar | grep "\.class"`
rm temp.jar
## end workaround

jar cfme ../md2021.jar ../manifest.txt mdw2021.CompetitionMainClass $CLASS

cd ..
