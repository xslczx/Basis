package com.xslczx.basis.sample

import android.Manifest
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.xslczx.basis.sample.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private lateinit var viewBinding: FragmentMainBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = FragmentMainBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mActivity = activity as? AppCompatActivity ?: return
        viewBinding.btnMusic.setOnClickListener {
            with((mActivity as? MainActivity)) {
                val args = Bundle().also { bundle ->
                    bundle.putBoolean(MainActivity.FRAGMENT_BANNED, false)
                }
                this?.showFragment(MainActivity.FRAGMENT_MUSIC, args)
            }
        }
        viewBinding.btnRecord.setOnClickListener {
            val mainActivity = (mActivity as? MainActivity) ?: return@setOnClickListener
            PermissionUtils.permission(Manifest.permission.RECORD_AUDIO)
                .callback(object : PermissionUtils.PermissionCallback {
                    override fun onGranted() {
                        mainActivity.showFragment(MainActivity.FRAGMENT_RECORD, null)
                    }

                    override fun onDenied(grantedList: MutableList<String>?, deniedList: MutableList<String>?) {
                    }

                })
                .request(mainActivity)
        }
        PermissionUtils.permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .callback(object : PermissionUtils.PermissionCallback {
                override fun onGranted() {

                }

                override fun onDenied(
                    grantedList: MutableList<String>?,
                    deniedList: MutableList<String>?
                ) {

                }

            }).request(mActivity)
    }

    override fun onResume() {
        super.onResume()
        val mActivity = activity as? MainActivity ?: return
        val toolbar = mActivity.findViewById<View>(R.id.toolbar) as Toolbar
        mActivity.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            toolbar.setTitle(R.string.app_name)
        }
        mActivity.setToolbarColor(Color.BLACK, Color.WHITE)
    }
}