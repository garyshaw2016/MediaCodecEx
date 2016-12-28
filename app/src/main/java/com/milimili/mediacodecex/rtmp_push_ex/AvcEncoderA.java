package com.milimili.mediacodecex.rtmp_push_ex;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import com.milimili.mediacodecex.encoder_ex.CameraThread;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by luxiansheng on 16/12/13.
 * 采集摄像头的数据编码成.h264文件
 * 编码：MediaCodec
 * 回调YUV数据为H264
 */

public class AvcEncoderA {
    private final static String TAG = "AvcEncoder";
    int m_width, m_height;
    int frameRate = 60;
    MediaCodec encoder;
    private final MediaFormat videoFormat;
    /**
     * 输出到某个.h264文件的流
     */
    private BufferedOutputStream bos;
    private boolean isRunning;
    private int TIMEOUT_US = 12000;
    //关键帧数据
    private byte[] configBytes;


    public AvcEncoderA() {
        m_width = 1280;
        m_height = 720;
        String mime = "video/avc";
        //创建待保存文件
        createFile();
        //检测可用的编码器
        getSupportCodec();
        videoFormat = MediaFormat.createVideoFormat(mime, m_width, m_height);
        videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
        videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, m_width * m_height * 5);
        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
        videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        //and so on

        //创建
        try {
            encoder = MediaCodec.createEncoderByType(mime);
            encoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * 获取可用的编码器
     */
    private void getSupportCodec() {
        int codecCount = MediaCodecList.getCodecCount();
        for (int i = 0; i < codecCount; i++) {
            MediaCodecInfo mediaCodecInfo = MediaCodecList.getCodecInfoAt(i);
            String[] supportedTypes = mediaCodecInfo.getSupportedTypes();
            for (String type : supportedTypes) {
                if (type.equals("video/avc")) {
                    Log.i(TAG, "supportMediaCodecType:" + type);
                    for (int colorFormat : mediaCodecInfo.getCapabilitiesForType(type).colorFormats) {
                        Log.i(TAG, "colorFormat:" + colorFormat);
                    }
                }
            }
        }
        Log.d(TAG, "pause");
    }

    /**
     * 开启线程开始编码
     */
    public void startEncoderThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //进入start状态
                encoder.start();
                encodeRunning();
            }
        }).start();
    }

    private void encodeRunning() {
        isRunning = true;
        byte[] inputDatas = null;
        long generateIndex = 0;
        long pts;
        ByteBuffer[] inputBuffers = encoder.getInputBuffers();
        ByteBuffer[] outputBuffers = encoder.getOutputBuffers();
        //开始循环取数据并编码
        while (isRunning) {
            if (CameraThread.YUVQueue.size() > 0) {
                //取数据
                inputDatas = CameraThread.YUVQueue.poll();
                byte[] yuv420p = new byte[m_width * m_height * 3 / 2];
                //转码
                NV21ToNV12(inputDatas, yuv420p, m_width, m_height);
                inputDatas = yuv420p;
            }
            if (inputDatas != null) {
                try {
                    int inIndex = encoder.dequeueInputBuffer(-1);
                    if (inIndex >= 0) {
                        //数据入 inputbuffers队列
                        pts = computePresentationTime(generateIndex);
                        ByteBuffer inputBuffer = inputBuffers[inIndex];
                        inputBuffer.clear();
                        inputBuffer.put(inputDatas);
                        encoder.queueInputBuffer(inIndex, 0, inputDatas.length, pts, 0);
                        generateIndex++;
                    }
                    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                    int outIndex = encoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_US);
                    while (outIndex >= 0) {
                        ByteBuffer outputBuffer = outputBuffers[outIndex];
                        byte[] outData = new byte[bufferInfo.size];
                        outputBuffer.get(outData);
                        //输出buffer有数据
                        //根据bufferInfo处理不同的帧
                        if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_CODEC_CONFIG) {
                            //配置信息帧，先保存数据
                            configBytes = new byte[bufferInfo.size];
                            configBytes = outData;
                        } else if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME) {
                            byte[] keyFrame = new byte[bufferInfo.size + configBytes.length];
                            System.arraycopy(configBytes, 0, keyFrame, 0, configBytes.length);//先cp配置帧
                            System.arraycopy(outData, 0, keyFrame, configBytes.length, outData.length);
                            //写入流
                            bos.write(keyFrame);
                        } else {
                            bos.write(outData);
                        }
                        encoder.releaseOutputBuffer(outIndex, false);
                        //更新outIndex
                        outIndex = encoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_US);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                //稍等片刻
                SystemClock.sleep(500);
            }
        }

    }

    /**
     * 结束编码，保存文件
     */
    public void stopEncoderThread() {
        isRunning = false;
        encoder.stop();
        encoder.release();
        //关闭流
        try {
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test1.h264";

    /**
     * 创建待保存的文件地址
     */
    private void createFile() {
        File file = new File(path);
        if (file.exists()) {
            boolean delete = file.delete();
            if (!delete) {
                Log.w(TAG, "file delete fail");
            }
        }
        //开一个流
        try {
            bos = new BufferedOutputStream(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void NV21ToNV12(byte[] nv21, byte[] nv12, int width, int height) {
        long startMs = System.currentTimeMillis();
        if (nv21 == null || nv12 == null) return;
        int framesize = width * height;
        int i = 0, j = 0;
        System.arraycopy(nv21, 0, nv12, 0, framesize);
        for (i = 0; i < framesize; i++) {
            nv12[i] = nv21[i];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j - 1] = nv21[j + framesize];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j] = nv21[j + framesize - 1];
        }
//        Log.i(TAG,"NV21ToNV12 one frame costs:"+(System.currentTimeMillis()-startMs));
    }

    /**
     * Generates the presentation time for frame N, in microseconds.
     */
    private long computePresentationTime(long frameIndex) {
        return 132 + frameIndex * 1000000 / frameRate;
    }
}
