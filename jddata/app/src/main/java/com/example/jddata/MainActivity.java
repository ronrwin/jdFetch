package com.example.jddata;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.jddata.service.AccService;
import com.example.jddata.service.Action;
import com.example.jddata.shelldroid.Env;
import com.example.jddata.shelldroid.EnvManager;
import com.example.jddata.shelldroid.ListAppActivity;
import com.example.jddata.shelldroid.Location;
import com.example.jddata.util.FileUtils;
import com.example.jddata.util.OpenAccessibilitySettingHelper;
import com.example.jddata.util.ScreenUtils;

import java.util.ArrayList;
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
    @BindView(R.id.is_test)
    CheckBox isTest;
    @BindView(R.id.search)
    Button search;
    @BindView(R.id.one_env)
    EditText oneEnv;
    @BindView(R.id.search_text)
    EditText searchText;
    @BindView(R.id.city_spinner)
    Spinner citySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        isTest.setChecked(MainApplication.sIsTest);
        isTest.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainApplication.sIsTest = isChecked;
            }
        });

        openSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenAccessibilitySettingHelper.jumpToSettingPage(MainActivity.this);// 跳转到开启页面
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainApplication.sSearchText = searchText.getText().toString();
                doAction(Action.SEARCH);
            }
        });
        main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doAction(Action.HOME);
            }
        });
        dmp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doAction(Action.DMP);
            }
        });
        niceBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doAction(Action.NICE_BUY);
            }
        });
        cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doAction(Action.CART);
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
                doAction(Action.JD_KILL);
            }
        });
        typeKill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doAction(Action.TYPE_KILL);
            }
        });
        brandKill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doAction(Action.BRAND_KILL);
            }
        });
        worthBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doAction(Action.WORTH_BUY);
            }
        });
        leaderboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doAction(Action.LEADERBOARD);
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

        MainApplication.sSelectLocation = MainApplication.sLocations[0];
        FileUtils.writeToFile(Environment.getExternalStorageDirectory().getAbsolutePath(), "location", MainApplication.sSelectLocation.latitude + "," + MainApplication.sSelectLocation.longitude);

        citySpinner.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return MainApplication.sLocations.length;
            }

            @Override
            public Object getItem(int position) {
                return MainApplication.sLocations[position];
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = new TextView(MainActivity.this);
                textView.setText(MainApplication.sLocations[position].name);
                return textView;
            }
        });

        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                MainApplication.sSelectLocation = MainApplication.sLocations[position];
                FileUtils.writeToFile(Environment.getExternalStorageDirectory().getAbsolutePath(), "location", MainApplication.sSelectLocation.latitude + "," + MainApplication.sSelectLocation.longitude);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void doAction(String action) {
        MainApplication.sTargetEnvName = oneEnv.getText().toString();
        if (TextUtils.isEmpty(MainApplication.sTargetEnvName)) {
            if (MainApplication.sIsTest) {
                createMachine(action);
                MainApplication.startMainJD();
            } else {
                BusHandler.getInstance().mAction = action;
                BusHandler.getInstance().mTaskId = 0;
                BusHandler.getInstance().start();
            }
        } else {
            createMachine(action);
            EnvManager.activeByName(MainApplication.sTargetEnvName);
        }

    }

    private void createMachine(String actionType) {
        BusHandler.getInstance().createMachine(actionType);
    }
}
