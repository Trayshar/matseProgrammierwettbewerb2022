cd src
javac -cp ../library/commons-cli-1.5.0.jar mdw2021/CompetitionMainClass.java 
jar cfme ../md2021.jar ../manifest.txt mdw2021.CompetitionMainClass mdw2021/*.class
cd ..
