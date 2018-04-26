package com.kayri.hometime

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        val animTranslateUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        val animTranslateLeft = AnimationUtils.loadAnimation(this, R.anim.slide_left)

        animTranslateUp.setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {

                val i = Intent(baseContext, MainActivity::class.java)
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this@SplashActivity, textView, getString(R.string.app_name)
                )
                startActivity(i, options.toBundle())
            }

            override fun onAnimationStart(animation: Animation?) {
                textView.startAnimation(animTranslateLeft)
            }
        })

        imageView.startAnimation(animTranslateUp)

    }

}
