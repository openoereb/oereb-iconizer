[![Build Status](https://travis-ci.org/openoereb/oereb-iconizer.svg?branch=master)](https://travis-ci.org/openoereb/oereb-iconizer)
# oereb-iconizer
Creates icons for OEREB-Rahmenmodell and saves them in a database table as bytea.

Programmbibliothek für das Herstellen von Symbolen für das ÖREB-Rahmenmodell. Die Symbole können in der Datenbank in einer Tabelle gespeichert werden resp. leere Attribute upgedated werden.

Inhalt der Dokumentation:

1. Beschreibung
2. Betriebsdokumentation
3. Entwicklerdokumentation
4. TODO

## Beschreibung
Im Teilmodell _Transferstruktur_ (`OeREBKRMtrsfr_V1_1`) des ÖREB-Rahmenmodells werden in der STRUCTURE `LegendeEintrag` das Symbol (`Symbol`) eines Legendeneintrages mit dem dazugehörigen Artcodes (`ArtCode`) gespeichert. Diese Symbole können für die Produktion des XML- und PDF-Auszuges verwendent werden.

Die Herstellung der einzelnen Symbole scheint bei der Umsetzung des ÖREB-Katasters eine der mühsameren Arbeiten zu sein, da z.B. die WMS-Spezifikation diesen Usecase nicht kennt und nur vollständige Legende zurückliefert. Für _MapServer_, _GeoServer_ und _QGIS-Server_ stehen vendor-spezifische Parameter zur Verfügung, die das Anfordern eines einzelen Symbols ermöglicht. Diese erweiterte Funktionalität macht sie _oereb-iconizer_ zu Nutze. 

Das Herstellen der einzelnen Symbole und das Speichern in der Datenbank ist in zwei Schritte/Methoden unterteilt, so besteht die Möglichkeit für das Herstellen der Symbole durch das Implementieren eines Interfaces andere WMS-Server zu unterstützen. Zum jetzigen Zeitpunkt gibt es eine Implementierung für QGIS-3.4.

### Einschränkungen
Es dürfen nicht mehr Symbole in der Datenbank nachgeführt werden als wirklich bereits Records vorhanden sind.

Artcode-Wert muss eindeutig sein.

Die Möglichkeit von gemeindeweisen Legenden wurde konzeptionell nicht berücksichtigt.

## Betriebsdokumentation
Bei jedem Git-Push wird mittels Travis ein Jar-Datei neu gebildet und als `oereb-iconizer-1.0.(Versionsnummer).jar` auf Bintray/jcenter bereitgestellt.

## Entwicklerdokumentation
Das Herstellen der Symbole und das Speichern dieser in der Datenbank ist in zwei Methoden aufgeteilt. Die erste Methode implementiert das Interface `SymbolTypeCodeBuilder`. Der Rückgabewert muss ein Map (Artcode <-> Symbol) sein. Wie man diese Map herstellt ist völlig offen. Im konkreten Fall für QGIS3 resp. für das Amt für Geoinformation Kanton Solothurn wird zuerst ein WMS-`GetStyles`-Request gemacht, um herauszufinden welche Symbole überhaupt vorhanden sind. Aufgrund dieses Dokumentes muss es auch möglich sein die Beziehung zwischen Artcode und Symbol herstellen zu können. Es muss aber nicht zwingend eine `PropertyIsEqualTo`-Rule sein (wie in der QGIS3-Implementierung). Sondern kann "irgendwie" sein, solange die Maschine weiss, wie sie jetzt die Beziehung zwischen Symbole und Artcode herstellen kann.

Die zweite Methode speichert das Symbol in der Datenbank mittels UPDATE-Query.

## TODO
- more tests...
