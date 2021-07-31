package com.xslczx.basis.android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.*;

public final class ImageUtils {
    private static final RatioAttribute RATIO_ATTRIBUTE;
    private static final CompressAttribute COMPRESS_ATTRIBUTE;
    private static int sPlan = 0;

    static {
        RATIO_ATTRIBUTE = new RatioAttribute();
        COMPRESS_ATTRIBUTE = new CompressAttribute();
    }

    private ImageUtils() {
    }

    /**
     * Get bitmap from specified image path
     */
    public static Bitmap getBitmap(String imgPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = false;
        newOpts.inSampleSize = 1;
        try {
            newOpts.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return BitmapFactory.decodeFile(imgPath, newOpts);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeFile(imgPath, newOpts);
    }

    /**
     * Store bitmap into specified image path
     *
     * @throws FileNotFoundException FileNotFoundException
     */
    public static void storeImage(Bitmap bitmap, String outPath) throws FileNotFoundException {
        try {
            FileOutputStream os = new FileOutputStream(outPath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 尺寸压缩，直接压缩位图
     * 实际上只是设置参数，只有调用start()才会执行动作
     */
    public static RatioAttribute ratio(Bitmap image) {
        sPlan = RatioAttribute.RATIO_BY_BITMAP;
        RATIO_ATTRIBUTE.image = image;
        return RATIO_ATTRIBUTE;
    }

    /**
     * 尺寸压缩，通过图片路径
     * 实际上只是设置参数，只有调用start()才会执行动作
     *
     * @param imgPath image path
     */
    public static RatioAttribute ratio(String imgPath) {
        sPlan = RatioAttribute.RATIO_BY_STR;
        RATIO_ATTRIBUTE.imgPath = imgPath;
        return RATIO_ATTRIBUTE;
    }

    /**
     * 质量压缩，直接压缩位图，并生成压缩图
     * 实际上只是设置参数，只有调用start()才会执行动作
     * Compress by quality,  and generate image to the path specified
     */
    public static CompressAttribute compress(Bitmap bitmap) {
        sPlan = CompressAttribute.COMPRESS_BY_BITMAP;
        COMPRESS_ATTRIBUTE.image = bitmap;
        return COMPRESS_ATTRIBUTE;
    }

    /**
     * 质量压缩，通过图片路径，并生成压缩图
     * 实际上只是设置参数，只有调用start()才会执行动作
     * Compress by quality,  and generate image to the path specified
     *
     * @param needsDelete Whether delete original file after compress
     */
    public static CompressAttribute compress(String imgPath, boolean needsDelete) {
        sPlan = CompressAttribute.COMPRESS_BY_STR;
        COMPRESS_ATTRIBUTE.imgPath = imgPath;
        COMPRESS_ATTRIBUTE.needsDelete = needsDelete;
        return COMPRESS_ATTRIBUTE;
    }

    /**
     * 尺寸压缩真正的属性、操作类
     */
    public static class RatioAttribute {
        private static final int RATIO_BY_BITMAP = 1;
        private static final int RATIO_BY_STR = 2;
        private float pixelW = 480f;
        private float pixelH = 480f;
        private Bitmap image;
        private String imgPath;
        private String outPath;

        public final RatioAttribute setPixel(float w, float h) {
            pixelW = w;
            pixelH = h;
            return RATIO_ATTRIBUTE;
        }

        public final RatioAttribute setOutPath(String path) {
            outPath = path;
            return RATIO_ATTRIBUTE;
        }

        /**
         * 尺寸压缩，直接压缩位图
         * Compress image by size, this will modify image width/height.
         * Used to get thumbnail
         */
        private Bitmap doRatioAndGenThumbByBitmap(Bitmap targetBitmap) throws FileNotFoundException {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            targetBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            if (os.toByteArray().length / 1024 > 10 * 1024) {
                os.reset();
                targetBitmap.compress(Bitmap.CompressFormat.JPEG, 50, os);
            }
            ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
            BitmapFactory.Options newOpts = new BitmapFactory.Options();
            newOpts.inJustDecodeBounds = true;
            newOpts.inPreferredConfig = Bitmap.Config.ARGB_8888;
            BitmapFactory.decodeStream(is, null, newOpts);
            int w = newOpts.outWidth;
            int h = newOpts.outHeight;
            int hh = (int) pixelH;
            int ww = (int) pixelW;
            newOpts.inSampleSize = calculateInSampleSize(w, h, hh, ww);
            newOpts.inJustDecodeBounds = false;
            is = new ByteArrayInputStream(os.toByteArray());
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, newOpts);
            if (outPath != null && !outPath.isEmpty()) {
                storeImage(bitmap, outPath);
                outPath = null;
            }
            return bitmap;
        }

        /**
         * 尺寸压缩，通过图片路径
         * Compress image by pixel, this will modify image width/height.
         * Used to get thumbnail
         *
         * @param targetPath image path
         */
        private Bitmap doRatioAndGenThumbByStr(String targetPath) throws FileNotFoundException {
            BitmapFactory.Options newOpts = new BitmapFactory.Options();
            newOpts.inJustDecodeBounds = true;
            newOpts.inPreferredConfig = Bitmap.Config.ARGB_8888;
            BitmapFactory.decodeFile(targetPath, newOpts);
            int w = newOpts.outWidth;
            int h = newOpts.outHeight;
            int hh = (int) RATIO_ATTRIBUTE.pixelH;
            int ww = (int) RATIO_ATTRIBUTE.pixelW;
            newOpts.inSampleSize = calculateInSampleSize(w, h, ww, hh);
            newOpts.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeFile(targetPath, newOpts);
            if (outPath != null && !outPath.isEmpty()) {
                storeImage(bitmap, outPath);
                outPath = null;
            }
            return bitmap;
        }

        private int calculateInSampleSize(final int outWidth, final int outHeight,
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

        public final Bitmap start() {
            Bitmap bitmap = null;
            try {
                switch (sPlan) {
                    case RATIO_BY_BITMAP:
                        bitmap = doRatioAndGenThumbByBitmap(image);
                        break;
                    case RATIO_BY_STR:
                        bitmap = doRatioAndGenThumbByStr(imgPath);
                        break;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return bitmap;
        }
    }

    /**
     * 质量压缩真正的属性、操作类
     */
    public static class CompressAttribute {
        private static final int COMPRESS_BY_BITMAP = 3;
        private static final int COMPRESS_BY_STR = 4;
        private Bitmap image;
        private String imgPath;
        private int maxSize = 100;
        private boolean needsDelete;
        private String outPath;

        public final CompressAttribute setMaxSize(int mSize) {
            this.maxSize = mSize;
            return COMPRESS_ATTRIBUTE;
        }

        public final CompressAttribute setOutPath(String path) {
            outPath = path;
            return COMPRESS_ATTRIBUTE;
        }

        /**
         * 质量压缩，直接压缩位图，并生成压缩图
         * Compress by quality,  and generate image to the path specified
         *
         * @param targetSize target will be compressed to be smaller than this size.(kb)
         * @throws IOException IOException
         */
        private Bitmap doCompressAndGenImageByBitmap(Bitmap targetBitmap, int targetSize) throws IOException {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            int options = 100;
            targetBitmap.compress(Bitmap.CompressFormat.JPEG, options, os);
            while (os.toByteArray().length / 1024 > targetSize) {
                os.reset();
                options -= 10;
                targetBitmap.compress(Bitmap.CompressFormat.JPEG, options, os);
            }
            if (outPath != null && !outPath.isEmpty()) {
                FileOutputStream fos = new FileOutputStream(outPath);
                fos.write(os.toByteArray());
                fos.flush();
                fos.close();
                outPath = null;
            }
            ByteArrayInputStream isBm =
                    new ByteArrayInputStream(os.toByteArray());
            return BitmapFactory.decodeStream(isBm, null, null);
        }

        /**
         * 质量压缩，通过图片路径，并生成压缩图
         * Compress by quality,  and generate image to the path specified
         *
         * @param targetSize target will be compressed to be smaller than this size.(kb)
         * @param needsDel   Whether delete original file after compress
         * @throws IOException IOException
         */
        private Bitmap doCompressAndGenImageByStr(String targetPath, int targetSize, boolean needsDel)
                throws IOException {
            Bitmap bitmap = doCompressAndGenImageByBitmap(getBitmap(targetPath), targetSize);
            if (needsDel) {
                File file = new File(targetPath);
                if (file.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                }
            }
            return bitmap;
        }

        public final Bitmap start() {
            Bitmap bitmap = null;
            try {
                switch (sPlan) {
                    case COMPRESS_BY_BITMAP:
                        bitmap = doCompressAndGenImageByBitmap(image, maxSize);
                        break;
                    case COMPRESS_BY_STR:
                        bitmap = doCompressAndGenImageByStr(imgPath, maxSize, needsDelete);
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }
    }
}
