package com.example.jddata.shelldroid;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.jddata.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewActivity extends Activity{

    @BindView(R.id.btn)
    Button btn;
    @BindView(R.id.textName)
    EditText textName;
    @BindView(R.id.textPhoneModel)
    EditText textPhoneModel;
    @BindView(R.id.textPhoneBrand)
    EditText textPhoneBrand;
    @BindView(R.id.textImei)
    EditText textImei;
    @BindView(R.id.spinner)
    Spinner spinner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_layout);
        ButterKnife.bind(this);
        spinner.setAdapter(new SpinnerAdapter());

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppInfo appInfo = (AppInfo) spinner.getSelectedItem();
                Env env = new Env();
                env.id = java.util.UUID.randomUUID().toString();
                env.envName = textName.getText().toString();
                env.appName = appInfo.appName;
                env.pkgName = appInfo.pkgName;
                env.active = false;
                env.deviceId = textImei.getText().toString();
                env.buildModel = textPhoneModel.getText().toString();
                env.buildManufacturer = textPhoneBrand.getText().toString();
                env.buildBrand = textPhoneBrand.getText().toString();
                save(env);
                quit();
            }
        });
    }

    public void save(Env env) {
        Log.d("zfr", "Save env: "+env);
        EnvManager.envDirBuild(env);
    }

    public void quit() {
        setResult(0);
        super.finish();
    }

}
