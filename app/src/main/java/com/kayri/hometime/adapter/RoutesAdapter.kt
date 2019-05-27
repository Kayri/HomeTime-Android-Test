package com.kayri.hometime.adapter

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.kayri.hometime.R
import com.kayri.hometime.models.Route
import io.realm.RealmResults
import kotlinx.android.synthetic.main.item_row_tram_route.view.*


class RoutesAdapter : RecyclerView.Adapter<RoutesAdapter.ViewHolder>() {

    var items: RealmResults<Route>? = null
    var tracker: SelectionTracker<Long>? = null

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_row_tram_route, parent, false))
    }

    override fun getItemCount(): Int {
        return items!!.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        tracker?.let {
            holder.bind(items?.get(position), it.isSelected(position.toLong()))
        }
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val noTextView = view.no_textView
        val descTextView = view.desc_textView

        fun bind(item: Route?, isActivated: Boolean = false) {
            noTextView.text = item!!.RouteNo
            descTextView.text = item!!.Description
            itemView.isActivated = isActivated
        }


        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
                object : ItemDetailsLookup.ItemDetails<Long>() {
                    override fun getPosition(): Int = adapterPosition
                    override fun getSelectionKey(): Long? = itemId
                    override fun inSelectionHotspot(e: MotionEvent): Boolean {
                        return true
                    }

                    override fun inDragRegion(e: MotionEvent): Boolean {
                        return true
                    }
                }
    }



}

class RoutesItemDetailsLookup(private val recyclerView: RecyclerView) :
        ItemDetailsLookup<Long>() {
    override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(event.x, event.y)
        if (view != null) {
            return (recyclerView.getChildViewHolder(view) as RoutesAdapter.ViewHolder)
                    .getItemDetails()
        }
        return null
    }
}
