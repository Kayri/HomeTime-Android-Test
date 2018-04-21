package com.kayri.hometime

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import com.kayri.hometime.utils.TramApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var disposable: Disposable? = null

    private val tramApiServe by lazy {
        TramApiService.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        refreshbtn.setOnClickListener {
            beginRefresh(listview)
        }

    }

    private fun beginRefresh(listView: ListView?) {

        disposable = tramApiServe.getRouteStopsByRoute(30,"TTIOSJSON","4e684966-2ba5-44c4-b643-86e97040d36c")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { result ->
                            val listItems = arrayOfNulls<String>(result.responseObject.size)
                            for (i in 0 until result.responseObject.size) {
                                val data = result.responseObject[i]
                                listItems[i] = data.StopName + " - " + data.StopNo
                            }
                            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listItems)
                            listview.adapter = adapter
                        },
                        { error -> Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show() }
                )
    }
}
