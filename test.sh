FAIL=""

for i in `ls input_files`; do
	echo "---"
	timeout 5m java -jar md2021.jar -i input_files/$i -r result_files/$i.out
	RESULT=`./check.sh  input_files/$i result_files/$i.out`
	echo "$RESULT"
	TEST=`echo $RESULT | grep "Test fehlgeschlagen"`
	if test -n "$TEST" ; then
		FAIL="1"
	fi
done

if test -n "$FAIL" ; then
	echo "Bei der Gesamtprüfung trat mindestens 1 Fehler auf"
	exit 1
fi
