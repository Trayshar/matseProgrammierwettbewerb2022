if test -z $2 ; then
	echo "usage: $0 <inputfile> <outputfile>"
	exit
fi

IN="$1"
OUT="$2"

echo "Teste ob die Loesung in $OUT korrekt ist"
RESULT=`java -jar library/RaetselTester.jar $OUT | grep Fehler | grep -v "Keine Fehler gefunden."`
echo "$RESULT"
if test -z "$RESULT" ; then
	echo "Teste ob die Wuerfel in $IN und $OUT uebereinstimmen"
	java -jar library/validator.jar $IN $OUT
else
	echo "Test fehlgeschlagen" 
fi
