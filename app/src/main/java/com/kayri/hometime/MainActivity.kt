package com.kayri.hometime

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.kayri.hometime.adapter.RoutesListAdapter
import com.kayri.hometime.adapter.StopsSpinAdapter
import com.kayri.hometime.adapter.TimeRecyclerAdapter
import com.kayri.hometime.models.Route
import com.kayri.hometime.models.Stop
import com.kayri.hometime.utils.NavigationIconClickListener
import com.kayri.hometime.utils.TramApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.backdrop_main.*
import kotlinx.android.synthetic.main.layout_tram_time.*

//const val ROUTE_SAVED = "com.HomeTime.ROUTE_SAVED"
//const val STOP_SAVED = "com.HomeTime.STOP_SAVED"


class MainActivity : AppCompatActivity() {
    lateinit var sharedPref: SharedPreferences

    lateinit var token: String
    lateinit var realm: Realm
    lateinit var routesList: RealmResults<Route>
    lateinit var stopsAdapter: StopsSpinAdapter
    lateinit var timeAdapter: TimeRecyclerAdapter
    lateinit var route: Route
    lateinit var stop: Stop
    lateinit var tramApiService: TramApiService
    var upStop: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        val navigationIconClickListener = NavigationIconClickListener(
                this,
                tram_layout,
                AccelerateDecelerateInterpolator(),
                ContextCompat.getDrawable(this, R.drawable.shr_branded_menu),
                ContextCompat.getDrawable(this, R.drawable.shr_close_menu))
        toolbar.setNavigationOnClickListener(navigationIconClickListener)

        tramApiService = TramApiService.create()
        realm = Realm.getInstance(RealmConfiguration.Builder()
                .name(BuildConfig.DATABASE_NAME)
                .schemaVersion(BuildConfig.DATABASE_VERSION.toLong())
                .deleteRealmIfMigrationNeeded()
                .build())

        token = sharedPref.getString(EXTRA_TOKEN, "")
        routesList = realm.where<Route>().findAll()

        //Listeners
        routes_listView.choiceMode = ListView.CHOICE_MODE_SINGLE
        routes_listView.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //Toast.makeText(parent?.context, routesList[position]?.RouteNo, Toast.LENGTH_SHORT).show()
                route = routesList[position]!!

                //sharedPref.edit().putInt(ROUTE_SAVED,position).apply()

                navigationIconClickListener.closeBackdrop()
                routeNo_textView.text = route.RouteNo
                routeDesc_textView.text = ": ${route.Description}"
                destinationFrom.text = if (upStop) route.DownDestination else route.UpDestination
                destinationTo.text = if (upStop) route.UpDestination else route.DownDestination

                stopsAdapter.items = if (upStop) route.ListStopsUP else route.ListStopsDown
                stopsAdapter.notifyDataSetChanged()

                //var stopNoSaved: Int = sharedPref.getInt(STOP_SAVED,0)
                stopsAdapter.items!![0]?.let { getNextPredictedRoutesCollection(route, it) }

            }

        }

        spinnerStops.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //Toast.makeText(parent?.context, item.ListStopsDown[position]?.StopName, Toast.LENGTH_SHORT).show()
                /*val stop = item.ListStopsDown[position]!!
                val stringSet = HashSet<String>()
                stringSet.add(item.id.toString())
                stringSet.add(position.toString())
                //sharedPref.edit().putInt(STOP_SAVED,stop.StopNo).apply()
                sharedPref.edit().putStringSet(STOP_SAVED, stringSet).apply()*/
                stopsAdapter.items!![position]?.let { getNextPredictedRoutesCollection(route, it) }

            }

        }

        switchDestination.setOnClickListener {
            upStop = !upStop
            destinationFrom.text = if (upStop) route.DownDestination else route.UpDestination
            destinationTo.text = if (upStop) route.UpDestination else route.DownDestination
            stopsAdapter.items = if (upStop) route.ListStopsUP else route.ListStopsDown
            stopsAdapter.notifyDataSetChanged()
            stopsAdapter.items!![0]?.let { getNextPredictedRoutesCollection(route, it) }

        }


        //Adapters
        routes_listView.adapter = RoutesListAdapter(this, routesList)
        stopsAdapter = StopsSpinAdapter(this)
        spinnerStops.adapter = stopsAdapter
        timeAdapter = TimeRecyclerAdapter(this)
        time_listView.layoutManager = LinearLayoutManager(this)
        time_listView.adapter = timeAdapter


        //TODO sharedPref last search or save
        //val routeNoSaved = sharedPref.getStringSet(STOP_SAVED, null)
        val routeNoSaved = 0
        routes_listView.setItemChecked(routeNoSaved, true)
        routes_listView.performItemClick(routes_listView.selectedView, routeNoSaved, 0)

        getFlagStopNo()
    }


    fun getNextPredictedRoutesCollection(route: Route, stop: Stop) {

        val disposable = (tramApiService.getNextPredictedRoutesCollection(stop.StopNo, route.InternalRouteNo, false, BuildConfig.API_KEY_AID, 2, token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    timeAdapter.items = result.responseObject
                    timeAdapter.notifyDataSetChanged()
                    //Toast.makeText(this, route.RouteNo, Toast.LENGTH_SHORT).show()

                    /*
                    listTime1 = result.responseObject
                    TransitionManager.beginDelayedTransition(constraintLayout)
                    recyclerView2.layoutManager = LinearLayoutManager(baseContext)
                    recyclerView2.adapter = TramAdapter(listTime1, baseContext)
                    direction4.text = if (isChecked) chosenRoute.UpDestination else chosenRoute.DownDestination
                    textView2.text = getString(R.string.direction)*/

                }, this::handleError))
    }


    fun getFlagStopNo() {

        Handler().postDelayed({
            val stop = realm.where<Stop>().equalTo("FlagStopNo", "").findFirst()
            if (stop != null) {
                val observable = tramApiService.getStopInformation(stop.StopNo, BuildConfig.API_KEY_AID, token)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ result ->

                            realm.beginTransaction()
                            stop.FlagStopNo = result.responseObject[0].FlagStopNo
                            realm.commitTransaction()

                        }, this::handleError)
                getFlagStopNo()
            }
        }, 500)

    }

    private fun handleError(error: Throwable) {
        Log.w("DEBUG", error.localizedMessage)
        /*Snackbar.make(findViewById(R.id.constraintSplash), "Error ${error.localizedMessage}", Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.retry) { getDeviceToken() }.show()*/
        Toast.makeText(this, "Error ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}