package com.xslczx.basis.android.audio;

public interface Recorder {

    /**
     * 开始
     */
    void startRecording();

    /**
     * 暂停
     */
    void pauseRecording();

    /**
     * 继续
     */
    void resumeRecording();

    /**
     * 停止
     */
    void stopRecording();
}
