package com.xslczx.basis.sample.ui.music

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.squareup.picasso.Picasso
import com.xslczx.basis.android.SizeUtils
import com.xslczx.basis.sample.Music
import com.xslczx.basis.sample.R
import com.xslczx.basis.sample.adapter.BaseQuickAdapter
import com.xslczx.basis.sample.adapter.BaseViewHolder

class MusicAdapter : BaseQuickAdapter<Music, BaseViewHolder>() {

    override fun getDelegateView(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): View {
        return inflater.inflate(R.layout.item_music, parent, false)
    }

    override fun convert(context: Context, holder: BaseViewHolder, t: Music, position: Int) {
        holder.setText(R.id.i_tv_music_title, "${t.title}(${t.author})[${t.sort}]")
            .setImageResource(
                R.id.i_iv_selected, if (isSelected(position)) android.R.drawable.radiobutton_on_background
                else android.R.drawable.radiobutton_off_background
            )
        Picasso.get()
            .load(t.pic)
            .resize(SizeUtils.dp2px(100f), SizeUtils.dp2px(100f))
            .into((holder.getView(R.id.i_iv_album) as ImageView))
    }
}