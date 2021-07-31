package com.xslczx.basis.java;

public final class CalculateUtils {
    private CalculateUtils() {
    }

    /**
     * 按比例计算适应最大宽高后的尺寸
     *
     * @param maxWidth    最大宽度
     * @param maxHeight   最大高度
     * @param aspectRatio 比例
     * @return 尺寸[width, height]
     */
    public static float[] calculateFitSize(float maxWidth, float maxHeight, float aspectRatio) {
        float maxRatio = maxWidth / maxHeight;
        float actualWidth, actualHeight;
        if (aspectRatio < maxRatio) {
            actualHeight = maxHeight;
            actualWidth = aspectRatio * actualHeight;
        } else if (aspectRatio > maxRatio) {
            actualWidth = maxWidth;
            actualHeight = maxWidth / aspectRatio;
        } else {
            actualHeight = maxHeight;
            actualWidth = maxWidth;
        }
        return new float[]{actualWidth, actualHeight};
    }

    /**
     * 计算缩放比例 ( 根据宽度比例转换高度 )
     *
     * @param targetWidth   需要的最终宽度
     * @param currentWidth  当前宽度
     * @param currentHeight 当前高度
     * @return float[] { 宽度, 高度 }
     */
    public static float[] calcScaleToWidth(
            final float targetWidth,
            final float currentWidth,
            final float currentHeight) {
        if (currentWidth == 0f) {
            return new float[]{0f, 0f};
        }
        // 计算比例
        float scale = targetWidth / currentWidth;
        // 计算缩放后的高度
        float scaleHeight = scale * currentHeight;
        // 返回对应的数据
        return new float[]{targetWidth, scaleHeight};
    }

    /**
     * 计算缩放比例 ( 根据高度比例转换宽度 )
     *
     * @param targetHeight  需要的最终高度
     * @param currentWidth  当前宽度
     * @param currentHeight 当前高度
     * @return float[] { 宽度, 高度 }
     */
    public static float[] calcScaleToHeight(
            final float targetHeight,
            final float currentWidth,
            final float currentHeight) {
        if (currentHeight == 0f) {
            return new float[]{0f, 0f};
        }
        // 计算比例
        float scale = targetHeight / currentHeight;
        // 计算缩放后的宽度
        float scaleWidth = scale * currentWidth;
        // 返回对应的数据
        return new float[]{scaleWidth, targetHeight};
    }

    /**
     * 通过宽度、高度根据对应的比例, 转换成对应的比例宽度高度 ( 智能转换 )
     *
     * @param width       宽度
     * @param height      高度
     * @param widthScale  宽度比例
     * @param heightScale 高度比例
     * @return float[] { 宽度, 高度 }
     */
    public static float[] calcWidthHeightToScale(
            final float width,
            final float height,
            final float widthScale,
            final float heightScale) {
        // 如果宽度的比例, 大于等于高度比例
        if (widthScale >= heightScale) { // 以宽度为基准
            // 设置宽度, 以宽度为基准
            float scaleWidth = width;
            // 计算宽度
            float scaleHeight = scaleWidth * (heightScale / widthScale);
            // 返回对应的比例
            return new float[]{scaleWidth, scaleHeight};
        } else { // 以高度为基准
            // 设置高度
            float scaleHeight = height;
            // 同步缩放比例
            float scaleWidth = scaleHeight * (widthScale / heightScale);
            // 返回对应的比例
            return new float[]{scaleWidth, scaleHeight};
        }
    }

    /**
     * 以宽度为基准, 转换对应比例的高度
     *
     * @param width       宽度
     * @param widthScale  宽度比例
     * @param heightScale 高度比例
     * @return float[] { 宽度, 高度 }
     */
    public static float[] calcWidthToScale(
            final float width,
            final float widthScale,
            final float heightScale) {
        // 设置宽度
        float scaleWidth = width;
        // 计算高度
        float scaleHeight = scaleWidth * (heightScale / widthScale);
        // 返回对应的比例
        return new float[]{scaleWidth, scaleHeight};
    }

    /**
     * 以高度为基准, 转换对应比例的宽度
     *
     * @param height      高度
     * @param widthScale  宽度比例
     * @param heightScale 高度比例
     * @return float[] { 宽度, 高度 }
     */
    public static float[] calcHeightToScale(
            final float height,
            final float widthScale,
            final float heightScale) {
        // 设置高度
        float scaleHeight = height;
        // 计算宽度
        float scaleWidth = scaleHeight * (widthScale / heightScale);
        // 返回对应的比例
        return new float[]{scaleWidth, scaleHeight};
    }
}
