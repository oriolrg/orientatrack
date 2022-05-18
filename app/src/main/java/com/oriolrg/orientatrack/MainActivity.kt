package com.oriolrg.orientatrack

import android.os.Bundle
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.SAXException
import java.io.IOException
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

data class Point( var altura: String, var temps: String, var lat: String, var lon: String)
data class Track(var name: String, var punts: Point)
class MainActivity : AppCompatActivity() {
    var trkDataHashMap = HashMap< String, String>()
    var trkList: ArrayList< HashMap< String, String>> = ArrayList()
    var mylist : MutableList<Track>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try {
            val lv = findViewById<ListView>(R.id.listView)
            carregarTrack();
            //Mostra la llista de punts per pantalla
            /*val adapter = SimpleAdapter(
                this@MainActivity,
                trkList,
                R.layout.custom_list, arrayOf("nom", "altura", "temps", "coordenades"),
                intArrayOf(R.id.nom, R.id.altura, R.id.temps, R.id.coordenades))
            lv.setAdapter(adapter)*/
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ParserConfigurationException) {
            e.printStackTrace()
        } catch (e: SAXException) {
            e.printStackTrace()
        }
    }

    private fun calcularDistanciaTrack(): Double{
        val istream = assets.open("myGpx.gpx")
        val builderFactory = DocumentBuilderFactory.newInstance()
        val docBuilder = builderFactory.newDocumentBuilder()
        val doc = docBuilder.parse(istream)
        val nList = doc.getElementsByTagName("trkpt")
        val eTrack = doc.getElementsByTagName("metadata").item(0) as Element
        val lenPoints = getPunts(nList)
        var total: Double = 0.0

        for (i in 0 until nList.getLength()-1 step 1) {
            if (nList.item(0).getNodeType().equals(Node.ELEMENT_NODE)) {

                total += calcularDistanciaEntreCoordenades(
                    nList.item(i).attributes.getNamedItem("lon").nodeValue,
                    nList.item(i).attributes.getNamedItem("lat").nodeValue,
                    nList.item(i + 1).attributes.getNamedItem("lon").nodeValue,
                    nList.item(i + 1).attributes.getNamedItem("lat").nodeValue,
                )
            }
        }

        return total
    }

    private fun calcularDistanciaEntreCoordenades(lat1:String, lon1:String,lat2:String, lon2:String): Double {
        var R = 6378.137;//Radi de la tierra en km
        var dLat = rad(lat2.toDouble() - lat1.toDouble());
        var dLong = rad(lon2.toDouble() - lon1.toDouble());
        var a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(rad(lat1.toDouble())) * Math.cos(rad(lat2.toDouble())) * Math.sin(dLong / 2) * Math.sin(dLong / 2);
        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        var d = R * c;
        return d;//Retorna tres decimals
    }

    private fun rad(x: Double): Double {
        return x * Math.PI / 180;
    }


    /*
    * Carrego el track de assets
    * TODO emmagatzemar els punts en una estructura de dades o Objecte que pugui treballar
    * */
    public fun carregarTrack() {
        val istream = assets.open("myGpx.gpx")
        val builderFactory = DocumentBuilderFactory.newInstance()
        val docBuilder = builderFactory.newDocumentBuilder()
        val doc = docBuilder.parse(istream)
        val nList = doc.getElementsByTagName("trkpt")
        val eTrack = doc.getElementsByTagName("metadata").item(0) as Element
        val lenPoints = getPunts(nList)
        val recorregutTrack = calcularDistanciaTrack()
        val distanciaRecorreguda: TextView = findViewById(R.id.distanciaRecorreguda) as TextView
        distanciaRecorreguda.text = recorregutTrack.toString()
        //extrec el numero de punts i els emagatzema a la llista
        for (i in 10 until nList.getLength() step lenPoints!!) {
            if (nList.item(0).getNodeType().equals(Node.ELEMENT_NODE) ) {
                //creating instance of HashMap to put the data of node value
                trkDataHashMap = HashMap()
                val element = nList.item(i) as Element
                val track = Track(getNodeValue("name", eTrack),
                    Point(getNodeValue("ele", element),
                        getNodeValue("time", element),
                        nList.item(i).attributes.getNamedItem("lat").nodeValue,
                        nList.item(i).attributes.getNamedItem("lon").nodeValue))
                val (name, punt) = track
                mylist?.add(track)
                trkDataHashMap.put("nom", name)
                trkDataHashMap.put("altura", punt.altura)
                trkDataHashMap.put("temps", punt.temps)
                trkDataHashMap.put("coordenades", "X: " + punt.lat + ", Y: " + punt.lon)
                trkDataHashMap.put("lat", punt.lat)
                trkDataHashMap.put("lon", punt.lon)
                //adding the HashMap data to ArrayList
                trkList.add(trkDataHashMap)
            }
        }
        trkDataHashMap.put("recorregutTrack", recorregutTrack.toString())
    }

    /*
    * Calculo el numero de punts necessaris
    * A partir d'aqui es miraran diferents condicions
    * TODO Depenent dels km del track mes o menys punts
    * */
    private fun getPunts(nList: NodeList?): Int? {
        var resultat: Int? = nList?.getLength()?.div(10)
        return resultat
    }

    private fun getNodeValue(tag: String, element: Element): String {
        val nodeList = element.getElementsByTagName(tag)
        val node = nodeList.item(0)
        if (node != null) {
            if (node.hasChildNodes()) {
                val child = node.getFirstChild()
                while (child != null) {
                    if (child.getNodeType() === Node.TEXT_NODE) {
                        return child.getNodeValue()
                    }
                }
            }
        }
        return ""
    }


}