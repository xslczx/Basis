package com.xslczx.basis.android.audio;

import java.io.File;

/**
 * Pcm格式的音频记录器
 */
public class PcmRecorder extends BaseDataRecorder {

    public PcmRecorder(File file, AudioRecordConfig config, PullTransport pullTransport) {
        super(file, config, pullTransport);
    }
}
