package com.kayri.hometime.models

import io.realm.RealmObject
import io.realm.RealmList
import io.realm.annotations.PrimaryKey


//Get the token
class DeviceTokenParent(val responseObject: List<DeviceToken>)
class DeviceToken(val DeviceToken: String)

//Return all routes
class DestinationsForAllRoutesParent(val responseObject: List<DestinationsForAllRoutes>)
class DestinationsForAllRoutes(
        val RouteNumber: String,
        val Name: String,
        val IsUpStop: Boolean
)

//Return all routes without 35
class RouteSummariesParent(val responseObject: List<RouteSummaries>)
class RouteSummaries(
        val Description: String,
        val RouteNo: String,
        val InternalRouteNo: Int,
        val UpDestination: String,
        val DownDestination: String)

//Return all Stop for one Route
class RouteStopsByRouteParent(val responseObject: List<RouteStopsByRoute>)
class RouteStopsByRoute(
        val StopName: String,
        val StopNo: Int,
        val SuburbName: String,
        val StopSequence: Int,
        val UpStop: Boolean
)

//Return stop information
class StopInformationParent(val responseObject: List<StopInformation>)
class StopInformation(
        val FlagStopNo: String
        /*val HasConnectingBuses: Boolean,
        val HasConnectingTrains: Boolean,
        val HasConnectingTrams: Boolean*/
)

//Return all Route at one Stop
class MainRoutesForStopParent(val responseObject: List<MainRoutesForStop>)
class MainRoutesForStop(val RouteNo: String)

//Return prediction for Stop and Route
class NextPredictedRoutesCollectionParent(val responseObject: List<NextPredictedRoutesCollection>)
class NextPredictedRoutesCollection(
        val PredictedArrivalDateTime: String,
        val VehicleNo: Int
)

// DATABASE MODEL
//TODO fusion with retrofit class model possible ??
open class Route : RealmObject() {
    @PrimaryKey
    var InternalRouteNo: Int = 0
    var Description: String = ""
    var RouteNo: String = ""
    var UpDestination: String = ""
    var DownDestination: String = ""
    var ListStopsUP: RealmList<Stop> = RealmList()
    var ListStopsDown: RealmList<Stop> = RealmList()
}

open class Stop : RealmObject(){
    @PrimaryKey
    var StopNo: Int = 0
    var StopName: String = ""
    var SuburbName: String = ""
    // StopSequence changes depending on RouteNo
    //var StopSequence: Int = 0
    var UpStop: Boolean? = null
    var FlagStopNo: String = ""
}