package com.example.jddata;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import com.example.jddata.shelldroid.Env;
import com.example.jddata.shelldroid.EnvManager;
import com.example.jddata.shelldroid.ListAppActivity;
import com.example.jddata.shelldroid.NewActivity;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends Activity{

    @BindView(R.id.create10)
    Button create10;
    @BindView(R.id.shelldroid)
    Button shelldroid;
    @BindView(R.id.open_setting)
    Button openSetting;
    @BindView(R.id.goods_buy)
    Button goodsBuy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Action action = new Action();
                action.actionType = Action.SEARCH;
                ActionMachine machine = new ActionMachine(action);
                MainHandler.getInstance().mCurrentMachine = machine;

                MainApplication.startMainJD();
            }
        });

        openSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenAccessibilitySettingHelper.jumpToSettingPage(MainActivity.this);// 跳转到开启页面
            }
        });

        goodsBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Action action = new Action();
                action.actionType = Action.BUY_GOODS;
                ActionMachine machine = new ActionMachine(action);
                MainHandler.getInstance().mCurrentMachine = machine;

                MainApplication.startMainJD();
            }
        });

        shelldroid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListAppActivity.class);
                startActivity(intent);
            }
        });
        create10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Env> envs = EnvManager.scanEnvs();
                int max = 0;
                for (Env env : envs) {
                    int index = Integer.parseInt(env.envName);
                    max = index > max ? index : max;
                }
                for (int i = 0; i < 10; i++) {
                    EnvManager.envDirBuild(EnvManager.createJDApp(AccService.PACKAGE_NAME, "" + (max+1+i)));
                }
            }
        });
    }
}
