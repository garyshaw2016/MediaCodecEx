package com.milimili.mediacodecex;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.milimili.mediacodecex.audio_ex.AudioEx_Activity;
import com.milimili.mediacodecex.camera_ex.CameraExActivity;
import com.milimili.mediacodecex.decoder_ex.DecodeExActivity;
import com.milimili.mediacodecex.encoder2_ex.EncodeEx2_Activity;
import com.milimili.mediacodecex.encoder_ex.EncodeEx_Activity;
import com.milimili.mediacodecex.mediamuxer_ex.MuxActivity;
import com.milimili.mediacodecex.rtmp_push_ex.RtmpPushActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_camera_exercise).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,CameraExActivity.class));
            }
        });
        findViewById(R.id.btn_decode_exercise).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DecodeExActivity.class));
            }
        });
        findViewById(R.id.btn_encode_exercise).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, EncodeEx_Activity.class));
            }
        });
        findViewById(R.id.btn_encode2_exercise).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, EncodeEx2_Activity.class));
            }
        });
        findViewById(R.id.btn_mux).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,MuxActivity.class));
            }
        });
        findViewById(R.id.btn_audio_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AudioEx_Activity.class));
            }
        });

        findViewById(R.id.btn_rtmp_push).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RtmpPushActivity.class));
            }
        });


    }
}
