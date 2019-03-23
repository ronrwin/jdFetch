package com.example.jddata.shelldroid;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.jddata.GlobalInfo;
import com.example.jddata.Location;
import com.example.jddata.MainApplication;

public class LocationSpinnerAdapter extends BaseAdapter {


    @Override
    public int getCount() {
        return GlobalInfo.sLocations.length;
    }

    @Override
    public Object getItem(int position) {
        return GlobalInfo.sLocations[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView tv = new TextView(MainApplication.sContext);
        Location location = GlobalInfo.sLocations[position];
        tv.setText(location.toString());
        tv.setTextColor(Color.BLACK);
        return tv;
    }
}
