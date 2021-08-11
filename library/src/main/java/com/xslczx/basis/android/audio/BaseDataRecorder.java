package com.xslczx.basis.android.audio;

import android.media.AudioRecord;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Base Recorder (Only record the original audio data.)
 */
public class BaseDataRecorder implements Recorder {
    protected PullTransport pullTransport;
    protected AudioRecordConfig config;
    protected int bufferSizeInBytes;// 缓冲区大小
    protected File file;
    private AudioRecord audioRecord;
    private OutputStream outputStream;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    protected BaseDataRecorder(File file, AudioRecordConfig config, PullTransport pullTransport) {
        this.file = file;
        this.config = config;
        this.pullTransport = pullTransport;
        // 计算缓冲区大小
        this.bufferSizeInBytes = AudioRecord.getMinBufferSize(
                config.getSampleRateInHz(),
                config.getChannelConfig(),
                config.getAudioFormat()
        );
    }

    @Override
    public void startRecording() {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                startRecord();
            }
        });
    }

    private void startRecord() {
        try {
            if (audioRecord == null) {
                audioRecord = new AudioRecord(config.getAudioSource(), config.getSampleRateInHz(),
                        config.getChannelConfig(), config.getAudioFormat(), bufferSizeInBytes);
            }
            if (outputStream == null) {
                outputStream = new FileOutputStream(file);
            }
            audioRecord.startRecording();
            pullTransport.isEnableToBePulled(true);
            pullTransport.startPoolingAndWriting(audioRecord, bufferSizeInBytes, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pauseRecording() {
        pullTransport.isEnableToBePulled(false);
    }

    @Override
    public void resumeRecording() {
        startRecording();
    }

    @Override
    public void stopRecording() {
        pauseRecording();
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
        if (outputStream != null) {
            try {
                outputStream.flush();
                outputStream.close();
                outputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}