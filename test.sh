LIB=library/commons-cli-1.5.0.jar
echo "dies ist ein lokaler test, der einen Trigger ausloesen soll (tach, test)"
echo "Verzeichnis: "`pwd`
echo "Zeitstempel: "`date`

java -cp $LIB:. mdw2021/CompetitionMainClass --t1 
java -cp $LIB:. mdw2021/CompetitionMainClass --t2 "Test Text zum Argument testen"
java -cp $LIB:. mdw2021/CompetitionMainClass --t3 
