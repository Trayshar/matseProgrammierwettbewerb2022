javac -cp library/commons-cli-1.5.0.jar mdw2021/CompetitionMainClass.java mdw2021/InputFileSyntaxChecker.java mdw2021/ResultFileSyntaxChecker.java mdw2021/SemanticsChecker.java
jar cfme md2021.jar manifest.txt mdw2021.CompetitionMainClass mdw2021/*.class
