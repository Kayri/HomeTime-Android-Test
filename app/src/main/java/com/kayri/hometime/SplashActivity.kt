package com.kayri.hometime

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_splash.*
import pl.droidsonroids.gif.AnimationListener
import pl.droidsonroids.gif.GifDrawable

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val gifFromResource = GifDrawable(resources, R.raw.bg_train_journey)

        gifView.setImageDrawable(gifFromResource)

        val animFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        animFadeIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                val i = Intent(baseContext, MainActivity::class.java)
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this@SplashActivity, titleView, getString(R.string.app_name)
                )
                startActivity(i, options.toBundle())

            }

            override fun onAnimationStart(animation: Animation?) {

            }
        })
        animFadeIn.duration = gifFromResource.duration.toLong()
        titleView.startAnimation(animFadeIn)

        //gifFromResource.addAnimationListener {  }

        //TODO save / update routes on sqldb

        //TODO GetDeviceToken api

        //TODO Error Connection

        //TODO SharedPreferences ?



    }

}
