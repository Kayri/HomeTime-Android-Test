package com.kayri.hometime

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
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

        val animBackground = ObjectAnimator.ofInt(constraintSplash, "backgroundColor", resources.getColor(R.color.colorSplash), resources.getColor(R.color.colorPrimary)).setDuration(800)
        animBackground.setEvaluator(ArgbEvaluator())
        animBackground.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                val i = Intent(baseContext, MainActivity::class.java)
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this@SplashActivity, textView, getString(R.string.app_name)
                )
                startActivity(i, options.toBundle())
            }

        })

        animTranslateUp.setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                animBackground.start()

            }

            override fun onAnimationStart(animation: Animation?) {
                textView.startAnimation(animTranslateLeft)

            }
        })

        imageView.startAnimation(animTranslateUp)

    }

}
