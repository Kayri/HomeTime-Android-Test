package com.kayri.hometime.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.kayri.hometime.R
import com.kayri.hometime.models.Route
import io.realm.RealmResults

class RoutesListAdapter(val context: Context, val items: RealmResults<Route>) : BaseAdapter() {

    //TODO merge RouteAdapter and StopAdapter

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val vh: ItemRowHolder
        if (convertView == null) {
            view = inflater.inflate(R.layout.item_backdrop_route, parent, false)
            vh = ItemRowHolder(view)
            view?.tag = vh
        } else {
            view = convertView
            vh = view.tag as ItemRowHolder
        }


        vh.number.text = items[position]?.RouteNo
        vh.description.text = items[position]?.Description

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

        val number: TextView = row?.findViewById(R.id.no_textView) as TextView
        val description: TextView = row?.findViewById(R.id.desc_textView) as TextView

    }

}