package com.milimili.mediacodecex.audio_ex;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.milimili.mediacodecex.utils.FileUtil;

/**
 * Created by luxiansheng on 16/12/16.
 * AudioRecord
 */

public class AudioRecord_ extends Thread {
    private static final String TAG = "AudioRecord_";
    private AudioRecord audioRecorder;
    private WavFileWriter wavFileWriter;
    private boolean isLoop;

    public void startRecord() {
        isLoop = true;
        wavFileWriter = new WavFileWriter();
        this.start();
    }

    public void stopRecord() throws Exception {
        interrupt();
        isLoop = false;
        join(1000);
        if (audioRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            audioRecorder.stop();
        }
        audioRecorder.release();
        wavFileWriter.stopWrite();
    }


    /**
     * 音频采集器初始化
     *
     * @return isConfigSuccess 是否配置成功
     */
    private boolean audioConfig() throws Exception {
        int audioSrc = MediaRecorder.AudioSource.CAMCORDER;//数据源
        int sampleRateInHz = 44100;//采样率 4000-192000
        int channelConfig = AudioFormat.CHANNEL_IN_STEREO;//双声道
        int audioForamt = AudioFormat.ENCODING_PCM_16BIT;//（量化精度）位宽
        int minBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioForamt);
        if (minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            throw new RuntimeException("error bad buffer size");
        }
        audioRecorder = new AudioRecord(audioSrc, sampleRateInHz,
                channelConfig, audioForamt, minBufferSize*4);
        if (audioRecorder.getState() == AudioRecord.STATE_UNINITIALIZED) {
            Log.e(TAG, "AudioRecord initialize fail !");
            return false;
        }

        wavFileWriter.openFile(FileUtil.createFile("test.wav")
                .getAbsolutePath(),sampleRateInHz,16,2);

        return true;

    }

    private static final int SAMPLES_PER_FRAME = 1024;
    @Override
    public void run() {
        try {
            boolean isSucess = audioConfig();
            if (isSucess) {
                audioRecorder.startRecording();
                byte[] minbuffers;
                while (isLoop) {
                    minbuffers = new byte[SAMPLES_PER_FRAME*2];
                    int ret = audioRecorder.read(minbuffers, 0, minbuffers.length);
                    if (ret == AudioRecord.ERROR_INVALID_OPERATION) {
                        Log.e(TAG, "Error ERROR_INVALID_OPERATION");
                    } else if (ret == AudioRecord.ERROR_BAD_VALUE) {
                        Log.e(TAG, "Error ERROR_BAD_VALUE");
                    } else {
                        wavFileWriter.write(minbuffers);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
