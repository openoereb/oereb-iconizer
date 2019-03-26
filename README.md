[![Build Status](https://travis-ci.org/openoereb/oereb-iconizer.svg?branch=master)](https://travis-ci.org/openoereb/oereb-iconizer)
# oereb-iconizer
Creates icons for OEREB-Rahmenmodell and saves them in a database table as bytea.

Programmbibliothek für das Herstellen von Symbolen für das ÖREB-Rahmenmodell. Die Symbole können in der Datenbank in einer Tabelle gespeichert werden resp. leere Attribute upgedated werden.

Inhalt der Dokumentation:

1. Beschreibung
2. Betriebsdokumentation
3. Entwicklerdokumentation
4. TODO
5. Hinweise

## Beschreibung
Im Teilmodell _Transferstruktur_ (`OeREBKRMtrsfr_V1_1`) des ÖREB-Rahmenmodells werden in der STRUCTURE `LegendeEintrag` das Symbol (`Symbol`) eines Legendeneintrages mit dem dazugehörigen Artcodes (`ArtCode`) gespeichert. Diese Symbole können für die Produktion des XML- und PDF-Auszuges verwendent werden.

Die Herstellung der einzelnen Symbole scheint bei der Umsetzung des ÖREB-Katasters eine der mühsameren Arbeiten zu sein, da z.B. die WMS-Spezifikation diesen Usecase nicht kennt und nur vollständige Legende zurückliefert. Für _MapServer_, _GeoServer_ und _QGIS-Server_ stehen vendor-spezifische Parameter zur Verfügung, die das Anfordern eines einzelen Symbols ermöglicht. Diese erweiterte Funktionalität macht sie _oereb-iconizer_ zu Nutze. 

Das Herstellen der einzelnen Symbole und das Speichern in der Datenbank ist in zwei Schritte/Methoden unterteilt, so besteht die Möglichkeit für das Herstellen der Symbole durch das Implementieren eines Interfaces andere WMS-Server zu unterstützen. Zum jetzigen Zeitpunkt gibt es eine Implementierung für QGIS-3.4.

## Betriebsdokumentation
Bei jedem Git-Push wird mittels Travis ein Jar-Datei neu gebildet und als `oereb-iconizer-1.0.(Versionsnummer).jar` auf Bintray/jcenter bereitgestellt.

## Entwicklerdokumentation
Fubar... CONTINUE HERE....

## TODO

## Hinweise
### Qgis-3.4-Implementierung (Amt für Geoinformation Kanton Solothurn)
...


## OLD DOCUMENTATION

## Developing
testcontainers / Docker images!!

## QGIS-Server

### Docker
Start WMS server:
```
docker run --rm -v $PWD/src/test/resources/qgis3:/data -p 8380:80 sogis/qgis-server-base:3.4
```

GetStyles:
```
http://localhost:8380/qgis/npl?&SERVICE=WMS&REQUEST=GetStyles&LAYERS=npl&SLD_VERSION=1.1.0
```

GetLegendGraphic:
```
http://localhost:8380/qgis/npl?&SERVICE=WMS&VERSION=1.3.0&REQUEST=GetLegendGraphic&LAYER=npl&FORMAT=image/png&STYLE=default&SLD_VERSION=1.1.0&RULELABEL=false&LAYERTITLE=false
```

### Vagrant
```
http://192.168.50.8/cgi-bin/qgis_mapserv.fcgi?map=/vagrant/data/npl.qgs&SERVICE=WMS&REQUEST=GetCapabilities&VERSION=1.3.0
```



## Notizen
Im Rahmenmodell (Transferstruktur) muss das passende Symbol dem Attribut `artcode` zugewiesen werden. Das ist automatisch nur möglich wenn man Abmachungen trifft, d.h. aus dem maschinenlesbaren Konfigurationsfile (in unserem Fall das SLD via GetStyles) muss die Maschine lesen können welches Symbol welchem `artcode`(-Wert) zugewiesen werden kann. In einer ersten Version gehe ich davon aus, dass im Filter `ogc:PropertyIsEqualTo` der Artcode steht. Weil aber vielleicht nicht immer mit  `ogc:PropertyIsEqualTo` gearbeitet wird, wäre z.B. auch das `se:Description`- oder `se:Abstract`-Feld möglich. Jedenfalls sollte (oder muss) es katasterinfrastrukturweit identisch sein.

Es darf pro Rahmenmodell-Artcode nur eine Rule geben, d.h. zum Beispiel keine massstabsabhängigen Darstellungen. Sind mehrere Rules für den gleichen Artcode vorhanden, wird der erste wieder überschrieben.



--> Gehört wohl ins Betriebshandbuch.

Es braucht eine Mapping-Behälter-Zwischenschicht: Artcode <-> Symbol. Dieser Behälter wird dann abgearbeitet, d.h. es werden die INSERT-Statements daraus generiert.

Für das Abfüllen des Behälters kann es verschiedene Implmentierungen eines Interfaces geben, z.B. QGIS, MapServer, GeoServer (falls SLD zu verschieden ist) oder auch komplett was Anderes.

