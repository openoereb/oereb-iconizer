# oereb-iconizer
Creates icons for OEREB-Rahmenmodell and saves them in a ili2db created database structure.

## QGIS-Server

Start WMS server:
```
docker run --rm -v $PWD/data:/data -p 80:80 sogis/qgis-server-base:3.4
```

GetStyles:
```
http://localhost/qgis/npl?&SERVICE=WMS&REQUEST=GetStyles&LAYERS=npl&SLD_VERSION=1.1.0
```

GetLegendGraphic:
```
http://localhost:8380/?&SERVICE=WMS&VERSION=1.3.0&REQUEST=GetLegendGraphic&LAYER=npl&FORMAT=image/png&STYLE=default&SLD_VERSION=1.1.0&RULELABEL=false&LAYERTITLE=false
```


## Notizen
Im Rahmenmodell (Transferstruktur) muss das passende Symbol dem Attribut `artcode` zugewiesen werden. Das ist automatisch nur möglich wenn man Abmachungen trifft, d.h. aus dem maschinenlesbaren Konfigurationsfile (in unserem Fall das SLD via GetStyles) muss die Maschine lesen können welches Symbol welchem `artcode`(-Wert) zugewiesen werden kann. In einer ersten Version gehe ich davon aus, dass im Filter `ogc:PropertyIsEqualTo` der Artcode steht. Weil aber vielleicht nicht immer mit  `ogc:PropertyIsEqualTo` gearbeitet wird, wäre z.B. auch das `se:Description`- oder `se:Abstract`-Feld möglich. Jedenfalls sollte (oder muss) es katasterinfrastrukturweit identisch sein.

Es darf pro Rahmenmodell-Artcode nur eine Rule geben, d.h. zum Beispiel keine massstabsabhängigen Darstellungen. Sind mehrere Rules für den gleichen Artcode vorhanden, wird der erste wieder überschrieben.



--> Gehört wohl ins Betriebshandbuch.

Es braucht eine Mapping-Behälter-Zwischenschicht: Artcode <-> Symbol. Dieser Behälter wird dann abgearbeitet, d.h. es werden die INSERT-Statements daraus generiert.

Für das Abfüllen des Behälters kann es verschiedene Implmentierungen eines Interfaces geben, z.B. QGIS, MapServer, GeoServer (falls SLD zu verschieden ist) oder auch komplett was Anderes.

