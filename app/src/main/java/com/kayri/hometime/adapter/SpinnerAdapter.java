package com.kayri.hometime.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.kayri.hometime.R;
import com.kayri.hometime.models.RouteStopsByRoute;

import java.util.List;

//TODO Refactor kt
public class SpinnerAdapter extends ArrayAdapter {
    private Context context;
    private int resource;
    private List<RouteStopsByRoute> list;

    public SpinnerAdapter(@NonNull Context context, int resource, @NonNull List objects) {
        super(context, resource, objects);

        this.context = context;
        this.resource = resource;
        list = objects;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView,
                                @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @Override
    public @NonNull
    View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    private View createItemView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(resource, parent, false);
        TextView tvSpinner = view.findViewById(R.id.spinTextView);
        tvSpinner.setText(list.get(position).getStopName() + " - " + list.get(position).getSuburbName());
        return view;
    }
}