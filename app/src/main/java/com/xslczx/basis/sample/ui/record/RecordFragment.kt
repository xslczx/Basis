package com.xslczx.basis.sample.ui.record

import android.graphics.Color
import android.media.AudioFormat
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.xslczx.basis.android.AppBasis
import com.xslczx.basis.android.LogUtils
import com.xslczx.basis.android.PlayerManager
import com.xslczx.basis.android.audio.AudioRecordConfig
import com.xslczx.basis.android.audio.BasisRecorder
import com.xslczx.basis.android.audio.PullTransport
import com.xslczx.basis.android.audio.Recorder
import com.xslczx.basis.sample.R
import com.xslczx.basis.sample.databinding.FragmentRecordBinding
import github.hotstu.lame4droid.LameMp3Manager
import per.goweii.anylayer.floats.FloatLayer
import java.io.File

class RecordFragment : Fragment() {

    private lateinit var viewBinding: FragmentRecordBinding
    private var floatLayer: FloatLayer? = null
    private var recording = false
    private var audioFile: File? = null
    private val playerManager by lazy { PlayerManager() }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentRecordBinding.inflate(inflater)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mActivity = activity as? AppCompatActivity ?: return
        mActivity.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "音频录制"
            (mActivity.findViewById<View>(R.id.toolbar) as Toolbar).setNavigationOnClickListener { v: View? ->
                mActivity.supportFragmentManager.popBackStack()
            }
        }
        viewBinding.btnRecord.setOnClickListener {
            if (recording) {
                recording = false
                viewBinding.btnRecord.text = "开始录制"
                recorder?.stopRecording()
                playerManager.apply {
                    setPlayFile(audioFile)
                    start()
                    setOnPlayInfoListener(object : PlayerManager.OnPlayInfoListenerImpl() {
                        override fun onCompletion(mediaPlayer: MediaPlayer?) {
                            super.onCompletion(mediaPlayer)
                            setPlayFile(audioFile)
                            start()
                        }
                    })
                }
            } else {
                playerManager.stop()
                recording = true
                viewBinding.btnRecord.text = "结束录制"
                audioFile = File(
                    AppBasis.getApp().getExternalFilesDir(Environment.DIRECTORY_MUSIC),
                    "${System.currentTimeMillis()}.wav"
                )
                recorder = BasisRecorder.wav(
                    audioFile,
                    AudioRecordConfig(
                        MediaRecorder.AudioSource.MIC,
                        16000,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                    ),
                    PullTransport.Noise()
                        .setOnAudioChunkPulledListener {
                            LogUtils.d("maxAmplitude:${it.maxAmplitude()}")
                            val mp3Buffer = it.toBytes()
                        }
                )
                recorder?.startRecording()
            }
        }
        showFloat()
    }

    private var recorder: Recorder? = null

    override fun onDestroyView() {
        super.onDestroyView()
        dismissFloat()
        LameMp3Manager.INSTANCE.stopRecorder()
    }

    override fun onDestroy() {
        super.onDestroy()
        playerManager.release()
    }

    private fun showFloat() {
        val floatCardView = CardView(requireContext())
        val floatIconView = ImageView(requireContext())
        floatIconView.setImageResource(R.drawable.ic_logo)
        floatIconView.scaleType = ImageView.ScaleType.FIT_CENTER
        floatIconView.setBackgroundResource(R.color.colorPrimary)
        floatCardView.addView(floatIconView)
        floatCardView.setCardBackgroundColor(Color.TRANSPARENT)
        floatCardView.radius = 90f
        floatCardView.layoutParams = ViewGroup.LayoutParams(180, 180)
        floatLayer = FloatLayer(requireActivity())
            .floatView(floatCardView)
            .snapEdge(FloatLayer.Edge.ALL)
            .outside(true)
            .defPercentX(1F)
            .defPercentY(0.6f)
            .defAlpha(0f)
            .defScale(0f)
            .normalAlpha(0.9f)
            .normalScale(1F)
            .lowProfileDelay(3000)
            .lowProfileAlpha(0.6f)
            .lowProfileScale(0.9f)
            .lowProfileIndent(0.5f)
            .paddingLeft(45)
            .paddingTop(45)
            .paddingRight(45)
            .paddingBottom(45)
            .marginLeft(0)
            .marginTop(0)
            .marginRight(0)
            .marginBottom(0)
            .onFloatClick { layer, view ->
                Toast.makeText(requireContext(), "点击了悬浮框", Toast.LENGTH_SHORT).show()
            }
        floatLayer?.show()
    }

    private fun dismissFloat() {
        floatLayer?.dismiss()
        floatLayer = null
    }
}