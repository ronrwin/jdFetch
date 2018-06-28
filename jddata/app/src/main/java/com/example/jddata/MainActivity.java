package com.example.jddata;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.jddata.service.AccService;
import com.example.jddata.service.Action;
import com.example.jddata.service.ActionMachine;
import com.example.jddata.shelldroid.Env;
import com.example.jddata.shelldroid.EnvManager;
import com.example.jddata.shelldroid.ListAppActivity;
import com.example.jddata.util.OpenAccessibilitySettingHelper;
import com.example.jddata.util.ScreenUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends Activity{

    @BindView(R.id.create10)
    Button create10;
    @BindView(R.id.shelldroid)
    Button shelldroid;
    @BindView(R.id.open_setting)
    Button openSetting;
    @BindView(R.id.cart)
    Button cart;
    @BindView(R.id.main)
    Button main;
    @BindView(R.id.leaderboard)
    Button leaderboard;
    @BindView(R.id.jd_kill)
    Button jdKill;
    @BindView(R.id.worth_buy)
    Button worthBuy;
    @BindView(R.id.nice_buy)
    Button niceBuy;
    @BindView(R.id.brand_kill)
    Button brandKill;
    @BindView(R.id.type_kill)
    Button typeKill;
    @BindView(R.id.screenshot)
    Button screenshot;
    @BindView(R.id.dmp)
    Button dmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createMachine(Action.SEARCH_HAIFEISI);
                MainApplication.startMainJD();
            }
        });

        openSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenAccessibilitySettingHelper.jumpToSettingPage(MainActivity.this);// 跳转到开启页面
            }
        });

        main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                createMachine(Action.HOME);
//                MainApplication.startMainJD();

                BusHandler.getInstance().mAction = Action.HOME;
                BusHandler.getInstance().mTaskId = 0;
                BusHandler.getInstance().start();
            }
        });

        dmp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createMachine(Action.DMP);
                MainApplication.startMainJD();
            }
        });

        niceBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createMachine(Action.NICE_BUY);
                MainApplication.startMainJD();
            }
        });

        cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createMachine(Action.CART);
                MainApplication.startMainJD();
            }
        });

        screenshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScreenUtils.scrrenShot();
            }
        });

        jdKill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createMachine(Action.JD_KILL);
                MainApplication.startMainJD();
            }
        });

        typeKill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createMachine(Action.TYPE_KILL);
                MainApplication.startMainJD();
            }
        });

        brandKill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createMachine(Action.BRAND_KILL);
                MainApplication.startMainJD();
            }
        });

        worthBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createMachine(Action.WORTH_BUY);
                MainApplication.startMainJD();
            }
        });

        leaderboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createMachine(Action.LEADERBOARD);
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
                HashMap<String, Env> map = new HashMap<>();
                for (Env env : envs) {
                    if (!map.containsKey(env.envName)) {
                        map.put(env.envName, env);
                    }
                }

                int index = 0;
                int createCount = 0;
                while (createCount < 10) {
                    if (map.containsKey("" + index)) {
                        index++;
                        continue;
                    } else {
                        EnvManager.envDirBuild(EnvManager.createJDApp(AccService.PACKAGE_NAME, "" + index));
                        index++;
                        createCount++;
                    }
                }
            }
        });
    }

    private void createMachine(String actionType) {
        BusHandler.getInstance().createMachine(actionType);
    }
}
