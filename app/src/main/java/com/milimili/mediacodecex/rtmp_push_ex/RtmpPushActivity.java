package com.milimili.mediacodecex.rtmp_push_ex;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;

import com.milimili.mediacodecex.R;

public class RtmpPushActivity extends AppCompatActivity {

    private SurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtmp_push);
        surfaceView = (SurfaceView) findViewById(R.id.previewSurfaceView);
    }
}
