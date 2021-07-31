package com.xslczx.basis.android;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;

import java.io.*;
import java.util.List;

public final class BitmapUtils {

    private BitmapUtils() {

    }

    /**
     * 设置水印图片在左上角
     */
    public static Bitmap createWaterMaskLeftTop(Context context, Bitmap src, Bitmap watermark,
                                                int paddingLeft, int paddingTop) {
        return createWaterMaskBitmap(src, watermark,
                dp2px(context, paddingLeft), dp2px(context, paddingTop));
    }

    private static Bitmap createWaterMaskBitmap(Bitmap src, Bitmap watermark, int paddingLeft,
                                                int paddingTop) {
        if (src == null) {
            return null;
        }
        int width = src.getWidth();
        int height = src.getHeight();
        //创建一个bitmap
        Bitmap newb = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);// 创建一个新的和SRC长度宽度一样的位图
        //将该图片作为画布
        Canvas canvas = new Canvas(newb);
        //在画布 0，0坐标上开始绘制原始图片
        canvas.drawBitmap(src, 0, 0, null);
        //在画布上绘制水印图片
        canvas.drawBitmap(watermark, paddingLeft, paddingTop, null);
        // 保存
        canvas.save();
        // 存储
        canvas.restore();
        return newb;
    }

    /**
     * 设置水印图片在右下角
     *
     * @param context 上下文
     */
    public static Bitmap createWaterMaskRightBottom(Context context, Bitmap src, Bitmap watermark,
                                                    int paddingRight, int paddingBottom) {
        return createWaterMaskBitmap(src, watermark,
                src.getWidth() - watermark.getWidth() - dp2px(context, paddingRight),
                src.getHeight() - watermark.getHeight() - dp2px(context, paddingBottom));
    }

    /**
     * 设置水印图片到右上角
     */
    public static Bitmap createWaterMaskRightTop(Context context, Bitmap src, Bitmap watermark,
                                                 int paddingRight, int paddingTop) {
        return createWaterMaskBitmap(src, watermark,
                src.getWidth() - watermark.getWidth() - dp2px(context, paddingRight),
                dp2px(context, paddingTop));
    }

    /**
     * 设置水印图片到左下角
     */
    public static Bitmap createWaterMaskLeftBottom(Context context, Bitmap src, Bitmap watermark,
                                                   int paddingLeft, int paddingBottom) {
        return createWaterMaskBitmap(src, watermark, dp2px(context, paddingLeft),
                src.getHeight() - watermark.getHeight() - dp2px(context, paddingBottom));
    }

    /**
     * 设置水印图片到中间
     */
    public static Bitmap createWaterMaskCenter(Bitmap src, Bitmap watermark) {
        return createWaterMaskBitmap(src, watermark,
                (src.getWidth() - watermark.getWidth()) / 2,
                (src.getHeight() - watermark.getHeight()) / 2);
    }

    /**
     * 给图片添加文字到左上角
     */
    public static Bitmap drawTextToLeftTop(Context context, Bitmap bitmap, String text, int size,
                                           int color, int paddingLeft, int paddingTop) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize(dp2px(context, size));
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return drawTextToBitmap(context, bitmap, text, paint, bounds,
                dp2px(context, paddingLeft),
                dp2px(context, paddingTop) + bounds.height());
    }

    /**
     * 绘制文字到右下角
     */
    public static Bitmap drawTextToRightBottom(Context context, Bitmap bitmap, String text, int size,
                                               int color, int paddingRight, int paddingBottom) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize(dp2px(context, size));
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return drawTextToBitmap(context, bitmap, text, paint, bounds,
                bitmap.getWidth() - bounds.width() - dp2px(context, paddingRight),
                bitmap.getHeight() - dp2px(context, paddingBottom));
    }

    /**
     * 绘制文字到右上方
     */
    public static Bitmap drawTextToRightTop(Context context, Bitmap bitmap, String text, int size,
                                            int color, int paddingRight, int paddingTop) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize(dp2px(context, size));
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return drawTextToBitmap(context, bitmap, text, paint, bounds,
                bitmap.getWidth() - bounds.width() - dp2px(context, paddingRight),
                dp2px(context, paddingTop) + bounds.height());
    }

    /**
     * 绘制文字到左下方
     */
    public static Bitmap drawTextToLeftBottom(Context context, Bitmap bitmap, String text, int size,
                                              int color, int paddingLeft, int paddingBottom) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize(dp2px(context, size));
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return drawTextToBitmap(context, bitmap, text, paint, bounds,
                dp2px(context, paddingLeft),
                bitmap.getHeight() - dp2px(context, paddingBottom));
    }

    /**
     * 绘制文字到中间
     */
    public static Bitmap drawTextToCenter(Context context, Bitmap bitmap, String text, int size,
                                          int color) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize(dp2px(context, size));
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return drawTextToBitmap(context, bitmap, text, paint, bounds,
                (bitmap.getWidth() - bounds.width()) / 2,
                (bitmap.getHeight() + bounds.height()) / 2);
    }

    //图片上绘制文字
    private static Bitmap drawTextToBitmap(Context context, Bitmap bitmap, String text, Paint paint,
                                           Rect bounds, int paddingLeft, int paddingTop) {
        Bitmap.Config bitmapConfig = bitmap.getConfig();

        paint.setDither(true); // 获取跟清晰的图像采样
        paint.setFilterBitmap(true);// 过滤一些
        if (bitmapConfig == null) {
            bitmapConfig = Bitmap.Config.ARGB_8888;
        }
        Bitmap result = bitmap.copy(bitmapConfig, true);
        Canvas canvas = new Canvas(result);
        canvas.drawText(text, paddingLeft, paddingTop, paint);
        return result;
    }

    /**
     * dip转pix
     */
    private static int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    /**
     * bitmap转byte[]
     *
     * @param bitmap  源bitmap
     * @param format  格式
     * @param quality 质量
     * @return byte[]
     */
    public static byte[] bitmap2Bytes(final Bitmap bitmap,
                                      final Bitmap.CompressFormat format, int quality) {
        if (isEmptyBitmap(bitmap)) return null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(format, quality, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * byte[]转bitmap
     *
     * @param bytes byte[]
     * @return bitmap
     */
    public static Bitmap bytes2Bitmap(final byte[] bytes) {
        return (bytes == null || bytes.length == 0)
                ? null
                : BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * drawable转bitmap
     *
     * @param drawable drawable
     * @return bitmap
     */
    public static Bitmap drawable2Bitmap(final Drawable drawable) {
        if (drawable == null) return null;
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }
        Bitmap bitmap;
        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1,
                    drawable.getOpacity() != PixelFormat.OPAQUE
                            ? Bitmap.Config.ARGB_8888
                            : Bitmap.Config.RGB_565);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    drawable.getOpacity() != PixelFormat.OPAQUE
                            ? Bitmap.Config.ARGB_8888
                            : Bitmap.Config.RGB_565);
        }
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * bitmap转drawable
     *
     * @param context 上下文
     * @param bitmap  bitmap
     * @return BitmapDrawable
     */
    public static Drawable bitmap2Drawable(Context context, final Bitmap bitmap) {
        return bitmap == null ? null : new BitmapDrawable(context.getResources(), bitmap);
    }

    /**
     * 合并Bitmap
     *
     * @param bgd 背景Bitmap
     * @param fg  前景Bitmap
     * @return 合成后的Bitmap
     */
    public static Bitmap combineImages(Bitmap bgd, Bitmap fg) {
        Bitmap bmp;
        int width = Math.max(bgd.getWidth(), fg.getWidth());
        int height = Math.max(bgd.getHeight(), fg.getHeight());
        bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        Canvas canvas = new Canvas(bmp);
        canvas.drawBitmap(bgd, 0, 0, null);
        canvas.drawBitmap(fg, 0, 0, paint);
        return bmp;
    }

    /**
     * 合并
     *
     * @param bgd 后景Bitmap
     * @param fg  前景Bitmap
     * @return 合成后Bitmap
     */
    public static Bitmap combineImagesToSameSize(Bitmap bgd, Bitmap fg) {
        int width = Math.min(bgd.getWidth(), fg.getWidth());
        int height = Math.min(bgd.getHeight(), fg.getHeight());
        Bitmap bitmapFg = fg;
        Bitmap bitmapBgd = bgd;
        if (fg.getWidth() != width && fg.getHeight() != height) {
            bitmapFg = scaleBitmap(fg, width, height, false);
        }
        if (bgd.getWidth() != width && bgd.getHeight() != height) {
            bitmapBgd = scaleBitmap(bgd, width, height, false);
        }
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        Canvas canvas = new Canvas(bmp);
        canvas.drawBitmap(bitmapBgd, 0, 0, null);
        canvas.drawBitmap(bitmapFg, 0, 0, paint);
        return bmp;
    }

    /**
     * 截图
     *
     * @param view view
     * @return bitmap
     */
    public static Bitmap snapshot(final View view) {
        if (view == null) return null;
        boolean drawingCacheEnabled = view.isDrawingCacheEnabled();
        boolean willNotCacheDrawing = view.willNotCacheDrawing();
        view.setDrawingCacheEnabled(true);
        view.setWillNotCacheDrawing(false);
        Bitmap drawingCache = view.getDrawingCache();
        Bitmap bitmap;
        if (null == drawingCache || drawingCache.isRecycled()) {
            view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
            view.buildDrawingCache();
            drawingCache = view.getDrawingCache();
            if (null == drawingCache || drawingCache.isRecycled()) {
                bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                        Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(bitmap);
                view.draw(canvas);
            } else {
                bitmap = Bitmap.createBitmap(drawingCache);
            }
        } else {
            bitmap = Bitmap.createBitmap(drawingCache);
        }
        view.setWillNotCacheDrawing(willNotCacheDrawing);
        view.setDrawingCacheEnabled(drawingCacheEnabled);
        return bitmap;
    }

    /**
     * 压缩图片尺寸
     *
     * @param file      文件
     * @param maxWidth  限制的最大宽度
     * @param maxHeight 限制的最大高度
     * @return bitmap
     */
    public static Bitmap compressBitmap(final File file, final int maxWidth, final int maxHeight) {
        if (file == null) return null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        options.inSampleSize =
                calculateInSampleSize(options.outWidth, options.outHeight, maxWidth,
                        maxHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    }

    /**
     * 计算采样,不能精确的指定图片的大小
     *
     * @param outWidth  原始宽度
     * @param outHeight 原始高度
     * @param maxWidth  最大宽度
     * @param maxHeight 最大高度
     * @return 采样
     */
    public static int calculateInSampleSize(final int outWidth, final int outHeight,
                                            final int maxWidth,
                                            final int maxHeight) {
        int width = outWidth, height = outHeight;
        int inSampleSize = 1;
        while (height > maxHeight || width > maxWidth) {
            height >>= 1;
            width >>= 1;
            inSampleSize <<= 1;
        }
        return inSampleSize;
    }

    /**
     * 计算采样,不能精确的指定图片的大小
     *
     * @param outWidth  原始宽度
     * @param outHeight 原始高度
     * @param reqWidth  最大宽度
     * @param reqHeight 最大高度
     * @return 采样
     */
    public static int calculateInSampleSize2(final int outWidth, final int outHeight,
                                             final int reqWidth,
                                             final int reqHeight) {
        int inSampleSize = 1;
        if (outHeight > reqHeight || outWidth > reqWidth) {
            final int suitedValue = Math.max(reqHeight, reqWidth);
            final int heightRatio = Math.round((float) outHeight / (float) suitedValue);
            final int widthRatio = Math.round((float) outWidth / (float) suitedValue);
            inSampleSize = Math.max(heightRatio, widthRatio);
        }
        return inSampleSize;
    }

    /**
     * 压缩图片尺寸
     *
     * @param is        输入流
     * @param maxWidth  限制的最大宽度
     * @param maxHeight 限制的最大高度
     * @return bitmap
     */
    public static Bitmap compressBitmap(final InputStream is, final int maxWidth,
                                        final int maxHeight) {
        if (is == null) return null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, options);
        options.inSampleSize =
                calculateInSampleSize(options.outWidth, options.outHeight, maxWidth,
                        maxHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(is, null, options);
    }

    /**
     * @param fd        文件描述符
     * @param maxWidth  限制的最大宽度
     * @param maxHeight 限制的最大高度
     * @return bitmap
     */
    public static Bitmap compressBitmap(final FileDescriptor fd, final int maxWidth,
                                        final int maxHeight) {
        if (fd == null) return null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd, null, options);
        options.inSampleSize =
                calculateInSampleSize(options.outWidth, options.outHeight, maxWidth,
                        maxHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fd, null, options);
    }

    /**
     * @param context     上下文
     * @param drawableRes 图片资源id
     * @param maxWidth    限制的最大宽度
     * @param maxHeight   限制的最大高度
     * @return bitmap
     */
    public static Bitmap compressBitmap(Context context, final int drawableRes,
                                        final int maxWidth,
                                        final int maxHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        final Resources resources = context.getResources();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, drawableRes, options);
        options.inSampleSize =
                calculateInSampleSize(options.outWidth, options.outHeight, maxWidth,
                        maxHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(resources, drawableRes, options);
    }

    /**
     * 缩放到指定宽高
     *
     * @param bitmap    源bitmap
     * @param newWidth  新宽
     * @param newHeight 新高
     * @param degree    旋转角度
     * @return 生成bitmap
     */
    public static Bitmap scaleBitmap(Bitmap bitmap, int newWidth, int newHeight, int degree) {
        float initScale;
        float initTranslateX;
        float initTranslateY;
        if (bitmap.getWidth() * 1.0f / bitmap.getHeight() > newWidth * 1.0f / newHeight) {
            initScale = newWidth * 1.0f / bitmap.getWidth();
            initTranslateX = 0;
            initTranslateY = (newHeight - bitmap.getHeight() * initScale) / 2;
        } else {
            initScale = newHeight * 1.0f / bitmap.getHeight();
            initTranslateX = (newWidth - bitmap.getWidth() * initScale) / 2;
            initTranslateY = 0;
        }
        Matrix matrix = new Matrix();
        matrix.postScale(initScale, initScale);
        matrix.postTranslate(initTranslateX, initTranslateY);
        Bitmap newBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(bitmap, matrix, null);
        matrix.setRotate(degree);
        newBitmap =
                Bitmap.createBitmap(newBitmap, 0, 0, newBitmap.getWidth(), newBitmap.getHeight(), matrix,
                        true);
        return newBitmap;
    }

    /**
     * 缩放bitmap
     *
     * @param src         源bitmap
     * @param scaleWidth  宽
     * @param scaleHeight 高
     * @param recycle     是否回收
     * @return 生成bitmap
     */
    public static Bitmap scaleBitmap(final Bitmap src,
                                     final float scaleWidth,
                                     final float scaleHeight,
                                     final boolean recycle) {
        if (isEmptyBitmap(src)) return null;
        Matrix matrix = new Matrix();
        matrix.setScale(scaleWidth, scaleHeight);
        Bitmap ret = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        if (recycle && !src.isRecycled() && ret != src) src.recycle();
        return ret;
    }

    /**
     * 在不加载图片的情况下获取尺寸
     *
     * @param context 上下文
     * @param rawRes  图片资源id
     * @return 尺寸[width, height]
     */
    public static int[] getBitmapSize(Context context, final int rawRes) {
        TypedValue value = new TypedValue();
        BitmapFactory.Options options = new BitmapFactory.Options();
        context.getResources().openRawResource(rawRes, value);
        options.inTargetDensity = value.density;
        options.inScaled = false;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), rawRes, options);
        return new int[]{options.outWidth, options.outHeight};
    }

    /**
     * 在不加载图片的情况下获取尺寸
     *
     * @param path 文件路径
     * @return 尺寸[width, height]
     */
    public static int[] getBitmapSize(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        return new int[]{options.outWidth, options.outHeight};
    }

    /**
     * 裁剪或者缩放适合尺寸的图片
     *
     * @param src    源bitmap
     * @param width  宽
     * @param height 高
     * @return 生成bitmap
     */
    public static Bitmap getFitBitmap(Bitmap src, int width, int height) {
        Bitmap dest = null;
        if (src.getWidth() >= width && src.getHeight() >= height) {
            int startX = (src.getWidth() - width) / 2;
            int startY = (src.getHeight() - height) / 2;
            dest = Bitmap.createBitmap(src, startX, startY, width, height);
        } else if (src.getWidth() <= width && src.getHeight() >= height) {
            Bitmap white = Bitmap.createBitmap(width, src.getHeight(), Bitmap.Config.ARGB_8888);

            int startX = (src.getWidth() - width) / 2;
            int startY = (src.getHeight() - height) / 2;
            Canvas canvas = new Canvas(white);
            canvas.drawBitmap(src, Math.abs(startX), startY, new Paint());

            dest = Bitmap.createBitmap(white, 0, startY, width, height);
        } else if (src.getWidth() >= width && src.getHeight() <= height) {
            Bitmap white = Bitmap.createBitmap(src.getWidth(), height, Bitmap.Config.ARGB_8888);

            int startX = (src.getWidth() - width) / 2;
            int startY = (src.getHeight() - height) / 2;
            Canvas canvas = new Canvas(white);
            canvas.drawBitmap(src, startX, Math.abs(startY), new Paint());

            dest = Bitmap.createBitmap(white, startX, 0, width, height);
        } else if (src.getWidth() <= width && src.getHeight() <= height) {
            Bitmap white = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            int startX = (src.getWidth() - width) / 2;
            int startY = (src.getHeight() - height) / 2;
            Canvas canvas = new Canvas(white);
            canvas.drawBitmap(src, Math.abs(startX), Math.abs(startY), new Paint());

            dest = Bitmap.createBitmap(white, 0, 0, width, height);
        }
        return dest;
    }

    private static void recycleBitmap(Bitmap bitmap) {
        if (null != bitmap && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    /**
     * 回收bitmap
     *
     * @param bitmaps 回收的bitmap
     */
    public static void recycleBitmap(Bitmap... bitmaps) {
        for (Bitmap b : bitmaps) {
            recycleBitmap(b);
        }
    }

    /**
     * 回收bitmap
     *
     * @param bitmaps 回收的bitmap
     */
    public static void recycleBitmap(List<Bitmap> bitmaps) {
        for (Bitmap b : bitmaps) {
            recycleBitmap(b);
        }
    }

    /**
     * 调整图片旋转角度
     *
     * @param bm                源bitmap
     * @param orientationDegree 旋转角度
     * @return 生成bitmap
     */
    public static Bitmap rotateBitmap(Bitmap bm, final int orientationDegree) {
        Matrix m = new Matrix();
        m.setRotate(orientationDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        float targetX, targetY;
        if (orientationDegree == 90) {
            targetX = bm.getHeight();
            targetY = 0;
        } else {
            targetX = bm.getHeight();
            targetY = bm.getWidth();
        }
        final float[] values = new float[9];
        m.getValues(values);
        float x1 = values[Matrix.MTRANS_X];
        float y1 = values[Matrix.MTRANS_Y];
        m.postTranslate(targetX - x1, targetY - y1);
        Bitmap bm1 = Bitmap.createBitmap(bm.getHeight(), bm.getWidth(), Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        Canvas canvas = new Canvas(bm1);
        canvas.drawBitmap(bm, m, paint);
        return bm1;
    }

    /**
     * 是否是有效bitmap
     *
     * @param src 源bitmap
     * @return 如果有效返回true
     */
    public static boolean isValidBitmap(Bitmap src) {
        return !isEmptyBitmap(src) && !src.isRecycled();
    }

    /**
     * 灰度处理
     *
     * @param src     源bitmap
     * @param recycle 是否回收
     * @return 生成bitmap
     */
    public static Bitmap toGray(final Bitmap src, final boolean recycle) {
        if (isEmptyBitmap(src)) return null;
        Bitmap ret = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        Canvas canvas = new Canvas(ret);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter colorMatrixColorFilter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(colorMatrixColorFilter);
        canvas.drawBitmap(src, 0, 0, paint);
        if (recycle && !src.isRecycled() && ret != src) src.recycle();
        return ret;
    }

    /**
     * bitmap 保存到文件
     *
     * @param src     源bitmap
     * @param file    文件
     * @param format  格式
     * @param quality 质量
     * @param recycle 是否回收
     * @return 如果成功返回true
     */
    public static boolean saveBitmap(final Bitmap src,
                                     final File file,
                                     final Bitmap.CompressFormat format,
                                     final int quality,
                                     final boolean recycle) {
        if (isEmptyBitmap(src)) {
            return false;
        }
        if (src.isRecycled()) {
            return false;
        }
        if (!createFileByDeleteOldFile(file)) {
            return false;
        }
        OutputStream os = null;
        boolean ret = false;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file));
            ret = src.compress(format, quality, os);
            if (recycle && !src.isRecycled()) src.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    private static boolean createFileByDeleteOldFile(final File file) {
        if (file == null) return false;
        if (file.exists() && !file.delete()) return false;
        if (!createOrExistsDir(file.getParentFile())) return false;
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean createOrExistsDir(final File file) {
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    private static boolean isEmptyBitmap(Bitmap src) {
        return src == null || src.getWidth() == 0 || src.getHeight() == 0;
    }
}
