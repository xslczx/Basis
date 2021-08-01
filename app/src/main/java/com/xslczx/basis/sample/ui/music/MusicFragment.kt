package com.xslczx.basis.sample.ui.music

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.picasso.Picasso
import com.xslczx.basis.android.LogUtils
import com.xslczx.basis.android.SizeUtils
import com.xslczx.basis.android.player.BasisMediaManager
import com.xslczx.basis.java.JsonUtils
import com.xslczx.basis.java.timer.BasisTimer
import com.xslczx.basis.java.timer.TimerManager
import com.xslczx.basis.sample.AppExecutors
import com.xslczx.basis.sample.MainActivity
import com.xslczx.basis.sample.Music
import com.xslczx.basis.sample.R
import com.xslczx.basis.sample.adapter.SelectMode
import com.xslczx.basis.sample.adapter.SpaceItemDecoration
import com.xslczx.basis.sample.databinding.FragmentMusicBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit


class MusicFragment : Fragment() {

    private lateinit var viewBinding: FragmentMusicBinding
    private val listIterator = mutableListOf<Music>()
    private var currentPosition = 0
    private var mBanned = false
    private var currentMusic: Music? = null
    private val musicAdapter by lazy(LazyThreadSafetyMode.NONE) { MusicAdapter() }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        arguments?.let {
            mBanned = it.getBoolean(MainActivity.FRAGMENT_BANNED, false)
        }
        setHasOptionsMenu(!mBanned)
        viewBinding = FragmentMusicBinding.inflate(layoutInflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mActivity = activity as? AppCompatActivity ?: return
        mActivity.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(!mBanned)
            title = if (mBanned) getString(R.string.app_name) else "音乐播放"
            (mActivity.findViewById<View>(R.id.toolbar) as Toolbar).setNavigationOnClickListener { v: View? ->
                mActivity.supportFragmentManager.popBackStack()
            }
        }
        viewBinding.progressCircular.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val progress = seekBar?.progress ?: 0
                BasisMediaManager.getInstance().mediaPlayer.pause()
                BasisMediaManager.getInstance().mediaPlayer.seekTo(progress)
                BasisMediaManager.getInstance().mediaPlayer.start()
            }

        })
        viewBinding.btnMusicPrevious.setOnClickListener {
            previousMusic()?.let {
                currentPosition--
                playMusic(it)
                musicAdapter.setSelected(currentPosition)
            }
        }
        viewBinding.btnMusicNext.setOnClickListener {
            nextMusic()?.let {
                currentPosition++
                playMusic(it)
                musicAdapter.setSelected(currentPosition)
            }
        }
        viewBinding.btnMusicPlay.setOnClickListener {
            val playing = BasisMediaManager.getInstance()
                .isPlaying
            takeIf { playing }?.let {
                viewBinding.btnMusicPlay.setImageResource(android.R.drawable.ic_media_play)
                BasisMediaManager.getInstance().pause()
            } ?: let {
                viewBinding.btnMusicPlay.setImageResource(android.R.drawable.ic_media_pause)
                currentMusic?.takeIf { BasisMediaManager.getInstance().isNullMediaPlayer }
                    ?.let {
                        playMusic(it)
                    } ?: BasisMediaManager.getInstance().resume()
            }
        }
        viewBinding.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        viewBinding.recyclerView.adapter = musicAdapter
        viewBinding.recyclerView.addItemDecoration(SpaceItemDecoration(SizeUtils.dp2px(10f), Color.TRANSPARENT))
        musicAdapter.selectMode = SelectMode.SINGLE_SELECT
        musicAdapter.setOnItemSingleSelectListener { layoutPosition, isSelected ->
            currentPosition = layoutPosition
            musicAdapter.getItemOrNull(layoutPosition)
                ?.let {
                    playMusic(it)
                }
        }
        BasisMediaManager.getInstance()
            .setMediaListener(object : BasisMediaManager.MediaListener {
                override fun onPrepared() {
                    viewBinding.progressCircular.max = BasisMediaManager.getInstance().duration
                    BasisMediaManager.getInstance().mediaPlayer.start()
                    viewBinding.btnMusicPlay.setImageResource(android.R.drawable.ic_media_pause)
                }

                override fun onCompletion() {
                    viewBinding.btnMusicPlay.setImageResource(android.R.drawable.ic_media_play)
                    nextMusic()?.let { playMusic(it) }
                }

                override fun onBufferingUpdate(percent: Int) {
                    val progress = BasisMediaManager.getInstance().duration * percent / 100
                    viewBinding.progressCircular.secondaryProgress = progress
                }

                override fun onSeekComplete() {
                    BasisMediaManager.getInstance().mediaPlayer.start()
                }

                override fun onError(what: Int, extra: Int): Boolean {
                    val ignoreWhat = BasisMediaManager.isIgnoreWhat(what)
                    if (!ignoreWhat) {
                        LogUtils.w("onError:$what,extra:$extra")
                    }
                    return false
                }

                override fun onVideoSizeChanged(width: Int, height: Int) {

                }

            })
        loadData()
    }

    private fun loadData() {
        viewBinding.progressCircle.show()
        CompletableFuture.supplyAsync({
            val list = arrayListOf<Music>()
            loadMusic("热歌榜")?.let {
                list.add(it)
            }
            loadMusic("新歌榜")?.let {
                list.add(it)
            }
            loadMusic("飙升榜")?.let {
                list.add(it)
            }
            list
        }, AppExecutors.io).thenApplyAsync({
            listIterator.clear()
            listIterator.addAll(it)
            listIterator
        }, AppExecutors.compute)
            .whenCompleteAsync({ t, u ->
                musicAdapter.setNewData(t)
                musicAdapter.setSelected(-1)
                viewBinding.progressCircle.hide()
                listIterator.getOrNull(0)?.let {
                    refreshState(it)
                }
            }, AppExecutors.main)
    }

    private fun refreshState(music: Music) {
        currentMusic = music
        music.applyColor(activity)
        Picasso.get().load(music.pic).into(viewBinding.ivMusicAlbum)
        viewBinding.tvMusicTitle.text = "当前播放：${music.title}-${music.author}"
        nextMusic()?.let {
            viewBinding.tvMusicTips.text = "下一曲：${it.title}"
            it.preloadColor()
        } ?: let {
            viewBinding.tvMusicTips.text = "没有下一曲了"
        }
    }

    private fun loadMusic(sort: String? = "热歌榜"): Music? {
        val okHttpClient = OkHttpClient.Builder()
            .readTimeout(6000, TimeUnit.MILLISECONDS)
            .writeTimeout(6000, TimeUnit.MILLISECONDS)
            .build()
        val request = Request.Builder()
            .url("https://api.uomg.com/api/rand.music?sort=${sort}&format=json")
            .get()
            .build()
        try {
            val execute = okHttpClient.newCall(request).execute()
            val body = execute.body()
            body?.string()?.let {
                val code = JsonUtils.getInt(it, "code", 10000)
                LogUtils.w(it)
                when (code) {
                    1 -> {
                        val data = JsonUtils.getJSONObject(it, "data", JSONObject())
                        val name = data.getString("name")
                        val url = data.getString("url")
                        val picurl = data.getString("picurl")
                        val artistsname = data.getString("artistsname")
                        return Music(url, name, artistsname, picurl, sort ?: "热歌榜")
                    }
                    10001 -> {
                    }//错误的请求KEY
                    10002 -> {
                    }//该KEY无请求权限
                    10003 -> {
                    }//KEY过期
                    10004 -> {
                    }//错误的OPENID
                    10005 -> {
                    }//应用未审核超时，请提交认证
                    10007 -> {
                    }//未知的请求源
                    10008 -> {
                    }//被禁止的IP
                    10009 -> {
                    }//被禁止的KEY
                    10011 -> {
                    }//当前IP请求超过限制
                    10012 -> {
                    }//请求超过次数限制
                    10013 -> {
                    }//测试KEY超过请求限制
                    10014 -> {
                    }//系统内部异常(调用充值类业务时，请务必联系客服或通过订单查询接口检测订单，避免造成损失)
                    10020 -> {
                    }//接口异常
                    10021 -> {
                    }//接口停用
                    else -> {

                    }
                }
            }
        } catch (e: Exception) {
            LogUtils.w(e)
        }
        return null
    }

    private fun nextMusic(): Music? {
        val nextIndex = currentPosition + 1
        return if (nextIndex < listIterator.size) listIterator[nextIndex] else null
    }

    private fun previousMusic(): Music? {
        val previousIndex = currentPosition - 1
        return if (previousIndex >= 0) listIterator[previousIndex] else null
    }

    private fun playMusic(music: Music) {
        refreshState(music)
        BasisMediaManager.getInstance()
            .playPrepare(
                music.url, false
            )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        BasisTimer.Builder(500, 1000)
            .setTag("play_progress")
            .build()
            .setCallback { timer, number, end, infinite ->
                if (BasisMediaManager.getInstance().isPlaying) {
                    val currentPosition = BasisMediaManager.getInstance()
                        .currentPosition
                    viewBinding.progressCircular.progress = currentPosition
                }
            }
            .start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        TimerManager.getTimer("play_progress").stop()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.menu_music, menu)
        menu.setGroupVisible(R.id.not_lock, true)
        menu.setGroupVisible(R.id.lock, false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_play_cycle -> {
            }
            R.id.action_play_shuffle -> {
            }
            R.id.action_play_single -> {
            }
            R.id.action_play_oder -> {
            }
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        currentMusic?.applyColor(activity)
    }

    override fun onDestroy() {
        super.onDestroy()
        BasisMediaManager.getInstance().stop()
    }
}