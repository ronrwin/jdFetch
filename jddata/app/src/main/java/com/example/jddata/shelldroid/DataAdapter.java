package com.example.jddata.shelldroid;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jddata.MainApplication;
import com.example.jddata.R;

import java.util.ArrayList;

public class DataAdapter extends BaseAdapter{

    public ArrayList<Env> envs = EnvManager.scanEnvs();

    @Override
    public int getCount() {
        return envs.size();
    }

    @Override
    public Object getItem(int position) {
        return envs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = LayoutInflater.from(MainApplication.getContext()).inflate(R.layout.card, parent, false);
        final ViewHolder vh;
        if (convertView == null) {
            vh = new ViewHolder(v);
            convertView = v;
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        final Env env = envs.get(position);
        if (env != null) {
            Drawable drawable = AndroidUtils.getIcon(env.pkgName);
            if (drawable != null) {
                vh.ivIcon.setImageDrawable(AndroidUtils.getIcon(env.pkgName));
            }
            vh.textName.setText(env.envName);
            vh.textAppName.setText(env.appName);
            vh.textImei.setText(env.deviceId);

            vh.btn.setText("Delete");
            vh.btn.setTag(env);
            vh.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EnvManager.delete((Env)v.getTag());
                    envs = EnvManager.scanEnvs();
                    notifyDataSetChanged();
                }
            });

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(EnvManager.TAG, "env clicked!: " + env);
                    EnvManager.active(env);
                }
            });
        }
        return convertView;
    }

    public class ViewHolder {
        ImageView ivIcon;
        TextView textName;
        TextView textAppName;
        TextView textImei;
        Button btn;

        public ViewHolder(View v) {
            ivIcon = v.findViewById(R.id.icon);
            textName = v.findViewById(R.id.envName);
            textAppName = v.findViewById(R.id.appName);
            textImei = v.findViewById(R.id.imei);
            btn = v.findViewById(R.id.my_button);
        }
    }
}
