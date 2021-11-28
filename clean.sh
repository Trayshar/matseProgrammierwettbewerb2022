rm -f md2021.jar

## unfortunately find is not supported by ci image
# CLASS=`find . -name "*.class"`
## so we have to missuse jar as a workaround
jar cf temp.jar .
CLASS=`jar tf temp.jar | grep "\.class"`
rm temp.jar
## end workaround

if test -n "$CLASS"; then
	rm $CLASS
fi
