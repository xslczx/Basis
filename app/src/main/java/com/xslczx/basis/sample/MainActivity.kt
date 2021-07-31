package com.xslczx.basis.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.xslczx.basis.sample.databinding.ActivityMainBinding
import com.xslczx.basis.sample.ui.music.MusicFragment

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG_FRAGMENT_NAME = "fragment_name"
        const val FRAGMENT_MUSIC = "fragment_music"
        const val FRAGMENT_MAIN = "fragment_main"
        const val FRAGMENT_BANNED = "banned"
    }

    private lateinit var viewBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        setSupportActionBar(findViewById(R.id.toolbar))
        val fragment: Fragment = MainFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.contentFragment, fragment, FRAGMENT_MAIN)
            .setPrimaryNavigationFragment(fragment)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        val tag = intent.getStringExtra(TAG_FRAGMENT_NAME)
        tag?.let {
            showFragment(it, null)
        }
    }

    fun showFragment(tag: String, args: Bundle?) {
        if (isFinishing || isDestroyed) {
            return
        }
        val fm = supportFragmentManager
        var fragment = fm.findFragmentByTag(tag)
        if (fragment == null) {
            when (tag) {
                FRAGMENT_MUSIC -> fragment = MusicFragment().also {
                    it.arguments = args
                }
                else -> throw IllegalArgumentException("Failed to create fragment: unknown tag $tag")
            }
        }
        val trx = fm.beginTransaction()
        trx.replace(R.id.contentFragment, fragment, tag)
            .addToBackStack(tag)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()
    }
}