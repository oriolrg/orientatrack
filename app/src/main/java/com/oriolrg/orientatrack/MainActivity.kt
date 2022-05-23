package com.oriolrg.orientatrack

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.SAXException
import java.io.IOException
import java.lang.Math.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

data class Point( var lat: Double, var lon: Double){
    var altura: Double? =0.0
    var temps: String? = null
}
data class Track(var name: String, var punts: Point)
class MainActivity : AppCompatActivity() {
    var trkDataHashMap = HashMap< String, String>()
    var trkList: ArrayList< HashMap< String, String>> = ArrayList()
    var mylist : MutableList<Track>? = null
    //Variables posicio temps real
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    var desti = Point( 42.13708, 1.58497)

        //permision id es unic
    private var PERMISION_ID = 1000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try {
            val lv = findViewById<ListView>(R.id.listView)
            carregarTrack()
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

            Log.d("Debug:",CheckPermission().toString())
            Log.d("Debug:",isLocationEnabled().toString())
            ActivityResultContracts.RequestPermission()
            Log.d("debug", "getLastLocation()")
            getLastLocation()
            NewLocationData()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ParserConfigurationException) {
            e.printStackTrace()
        } catch (e: SAXException) {
            e.printStackTrace()
        }
    }
    //creem la funcio que ens permet obtenir l'ultima posicio
    fun getLastLocation(){
        val LocalitzacioTxt = findViewById<TextView>(R.id.LocalitzacioTxt)
        if(isLocationEnabled()){
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                RequestPermission()
            }else{
                fusedLocationProviderClient.lastLocation.addOnCompleteListener { task->
                    val location: Location? = task.result
                    if(location == null){
                        Log.d("Debug:" ,"NewLocationData")
                        val locationTipus = findViewById<TextView>(R.id.locationTipus)
                        locationTipus.text = "NewLocationData"
                        NewLocationData()
                    }else{
                        var localitzacio = Point(location.latitude,location.longitude)
                        val distanciaTxt = findViewById<TextView>(R.id.DistanciaTxt)
                        LocalitzacioTxt.text = "Les teves cordenades son: \n Latitud:" + location.latitude + "; Longitud:" + location.longitude + "; Altitud:" + location.altitude

                        //var distanciaPunt = calcularDistanciaEntreCoordenades(location.latitude,location.longitude, destiX, destiY)
                        //var anglePunt = calculateAngle(location.latitude,location.longitude, destiX, destiY)
                        var distanciaPunt = calcularDistanciaEntreCoordenades(localitzacio.lat,localitzacio.lon, desti.lat, desti.lon)
                        var anglePunt = calculateAngle(localitzacio.lat,localitzacio.lon, desti.lat, desti.lon)
                        val locationTipus = findViewById<TextView>(R.id.locationTipus)
                        locationTipus.text = anglePunt.toString()+"º"
                        distanciaTxt.text = "El punt està a:" + distanciaPunt + "m"
                    }
                }
            }
        }else{
            Toast.makeText(this,"Please Turn on Your device Location",Toast.LENGTH_SHORT).show()
        }
    }
    fun NewLocationData(){
        Log.d("NewLocationData Debug:" ,"Entra")
        var locationRequest =  LocationRequest.create().apply {
            interval = 100
            fastestInterval = 50
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime = 100
        }
        /*locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0*/
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            RequestPermission()
        }else{
            Log.d("Debug:" ,"getMainLooper")
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,locationCallback, Looper.getMainLooper()
            )
        }

    }
    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            val LocalitzacioTxt = findViewById<TextView>(R.id.LocalitzacioTxt)
            val DistanciaTxt = findViewById<TextView>(R.id.DistanciaTxt)
            var lastLocation = Point(locationResult.lastLocation.latitude,locationResult.lastLocation.longitude)
            lastLocation.altura = locationResult.lastLocation.altitude
            //var lastLocation2 = Location(lastLocation.latitude,lastLocation.longitude)
            //Assignem la nova localitzacio

            var distanciaPunt = calcularDistanciaEntreCoordenades(lastLocation.lat,lastLocation.lon, desti.lat, desti.lon)
            var anglePunt = calculateAngle(lastLocation.lat,lastLocation.lon, desti.lat, desti.lon)
            val locationTipus = findViewById<TextView>(R.id.locationTipus)
            locationTipus.text = anglePunt.toString()+"º"
            DistanciaTxt.text = "El punt està a:" + distanciaPunt + "m"

            LocalitzacioTxt.text = "Les teves cordenades son: \n Latitud:" + lastLocation.lat + "; Longitud:" + lastLocation.lon + "; Altitud:" + lastLocation.altura

        }

    }
    private fun calculateAngle(x1:Double, y1:Double, x2:Double, y2:Double):Double
    {
        var start_latitude  = toRadians(x1)
        var start_longitude = toRadians(y1)
        var stop_latitude   = toRadians(x2)
        var stop_longitude  = toRadians(y2)
        var y = kotlin.math.sin(stop_longitude-start_longitude) * kotlin.math.cos(stop_latitude)
        var x = kotlin.math.cos(start_latitude)*kotlin.math.sin(stop_latitude) -
                kotlin.math.sin(start_latitude)*kotlin.math.cos(stop_latitude)*kotlin.math.cos(stop_longitude-start_longitude)
        var brng = toDegrees(kotlin.math.atan2(y, x))
        if (brng < 0 ) {
            brng = brng + 360
        }
        return brng
    }
    //Creem la funció que xequeja si tenim permisos
    private fun CheckPermission():Boolean{
        return !(ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED)
    }
    //Funcio que ens permetra obtenir el permis d'ubicacio de l'usuari
    private fun RequestPermission(){
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION),
            PERMISION_ID
        )
    }
    //Funcio que xequeha si el servei de localització està activat
    private fun isLocationEnabled():Boolean{

        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //Aquest es una funcio que xequeha el resultat dels permisos
        //Ho utilitzem per debgar el codi
        if(requestCode == PERMISION_ID){
            if(grantResults.isNotEmpty() && grantResults[0] ==PackageManager.PERMISSION_GRANTED){
                Log.d("debug", "Tens permisos")
            }
        }
    }
    private fun calcularDistanciaTrack(): Double{
        val istream = assets.open("myGpx.gpx")
        val builderFactory = DocumentBuilderFactory.newInstance()
        val docBuilder = builderFactory.newDocumentBuilder()
        val doc = docBuilder.parse(istream)
        val nList = doc.getElementsByTagName("trkpt")
        var total = 0.0

        for (i in 0 until nList.getLength()-1 step 1) {
            if (nList.item(0).getNodeType().equals(Node.ELEMENT_NODE)) {

                total += calcularDistanciaEntreCoordenades(
                    nList.item(i).attributes.getNamedItem("lon").nodeValue.toDouble(),
                    nList.item(i).attributes.getNamedItem("lat").nodeValue.toDouble(),
                    nList.item(i + 1).attributes.getNamedItem("lon").nodeValue.toDouble(),
                    nList.item(i + 1).attributes.getNamedItem("lat").nodeValue.toDouble(),
                )
            }
        }

        return total
    }

    private fun calcularDistanciaEntreCoordenades(lat1:Double, lon1:Double,lat2:Double, lon2:Double): Double {
        var R = 6378137//Radi de la tierra en km
        var dLat = toRadians(lat2) - toRadians(lat1)
        var dLong = toRadians(lon2) - toRadians(lon1)
        var a = sin(dLat / 2) * sin(dLat / 2) + cos(toRadians(lat1)) * cos(toRadians(lat2)) * sin(dLong / 2) * sin(dLong / 2)
        var c = 2 * atan2(sqrt(a), sqrt(1 - a))
        var d = R * c
        return d//Retorna tres decimals
    }



    /*
    * Carrego el track de assets
    * TODO emmagatzemar els punts en una estructura de dades o Objecte que pugui treballar
    * */
    fun carregarTrack() {
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
                    Point(
                        nList.item(i).attributes.getNamedItem("lat").nodeValue.toDouble(),
                        nList.item(i).attributes.getNamedItem("lon").nodeValue.toDouble()))
                track.punts.altura = getNodeValue("ele", element).toDouble()
                track.punts.temps = getNodeValue("time", element)
                val (name, punt) = track
                mylist?.add(track)
                trkDataHashMap.put("nom", name)
                trkDataHashMap["altura"] = punt.altura.toString()
                trkDataHashMap["temps"] = punt.temps.toString()
                trkDataHashMap["coordenades"] = "X: " + punt.lat + ", Y: " + punt.lon
                trkDataHashMap["lat"] = punt.lat.toString()
                trkDataHashMap["lon"] = punt.lon.toString()
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
                    if (child.getNodeType() == Node.TEXT_NODE) {
                        return child.getNodeValue()
                    }
                }
            }
        }
        return ""
    }


}