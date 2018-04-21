package com.kayri.hometime.models

//Get the token
class DeviceToken(val responseObject: List<DeviceTokenChild>)
class DeviceTokenChild(val DeviceToken: String)

//Return all routes
class RouteSummaries(val responseObject: List<RouteSummariesChild>)
class RouteSummariesChild(
        val Description: String,
        val RouteNo: String,
        val InternalRouteNo: Int)

//Return all Stop for one Route
class RouteStopsByRoute(val responseObject: List<RouteStopsByRouteChild>)
class RouteStopsByRouteChild(
        val Description: String,
        val StopNo: String,
        val StopName: String,
        val SuburbName: String,
        val HasConnectingBuses: Boolean,
        val HasConnectingTrains: Boolean
)


//Return all Route at one Stop
class MainRoutesForStop(val responseObject: List<MainRoutesForStopChild>)
class MainRoutesForStopChild(val RouteNo : String)

//Return precdiction for Stop and Route
class NextPredictedRoutesCollection(val responseObject: List<NextPredictedRoutesCollectionChild>)
class NextPredictedRoutesCollectionChild(
        val PredictedArrivalDateTime: String,
        val VehicleNo: Int
)