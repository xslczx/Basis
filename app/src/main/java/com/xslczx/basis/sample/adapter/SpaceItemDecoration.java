package com.xslczx.basis.sample.adapter;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

    private final int mSpace;
    private final Paint mPaint;
    private int mSpanCount;
    private int mMaxSpanGroupIndex;

    public SpaceItemDecoration(int space, int spaceColor) {
        this.mSpace = space;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(spaceColor);
        mPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * 获取Item的偏移量
     */
    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int position = parent.getChildAdapterPosition(view);
        view.setTag(position);
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            GridLayoutManager.SpanSizeLookup spanSizeLookup = gridLayoutManager.getSpanSizeLookup();
            mSpanCount = gridLayoutManager.getSpanCount();
            mMaxSpanGroupIndex = spanSizeLookup.getSpanGroupIndex(parent.getAdapter().getItemCount() - 1, mSpanCount);
            int spanSize = spanSizeLookup.getSpanSize(position);
            int spanIndex = spanSizeLookup.getSpanIndex(position, mSpanCount);
            int spanGroupIndex = spanSizeLookup.getSpanGroupIndex(position, mSpanCount);
            if (spanSize < mSpanCount && spanIndex != 0) {
                outRect.left = mSpace;
            }
            if (spanGroupIndex != 0) {
                outRect.top = mSpace;
            }
        } else if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            if (position != 0) {
                if (linearLayoutManager.getOrientation() == LinearLayoutManager.HORIZONTAL) {
                    outRect.left = mSpace;
                } else {
                    outRect.top = mSpace;
                }
            }
        }
    }

    /**
     * 绘制分割线
     */
    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            drawSpace(c, parent);
        } else if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            if (linearLayoutManager.getOrientation() == LinearLayoutManager.HORIZONTAL) {
                drawVertical(c, parent);
            } else {
                drawHorizontal(c, parent);
            }
        }
    }

    private void drawHorizontal(Canvas c, RecyclerView parent) {
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) parent.getLayoutManager();
        int top, bottom, left, right;
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            int position = (int) child.getTag();
            if (position == linearLayoutManager.getItemCount() - 1) continue;
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            top = child.getBottom() + layoutParams.bottomMargin;
            bottom = top + mSpace;
            left = child.getLeft() - layoutParams.leftMargin;
            right = child.getRight() + layoutParams.rightMargin;
            c.drawRect(left, top, right, bottom, mPaint);
        }
    }

    private void drawVertical(Canvas c, RecyclerView parent) {
        int top, bottom, left, right;
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            int position = (int) child.getTag();
            if (position == 0) continue;
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            top = child.getTop() - layoutParams.topMargin;
            bottom = child.getBottom() + layoutParams.bottomMargin;
            left = child.getLeft() - layoutParams.leftMargin - mSpace;
            right = left + mSpace;
            c.drawRect(left, top, right, bottom, mPaint);
        }
    }

    /**
     * 绘制分割线
     */
    private void drawSpace(Canvas canvas, RecyclerView parent) {
        GridLayoutManager gridLayoutManager = (GridLayoutManager) parent.getLayoutManager();
        int spanCount = gridLayoutManager.getSpanCount();
        GridLayoutManager.SpanSizeLookup spanSizeLookup = gridLayoutManager.getSpanSizeLookup();
        int childCount = parent.getChildCount();
        int top, bottom, left, right;
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            int position = (int) child.getTag();
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            int spanGroupIndex = spanSizeLookup.getSpanGroupIndex(position, spanCount);
            int spanSize = spanSizeLookup.getSpanSize(position);
            int spanIndex = spanSizeLookup.getSpanIndex(position, spanCount);
            if (spanGroupIndex < mMaxSpanGroupIndex) {
                top = child.getBottom() + layoutParams.bottomMargin;
                bottom = top + mSpace;
                left = child.getLeft() - layoutParams.leftMargin;
                right = child.getRight() + layoutParams.rightMargin + mSpace;
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
            if (spanSize != mSpanCount && spanIndex != 0) {
                top = child.getTop() - layoutParams.topMargin;
                bottom = child.getBottom() + layoutParams.bottomMargin;
                left = child.getLeft() - layoutParams.leftMargin - mSpace;
                right = left + mSpace;
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
        }
    }
}
