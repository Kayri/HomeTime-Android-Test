package com.kayri.hometime.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.kayri.hometime.R
import com.kayri.hometime.models.Stop
import io.realm.RealmList

class StopsSpinAdapter(val context: Context, val items: RealmList<Stop>) : BaseAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val vh: ItemRowHolder
        if (convertView == null) {
            view = inflater.inflate(R.layout.item_spin_tram_stop, parent, false)
            vh = ItemRowHolder(view)
            view?.tag = vh
        } else {
            view = convertView
            vh = view.tag as ItemRowHolder
        }

        vh.flag.text = items[position]?.FlagStopNo
        vh.stopName.text = items[position]?.StopName

        return view
    }

    override fun getItem(position: Int): Any? {
        return null

    }

    override fun getItemId(position: Int): Long {
        return 0

    }

    override fun getCount(): Int {
        return items.size
    }

    private class ItemRowHolder(row: View?) {

        val flag: TextView = row?.findViewById(R.id.flagStopNo) as TextView
        val stopName: TextView = row?.findViewById(R.id.stopName) as TextView

    }
}