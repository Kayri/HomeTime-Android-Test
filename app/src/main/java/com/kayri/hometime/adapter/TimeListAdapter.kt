package com.kayri.hometime.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kayri.hometime.R
import com.kayri.hometime.models.NextPredictedRoutesCollection
import java.text.SimpleDateFormat
import java.util.*

class TimeRecyclerAdapter(val context: Context) : RecyclerView.Adapter<TimeRecyclerAdapter.ViewHolder>() {

    var items: List<NextPredictedRoutesCollection>? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (!items.isNullOrEmpty()) {
            var timeMillisecond: String = items!![position].PredictedArrivalDateTime
            timeMillisecond = if (timeMillisecond.indexOf("(") == -1 || timeMillisecond.indexOf("+") == -1) "" else timeMillisecond.substring(timeMillisecond.indexOf("(") + 1, timeMillisecond.indexOf("+"))

            val currentDateTime = Calendar.getInstance()
            val timeLeft = timeMillisecond.toLong().minus(currentDateTime.timeInMillis)

            val dateFormatter = SimpleDateFormat("E dd hh:mm a", Locale("Melbourne"))
            val timeFormatter = SimpleDateFormat("HH:mm", Locale("Melbourne"))
            when (timeLeft) {
                in 0..60000 -> holder.timeLeft.text = "Now"
                in 60000..3600000 -> timeFormatter.applyPattern("mm")
                else -> timeFormatter.applyPattern("HH:mm")
            }
            timeFormatter.timeZone = TimeZone.getTimeZone("Melbourne")

            holder.time.text = dateFormatter.format(Date(timeMillisecond.toLong()))
            holder.timeLeft.text = "( ${timeFormatter.format(Date(timeLeft))} min)"
            holder.vehiculeNo.text = items!![position]?.VehicleNo.toString()

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_list_time, parent, false))
    }

    override fun getItemCount(): Int {
        return if (items.isNullOrEmpty()) 0 else items!!.size
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val time: TextView = view?.findViewById(R.id.time_textView) as TextView
        val timeLeft: TextView = view?.findViewById(R.id.timeLeft_textView) as TextView
        val vehiculeNo: TextView = view?.findViewById(R.id.vehicleNo_textView) as TextView

    }
}
