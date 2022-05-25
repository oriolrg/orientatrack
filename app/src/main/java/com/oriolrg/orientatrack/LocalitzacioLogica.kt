package com.oriolrg.orientatrack

import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
* Calculo la distància entre la posicio inicial i el punt de destí
* @ return distancia Double
* */
fun calcularDistanciaEntreCoordenades(inici: LocalitzacioData.Point,fi:LocalitzacioData.Point): Double {
    val radiTerra = 6378137//Radi de la tierra en km
    var dLat = Math.toRadians(fi.lat) - Math.toRadians(inici.lat)
    var dLong = Math.toRadians(fi.lon) - Math.toRadians(inici.lon)
    var a = sin(dLat / 2) * sin(dLat / 2) + cos(Math.toRadians(inici.lat)) * cos(
            Math.toRadians(fi.lat)) * sin(dLong / 2) * sin(dLong / 2)
    var c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return radiTerra * c//Retorna tres decimals
}

/**
 * Calcula la distancia total d'un track rebut com a Document
 * return total
 */
fun calcularDistanciaTrack(doc: Document): Double{
    val nList = doc.getElementsByTagName("trkpt")
    var total = 0.0

    for (i in 0 until nList.getLength()-1 step 1) {
        if (nList.item(0).getNodeType().equals(Node.ELEMENT_NODE)) {
            var posicioActual = LocalitzacioData.Point(
                nList.item(i).attributes.getNamedItem("lon").nodeValue.toDouble(),
                nList.item(i).attributes.getNamedItem("lat").nodeValue.toDouble()
            )
            var posicioFinal = LocalitzacioData.Point(
                nList.item(i + 1).attributes.getNamedItem("lon").nodeValue.toDouble(),
                nList.item(i + 1).attributes.getNamedItem("lat").nodeValue.toDouble()
            )
            total += calcularDistanciaEntreCoordenades(posicioActual,
                posicioFinal
            )
        }
    }

    return total
}

/**
* Calculo l'angle entre la posicio inicial i el punt de destí
* @ return brng Double
* */
fun calcularAngle(inici: LocalitzacioData.Point,fi:LocalitzacioData.Point):Double {
    var iniLat = Math.toRadians(inici.lat)
    var iniLon = Math.toRadians(inici.lon)
    var fiLat = Math.toRadians(fi.lat)
    var fiLon = Math.toRadians(fi.lon)
    val y = Math.sin(fiLon - iniLon) * Math.cos(fiLat)
    val x = Math.cos(iniLat) * Math.sin(fiLat) -
            Math.sin(iniLat) * Math.cos(fiLat) * Math.cos(fiLon - iniLon)
    var resultat = Math.toDegrees(Math.atan2(y, x))
    //converteixo a graus positius
    if (resultat<0.0){
        resultat = resultat + 360.0
    }
    println("Angle=" + (resultat * 180 / Math.PI + 360) % 360) // in degrees
    return resultat
}
    /*
    var startLatitude  = 1.585565
    var startLongitude = 42.1368733
    var stopLatitude   = 1.59097
    var stopLongitude  = 42.13764

    var y = sin(stopLongitude*Math.PI / 180 - startLongitude*Math.PI / 180) * cos(stopLatitude*Math.PI / 180)
    var x = cos(startLatitude*Math.PI / 180)* sin(stopLatitude*Math.PI / 180) -
            sin(startLatitude*Math.PI / 180)* cos(stopLongitude*Math.PI / 180)* cos((stopLongitude*Math.PI / 180)-startLongitude*Math.PI / 180)
    var brng = Math.toDegrees(Math.atan2(x, y));
    return brng*/

/**
* Calculo el numero de punts necessaris
* A partir d'aqui es miraran diferents condicions
* TODO Depenent dels km del track mes o menys punts
* */
fun getPunts(nList: NodeList?): Int? {
    return nList?.length?.div(10)
}

/**
* Calculo quan estic suficientment aprop del punt
* TODO crear funcio per calcular quan he arribat al punt i passa al següent punt de la llista
* */

/**
* Calculo la distància total del track
* TODO transportar la funcio del MainActivity separant la logica de obrir el fitxer
* */
