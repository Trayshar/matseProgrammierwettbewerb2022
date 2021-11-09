echo "dies ist ein lokaler test, der einen Trigger ausloesen soll (tach, test)"
echo "Verzeichnis: "`pwd`
echo "Zeitstempel: "`date`

java -jar md2021.jar --t1
java -jar md2021.jar --t2 "Test Text zum Argument testen"
java -jar md2021.jar --t3
