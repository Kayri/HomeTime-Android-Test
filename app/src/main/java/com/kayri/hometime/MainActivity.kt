package com.kayri.hometime

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.kayri.hometime.adapter.SpinnerAdapter
import com.kayri.hometime.adapter.TramAdapter
import com.kayri.hometime.models.NextPredictedRoutesCollection
import com.kayri.hometime.models.RouteStopsByRoute
import com.kayri.hometime.models.RouteSummaries
import com.kayri.hometime.utils.TramApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private var disposable: Disposable? = null
    private lateinit var token: String
    private val aid: String = "TTIOSJSON"
    private lateinit var chosenRoute: RouteSummaries
    private var listRoute: MutableList<RouteSummaries> = mutableListOf()
    private lateinit var chosenStop: RouteStopsByRoute
    private var listStop1: MutableList<RouteStopsByRoute> = mutableListOf()
    private var listStop2: MutableList<RouteStopsByRoute> = mutableListOf()
    private lateinit var listTime1: List<NextPredictedRoutesCollection>
    private lateinit var listTime2: List<NextPredictedRoutesCollection>
    private val tramApiServe = TramApiService.create()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val animationDrawable = constraintLayout.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(1500)
        animationDrawable.setExitFadeDuration(3000)
        animationDrawable.start()

        getNewDeviceToken()

        spin_route.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //Toast.makeText(parent!!.context, listRoute[position].RouteNo, Toast.LENGTH_SHORT).show()
                chosenRoute = listRoute[position]
                direction1.text = chosenRoute.DownDestination
                direction2.text = chosenRoute.UpDestination

                //Kotlin doesn’t have a ternary operator
                direction3.text = if (switchDirection.isChecked) chosenRoute.UpDestination else chosenRoute.DownDestination
                direction4.text = if (switchDirection.isChecked) chosenRoute.DownDestination else chosenRoute.UpDestination
                spin_stop.visibility = View.GONE
                group1.visibility = View.GONE
                group2.visibility = View.GONE

                getStopList(chosenRoute.InternalRouteNo)

            }

        }

        spin_stop.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //Toast.makeText(parent!!.context, listStop[position].StopNo.toString(), Toast.LENGTH_SHORT).show()
                group1.visibility = View.GONE
                group2.visibility = View.GONE
                chosenStop = if (switchDirection.isChecked) listStop2[position] else listStop1[position]
                getTimeByRoutAndStop(chosenRoute, chosenStop)

            }
        }

        //TODO Change for Arrow display (ToggleButton ? )
        switchDirection.setOnCheckedChangeListener { buttonView, isChecked ->
            direction3.text = if (switchDirection.isChecked) chosenRoute.UpDestination else chosenRoute.DownDestination
            direction4.text = if (switchDirection.isChecked) chosenRoute.DownDestination else chosenRoute.UpDestination
            group1.visibility = View.GONE
            group2.visibility = View.GONE

            spin_stop.adapter = if (switchDirection.isChecked) SpinnerAdapter(this, R.layout.layout_spinner_item, listStop2)
            else SpinnerAdapter(this, R.layout.layout_spinner_item, listStop1)
        }

        /*refreshbtn.setOnClickListener {
            beginRefresh(listview)
        }*/

    }

    private fun getNewDeviceToken() {
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
                            Log.d("Error token", error.message)
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
                        { error -> Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show() }
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
                            val adapter = ArrayAdapter(this, R.layout.layout_spinner_item, R.id.spinTextView, listItems)
                            spin_route.adapter = adapter
                            spin_route.visibility = View.VISIBLE
                        },
                        { error -> Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show() }
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
                            val adapter = if (switchDirection.isChecked) SpinnerAdapter(this, R.layout.layout_spinner_item, listStop2)
                            else SpinnerAdapter(this, R.layout.layout_spinner_item, listStop1)
                            spin_stop.adapter = adapter
                            TransitionManager.beginDelayedTransition(constraintLayout)
                            spin_stop.visibility = View.VISIBLE
                        },
                        { error -> Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show() }
                )
    }

    private fun getTimeByRoutAndStop(chosenRoute: RouteSummaries, chosenStop: RouteStopsByRoute) {
        recyclerView1.adapter = null
        recyclerView2.adapter = null

        disposable = tramApiServe.getNextPredictedRoutesCollection(chosenStop.StopNo, chosenRoute.InternalRouteNo, false, aid, 2, token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { result ->
                            listTime1 = result.responseObject
                            recyclerView1.layoutManager = LinearLayoutManager(baseContext)
                            recyclerView1.adapter = TramAdapter(listTime1, baseContext)
                            TransitionManager.beginDelayedTransition(constraintLayout)
                            group1.visibility = View.VISIBLE
                        },
                        { error -> Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show() }
                )

        var inverseStop = if (switchDirection.isChecked) listStop1.find { it.StopSequence == chosenStop.StopSequence } else listStop2.find { it.StopSequence == chosenStop.StopSequence }

        if (inverseStop != null)
            disposable = tramApiServe.getNextPredictedRoutesCollection(inverseStop!!.StopNo, chosenRoute.InternalRouteNo, false, aid, 2, token)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { result ->
                                listTime2 = result.responseObject
                                recyclerView2.layoutManager = LinearLayoutManager(baseContext)
                                recyclerView2.adapter = TramAdapter(listTime2, baseContext)
                                TransitionManager.beginDelayedTransition(constraintLayout)
                                group2.visibility = View.VISIBLE
                            },
                            { /*error -> Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show()*/ }
                    )


    }
}
