package com.milimili.mediacodecex.audio_record_ex;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.milimili.mediacodecex.R;

public class AudioEx_Activity extends AppCompatActivity {
    private static final String TAG = "AudioEx_Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_ex_);
        Button start = (Button) findViewById(R.id.btn_start_record);
        Button stop = (Button) findViewById(R.id.btn_stop_record);

        final AudioRecordEx th = new AudioRecordEx();
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                th.start();
                Log.i(TAG,"开始录制");
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    th.stopRecord();
                    Log.i(TAG,"结束录制");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
