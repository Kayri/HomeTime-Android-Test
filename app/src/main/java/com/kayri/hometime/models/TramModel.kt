package com.kayri.hometime.models

//Get the token
class DeviceToken(val responseObject: List<DeviceTokenChild>)

class DeviceTokenChild(val DeviceToken: String)

//Return all routes
class RouteSummaries(val responseObject: List<RouteSummariesChild>)

class RouteSummariesChild(
        val Description: String,
        val RouteNo: String,
        val InternalRouteNo: Int,
        val UpDestination: String,
        val DownDestination: String)

//Return all Stop for one Route
class ListOfStopsByRouteNoAndDirection(val responseObject: List<ListOfStopsByRouteNoAndDirectionInfoChild>)

class ListOfStopsByRouteNoAndDirectionInfoChild(
        val Name: String,
        val StopNo: Int,
        val SuburbName: String
)

//Return all Route at one Stop
class MainRoutesForStop(val responseObject: List<MainRoutesForStopChild>)

class MainRoutesForStopChild(val RouteNo: String)

//Return prediction for Stop and Route
class NextPredictedRoutesCollection(val responseObject: List<NextPredictedRoutesCollectionChild>)

class NextPredictedRoutesCollectionChild(
        val PredictedArrivalDateTime: String,
        val VehicleNo: Int
)