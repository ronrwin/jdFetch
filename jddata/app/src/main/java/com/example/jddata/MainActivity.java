package com.example.jddata;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                try {
//                    Intent startIntent = new Intent();
//                    startIntent.setAction(Intent.ACTION_VIEW);
//                    startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startIntent.setClassName("com.jingdong.app.mall", "com.jd.lib.search.view.Activity.SearchActivity");
//
//                    startActivity(startIntent);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

                Action action = new Action();
                action.actionType = "1";
                ActionMachine machine = new ActionMachine(action);
                MainHandler.getInstance().mCurrentMachine = machine;
            }
        });

        findViewById(R.id.open_setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenAccessibilitySettingHelper.jumpToSettingPage(MainActivity.this);// 跳转到开启页面
            }
        });
    }
}
