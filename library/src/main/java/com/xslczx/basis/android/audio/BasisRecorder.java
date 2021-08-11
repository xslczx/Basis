package com.xslczx.basis.android.audio;

import java.io.File;

public class BasisRecorder {

    private BasisRecorder() {
    }

    public static Recorder pcm(File file, AudioRecordConfig config, PullTransport pullTransport) {
        return new PcmRecorder(file, config, pullTransport);
    }

    public static Recorder wav(File file, AudioRecordConfig config, PullTransport pullTransport) {
        return new WavRecorder(file, config, pullTransport);
    }
}
