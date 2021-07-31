package com.xslczx.basis.android.player;

import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.xslczx.basis.android.LogUtils;

/**
 * 视频播放控制器
 */
public class BasisVideoPlayerControl
        implements SurfaceHolder.Callback,
        BasisMediaManager.MediaListener {

    // 播放预览载体 SurfaceView
    private final SurfaceView mSurfaceView;
    // 播放设置
    private BasisMediaManager.MediaSet mMediaSet;
    // 画面预览回调
    private SurfaceHolder mSurfaceHolder;
    // 判断是否自动播放
    private boolean mAutoPlay;
    // 播放事件监听
    private BasisMediaManager.MediaListener mMediaListener;

    /**
     * 构造函数
     *
     * @param surfaceView {@link SurfaceView}
     */
    public BasisVideoPlayerControl(final SurfaceView surfaceView) {
        this(surfaceView, false);
    }

    /**
     * 构造函数
     *
     * @param surfaceView {@link SurfaceView}
     * @param autoPlay    是否自动播放
     */
    public BasisVideoPlayerControl(
            final SurfaceView surfaceView,
            final boolean autoPlay) {
        this.mSurfaceView = surfaceView;
        this.mAutoPlay = autoPlay;

        // 初始化 DevMediaManager 回调事件类
        BasisMediaManager.getInstance().setMediaListener(this);
    }

    /**
     * 重置操作
     */
    private void resetOperate() {
        // 移除旧的回调
        if (mSurfaceHolder != null) {
            mSurfaceHolder.removeCallback(this);
        }
        // 设置 Holder
        mSurfaceHolder = mSurfaceView.getHolder();
        // 移除旧的回调
        if (mSurfaceHolder != null) {
            // 重新添加回调
            mSurfaceHolder.removeCallback(this);
            mSurfaceHolder.addCallback(this);
        }
    }

    /**
     * Surface 改变通知
     *
     * @param holder {@link SurfaceHolder}
     * @param format {@link PixelFormat} 像素格式
     * @param width  宽度
     * @param height 高度
     */
    @Override
    public void surfaceChanged(
            SurfaceHolder holder,
            int format,
            int width,
            int height) {
        LogUtils.d(String.format("surfaceChanged - format: %s, width: %s, height: %s", format, width, height));
    }

    /**
     * Surface 创建
     *
     * @param holder {@link SurfaceHolder}
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        LogUtils.d("surfaceCreated");
        try {
            // 开始播放
            BasisMediaManager.getInstance().playPrepare(mMediaSet);
            LogUtils.d("setDisplay(surfaceHolder) - Success");
        } catch (Exception e) {
            LogUtils.w(e);
        }
    }

    /**
     * Surface 销毁
     *
     * @param holder {@link SurfaceHolder}
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        LogUtils.d("surfaceDestroyed");
    }

    /**
     * 准备完成回调
     */
    @Override
    public void onPrepared() {
        LogUtils.d("onPrepared");
        if (mSurfaceView != null) {
            // 如果等于 null, 或者不在显示中, 则跳过
            if (mSurfaceHolder.getSurface() == null || !mSurfaceHolder.getSurface().isValid()) {
                return;
            }
            try {
                MediaPlayer mPlayer = BasisMediaManager.getInstance().getMediaPlayer();
                mPlayer.setDisplay(mSurfaceHolder);
            } catch (Exception e) {
                LogUtils.w(e);
            }
            // 判断是否自动播放
            if (mAutoPlay) {
                try { // 如果没有设置则直接播放
                    BasisMediaManager.getInstance().getMediaPlayer().start();
                } catch (Exception e) {
                    LogUtils.w(e);
                }
            }
            // 触发回调
            if (mMediaListener != null) {
                mMediaListener.onPrepared();
            }
        }
    }

    /**
     * 播放完成 / 结束
     */
    @Override
    public void onCompletion() {
        LogUtils.d("onCompletion");
        // 触发回调
        if (mMediaListener != null) {
            mMediaListener.onCompletion();
        }
    }

    /**
     * 缓存进度
     *
     * @param percent 缓冲百分比进度
     */
    @Override
    public void onBufferingUpdate(int percent) {
        LogUtils.d(String.format("onBufferingUpdate: %s", percent));
        if (mMediaListener != null) {
            mMediaListener.onBufferingUpdate(percent);
        }
    }

    /**
     * 滑动进度加载成功
     */
    @Override
    public void onSeekComplete() {
        LogUtils.d("onSeekComplete");
        if (mMediaListener != null) {
            mMediaListener.onSeekComplete();
        }
    }

    /**
     * 播放出错回调
     *
     * @param what  异常 what
     * @param extra 异常 extra
     * @return {@code true} 处理异常, {@code false} 调用 OnCompletionListener
     */
    @Override
    public boolean onError(
            int what,
            int extra) {
        LogUtils.d(String.format("onError what: %s, extra: %s", what, extra));
        // 触发回调
        if (mMediaListener != null) {
            return mMediaListener.onError(what, extra);
        }
        return false;
    }

    /**
     * 视频大小改变通知
     *
     * @param width  宽度
     * @param height 高度
     */
    @Override
    public void onVideoSizeChanged(
            int width,
            int height) {
        LogUtils.d(String.format("onVideoSizeChanged - width: %s, height: %s", width, height));
        // 触发回调
        if (mMediaListener != null) {
            mMediaListener.onVideoSizeChanged(width, height);
        }
    }

    /**
     * 设置播放监听事件
     *
     * @param mediaListener {@link BasisMediaManager.MediaListener}
     * @return {@link BasisVideoPlayerControl}
     */
    public BasisVideoPlayerControl setMediaListener(final BasisMediaManager.MediaListener mediaListener) {
        this.mMediaListener = mediaListener;
        return this;
    }

    /**
     * 暂停播放
     */
    public void pausePlayer() {
        BasisMediaManager.getInstance().pause();
    }

    /**
     * 停止播放
     */
    public void stopPlayer() {
        BasisMediaManager.getInstance().stop();
    }

    /**
     * 开始播放
     *
     * @param playUri 播放地址
     */
    public void startPlayer(final String playUri) {
        startPlayer(playUri, false);
    }

    /**
     * 开始播放
     *
     * @param playUri   播放地址
     * @param isLooping 是否循环播放
     */
    public void startPlayer(
            final String playUri,
            final boolean isLooping) {
        // 设置播放信息
        this.mMediaSet = new BasisMediaManager.MediaSet() {
            @Override
            public boolean isLooping() {
                return isLooping;
            }

            @Override
            public void setMediaConfig(MediaPlayer mediaPlayer)
                    throws Exception {
                mediaPlayer.setDataSource(playUri);
            }
        };
        // 重置操作
        resetOperate();
    }

    /**
     * 开始播放
     *
     * @param mediaSet 播放设置
     */
    public void startPlayer(final BasisMediaManager.MediaSet mediaSet) {
        // 设置播放信息
        this.mMediaSet = mediaSet;
        // 重置操作
        resetOperate();
    }

    /**
     * 获取 SurfaceView
     *
     * @return {@link SurfaceView}
     */
    public SurfaceView getSurfaceView() {
        return mSurfaceView;
    }

    /**
     * 是否播放中
     *
     * @return {@code true} yes, {@code false} no
     */
    public boolean isPlaying() {
        return BasisMediaManager.getInstance().isPlaying();
    }

    /**
     * 是否播放中
     *
     * @param uri 播放地址
     * @return {@code true} yes, {@code false} no
     */
    public boolean isPlaying(final String uri) {
        if (!TextUtils.isEmpty(uri)) { // 需要播放的地址, 必须不等于 null
            // 获取之前播放路径
            String playUri = BasisMediaManager.getInstance().getPlayUri();
            // 如果不等于 null, 并且播放地址相同
            if (playUri != null && playUri.equals(uri)) {
                try {
                    return BasisMediaManager.getInstance().isPlaying();
                } catch (Exception e) {
                    LogUtils.w(e);
                }
            }
        }
        return false;
    }

    /**
     * 判断是否自动播放
     *
     * @return {@code true} yes, {@code false} no
     */
    public boolean isAutoPlay() {
        return mAutoPlay;
    }

    /**
     * 设置自动播放
     *
     * @param autoPlay 是否自动播放
     * @return {@link BasisVideoPlayerControl}
     */
    public BasisVideoPlayerControl setAutoPlay(final boolean autoPlay) {
        this.mAutoPlay = autoPlay;
        return this;
    }

    /**
     * 获取播放地址
     *
     * @return 播放地址
     */
    public String getPlayUri() {
        return BasisMediaManager.getInstance().getPlayUri();
    }

    /**
     * 获取视频宽度
     *
     * @return 视频宽度
     */
    public int getVideoWidth() {
        return BasisMediaManager.getInstance().getVideoWidth();
    }

    /**
     * 获取视频高度
     *
     * @return 视频高度
     */
    public int getVideoHeight() {
        return BasisMediaManager.getInstance().getVideoHeight();
    }

    /**
     * 获取播放时间
     *
     * @return 播放时间
     */
    public int getCurrentPosition() {
        return BasisMediaManager.getInstance().getCurrentPosition();
    }

    /**
     * 获取资源总时间
     *
     * @return 资源总时间
     */
    public int getDuration() {
        return BasisMediaManager.getInstance().getDuration();
    }

    /**
     * 获取播放进度百分比
     *
     * @return 播放进度百分比
     */
    public int getPlayPercent() {
        return BasisMediaManager.getInstance().getPlayPercent();
    }
}