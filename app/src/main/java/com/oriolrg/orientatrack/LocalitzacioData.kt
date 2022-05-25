package com.oriolrg.orientatrack

class LocalitzacioData {
    data class Point( var lat: Double, var lon: Double){
        var altura: Double? =0.0
        var temps: String? = null
        var fet:Boolean? = false
    }
    data class Track(var name: String, var punts: Point)

}