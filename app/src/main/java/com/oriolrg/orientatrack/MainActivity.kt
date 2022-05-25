package com.oriolrg.orientatrack

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.SAXException
import java.io.IOException
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

class MainActivity : AppCompatActivity() {
    private var trkDataHashMap = HashMap< String, String>()
    private var trkList: ArrayList< HashMap< String, String>> = ArrayList()
    private var mylist : MutableList<LocalitzacioData.Track>? = null
    //Variables posicio temps real
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    var desti = LocalitzacioData.Point( 42.13708, 1.58497)

    //permision id es unic
    private var PERMISION_ID = 1000
    //rang d'error permes al apropar-se a un punt, 1m
    private val RANG_ERROR_DISTANCIA = 8.0
    //Si la ruta esta finalitzata = true
    private var ESTAT_RUTA = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try {
            //carrega el track sobre el que es treballa a trkList: ArrayList
            carregarTrack()
            //Todo carregar es punts un a un
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            Log.d("Debug:",trkList[0]["lon"].toString())

            carregarPuntDesti()
            ActivityResultContracts.RequestPermission()
            if (!ESTAT_RUTA){
                getLastLocation()
                NewLocationData()
            }else{
                //TODO mostrar pantalla de finalitzacio de ruta
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ParserConfigurationException) {
            e.printStackTrace()
        } catch (e: SAXException) {
            e.printStackTrace()
        }
    }
    /**
     *  Funcio que ens carrega el primer punt que no tenim realitzat
     */
    private fun carregarPuntDesti():Boolean {
        //He de recorrer trkList fins a trobar un punt amb fet = false
        trkList.forEach() {
            if(it["fet"].toBoolean() == false){
                Log.d("debug", it.toString())
                desti.lat = it["lat"]!!.toDouble()
                desti.lon = it["lon"]!!.toDouble()
                val distanciaX = findViewById<TextView>(R.id.distanciaX)
                val distanciaY = findViewById<TextView>(R.id.distanciaY)
                distanciaX.text = desti.lat.toString()
                distanciaY.text = desti.lon.toString()
                return true
            }

        }
        ESTAT_RUTA = true
        Toast.makeText(this,"Ha finalitzat la ruta Enhorabona",Toast.LENGTH_SHORT).show()
        val punt = findViewById<TextView>(R.id.punt)
        punt.text = "Ha finalitzat la ruta Enhorabona"
        return false
    }

    private fun comprovarDistancia(distanciaPunt:Double):Boolean {
        if (distanciaPunt < RANG_ERROR_DISTANCIA){
            val punt = findViewById<TextView>(R.id.punt)
            trkList.forEachIndexed() {index, element ->
                if(element["fet"].toBoolean() == false){
                    var numeroPunt = index + 1
                    punt.text = "Ha arribat al punt " + numeroPunt
                    element["fet"]= "true"
                    //carrego el seguent punt quan la distancia a l'actual es < a distanciaPunt
                    carregarPuntDesti()
                    return true
                }
            }
        }

        return false
    }
    /**
     *  Funcio que ens permet obtenir l'ultima posicio
     */
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
                        val missatgeText = "NewLocationData"
                        locationTipus.text = missatgeText
                        NewLocationData()
                    }else{
                        if (!ESTAT_RUTA){
                            var localitzacio = LocalitzacioData.Point(location.latitude,location.longitude)
                            val distanciaTxt = findViewById<TextView>(R.id.DistanciaTxt)
                            var missatgeText = "Les teves cordenades son: \n Latitud:" + location.latitude + "; Longitud:" + location.longitude + "; Altitud:" + location.altitude
                            LocalitzacioTxt.text = missatgeText
                            var distanciaPunt = calcularDistanciaEntreCoordenades(localitzacio, desti)
                            //Todo si distancia es menor a x seguent punt
                            comprovarDistancia(distanciaPunt)
                            var anglePunt = calcularAngle(localitzacio, desti)
                            val locationTipus = findViewById<TextView>(R.id.locationTipus)
                            missatgeText = anglePunt.toString()+"º"
                            locationTipus.text = missatgeText
                            missatgeText = "El punt està a:" + distanciaPunt + "m"
                            distanciaTxt.text = missatgeText
                        }
                    }
                }
            }
        }else{
            Toast.makeText(this,"Please Turn on Your device Location",Toast.LENGTH_SHORT).show()
        }
    }


    fun NewLocationData(){
        var locationRequest =  LocationRequest.create().apply {
            interval = 100
            fastestInterval = 50
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime = 100
        }
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
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,locationCallback, Looper.getMainLooper()
            )
        }

    }
    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            if (!ESTAT_RUTA){
                val localitzacioTxt = findViewById<TextView>(R.id.LocalitzacioTxt)
                val distanciaTxt = findViewById<TextView>(R.id.DistanciaTxt)
                var lastLocation = LocalitzacioData.Point(locationResult.lastLocation.latitude,locationResult.lastLocation.longitude)
                lastLocation.altura = locationResult.lastLocation.altitude
                var distanciaPunt = calcularDistanciaEntreCoordenades(lastLocation, desti)
                comprovarDistancia(distanciaPunt)
                var anglePunt = calcularAngle(lastLocation, desti)
                val locationTipus = findViewById<TextView>(R.id.locationTipus)
                var missatgeText = anglePunt.toString()+"º"
                locationTipus.text = missatgeText
                missatgeText = "El punt està a:" + distanciaPunt + "m"
                distanciaTxt.text = missatgeText
                missatgeText = "Les teves cordenades son: \n Latitud:" + lastLocation.lat + "; Longitud:" + lastLocation.lon + "; Altitud:" + lastLocation.altura
                localitzacioTxt.text = missatgeText
            }

        }

    }

    /**
     * Funció que xequeja si tenim permisos

    private fun CheckPermission():Boolean{
        return !(ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED)
    }*/
    /**
     * Funcio que ens permetra obtenir el permis d'ubicacio de l'usuari
     */
    private fun RequestPermission(){
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION),
            PERMISION_ID
        )
    }

    /**
     * Funcio que xequeha si el servei de localització està activat
     */
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
    /**
    * Obro el fitxer
    * TODO Crear la funcio que obre el fitxe donat
    * */
    fun obrirFitxer(nomFitxer:String):Document{
        val istream = assets.open(nomFitxer)
        val builderFactory = DocumentBuilderFactory.newInstance()
        val docBuilder = builderFactory.newDocumentBuilder()
        val doc = docBuilder.parse(istream)
        return doc
    }

    /**
    * Carrego el track de assets
    *
    * */
    fun carregarTrack() {
        val doc = obrirFitxer("myGpx.gpx")
        val recorregutTrack = calcularDistanciaTrack(doc)
        val distanciaRecorreguda: TextView = findViewById(R.id.distanciaRecorreguda) as TextView

        val nList = doc.getElementsByTagName("trkpt")
        val eTrack = doc.getElementsByTagName("metadata").item(0) as Element
        val lenPoints = getPunts(nList)
        distanciaRecorreguda.text = recorregutTrack.toString()
        //extrec el numero de punts i els emagatzema a la llista
        for (i in 0 until nList.getLength() step lenPoints!!) {
            if (nList.item(0).getNodeType().equals(Node.ELEMENT_NODE) ) {
                //creating instance of HashMap to put the data of node value
                trkDataHashMap = HashMap()
                val element = nList.item(i) as Element
                val track = LocalitzacioData.Track(getNodeValue("name", eTrack),
                    LocalitzacioData.Point(
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
                trkDataHashMap["fet"] = false.toString()
                //adding the HashMap data to ArrayList
                trkList.add(trkDataHashMap)
            }
        }
        trkDataHashMap.put("recorregutTrack", recorregutTrack.toString())
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