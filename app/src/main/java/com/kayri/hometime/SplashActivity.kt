package com.kayri.hometime

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.app.AppCompatActivity
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


class SplashActivity : AppCompatActivity() {

    private lateinit var disposable: Disposable
    lateinit var tramApiService: TramApiService
    lateinit var token: String
    lateinit var realm: Realm
    lateinit var routesList: RealmResults<Route>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

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
        getDeviceToken()


        val gifFromResource = GifDrawable(resources, R.raw.bg_train_journey)
        gifView.setImageDrawable(gifFromResource)

        val animFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        animFadeIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                titleView.visibility = View.VISIBLE
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
                val i = Intent(baseContext, MainActivity::class.java)
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this@SplashActivity, titleView, getString(R.string.app_name)
                )
                startActivity(i, options.toBundle())
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

}
