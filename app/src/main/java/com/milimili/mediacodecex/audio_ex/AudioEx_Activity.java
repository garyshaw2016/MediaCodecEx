package com.milimili.mediacodecex.audio_ex;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.milimili.mediacodecex.R;

/**
 * 1.使用AudioRecord录制声音并将PCM流存储到本地
 * 2.使用MediaCodec编码原始PCM流并保存
 * 3.录制并保存为wav类型的文件并播放
 */
public class AudioEx_Activity extends AppCompatActivity {

    private boolean isRecording;
    private boolean isPlaying;

    private static final String TAG = "AudioEx_Activity";
    private Button record;
    private Button play;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_ex_);
        record = (Button) findViewById(R.id.btn_record);
        play = (Button) findViewById(R.id.btn_play);

        final AudioRecord_ th = new AudioRecord_();
        final AudioPlayback audioPlayback = new AudioPlayback();

        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording) {
                    Toast.makeText(AudioEx_Activity.this, "开始了", Toast.LENGTH_SHORT).show();
                    record.setText("停止录制WAV文件");
                    th.startRecord();
                } else {
                    try {
                        Toast.makeText(AudioEx_Activity.this, "停止了", Toast.LENGTH_SHORT).show();
                        record.setText("开始录制WAV文件");
                        th.stopRecord();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                isRecording = !isRecording;
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlaying) {
                    play.setText("停止播放");
                    audioPlayback.stopPlayBack();
                    Toast.makeText(AudioEx_Activity.this, "播放停止", Toast.LENGTH_SHORT).show();
                } else {
                    play.setText("开始播放");
                    audioPlayback.startPlayback();
                    Toast.makeText(AudioEx_Activity.this, "播放开始", Toast.LENGTH_SHORT).show();
                }
                isPlaying = !isPlaying;
            }
        });
    }
}
