package com.kayri.hometime.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kayri.hometime.R
import com.kayri.hometime.models.NextPredictedRoutesCollection
import kotlinx.android.synthetic.main.layout_tram_info.view.*
import java.text.SimpleDateFormat
import java.util.*


class TramAdapter(val items: List<NextPredictedRoutesCollection>, val context: Context?) : RecyclerView.Adapter<ViewHolder>() {

    val dateFormatter = SimpleDateFormat("E dd hh:mm a")
    val timeFormatter = SimpleDateFormat("HH:mm")


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_tram_info, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        var string = items[position].PredictedArrivalDateTime
        var startIndex = string.indexOf("(")
        var endIndex = string.indexOf("+")

        val millisecond: String = if (startIndex == -1 || endIndex == -1) "" else string.substring(startIndex + 1, endIndex)

        val currentDateTime = Calendar.getInstance()
        Log.d("TIME", currentDateTime.timeInMillis.toString())
        val diffTime = millisecond.toLong().minus(currentDateTime.timeInMillis)
        //currentDateTime.timeZone = TimeZone
        Log.d("DIFF", diffTime.toString())
        timeFormatter.timeZone = TimeZone.getTimeZone("Melbourne")

        holder.tvTime.text = dateFormatter.format(Date(millisecond.toLong())).toString()
        holder.tvTimeLeft.text = "( " + timeFormatter.format(Date(diffTime)).toString() + " )"
        holder.tvVehicle.text = items[position].VehicleNo.toString()
    }
}

class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val tvTime = view.time
    val tvTimeLeft = view.time_left
    val tvVehicle = view.vehicle
}