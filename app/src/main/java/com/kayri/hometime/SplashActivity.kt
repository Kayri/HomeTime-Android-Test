package com.kayri.hometime

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.core.app.ActivityOptionsCompat
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.kayri.hometime.models.Route
import com.kayri.hometime.models.Stop
import com.kayri.hometime.utils.TramApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_splash.*
import pl.droidsonroids.gif.GifDrawable
import java.text.SimpleDateFormat
import java.util.*

const val EXTRA_TOKEN = "com.HomeTime.EXTRA_TOKEN"

class SplashActivity : AppCompatActivity() {
    lateinit  var sharedPref: SharedPreferences

    private lateinit var disposable: Disposable
    private lateinit var tramApiService: TramApiService
    lateinit var token: String
    lateinit var realm: Realm
    lateinit var routesList: RealmResults<Route>
    var skipDownload = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        sharedPref = getSharedPreferences(getString(R.string.preference_file_key),Context.MODE_PRIVATE)

        disposable = CompositeDisposable()
        tramApiService = TramApiService.create()

        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
                .name(BuildConfig.DATABASE_NAME)
                .schemaVersion(BuildConfig.DATABASE_VERSION.toLong())
                .deleteRealmIfMigrationNeeded()
                .build()
        realm = Realm.getInstance(realmConfig)


        //TODO Skip if DB full and Token in SharedPreferences
        routesList = realm.where<Route>().findAll()
        if (routesList.isEmpty() || sharedPref.getString(EXTRA_TOKEN,"") == "")
            getDeviceToken()
        else  skipDownload = true


        val gifFromResource = GifDrawable(resources, R.raw.bg_train_journey)
        gifView.setImageDrawable(gifFromResource)

        val animFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        animFadeIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                titleView.visibility = View.VISIBLE
                if (skipDownload){
                    startActivity()
                }

            }

            override fun onAnimationStart(animation: Animation?) {

            }
        })
        animFadeIn.duration = gifFromResource.duration.toLong()
        titleView.startAnimation(animFadeIn)


        val date = SimpleDateFormat("E dd hh:mm a").format(Date(1557905148440))
        Toast.makeText(this, date.toString(), Toast.LENGTH_LONG).show()
        Log.d("DATE", date.toString())
        gifFromResource.addAnimationListener {
            //TODO find another way as listener
            if (disposable.isDisposed && !routesList.isEmpty()) {
                startActivity()
            }
            //Log.d("DISPO", "DISP: ${disposable?.isDisposed}")
        }


    }


    // GetDeviceToken api
    fun getDeviceToken() {
        disposable = (tramApiService.getDeviceToken(BuildConfig.API_KEY_AID, BuildConfig.API_KEY_DEVINFO)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { result ->
                            token = result.responseObject[0].DeviceToken
                            sharedPref.edit().putString(EXTRA_TOKEN,token).apply()
                            getRouteSummaries()
                        }, this::handleError))
    }

    fun getRouteSummaries() {
        disposable = (tramApiService.getRouteSummaries(BuildConfig.API_KEY_AID, token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->

                    //Save new Route object in sql db
                    //Sort Routes by Internal No
                    for (route in result.responseObject.sortedBy { it.InternalRouteNo }) {
                        if (realm.where<Route>().equalTo("InternalRouteNo", route.InternalRouteNo).findAll().isEmpty())
                            realm.executeTransaction {
                                val r = realm.createObject<Route>(route.InternalRouteNo)
                                r.RouteNo = route.RouteNo
                                r.Description = route.Description
                                r.DownDestination = route.DownDestination
                                r.UpDestination = route.UpDestination
                                getRouteStopsByRoute(r.InternalRouteNo, r)
                            }
                    }
                }, this::handleError))

    }

    fun getRouteStopsByRoute(internalRouteNo: Int, r: Route) {
        disposable = (tramApiService.getRouteStopsByRoute(internalRouteNo, BuildConfig.API_KEY_AID, token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { result ->

                            //Get or Save new Stop object in sql db
                            for (stop in result.responseObject) {
                                var stpTemp = realm.where<Stop>().equalTo("StopNo", stop.StopNo).findFirst()

                                if (stpTemp == null)
                                    realm.executeTransaction {
                                        val s = realm.createObject<Stop>(stop.StopNo)
                                        s.StopName = stop.StopName
                                        s.SuburbName = stop.SuburbName
                                        s.UpStop = stop.UpStop

                                        stpTemp = s
                                        //s.FlagStopNo = getStopInformation(stop.StopNo)
                                    }

                                //Update Stops List in Route Object
                                realm.executeTransaction {
                                    if (stop.UpStop)
                                        r.ListStopsUP.add(stpTemp)
                                    else
                                        r.ListStopsDown.add(stpTemp)

                                }
                            }
                        }, this::handleError))
    }

    //TODO Get Route 35
    /*private fun checkMissingRoute() {
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
    }*/


    /*
    fun getStopInformation(stopNo: Int): String {
        var flagStopNo = ""
        disposable?.add(tramApiService.getStopInformation(stopNo, BuildConfig.API_KEY_AID, token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { result ->
                            flagStopNo = result.responseObject[0].FlagStopNo
                        }, this::handleError))

        return flagStopNo
    }*/

    //Error Connection retry button
    private fun handleError(error: Throwable) {
        Log.d("ERROR", error.localizedMessage)
        Snackbar.make(findViewById(R.id.constraintSplash), "Error ${error.localizedMessage}", Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.retry) { getDeviceToken() }.show()
    }

    private fun startActivity(){
        val i = Intent(baseContext, MainActivity::class.java)
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this@SplashActivity, titleView, getString(R.string.app_name)
        )
        startActivity(i, options.toBundle())
    }

}
