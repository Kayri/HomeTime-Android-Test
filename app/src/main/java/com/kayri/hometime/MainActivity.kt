package com.kayri.hometime

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.kayri.hometime.adapter.RoutesListAdapter
import com.kayri.hometime.adapter.StopsSpinAdapter
import com.kayri.hometime.models.Route
import com.kayri.hometime.utils.NavigationIconClickListener
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.backdrop_main.*
import kotlinx.android.synthetic.main.layout_tram_time.*


class MainActivity : AppCompatActivity() {

    lateinit var token: String
    lateinit var realm: Realm
    lateinit var routesList: RealmResults<Route>
    //val routesAdapter = RoutesAdapter()
    //var routesTracker: SelectionTracker<Long>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val context = this

        val navigationIconClickListener = NavigationIconClickListener(
                context,
                tram_layout,
                AccelerateDecelerateInterpolator(),
                ContextCompat.getDrawable(this, R.drawable.shr_branded_menu), // Menu open icon
                ContextCompat.getDrawable(this, R.drawable.shr_close_menu))
        toolbar.setNavigationOnClickListener(navigationIconClickListener) // Menu close icon


        realm = Realm.getInstance(RealmConfiguration.Builder()
                .name(BuildConfig.DATABASE_NAME)
                .schemaVersion(BuildConfig.DATABASE_VERSION.toLong())
                .deleteRealmIfMigrationNeeded()
                .build())

        token = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
                .getString(EXTRA_TOKEN, "")
        routesList = realm.where<Route>().findAll()

        routes_listView.choiceMode = ListView.CHOICE_MODE_SINGLE
        routes_listView.adapter = RoutesListAdapter(this, routesList)
        //TODO SharedPref last route selected.
        //if no .getselect(0) & getSelectedView().setSelected(true)


        routes_listView.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Toast.makeText(parent?.context, routesList[position]?.RouteNo, Toast.LENGTH_SHORT).show()
                val item = routesList[position]!!


                navigationIconClickListener.closeBackdrop()
                toolbar.title = "${item.RouteNo} : ${item.Description}"

                displayTramTime(item)
            }

        }

        routes_listView.setItemChecked(0, true);
        routes_listView.performItemClick(routes_listView.selectedView, 0, 0)


    }


    private fun displayTramTime(item: Route) {
        // charge spinner

        val stopsAdapter = StopsSpinAdapter(this, item.ListStopsDown)
        spinnerStops.adapter = stopsAdapter
        spinnerStops.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Toast.makeText(parent?.context, item.ListStopsDown[position]?.StopName, Toast.LENGTH_SHORT).show()
            }

        }

        //spinnerStops.adapter =

    }


/*
private var disposable: Disposable? = null
private var chosenRoute: RouteSummaries? = null
private var listRoute: MutableList<RouteSummaries> = mutableListOf()
private var chosenStop: RouteStopsByRoute? = null
private var listStop1: MutableList<RouteStopsByRoute> = mutableListOf()
private var listStop2: MutableList<RouteStopsByRoute> = mutableListOf()
private lateinit var listTime1: List<NextPredictedRoutesCollection>
private lateinit var listTime2: List<NextPredictedRoutesCollection>
private val tramApiServe = TramApiService.create()
private var isChecked: Boolean = false*/

/*private fun getNewDeviceToken() {
    disposable = tramApiServe.getDeviceToken(aid, "HomeTimeiOS")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                    { result ->
                        token = result.responseObject[0].DeviceToken
                        Log.d("Result", token)
                        getRouteList()
                    },
                    { error ->
                        Toast.makeText(this, "Error Network", Toast.LENGTH_SHORT).show()
                        Log.w("Error Token", error.message)
                    }
            )
}

private fun getRouteList() {
    disposable = tramApiServe.getRouteSummaries(aid, token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                    { result ->
                        listRoute = result.responseObject as MutableList<RouteSummaries>
                        checkMissingRoute()
                    },
                    { error ->
                        Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show()
                        Log.w("Error RouteList", error.message)
                    }
            )
}

private fun checkMissingRoute() {
    disposable = tramApiServe.getDestinationsForAllRoutes(aid, token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                    { result ->

                        for (data in result.responseObject) {
                            if (listRoute.none { it.RouteNo == data.RouteNumber }) {
                                var tempUpDestination = ""
                                var tempDownDestination = ""
                                for (tmp in result.responseObject.filter { it.RouteNumber == data.RouteNumber }) {
                                    if (tmp.IsUpStop) tempUpDestination = tmp.Name
                                    else tempDownDestination = tmp.Name
                                }
                                listRoute.add(listRoute.size, RouteSummaries(tempDownDestination + " - " + tempUpDestination, data.RouteNumber, data.RouteNumber.toInt(), tempUpDestination, tempDownDestination))
                            }
                        }

                        fun selector(r: RouteSummaries): Int = r.InternalRouteNo
                        listRoute.sortBy { selector(it) }
                        val listItems = arrayOfNulls<String>(listRoute.size)
                        for (i in 0 until listRoute.size) {
                            listItems[i] = listRoute[i].RouteNo + " - " + listRoute[i].Description
                        }
                        val adapter = ArrayAdapter(this, R.layout.item_spin_tram_stop, R.id.spinTextView, listItems)
                        spin_route.adapter = adapter

                    },
                    { error ->
                        Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show()
                        Log.w("Error MissRoute", error.message)
                    }
            )
}

private fun getStopList(internalRouteNo: Int) {

    disposable = tramApiServe.getRouteStopsByRoute(internalRouteNo, aid, token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                    { result ->
                        //init listStop
                        listStop1.clear()
                        listStop2.clear()
                        for (stop in result.responseObject) {
                            if (!stop.UpStop)
                                listStop1.add(stop)
                            else
                                listStop2.add(stop)
                        }
                        val adapter = if (isChecked) SpinnerAdapter(this, R.layout.item_spin_tram_stop, listStop2)
                        else SpinnerAdapter(this, R.layout.item_spin_tram_stop, listStop1)
                        spin_stop.adapter = adapter

                    },
                    { error ->
                        Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
                        Log.w("Error StopList", error.message)
                    }
            )
}

private fun getTimeByRoutAndStop(chosenRoute: RouteSummaries, chosenStop: RouteStopsByRoute) {
    TransitionManager.beginDelayedTransition(constraintLayout)
    recyclerView1.adapter = null
    recyclerView2.adapter = null
    textView1.text = ""
    textView2.text = ""
    direction3.text = ""
    direction4.text = ""

    disposable = tramApiServe.getNextPredictedRoutesCollection(chosenStop.StopNo, chosenRoute.InternalRouteNo, false, aid, 2, token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            //Error on Route 35
            .filter { it.responseObject != null }
            .subscribe(
                    { result ->
                        listTime1 = result.responseObject
                        TransitionManager.beginDelayedTransition(constraintLayout)
                        recyclerView2.layoutManager = LinearLayoutManager(baseContext)
                        recyclerView2.adapter = TramAdapter(listTime1, baseContext)
                        direction4.text = if (isChecked) chosenRoute.UpDestination else chosenRoute.DownDestination
                        textView2.text = getString(R.string.direction)

                    },
                    { error ->
                        Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show()

                    }
            )

    var inverseStop = if (isChecked) listStop1.find { it.StopSequence == chosenStop.StopSequence } else listStop2.find { it.StopSequence == chosenStop.StopSequence }

    if (inverseStop != null)
        disposable = tramApiServe.getNextPredictedRoutesCollection(inverseStop!!.StopNo, chosenRoute.InternalRouteNo, false, aid, 2, token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                //Error on Route 35
                .filter { it.responseObject != null }
                .subscribe(
                        { result ->
                            listTime2 = result.responseObject
                            TransitionManager.beginDelayedTransition(constraintLayout)
                            recyclerView1.layoutManager = LinearLayoutManager(baseContext)
                            recyclerView1.adapter = TramAdapter(listTime2, baseContext)
                            direction3.text = if (isChecked) chosenRoute.DownDestination else chosenRoute.UpDestination
                            textView1.text = getString(R.string.direction)

                        }
                )
}*/

    override fun onBackPressed() {
        super.onBackPressed()
        //TransitionName view stay when activity close
        finish()
    }
}