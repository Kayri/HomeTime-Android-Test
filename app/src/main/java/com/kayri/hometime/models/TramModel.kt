package com.kayri.hometime.models

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

//Return all Route at one Stop
class MainRoutesForStopParent(val responseObject: List<MainRoutesForStop>)
class MainRoutesForStop(val RouteNo: String)

//Return prediction for Stop and Route
class NextPredictedRoutesCollectionParent(val responseObject: List<NextPredictedRoutesCollection>)
class NextPredictedRoutesCollection(
        val PredictedArrivalDateTime: String,
        val VehicleNo: Int
)