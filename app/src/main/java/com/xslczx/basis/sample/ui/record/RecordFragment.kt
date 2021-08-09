package com.xslczx.basis.sample.ui.record

import android.graphics.Color
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
import com.xslczx.basis.sample.R
import com.xslczx.basis.sample.databinding.FragmentRecordBinding
import github.hotstu.lame4droid.LameMp3Manager
import github.hotstu.lame4droid.RecorderListener
import per.goweii.anylayer.floats.FloatLayer
import java.io.File

class RecordFragment : Fragment() {

    private lateinit var viewBinding: FragmentRecordBinding
    private var floatLayer: FloatLayer? = null
    private var recording = false
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
                LameMp3Manager.INSTANCE.stopRecorder()
                viewBinding.btnRecord.text = "开始录制"
            } else {
                recording = true
                val externalFilesDir = AppBasis.getApp().getExternalFilesDir(Environment.DIRECTORY_MUSIC)
                LameMp3Manager.INSTANCE.startRecorder(
                    File(
                        externalFilesDir,
                        "${System.currentTimeMillis()}.mp3"
                    ).absolutePath
                )
                viewBinding.btnRecord.text = "结束录制"
                LameMp3Manager.INSTANCE.setRecorderListener(object : RecorderListener {
                    override fun onVolume(volume: Int) {
                    }

                    override fun onFinish(mp3SavePath: String?) {
                        Toast.makeText(AppBasis.getApp(), mp3SavePath, Toast.LENGTH_SHORT).show()
                    }

                })
            }
        }
        showFloat()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dismissFloat()
        LameMp3Manager.INSTANCE.stopRecorder()
    }

    override fun onDestroy() {
        super.onDestroy()
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