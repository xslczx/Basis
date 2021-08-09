package github.hotstu.lame4droid;


import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Mp3Recorder {

    private static final String TAG = Mp3Recorder.class.getSimpleName();


    //默认采样率
    private static final int DEFAULT_SAMPLING_RATE = 44100;

    //转换周期，录音每满160帧，进行一次转换
    private static final int FRAME_COUNT = 160;

    //输出MP3的码率
    private static final int BIT_RATE = 32;

    //根据资料假定的最大值。 实测时有时超过此值。
    private static final int MAX_VOLUME = 2000;

    private AudioRecord audioRecord = null;

    private int bufferSize;

    //private File mp3File;

    private int mVolume;

    private short[] mPCMBuffer;

    //private FileOutputStream os = null;

    private Mp3EncodeThread encodeThread;

    private int samplingRate;

    private int channelConfig;

    private PCMFormat audioFormat;

    private boolean isRecording = false;

    private ExecutorService executor = Executors.newFixedThreadPool(1);

    private RecorderListener finishListener;


    /**
     * Default constructor. Setup recorder with default sampling rate 1 channel,
     * 16 bits pcm
     */
    public Mp3Recorder() {
        this(DEFAULT_SAMPLING_RATE, AudioFormat.CHANNEL_IN_MONO, PCMFormat.PCM_16BIT);
    }

    public Mp3Recorder(int samplingRate, int channelConfig, PCMFormat audioFormat) {
        this.samplingRate = samplingRate;
        this.channelConfig = channelConfig;
        this.audioFormat = audioFormat;
    }

    private void initAudioRecorder(File save) {
        int bytesPerFrame = audioFormat.getBytesPerFrame();
        /* Get number of samples. Calculate the mPCMBuffer size (round up to the factor of given frame size) */
        int frameSize = AudioRecord.getMinBufferSize(samplingRate, channelConfig, audioFormat.getAudioFormat()) / bytesPerFrame;
        if (frameSize % FRAME_COUNT != 0) {
            frameSize = frameSize + (FRAME_COUNT - frameSize % FRAME_COUNT);
            Log.d(TAG, "Frame size: " + frameSize);
        }
        bufferSize = frameSize * bytesPerFrame;

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, samplingRate, channelConfig, audioFormat.getAudioFormat(), bufferSize);

        mPCMBuffer = new short[bufferSize];


        SimpleLame.init(samplingRate, 1, samplingRate, BIT_RATE);


        // Create and run thread used to encode data
        encodeThread = new Mp3EncodeThread(save, bufferSize);
        encodeThread.start();
        //给AudioRecord设置刷新监听，待录音帧数每次达到FRAME_COUNT，就通知转换线程转换一次数据
        //audioRecord.setRecordPositionUpdateListener(encodeThread, encodeThread.getHandler());
        //audioRecord.setPositionNotificationPeriod(FRAME_COUNT);
    }

    public void startRecording(final File mp3Save) {
        if (isRecording) return;
        Log.d(TAG, "Start recording");
        Log.d(TAG, "BufferSize = " + bufferSize);
        if (audioRecord == null) {
            initAudioRecorder(mp3Save);
        }
        audioRecord.startRecording();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                isRecording = true;
                //循环的从AudioRecord获取录音的PCM数据
                while (isRecording) {
                    int readSize = audioRecord.read(mPCMBuffer, 0, bufferSize);
                    if (readSize > 0) {
                        //待转换的PCM数据放到转换线程中
                        encodeThread.addChangeBuffer(mPCMBuffer, readSize);
                        calculateRealVolume(mPCMBuffer, readSize);
                    }
                }
                // 录音完毕，通知转换线程停止，并等待直到其转换完毕
                encodeThread.addChangeBuffer(null, -1);

                Log.d(TAG, "waiting for encoding thread");
                try {
                    encodeThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "done encoding thread");

                // 录音完毕，释放AudioRecord的资源
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
                encodeThread = null;

                //转换完毕后回调监听
                if (finishListener != null) finishListener.onFinish(mp3Save.getPath());
            }
        };
        executor.execute(runnable);
    }


    public void stopRecording()  {
        Log.d(TAG, "stop recording");
        isRecording = false;
    }

    //计算音量大小
    private void calculateRealVolume(short[] buffer, int readSize) {
        if (readSize <= 0) {
            return;
        }
        double sum = 0;
        for (int i = 0; i < readSize; i++) {
            sum += buffer[i] * buffer[i];
        }
        double amplitude = sum / readSize;
        mVolume = (int) Math.sqrt(amplitude);//均方差

        if (finishListener != null) {
            finishListener.onVolume(mVolume);
        }
    }



    public void setRecorderListener(RecorderListener listener) {
        this.finishListener = listener;
    }


}
