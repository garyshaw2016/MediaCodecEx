package com.milimili.mediacodecex.audio_record_ex;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.milimili.mediacodecex.utils.FileUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by luxiansheng on 16/12/16.
 * 1.使用AudioRecord录制声音并将PCM流存储到本地
 * 2.使用MediaCodec编码原始PCM流并保存
 */

public class AudioRecordEx extends Thread {
    private static final String TAG = "AudioRecordEx";
    private AudioRecord audioRecorder;

    private boolean isRecording = false;
    private int minBufferSize;
    private BufferedOutputStream bos;

    @Override
    public void run() {
        try {
            File file = FileUtil.createFile("test.pcm");
            bos = new BufferedOutputStream(new FileOutputStream(file));
            boolean isSucess = audioConfig();
            if (isSucess) {
                startRecord();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 音频采集器初始化
     *
     * @return isConfigSuccess 是否配置成功
     */
    private boolean audioConfig() throws Exception {
        if (isRecording) {
            throw new RuntimeException("mission is executing");
        }
        int audioSrc = MediaRecorder.AudioSource.MIC;//数据源
        int sampleRateInHz = 44100;//采样率 4000-192000
        int channelConfig = AudioFormat.CHANNEL_OUT_STEREO;//外放双声道
        int audioForamt = AudioFormat.ENCODING_PCM_16BIT;//（量化精度）位宽
        minBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioForamt);
        if (minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            throw new RuntimeException("error bad buffer size");
        }
        audioRecorder = new AudioRecord(audioSrc, sampleRateInHz,
                channelConfig, audioForamt, minBufferSize);
        if (audioRecorder.getState() == AudioRecord.STATE_UNINITIALIZED) {
            Log.e(TAG, "AudioRecord initialize fail !");
            return false;
        }

        return true;

    }

    public void startRecord() {
        audioRecorder.startRecording();
        byte[] minbuffers;
        while (!isInterrupted()) {
            minbuffers = new byte[minBufferSize];
            int ret = audioRecorder.read(minbuffers, 0, minBufferSize);
            if (ret == AudioRecord.ERROR_INVALID_OPERATION) {
                Log.e(TAG, "Error ERROR_INVALID_OPERATION");
            } else if (ret == AudioRecord.ERROR_BAD_VALUE) {
                Log.e(TAG, "Error ERROR_BAD_VALUE");
            } else {
                //写入到本地
                try {
                    bos.write(minbuffers);
                } catch (IOException e) {
                    e.printStackTrace();
                    audioRecorder.stop();
                    audioRecorder.release();
                    Log.e(TAG, "异常发生，录制停止");
                }
            }
        }
    }

    public void stopRecord() throws Exception {
        interrupted();
        join(1000);
        if (audioRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            audioRecorder.stop();
        }
        audioRecorder.release();
        //关闭流
        bos.close();
    }

}
