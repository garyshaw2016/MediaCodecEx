package com.milimili.mediacodecex.mediamuxer_ex;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.milimili.mediacodecex.R;

public class MuxActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mux);
        DecodeEncodeTest test = new DecodeEncodeTest();
        test.runTest();
    }
}
