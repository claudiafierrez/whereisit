package com.uoc.whereisitproject.util

fun streetViewUrl(
    lat: Double,
    lng: Double,
    apiKey: String,
    width: Int = 600,
    height: Int = 400,
    scale: Int = 2,
    heading: Int,
    pitch: Int
): String {
    val base = "https://maps.googleapis.com/maps/api/streetview"
    return "$base?size=${width}x${height}&scale=$scale&location=$lat,$lng&heading=$heading&pitch=$pitch&key=$apiKey"
}