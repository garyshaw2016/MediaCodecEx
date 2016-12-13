package com.milimili.mediacodecex.decoder_ex;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by luxiansheng on 16/12/12.
 * 音频播放器（解码器）
 */
public class AudioPlayerThread extends Thread {
    private static final String TAG = "AudioPlayerThread";
    private MediaCodec audioDecoder;
    private MediaExtractor audioExtractor;
    private AudioTrack audioTrack;
    private String samplePath;

    public AudioPlayerThread(String localPath) {
        samplePath = localPath;
    }

    @Override
    public void run() {
        audioExtractor = new MediaExtractor();
        try {
            audioExtractor.setDataSource(samplePath);
            for (int i = 0; i < audioExtractor.getTrackCount(); i++) {
                MediaFormat trackFormat = audioExtractor.getTrackFormat(i);
                String mine = trackFormat.getString(MediaFormat.KEY_MIME);
                if (mine.startsWith("audio/")) {
                    int sampleRate = trackFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                    audioExtractor.selectTrack(i);
                    //new
                    audioDecoder = MediaCodec.createDecoderByType(mine);
                    //configure
                    audioDecoder.configure(trackFormat, null, null, 0);
                    audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                            sampleRate, AudioFormat.CHANNEL_OUT_STEREO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_STEREO,
                                    AudioFormat.ENCODING_PCM_16BIT),
                            AudioTrack.MODE_STREAM);

                    break;
                }
            }


            if (audioDecoder == null) {
                Log.e(TAG, "audioDecoder==null");
                return;
            }

            audioDecoder.start();
            audioTrack.play();

            boolean isEos = false;
            ByteBuffer[] inputBuffers = audioDecoder.getInputBuffers();
            ByteBuffer[] outputBuffers = audioDecoder.getOutputBuffers();
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            byte[] soundBuffer = null;
            while (!Thread.interrupted()) {
                if (!isEos) {
                    int inIndex = audioDecoder.dequeueInputBuffer(10000);
                    try {
                        ByteBuffer inBuffer = inputBuffers[inIndex];
                        int sampleSize = audioExtractor.readSampleData(inBuffer, 0);
                        if (sampleSize >= 0) {
                            audioDecoder.queueInputBuffer(inIndex, 0, sampleSize, audioExtractor.getSampleTime(), 0);
                            audioExtractor.advance();
                        } else {
                            audioDecoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            isEos = true;
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                int outIndex = audioDecoder.dequeueOutputBuffer(info, 10000);
                switch (outIndex) {
                    case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                        Log.d(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
                        outputBuffers = audioDecoder.getOutputBuffers();
                        break;
                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                        Log.d(TAG, "New format " + audioDecoder.getOutputFormat());
                        final MediaFormat oformat = audioDecoder.getOutputFormat();
                        Log.d(TAG, "Output format has changed to " + oformat);
                        audioTrack.setPlaybackRate(oformat.getInteger(MediaFormat.KEY_SAMPLE_RATE));
                        break;
                    case MediaCodec.INFO_TRY_AGAIN_LATER:
                        Log.d(TAG, "dequeueOutputBuffer timed out!");
                        break;
                    default:
                        ByteBuffer outBuffer = outputBuffers[outIndex];
                        Log.v(TAG, "We can't use this buffer but render it due to the API limit, " + outBuffer);
                        if (soundBuffer==null || soundBuffer.length<info.size) {
                            soundBuffer = new byte[info.size];
                        }
                        outBuffer.rewind();
                        outBuffer.get(soundBuffer,0,info.size);
                        audioTrack.write(soundBuffer, 0, info.size);
                        audioDecoder.releaseOutputBuffer(outIndex, false);
                        break;
                }

                //停止播放
                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    Log.d(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                    break;
                }

            }

            audioDecoder.stop();
            audioDecoder.release();
            audioExtractor.release();
            audioDecoder = null;


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
