package com.xslczx.basis.sample.layer;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import androidx.annotation.*;
import androidx.core.graphics.ColorUtils;

public class GlobalConfig {
    private static final GlobalConfig INSTANCE = new GlobalConfig();
    @IntRange(from = 0)
    public long dialogAnimDuration = 220L;

    // DialogLayer
    @FloatRange(from = 0F, to = 1F)
    public float dialogDimAmount = 0.6F;
    @Nullable
    public Layer.AnimatorCreator dialogBackgroundAnimatorCreator = null;
    @Nullable
    public Layer.AnimatorCreator dialogContentAnimatorCreator = null;
    @Nullable
    public Layer.AnimatorCreator toastAnimatorCreator = null;

    // ToastLayer
    @IntRange(from = 0)
    public long toastAnimDuration = 220L;
    @IntRange(from = 0)
    public long toastDuration = 3000L;
    @DrawableRes
    public int toastBackgroundRes = -1;
    @ColorInt
    public int toastTextColorInt = Color.TRANSPARENT;
    @ColorRes
    public int toastTextColorRes = -1;
    @FloatRange(from = 0F, to = 1F)
    public float toastAlpha = 1;
    public int toastGravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
    public int toastMarginLeft = Integer.MIN_VALUE;
    public int toastMarginTop = Integer.MIN_VALUE;
    public int toastMarginRight = Integer.MIN_VALUE;
    public int toastMarginBottom = Integer.MIN_VALUE;
    @ColorInt
    public int guideBackgroundInt = ColorUtils.setAlphaComponent(Color.BLACK, (int) (255 * 0.5));

    // GuideLayer
    public long notificationDuration = 5000L;

    // NotificationLayer
    public int notificationMaxWidth = -1;
    public int notificationMaxHeight = -1;
    public CharSequence notificationLabel = null;
    public Drawable notificationIcon = null;
    public String notificationTimePattern = null;
    public boolean floatOutside = true;

    // FloatLayer
    public int floatSnapEdge = FloatLayer.Edge.HORIZONTAL;
    @FloatRange(from = -2F, to = 2F)
    public float floatDefPercentX = 2F;
    @FloatRange(from = -2F, to = 2F)
    public float floatDefPercentY = 0.236F;
    @FloatRange(from = 0F, to = 1F)
    public float floatDefAlpha = 1F;
    public float floatDefScale = 1F;
    public float floatPivotX = 0.5F;
    public float floatPivotY = 0.5F;
    @FloatRange(from = 0F, to = 1F)
    public float floatNormalAlpha = 1F;
    public float floatNormalScale = 1F;
    @IntRange(from = 0)
    public long floatLowProfileDelay = 3000L;
    @FloatRange(from = 0F, to = 1F)
    public float floatLowProfileAlpha = 0.8F;
    public float floatLowProfileScale = 1F;
    @FloatRange(from = 0F, to = 1F)
    public float floatLowProfileIndent = 0F;
    public int floatMarginLeft = Integer.MIN_VALUE;
    public int floatMarginTop = Integer.MIN_VALUE;
    public int floatMarginRight = Integer.MIN_VALUE;
    public int floatMarginBottom = Integer.MIN_VALUE;
    public int floatPaddingLeft = 0;
    public int floatPaddingTop = 0;
    public int floatPaddingRight = 0;
    public int floatPaddingBottom = 0;

    @NonNull
    public static GlobalConfig get() {
        return INSTANCE;
    }
}
