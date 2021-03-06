package com.xslczx.basis.sample.ui.music

import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.picasso.Picasso
import com.xslczx.basis.android.LogUtils
import com.xslczx.basis.android.PlayerManager
import com.xslczx.basis.android.SizeUtils
import com.xslczx.basis.java.JsonUtils
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
import per.goweii.anylayer.GlobalConfig
import per.goweii.anylayer.notification.NotificationLayer
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class MusicFragment : Fragment() {

    private lateinit var viewBinding: FragmentMusicBinding
    private val listIterator = mutableListOf<Music>()
    private var currentPosition = 0
    private var mBanned = false
    private var currentMusic: Music? = null
    private val musicAdapter by lazy(LazyThreadSafetyMode.NONE) { MusicAdapter() }
    private val playerManager by lazy(LazyThreadSafetyMode.NONE) { PlayerManager() }
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
            title = if (mBanned) getString(R.string.app_name) else "????????????"
            (mActivity.findViewById<View>(R.id.toolbar) as Toolbar).setNavigationOnClickListener { v: View? ->
                mActivity.supportFragmentManager.popBackStack()
            }
        }
        viewBinding.progressCircular.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                takeIf { fromUser }?.let {
                    val i = playerManager.duration * progress / 100
                    playerManager.pause()
                    LogUtils.d("seekTo:$i")
                    playerManager.seekTo(i)
                    playerManager.resume()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

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
            val playing = playerManager.isPlaying
            takeIf { playing }?.let {
                viewBinding.btnMusicPlay.setImageResource(android.R.drawable.ic_media_play)
                playerManager.pause()
            } ?: let {
                viewBinding.btnMusicPlay.setImageResource(android.R.drawable.ic_media_pause)
                currentMusic?.takeUnless { playerManager.isRunning }
                    ?.let {
                        playMusic(it)
                    } ?: playerManager.resume()
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
        playerManager.setOnPlayStateListener(object : PlayerManager.OnPlayStateListenerImpl() {
            override fun onPrepared() {
                super.onPrepared()
                LogUtils.d("onPrepared")
                viewBinding.btnMusicPlay.setImageResource(android.R.drawable.ic_media_pause)
            }

            override fun onStarted() {
                super.onStarted()
                LogUtils.d("onStarted")
                viewBinding.btnMusicPlay.setImageResource(android.R.drawable.ic_media_pause)
            }

            override fun onPaused() {
                super.onPaused()
                LogUtils.d("onPaused")
                viewBinding.btnMusicPlay.setImageResource(android.R.drawable.ic_media_play)
            }

            override fun onReset() {
                super.onReset()
                LogUtils.d("onReset")
            }

            override fun onStopped() {
                super.onStopped()
                LogUtils.d("onStopped")
                viewBinding.btnMusicPlay.setImageResource(android.R.drawable.ic_media_play)
            }
        })
        playerManager.setOnPlayInfoListener(object : PlayerManager.OnPlayInfoListenerImpl() {
            override fun onCompletion(mediaPlayer: MediaPlayer?) {
                super.onCompletion(mediaPlayer)
                LogUtils.d("onCompletion")
                nextMusic()?.let { playMusic(it) }
            }

            override fun onBufferingUpdate(mediaPlayer: MediaPlayer?, progress: Int) {
                super.onBufferingUpdate(mediaPlayer, progress)
                viewBinding.progressCircular.secondaryProgress = progress
            }

            override fun onPlayProgress(progress: Int) {
                super.onPlayProgress(progress)
                viewBinding.progressCircular.progress = progress
            }

            override fun onError(mp: MediaPlayer?, what: Int, extra: Int) {
                super.onError(mp, what, extra)
                LogUtils.d("onError:$what,$extra")
            }
        })
        loadData()
    }

    private fun loadData() {
        viewBinding.progressCircle.show()
        CompletableFuture.supplyAsync({
            val list = arrayListOf<Music>()
            loadMusic("?????????")?.let {
                list.add(it)
            }
            loadMusic("?????????")?.let {
                list.add(it)
            }
            loadMusic("?????????")?.let {
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
        viewBinding.tvMusicTitle.text = "???????????????${music.title}-${music.author}"
        nextMusic()?.let {
            viewBinding.tvMusicTips.text = "????????????${it.title}"
            it.preloadColor()
        } ?: let {
            viewBinding.tvMusicTips.text = "??????????????????"
        }
    }

    private fun loadMusic(sort: String? = "?????????"): Music? {
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
                        return Music(url, name, artistsname, picurl, sort ?: "?????????")
                    }
                    10001 -> {
                    }//???????????????KEY
                    10002 -> {
                    }//???KEY???????????????
                    10003 -> {
                    }//KEY??????
                    10004 -> {
                    }//?????????OPENID
                    10005 -> {
                    }//???????????????????????????????????????
                    10007 -> {
                    }//??????????????????
                    10008 -> {
                    }//????????????IP
                    10009 -> {
                    }//????????????KEY
                    10011 -> {
                    }//??????IP??????????????????
                    10012 -> {
                    }//????????????????????????
                    10013 -> {
                    }//??????KEY??????????????????
                    10014 -> {
                    }//??????????????????(????????????????????????????????????????????????????????????????????????????????????????????????????????????)
                    10020 -> {
                    }//????????????
                    10021 -> {
                    }//????????????
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
        playerManager.setPlayUrl(music.url)
            .setLooping(false)
            .setScreenOnWhilePlaying(true)
            .start()
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_logo)
        val zoomDrawableImage = ZoomDrawable.zoomDrawableImage(drawable!!, false, 72f, 72f, null, null)
        GlobalConfig.get().notificationTimePattern = "HH:mm"
        GlobalConfig.get().notificationIcon = zoomDrawableImage
        GlobalConfig.get().notificationLabel = getString(R.string.app_name)
        NotificationLayer(requireActivity())
            .title("????????????")
            .desc(music.title)
            .onNotificationClick { layer, view -> layer.dismiss() }
            .show()
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
        playerManager.release()
    }
}