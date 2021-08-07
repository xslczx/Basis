package com.xslczx.basis.android;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;

import java.io.File;
import java.io.IOException;

/**
 * 播放音频文件工具类
 */
public class PlayerManager {
    public static final int PLAY_STATE0 = 1;//文件
    public static final int PLAY_STATE1 = 2;//raw
    public static final int PLAY_STATE2 = 3;//assets
    public static final int PLAY_STATE3 = 4;//网络
    public static final int IS_PLAY_STOP = 100;//是否停止播放
    private int PLAY_STATE = -1;//判断
    private int duration;
    private boolean isPlaying = false;
    //播放文件的路径
    private File targetFile;
    //播放raw媒体源
    private int rawId;
    //播放assets媒体源
    private String assetsName;
    //播放网络资源
    private String netPath;
    private MediaPlayer mMediaPlayer = null;
    private OnPlayStateListener mOnPlayStateListener;
    private OnPlayInfoListener mOnPlayInfoListener;
    //多久获取一次进度 默认500毫秒
    private int mInterval = 500;
    private int mLastPercent;
    private final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            //停止前发送进度.
            if (mOnPlayInfoListener != null && mMediaPlayer != null && isPlaying()) {
                int playPercent = getPlayPercent();
                if (mLastPercent != playPercent) {
                    mLastPercent = playPercent;
                    mOnPlayInfoListener.onPlayProgress(playPercent);
                }
            }
            if (msg.what == IS_PLAY_STOP && mHandler != null) {
                mHandler.removeCallbacks(mRunnable);
            }
            return false;
        }
    });
    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (mOnPlayInfoListener != null && mMediaPlayer != null && isPlaying()) {
                int playPercent = getPlayPercent();
                if (mLastPercent != playPercent) {
                    mLastPercent = playPercent;
                    mOnPlayInfoListener.onPlayProgress(playPercent);
                }
            }
            mHandler.postDelayed(mRunnable, mInterval);
        }
    };
    private boolean mLooping = false;
    private boolean mKeepScreenOn = false;
    private float mLeftVolume = 1F, mRightVolume = 1F;
    private int mWakeMode = PowerManager.PARTIAL_WAKE_LOCK;

    public PlayerManager() {
    }

    /**
     * 设置文件路径
     *
     * @param file 需要播放文件的路径
     */
    public void setPlayFile(File file) {
        this.targetFile = file;
        PLAY_STATE = PLAY_STATE0;
        stop();
    }

    /**
     * 设置Raw播放
     *
     * @param rawId R.raw.music3
     */
    public PlayerManager setPlayRaw(int rawId) {
        this.rawId = rawId;
        PLAY_STATE = PLAY_STATE1;
        stop();
        return this;
    }

    /**
     * 设置Assets播放
     *
     * @param assetsName assets文件名
     */
    public PlayerManager setPlayAssets(String assetsName) {
        this.assetsName = assetsName;
        PLAY_STATE = PLAY_STATE2;
        stop();
        return this;
    }

    /**
     * 设置网络资源播放
     *
     * @param netPath 网络音乐地址
     */
    public PlayerManager setPlayUrl(String netPath) {
        this.netPath = netPath;
        PLAY_STATE = PLAY_STATE3;
        stop();
        return this;
    }

    /**
     * 开始播放
     *
     * @return true 开始播放， false 播放错误
     */
    public boolean start() {
        if (PLAY_STATE == PLAY_STATE1) {
            mMediaPlayer = MediaPlayer.create(AppBasis.getApp(), rawId);
        } else {
            mMediaPlayer = new MediaPlayer();
        }
        try {
            switch (PLAY_STATE) {
                case PLAY_STATE0:
                    mMediaPlayer.setDataSource(targetFile.getAbsolutePath());
                    mMediaPlayer.prepare();
                    break;
                case PLAY_STATE1:
                    break;
                case PLAY_STATE2:
                    AssetFileDescriptor fileDescriptor = AppBasis.getApp().getAssets().openFd(assetsName);
                    mMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
                    mMediaPlayer.prepare();
                    break;
                case PLAY_STATE3:
                    mMediaPlayer.setDataSource(netPath);
                    mMediaPlayer.prepareAsync();
                    break;
            }
            //播放完成自动停止
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    PlayerManager.this.isPlaying = false;
                    PlayerManager.this.duration = 0;
                    Message message = new Message();
                    message.what = IS_PLAY_STOP;
                    mHandler.sendMessage(message);
                    mediaPlayer.stop();
                    if (mOnPlayInfoListener != null) {
                        mOnPlayInfoListener.onCompletion(mediaPlayer);
                    }
                }
            });
            //准备完毕 自动播放
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.setVolume(mLeftVolume, mRightVolume);
                    mediaPlayer.setLooping(mLooping);
                    mediaPlayer.setScreenOnWhilePlaying(mKeepScreenOn);
                    if (mKeepScreenOn) {
                        try {
                            mediaPlayer.setWakeMode(AppBasis.getApp(), mWakeMode);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (mOnPlayStateListener != null) {
                        mOnPlayStateListener.onPrepared();
                    }
                    mediaPlayer.start();
                    mHandler.postDelayed(mRunnable, mInterval);
                    duration = mMediaPlayer.getDuration();
                    if (mOnPlayStateListener != null) {
                        mOnPlayStateListener.onStarted();
                    }
                }
            });
            //播放错误监听
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    if (isIgnoreWhat(what)) {
                        LogUtils.w("isIgnoreError:" + what);
                        if (mMediaPlayer != null) {
                            stop();
                        }
                        return true;
                    }
                    if (mOnPlayInfoListener != null) {
                        mOnPlayInfoListener.onError(mp, what, extra);
                    }
                    mHandler.removeCallbacks(mRunnable);
                    return false;
                }
            });
            //网络缓冲监听
            mMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    if (mOnPlayInfoListener != null) {
                        mOnPlayInfoListener.onBufferingUpdate(mp, percent);
                    }
                }
            });
            //调整进度监听
            mMediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mp) {
                    if (mOnPlayInfoListener != null) {
                        mOnPlayInfoListener.onSeekComplete(mp);
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            stop();
        }
        boolean result = (mMediaPlayer != null);
        this.isPlaying = result;
        return result;
    }

    /**
     * 停止播放
     */
    public void stop() {
        this.isPlaying = false;
        this.duration = 0;
        if (mMediaPlayer != null) {
            Message message = new Message();
            message.what = IS_PLAY_STOP;
            mHandler.sendMessage(message);
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            if (mOnPlayStateListener != null) {
                mOnPlayStateListener.onReset();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    /**
     * resume调用 继续播放也调用此方法即可
     */
    public void resume() {
        if (mMediaPlayer != null) {
            this.isPlaying = true;
            mHandler.postDelayed(mRunnable, mInterval);
            mMediaPlayer.start();
            if (mOnPlayStateListener != null) {
                mOnPlayStateListener.onStarted();
            }
        }
    }

    /**
     * 暂停
     */
    public void pause() {
        this.isPlaying = false;
        mHandler.removeCallbacks(mRunnable);
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
            if (mOnPlayStateListener != null) {
                mOnPlayStateListener.onPaused();
            }
        }
    }

    /**
     * 是否正在运行
     *
     * @return true 正在运行，false停止运行
     */
    public boolean isRunning() {
        return (mMediaPlayer != null);
    }

    /**
     * 是否在播放中
     *
     * @return true 正在播放，false 停止播放
     */
    public boolean isPlaying() {
        return this.isPlaying;
    }

    /**
     * 播放文件的时长
     *
     * @return 文件时长
     */
    public int getDuration() {
        if (mMediaPlayer == null) {
            return this.duration;
        }
        return mMediaPlayer.getDuration();
    }

    /**
     * 获取当前播放位置
     *
     * @return 当前播放位置值
     */
    public int getCurrentPosition() {
        if (mMediaPlayer == null) {
            return 0;
        }
        return mMediaPlayer.getCurrentPosition();
    }

    /**
     * 获取播放进度百分比
     *
     * @return 播放进度百分比
     */
    public int getPlayPercent() {
        return (getCurrentPosition() * 100) / getDuration();
    }

    /**
     * 左右声道大小
     *
     * @param leftVolume  左声道大小 0 - 1
     * @param rightVolume 右声道大小 0 - 1
     */
    public PlayerManager setVolume(float leftVolume, float rightVolume) {
        this.mLeftVolume = leftVolume;
        this.mRightVolume = rightVolume;
        if (mMediaPlayer != null) {
            mMediaPlayer.setVolume(leftVolume, rightVolume);
        }
        return this;
    }

    /**
     * 设置唤醒方式 需要在清单文件AndroidManifest.xml中添加权限 <uses-permission android:name="android.permission.WAKE_LOCK" />
     *
     * @param mode 唤醒模式
     */
    public PlayerManager setWakeMode(int mode) {
        this.mWakeMode = mode;
        if (mMediaPlayer != null) {
            mMediaPlayer.setWakeMode(AppBasis.getApp(), mode);
        }
        return this;
    }

    /**
     * 播放时不熄屏
     *
     * @param screenOn true 不息屏，false 息屏
     */
    public PlayerManager setScreenOnWhilePlaying(boolean screenOn) {
        this.mKeepScreenOn = screenOn;
        if (mMediaPlayer != null) {
            mMediaPlayer.setScreenOnWhilePlaying(screenOn);
        }
        return this;
    }

    /**
     * 指定播放位置 毫秒
     *
     * @param msec 要播放的值
     */
    public void seekTo(int msec) {
        if (mMediaPlayer != null) {
            int duration = getDuration();
            if (msec > duration) {
                LogUtils.w("seekTo " + msec + ",duration :" + duration);
            } else {
                mMediaPlayer.seekTo(msec);
            }
        }
    }

    /**
     * 是否循环播放
     *
     * @param looping true 循环播放，false 不循环
     */
    public PlayerManager setLooping(boolean looping) {
        mLooping = looping;
        if (mMediaPlayer != null) {
            mMediaPlayer.setLooping(looping);
        }
        return this;
    }

    /**
     * 是否忽略错误类型
     *
     * @param errorWhat onError 方法回调 what
     * @return {@code true} yes, {@code false} no
     */
    private boolean isIgnoreWhat(final int errorWhat) {
        // 是否忽略
        boolean ignore = false;
        switch (errorWhat) {
            case -38:
            case 1:
            case 100:
            case 700:
            case 701:
            case 800:
                ignore = true;
                break;
        }
        return ignore;
    }

    /**
     * 获取当前播放资源类型
     *
     * @return 当前资源类型
     */
    public int getMusicType() {
        return PLAY_STATE;
    }

    /**
     * 必须调用此方法 销毁，释放
     */
    public void release() {
        stop();
    }

    /**
     * 多久获取一次进度 毫秒
     *
     * @param interval 默认500
     */
    public PlayerManager setInterval(int interval) {
        this.mInterval = interval;
        return this;
    }

    /**
     * 获取MediaPlayer对象
     *
     * @return MediaPlayer
     */
    public MediaPlayer getMediaPlayer() {
        return this.mMediaPlayer;
    }

    /**
     * 功能监听
     */
    public void setOnPlayStateListener(OnPlayStateListener onPlayStateListener) {
        this.mOnPlayStateListener = onPlayStateListener;
    }

    /**
     * 播放信息监听
     */
    public void setOnPlayInfoListener(OnPlayInfoListener onPlayInfoListener) {
        this.mOnPlayInfoListener = onPlayInfoListener;
    }


    /**
     * 获取当前信息的回调
     */

    public interface OnPlayInfoListener {
        //播放错误监听
        void onError(MediaPlayer mp, int what, int extra);

        //播放完成监听
        void onCompletion(MediaPlayer mediaPlayer);

        //网络缓冲监听
        void onBufferingUpdate(MediaPlayer mediaPlayer, int i);

        //进度调整监听
        void onSeekComplete(MediaPlayer mediaPlayer);

        //时实播放进度
        void onPlayProgress(int progress);
    }

    public interface OnPlayStateListener {
        // 准备完成
        void onPrepared();

        // 开始播放
        void onStarted();

        // 暂停
        void onPaused();

        // 停止播放
        void onStopped();

        //重置
        void onReset();
    }

    public static class OnPlayStateListenerImpl implements OnPlayStateListener {

        @Override
        public void onPrepared() {

        }

        @Override
        public void onStarted() {

        }

        @Override
        public void onPaused() {

        }

        @Override
        public void onStopped() {

        }

        @Override
        public void onReset() {

        }
    }

    public static class OnPlayInfoListenerImpl implements OnPlayInfoListener {

        @Override
        public void onError(MediaPlayer mp, int what, int extra) {

        }

        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {

        }

        @Override
        public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

        }

        @Override
        public void onSeekComplete(MediaPlayer mediaPlayer) {

        }

        @Override
        public void onPlayProgress(int progress) {

        }
    }
}

