package com.oriolrg.orientatrack

import android.os.Bundle
import android.widget.ListView
import android.widget.SimpleAdapter
import androidx.appcompat.app.AppCompatActivity
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.SAXException
import java.io.IOException
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException


class MainActivity : AppCompatActivity() {
    var empDataHashMap = HashMap< String, String>()
    var empList: ArrayList< HashMap< String, String>> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try {
            val lv = findViewById<ListView>(R.id.listView)
            val istream = assets.open("myGpx.gpx")
            val builderFactory = DocumentBuilderFactory.newInstance()
            val docBuilder = builderFactory.newDocumentBuilder()
            val doc = docBuilder.parse(istream)
            //reading the tag "employee" of empdetail file
            val nList = doc.getElementsByTagName("trkpt")
            for (i in 0 until nList.getLength()) {
                println("\nLatitud :"
                        + nList.item(i).attributes.getNamedItem("lat").nodeValue);
                println("\nLongitud :"
                        + nList.item(i).attributes.getNamedItem("lon").nodeValue);
                println("\nNode Type :"
                        + nList.item(i).getNodeType());
                val tElement = nList.item(i) as Element
                if (nList.item(0).getNodeType().equals(Node.ELEMENT_NODE) ) {
                    //creating instance of HashMap to put the data of node value
                    empDataHashMap = HashMap()
                    val element = nList.item(i) as Element
                    empDataHashMap.put("nom", getNodeValue("name", element))
                    empDataHashMap.put("altura", getNodeValue("ele", element))
                    empDataHashMap.put("temps", getNodeValue("time", element))
                    empDataHashMap.put("coordenades", "X: " + nList.item(i).attributes.getNamedItem("lat").nodeValue + ", Y: " + nList.item(i).attributes.getNamedItem("lon").nodeValue)
                    //adding the HashMap data to ArrayList
                    empList.add(empDataHashMap)
                }
            }
            val adapter = SimpleAdapter(this@MainActivity, empList, R.layout.custom_list, arrayOf("nom", "altura", "temps", "coordenades"), intArrayOf(R.id.nom, R.id.altura, R.id.temps, R.id.coordenades))
            lv.setAdapter(adapter)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ParserConfigurationException) {
            e.printStackTrace()
        } catch (e: SAXException) {
            e.printStackTrace()
        }
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