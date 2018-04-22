package com.kayri.hometime

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.kayri.hometime.adapter.TramAdapter
import com.kayri.hometime.models.ListOfStopsByRouteNoAndDirectionInfoChild
import com.kayri.hometime.models.NextPredictedRoutesCollectionChild
import com.kayri.hometime.models.RouteSummariesChild
import com.kayri.hometime.utils.TramApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var disposable: Disposable? = null
    private lateinit var token: String
    private val aid: String = "TTIOSJSON"
    private lateinit var choosenRoute: RouteSummariesChild
    private lateinit var listRoute: List<RouteSummariesChild>
    private lateinit var choosenStop: ListOfStopsByRouteNoAndDirectionInfoChild
    private lateinit var listStop: List<ListOfStopsByRouteNoAndDirectionInfoChild>
    private lateinit var listTime1: List<NextPredictedRoutesCollectionChild>
    private lateinit var listTime2: List<NextPredictedRoutesCollectionChild>
    private val tramApiServe = TramApiService.create()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getNewDeviceToken()

        spin_route.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //Toast.makeText(parent!!.context, listRoute[position].RouteNo, Toast.LENGTH_SHORT).show()
                choosenRoute = listRoute[position]
                direction1.text = choosenRoute.DownDestination
                direction2.text = choosenRoute.UpDestination

                if (switchDirection.isChecked) {
                    direction4.text = choosenRoute.DownDestination
                    direction3.text = choosenRoute.UpDestination
                }
                else{
                    direction3.text = choosenRoute.DownDestination
                    direction4.text = choosenRoute.UpDestination
                }

                getStopList(choosenRoute.InternalRouteNo)

            }

        }

        spin_stop.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //Toast.makeText(parent!!.context, listStop[position].StopNo.toString(), Toast.LENGTH_SHORT).show()
                choosenStop = listStop[position]
                getTimeByRoutAndStop(choosenRoute, choosenStop)

            }
        }

        //TODO Change for Arrow display (ToggleButton ? )
        switchDirection.setOnCheckedChangeListener { buttonView, isChecked ->
            //TODO Refactor
            if (switchDirection.isChecked) {
                direction4.text = choosenRoute.DownDestination
                direction3.text = choosenRoute.UpDestination
            }
            else{
                direction3.text = choosenRoute.DownDestination
                direction4.text = choosenRoute.UpDestination
            }
            getStopList(choosenRoute.InternalRouteNo)
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
                            Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
                            Log.d("Error", error.message)
                        }
                )
    }

    private fun getRouteList() {
        disposable = tramApiServe.getRouteSummaries(aid, token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { result ->
                            listRoute = result.responseObject
                            val listItems = arrayOfNulls<String>(result.responseObject.size)
                            for (i in 0 until listRoute.size) {
                                listItems[i] = listRoute[i].RouteNo + " - " + listRoute[i].Description
                            }
                            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listItems)
                            spin_route.adapter = adapter
                        },
                        { error -> Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show() }
                )
    }

    private fun getStopList(internalRouteNo: Int) {

        disposable = tramApiServe.getListOfStopsByRouteNoAndDirection(internalRouteNo, switchDirection.isChecked, aid, "4e684966-2ba5-44c4-b643-86e97040d36c")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { result ->

                            //TODO Map sort by
                            listStop = result.responseObject
                            val listItems = arrayOfNulls<String>(result.responseObject.size)
                            for (i in 0 until listStop.size) {
                                listItems[i] = listStop[i].Name + " - " + listStop[i].SuburbName
                            }
                            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listItems)
                            spin_stop.adapter = adapter
                        },
                        { error -> Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show() }
                )
    }

    private fun getTimeByRoutAndStop(choosenRoute: RouteSummariesChild, choosenStop: ListOfStopsByRouteNoAndDirectionInfoChild) {
        recyclerView1.adapter = null
        recyclerView2.adapter = null

        disposable = tramApiServe.getNextPredictedRoutesCollection(choosenStop.StopNo, choosenRoute.InternalRouteNo, false, aid, 2, "4e684966-2ba5-44c4-b643-86e97040d36c")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { result ->
                            listTime1 = result.responseObject
                            recyclerView1.layoutManager = LinearLayoutManager(baseContext)
                            recyclerView1.adapter = TramAdapter(listTime1, baseContext)
                        },
                        { error -> Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show() }
                )
        disposable = tramApiServe.getNextPredictedRoutesCollection(choosenStop.StopNo, choosenRoute.InternalRouteNo, true, aid, 2, "4e684966-2ba5-44c4-b643-86e97040d36c")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { result ->
                            listTime2 = result.responseObject
                            recyclerView2.layoutManager = LinearLayoutManager(baseContext)
                            recyclerView2.adapter = TramAdapter(listTime2, baseContext)
                        },
                        { error -> Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show() }
                )


    }
}
