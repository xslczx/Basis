package github.hotstu.lame4droid;

public interface RecorderListener {
    void onVolume(int volume);

    void onFinish(String mp3SavePath);
}