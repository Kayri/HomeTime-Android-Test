package com.kayri.hometime

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.support.constraint.ConstraintSet
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.RotateAnimation
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
    private var chosenRoute: RouteSummaries? = null
    private var listRoute: MutableList<RouteSummaries> = mutableListOf()
    private var chosenStop: RouteStopsByRoute? = null
    private var listStop1: MutableList<RouteStopsByRoute> = mutableListOf()
    private var listStop2: MutableList<RouteStopsByRoute> = mutableListOf()
    private lateinit var listTime1: List<NextPredictedRoutesCollection>
    private lateinit var listTime2: List<NextPredictedRoutesCollection>
    private val tramApiServe = TramApiService.create()
    private var isChecked: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val animationDrawable = constraintLayout.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(1500)
        animationDrawable.setExitFadeDuration(3000)
        animationDrawable.start()

        getNewDeviceToken()

        spin_route.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                TransitionManager.beginDelayedTransition(constraintLayout)
                chosenRoute = listRoute[position]
                direction1.text = chosenRoute?.DownDestination
                direction2.text = chosenRoute?.UpDestination

                getStopList(chosenRoute!!.InternalRouteNo)

            }

        }

        spin_stop.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                chosenStop = if (isChecked) listStop2[position] else listStop1[position]
                getTimeByRoutAndStop(chosenRoute!!, chosenStop!!)

            }
        }

        val constraintSet1 = ConstraintSet()
        constraintSet1.clone(constraintLayout)
        val constraintSet2 = ConstraintSet()
        constraintSet2.clone(this, R.layout.activity_main_alt)
        var changed = false

        switchDirection.setOnClickListener { buttonView ->
            isChecked = !isChecked
            buttonView.animate()
                    .rotation(360f)
                    .withLayer()
                    .setDuration(500)
                    .setInterpolator(AnticipateOvershootInterpolator(2f))
                    .withEndAction {

                        val transition = ChangeBounds()
                        transition.interpolator = AccelerateDecelerateInterpolator()
                        transition.duration = 1200
                        TransitionManager.beginDelayedTransition(constraintLayout, transition)

                        val constraint = if (changed) constraintSet1 else constraintSet2
                        constraint.applyTo(constraintLayout)

                        changed = !changed
                        spin_stop.adapter = if (isChecked) SpinnerAdapter(this, R.layout.layout_spinner_item, listStop2)
                        else SpinnerAdapter(this, R.layout.layout_spinner_item, listStop1)

                    }
                    .start()
        }

        fab_refresh.setOnClickListener { fabView ->
            fabView.clearAnimation()
            val anim = RotateAnimation(0f, -1080f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                    0.5f)
            anim.duration = 500
            anim.interpolator = AccelerateDecelerateInterpolator()
            anim.setAnimationListener(object: Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                    if (chosenRoute != null && chosenStop != null)
                        getTimeByRoutAndStop(chosenRoute!!, chosenStop!!)
                    else
                        getNewDeviceToken()
                }

                override fun onAnimationStart(animation: Animation?) {
                }

            })
            fabView.startAnimation(anim)
        }


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
                            val adapter = ArrayAdapter(this, R.layout.layout_spinner_item, R.id.spinTextView, listItems)
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
                            val adapter = if (isChecked) SpinnerAdapter(this, R.layout.layout_spinner_item, listStop2)
                            else SpinnerAdapter(this, R.layout.layout_spinner_item, listStop1)
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
    }

    override fun onBackPressed() {
        super.onBackPressed()
        //TransitionName view stay when activity close
        finish()
    }
}