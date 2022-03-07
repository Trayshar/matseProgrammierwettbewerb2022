# Programmierwettbewerb 2022

## Allgemeines zu den Paketen
- `abstractions` enthält primär Konstanten und Interfaces zum nahtlosen Wechsel von Implementationen.
- `implementation` enthält die zugehörigen Implementationen; fast alles in diesem Paket implementiert etwas "Abstraktes"
- `tooling` enthält meine eigene Benchmark-Applikation. Ansonsten ist das irgendwelcher Code, der beim Nachdenken und Ausprobieren angefallen ist (und den ich nur zum synchronisieren committet habe, weil ich auf mehr als einem Gerät hier dran gearbeitet habe)
- `mdw2021` ist euer Code, unberührt.

## Solver
`TreeSolver` ist der Beste von den Dreien, aber da kein Puzzle was nicht (x,y,z) > 1 hat länger als 0.05 Sekunden dauert hab ich es unterlassen, für kleine Puzzles den neuen Solver einzuspannen.

## Aufgabenstellung
Die Aufgabenstellung in Textform befindet sich [hier](mdw2022.pdf).

## Kontinuierliche Einreichung der Lösungen
Um eine Lösung für den Wettbewerb einzureichen wird einfach das Interface IPuzzle implementiert.
Eine Dummy-Version der zu implementierenden Klasse findet sich an der folgenden Stelle im Repository:

- md2021-gruppe<1,2,...>
  - src
    - implementation
      - Puzzle.java

Es wird empfohlen für den automatisierten Ablauf am gesamten restlichen Repository und der vorgegebenen 
Programmstruktur keine Änderungen vorzunehmen.

Nach dem Pushen der Änderungen erfolgt im Projekt zunächst ein lokaler Test, der das Bauen und Aufrufen 
des Programms beinhaltet. Im Anschluss wird nach einem erfolgreichen Test die Programmversion zentral 
getestet und das Ergebnis ist auf der [Mathe-dual-Webseite](https://mathe-dual.de/index.php/wettbewerb-link/zwischenstand)
 einzusehen.

Treten Probleme auf, ist es sinnvoll die Fehlermeldungen im CI/CD-Bereich des Projektes einzusehen und 
sich, falls das nicht hilft, anschließend an [programmierwettbewerb@mathe-dual.de](mailto:programmierwettbewerb@mathe-dual.de) 
zu wenden.
