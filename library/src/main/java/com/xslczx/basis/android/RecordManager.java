package com.xslczx.basis.android;

import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class RecordManager {
    private final String TAG = ">>>:Record";
    //音频来源  ,  输出格式  ,  编码方式
    private final int AUDIO_SOURCE, OUTPUT_FORMAT, AUDIO_ENCODER;
    private MediaRecorderCallBack mMediaRecorderCallBack;
    //使用MediaRecorder录音
    private MediaRecorder mMediaRecorder;
    private boolean isRecording;
    //已完成录音路径
    private String path;
    //当前所有录音集合
    private ArrayList<String> mPathList;
    //获取分贝的间隔
    private int mInterval;
    //输出文件
    private File mFile;
    //最大录制秒
    private int mMaximum;
    //当前录制了多少秒
    private int mSecond;
    private int BASE = 1;
    //设置录音最大时间，0为不设置
    private Handler mHandler = new Handler();
    private Runnable mDbRunnable = new Runnable() {
        @Override
        public void run() {
            getDecibel();
        }
    };
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (mHandler != null) {
                //最大录制时间不等于0时就是已经设置了
                if (mSecond * 1000 >= mMaximum) {
                    mHandler.removeCallbacks(mRunnable);
                    stop();
                } else {
                    mHandler.postDelayed(mRunnable, 1000);
                    if (mMediaRecorderCallBack != null) {
                        mMediaRecorderCallBack.onProcess(++mSecond);
                    }
                }
            }
        }
    };

    private RecordManager(Builder mBuilder) {
        mPathList = new ArrayList<>();
        this.AUDIO_SOURCE = mBuilder.AUDIO_SOURCE;
        this.OUTPUT_FORMAT = mBuilder.OUTPUT_FORMAT;
        this.AUDIO_ENCODER = mBuilder.AUDIO_ENCODER;
        this.mInterval = mBuilder.INTERVAL;
        initMediaRecorder();
    }

    /**
     * 最大秒数限制 0为默认不设置
     *
     * @param second 秒
     */
    public void setMaximum(int second) {
        this.mMaximum = second * 1000;
    }

    private void initMediaRecorder() {
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        }
        // 设置音频来源     MIC == 麦克
        mMediaRecorder.setAudioSource(AUDIO_SOURCE == -2 ? MediaRecorder.AudioSource.MIC : AUDIO_SOURCE);
        // 设置默认音频输出格式   .amr 格式
        mMediaRecorder.setOutputFormat(OUTPUT_FORMAT == -2 ? MediaRecorder.OutputFormat.AMR_WB : OUTPUT_FORMAT);
        // 设置默认音频编码方式   .amr 编码
        mMediaRecorder.setAudioEncoder(AUDIO_ENCODER == -2 ? MediaRecorder.AudioEncoder.AMR_WB : AUDIO_ENCODER);
        if (mFile == null) {
            mFile = new File(getExternalDir(), System.currentTimeMillis() + ".amr");
            if (!mFile.exists()) {
                try {
                    mFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        path = mFile.getAbsolutePath();
        mPathList.add(path);
        //指定音频输出文件路径
        mMediaRecorder.setOutputFile(mFile.getAbsolutePath());
    }

    //是否正在录音
    public boolean isRecording() {
        return isRecording;
    }

    //获取录音文件路径
    public String getPath() {
        return path;
    }

    //获取当前所有录音文件路径
    public ArrayList<String> getPathList() {
        return mPathList;
    }

    //获取分贝大小
    private void getDecibel() {
        if (mMediaRecorder == null) return;
        double ratio = (double) mMediaRecorder.getMaxAmplitude() / BASE;
        double decibel = 0;
        if (ratio > 0) {
            decibel = 20 * Math.log10(ratio);
        }
        mHandler.postDelayed(mDbRunnable, mInterval);
        if (mMediaRecorderCallBack != null) mMediaRecorderCallBack.onDecibel((int) decibel);
    }

    //开始录音
    public void start() {
        if (mMediaRecorder == null) {
            initMediaRecorder();
        }
        if (!isRecording) {
            try {
                isRecording = true;
                mMediaRecorder.prepare();
                mMediaRecorder.start();  //开始录制
                //大于0判断是否最大时间显示
                if (mMaximum > 0) {
                    mHandler.postDelayed(mRunnable, 1000);
                }
                //开始录制
                if (mMediaRecorderCallBack != null) {
                    mMediaRecorderCallBack.onStarted();
                    getDecibel();
                }
            } catch (IOException e) {
                if (mMediaRecorderCallBack != null) {
                    mMediaRecorderCallBack.onStopped();
                    mMediaRecorderCallBack.onError(e);
                }
                //e.printStackTrace();
                isRecording = false;
            }
        } else {
            Log.i(TAG, "音频录制中...");
        }
    }

    //停止录音
    public void stop() {
        if (mMediaRecorder != null && isRecording) {
            if (mMediaRecorderCallBack != null) {
                mMediaRecorderCallBack.onStopped();
            }
            //停止分贝获取
            if (mHandler != null) {
                mHandler.removeCallbacks(mDbRunnable);
                mHandler.removeCallbacks(mRunnable);
            }
            isRecording = false;
            try {
                mSecond = 0;
                mMediaRecorder.stop();
                mMediaRecorder.release();
                mMediaRecorder = null;
                mFile = null;
            } catch (RuntimeException e) {
                if (mMediaRecorderCallBack != null) {
                    mMediaRecorderCallBack.onMessage("时间太短");
                }
            } finally {
                mMediaRecorder = null;
                mFile = null;
            }
        } else {
            Log.i(TAG, "录音未开始");
        }
    }

    //必须在onDestroy调用此方法，否则会消耗资源
    public void release() {
        if (mMediaRecorder != null && isRecording) {
            isRecording = false;
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mPathList.clear();
        }
        mHandler.removeCallbacks(mRunnable);
        mHandler.removeCallbacks(mDbRunnable);
        mHandler = null;
        mRunnable = null;
        mDbRunnable = null;
        mMediaRecorder = null;
    }

    public void setMediaRecorderCallBack(MediaRecorderCallBack mediaRecorderCallBack) {
        this.mMediaRecorderCallBack = mediaRecorderCallBack;
    }

    /**
     * 获取外部存储目录.
     */
    private String getExternalDir() {
        //新建录音存储的文件夹
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES).getPath() + "/recorder";
        //如果目录不存在,创建目录.
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return path;
    }

    public interface MediaRecorderCallBack {
        void onStarted();//开始录制

        void onStopped();//停止录制

        void onError(Exception e);

        //这里提示错误
        void onMessage(String error);

        //second 录制了几秒
        void onProcess(int second);//录制中

        void onDecibel(int decibel);//分贝大小
    }

    public static class MediaRecorderCallBackImpl implements MediaRecorderCallBack {

        @Override
        public void onStarted() {

        }

        @Override
        public void onStopped() {

        }

        @Override
        public void onError(Exception e) {

        }

        @Override
        public void onMessage(String error) {

        }

        @Override
        public void onProcess(int second) {

        }

        @Override
        public void onDecibel(int decibel) {

        }
    }

    public static class Builder {
        //可选字段，不传递参数为默认值
        private int AUDIO_SOURCE, OUTPUT_FORMAT, AUDIO_ENCODER = -2;
        private int INTERVAL = 500;

        //这里的File会替换掉重新录音前的文件，如果不想替换传递null
        public Builder() {

        }

        public Builder setInterval(int interval) {
            this.INTERVAL = interval;
            return this;
        }

        public Builder setAudioSource(int audioSource) {
            this.AUDIO_SOURCE = audioSource;
            return this;
        }

        public Builder setOutputFormat(int outputFormat) {
            this.OUTPUT_FORMAT = outputFormat;
            return this;
        }

        public Builder setAudioEncoder(int audioEncoder) {
            this.AUDIO_ENCODER = audioEncoder;
            return this;
        }

        public RecordManager build() {
            return new RecordManager(this);
        }
    }
}
