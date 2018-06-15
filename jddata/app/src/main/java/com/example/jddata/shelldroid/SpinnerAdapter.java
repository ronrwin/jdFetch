package com.example.jddata.shelldroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jddata.R;

import java.util.ArrayList;

public class SpinnerAdapter extends BaseAdapter {

    public ArrayList<AppInfo> data = AndroidUtils.getInstalledAppInfo();

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.spinner_item, parent, false);
        if(v != null) {
            ImageView iv = v.findViewById(R.id.itemIcon);
            TextView tv = v.findViewById(R.id.itemName);
            AppInfo app = data.get(position);
            iv.setImageDrawable(app.icon);
            tv.setText(app.appName);
        }
        return v;
    }
}
