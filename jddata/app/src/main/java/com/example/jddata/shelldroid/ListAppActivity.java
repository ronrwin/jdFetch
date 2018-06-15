package com.example.jddata.shelldroid;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;


import com.example.jddata.MainActivity;
import com.example.jddata.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ListAppActivity extends Activity{

    @BindView(R.id.rv)
    ListView listview;

    @BindView(R.id.add)
    Button add;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_layout);
        ButterKnife.bind(this);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListAppActivity.this, NewActivity.class);
                startActivityForResult(intent, 0);
            }
        });
    }

    @Override
    protected void onResume() {
        listview.setAdapter(new DataAdapter());
        super.onResume();
    }
}
