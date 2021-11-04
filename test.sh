echo "dies ist ein lokaler test, der einen Trigger ausloesen soll (tach, test)"
echo "Verzeichnis: "`pwd`
echo "Zeitstempel: "`date`

java -Djava.ext.dirs=library mdw2021/CompetitionMainClass --t1
java -Djava.ext.dirs=library mdw2021/CompetitionMainClass --t2 "Test Text zum Argument testen"
java -Djava.ext.dirs=library mdw2021/CompetitionMainClass --t3 
