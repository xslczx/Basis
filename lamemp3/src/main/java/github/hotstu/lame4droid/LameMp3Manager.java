package github.hotstu.lame4droid;


import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;


public enum LameMp3Manager implements RecorderListener {
    INSTANCE;

    private static final String TAG = LameMp3Manager.class.getSimpleName();
    private Mp3Recorder mp3Recorder;
    private boolean cancel = false;
    private boolean stop = false;
    private RecorderListener mediaRecorderListener;
    private final Handler mHandler;

    LameMp3Manager() {
        mp3Recorder = new Mp3Recorder();
        mp3Recorder.setRecorderListener(this);
        mHandler = new Handler(Looper.getMainLooper());
    }

    public void setRecorderListener(RecorderListener listener) {
        mediaRecorderListener = listener;
    }

    public void startRecorder(String saveMp3FullName) {
        cancel = stop = false;
        mp3Recorder.startRecording(createMp3SaveFile(saveMp3FullName));
    }

    public void cancelRecorder() {
        mp3Recorder.stopRecording();
        cancel = true;
    }

    public void stopRecorder() {
        mp3Recorder.stopRecording();
        stop = true;
    }

    private File createMp3SaveFile(String saveMp3FullName) {
        File mp3 = new File(saveMp3FullName);
        Log.d(TAG, "create mp3 file for the recorder");
        return mp3;
    }


    @Override
    public void onVolume(final int volume) {
        //Log.d(TAG, "onVolume " + volume);
        if (mediaRecorderListener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mediaRecorderListener != null) {
                        mediaRecorderListener.onVolume(volume);
                    }
                }
            });
        }
    }

    @Override
    public void onFinish(final String mp3FilePath) {
        if (cancel) {
            //录音取消的话，将之前的录音数据清掉
            File mp3 = new File(mp3FilePath);
            if (mp3.exists()) {
                mp3.delete();
            }
            cancel = false;
        } else if (stop) {
            stop = false;
            if (mediaRecorderListener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mediaRecorderListener != null) {
                            mediaRecorderListener.onFinish(mp3FilePath);
                        }
                    }
                });

            }
        }
    }
}
