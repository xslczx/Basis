package com.xslczx.basis.android;

import com.xslczx.basis.java.StringUtils;

import java.util.*;

/**
 * 颜色工具类
 *
 */
public final class ColorUtils {

    // 内部解析器
    private static ColorInfo.Parser sParser;

    /**
     * 0-255 十进值转换成十六进制, 如 255 就是 ff
     * 255 * 0.x = 十进制 转 十六进制
     * <p></p>
     * 透明度 0 - 100
     * 00、19、33、4C、66、7F、99、B2、CC、E5、FF
     * 透明度 5 - 95
     * 0D、26、40、59、73、8C、A6、BF、D9、F2
     */

    static {
        // 设置 Color 解析器
        setParser(new ColorInfo.ColorParser());
    }

    private ColorUtils() {
    }

    /**
     * 获取十六进制透明度字符串
     *
     * @param alpha 0-255
     * @return 透明度 ( 十六进制 ) 值
     */
    public static String hexAlpha(final int alpha) {
        try {
            if (alpha >= 0 && alpha <= 255) {
                return Integer.toHexString(alpha);
            }
        } catch (Exception ignored) {

        }
        return null;
    }

    /**
     * 返回一个颜色 ARGB 色值数组 ( 返回十进制 )
     *
     * @param color argb/rgb color
     * @return ARGB 色值数组 { alpha, red, green, blue }
     */
    public static int[] getARGB(final int color) {
        int[] argb = new int[4];
        if (ColorUtils.isARGB(color)) {
            argb[0] = alpha(color);
        } else {
            argb[0] = 255;
        }
        argb[1] = red(color);
        argb[2] = green(color);
        argb[3] = blue(color);
        return argb;
    }

    /**
     * 返回一个颜色中的透明度值 ( 返回十进制 )
     *
     * @param color argb color
     * @return alpha 值
     */
    public static int alpha(final int color) {
        return color >>> 24;
    }

    /**
     * 返回一个颜色中的透明度百分比值
     *
     * @param color argb color
     * @return alpha 百分比值
     */
    public static float alphaPercent(final int color) {
        return percent(alpha(color), 255).floatValue();
    }

    /**
     * 返回一个颜色中红色的色值 ( 返回十进制 )
     *
     * @param color argb/rgb color
     * @return red 值
     */
    public static int red(final int color) {
        return (color >> 16) & 0xFF;
    }

    /**
     * 返回一个颜色中红色的百分比值
     *
     * @param color argb/rgb color
     * @return red 百分比值
     */
    public static float redPercent(final int color) {
        return percent(red(color), 255).floatValue();
    }

    /**
     * 计算百分比值 ( 最大 100% )
     *
     * @param value 指定值
     * @param max   最大值
     * @return 百分比值
     */
    private static Double percent(
            final double value,
            final double max
    ) {
        if (max <= 0) return 0D;
        if (value <= 0) return 0D;
        if (value >= max) return 1D;
        return value / max;
    }

    /**
     * 返回一个颜色中绿色的色值 ( 返回十进制 )
     *
     * @param color argb/rgb color
     * @return green 值
     */
    public static int green(final int color) {
        return (color >> 8) & 0xFF;
    }


    /**
     * 返回一个颜色中绿色的百分比值
     *
     * @param color argb/rgb color
     * @return green 百分比值
     */
    public static float greenPercent(final int color) {
        return percent(green(color), 255).floatValue();
    }

    /**
     * 返回一个颜色中蓝色的色值 ( 返回十进制 )
     *
     * @param color argb/rgb color
     * @return blue 值
     */
    public static int blue(final int color) {
        return color & 0xFF;
    }


    /**
     * 返回一个颜色中蓝色的百分比值
     *
     * @param color argb/rgb color
     * @return blue 百分比值
     */
    public static float bluePercent(final int color) {
        return percent(blue(color), 255).floatValue();
    }

    /**
     * 根据对应的 red、green、blue 生成一个颜色值
     *
     * @param red   红色值 [0-255]
     * @param green 绿色值 [0-255]
     * @param blue  蓝色值 [0-255]
     * @return rgb 颜色值
     */
    public static int rgb(
            final int red,
            final int green,
            final int blue
    ) {
        return 0xff000000 | (red << 16) | (green << 8) | blue;
    }


    /**
     * 根据对应的 red、green、blue 生成一个颜色值
     *
     * @param red   红色值 [0-255]
     * @param green 绿色值 [0-255]
     * @param blue  蓝色值 [0-255]
     * @return rgb 颜色值
     */
    public static int rgb(
            final float red,
            final float green,
            final float blue
    ) {
        return 0xff000000 |
                ((int) (red * 255.0f + 0.5f) << 16) |
                ((int) (green * 255.0f + 0.5f) << 8) |
                (int) (blue * 255.0f + 0.5f);
    }

    /**
     * 根据对应的 alpha、red、green、blue 生成一个颜色值 ( 含透明度 )
     *
     * @param alpha 透明度 [0-255]
     * @param red   红色值 [0-255]
     * @param green 绿色值 [0-255]
     * @param blue  蓝色值 [0-255]
     * @return argb 颜色值
     */
    public static int argb(
            final int alpha,
            final int red,
            final int green,
            final int blue
    ) {
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }


    /**
     * 根据对应的 alpha、red、green、blue 生成一个颜色值 ( 含透明度 )
     *
     * @param alpha 透明度 [0-255]
     * @param red   红色值 [0-255]
     * @param green 绿色值 [0-255]
     * @param blue  蓝色值 [0-255]
     * @return argb 颜色值
     */
    public static int argb(
            final float alpha,
            final float red,
            final float green,
            final float blue
    ) {
        return ((int) (alpha * 255.0f + 0.5f) << 24) |
                ((int) (red * 255.0f + 0.5f) << 16) |
                ((int) (green * 255.0f + 0.5f) << 8) |
                (int) (blue * 255.0f + 0.5f);
    }

    /**
     * 判断颜色 RGB 是否有效
     *
     * @param color rgb color
     * @return {@code true} yes, {@code false} no
     */
    public static boolean isRGB(final int color) {
        int red = red(color);
        int green = green(color);
        int blue = blue(color);
        return (red <= 255 && red >= 0) &&
                (green <= 255 && green >= 0) &&
                (blue <= 255 && blue >= 0);
    }

    /**
     * 判断颜色 ARGB 是否有效
     *
     * @param color argb color
     * @return {@code true} yes, {@code false} no
     */
    public static boolean isARGB(final int color) {
        int alpha = alpha(color);
        int red = red(color);
        int green = green(color);
        int blue = blue(color);
        return (alpha <= 255 && alpha >= 0) &&
                (red <= 255 && red >= 0) &&
                (green <= 255 && green >= 0) &&
                (blue <= 255 && blue >= 0);
    }

    /**
     * 设置透明度
     *
     * @param color argb/rgb color
     * @param alpha 透明度 [0-255]
     * @return argb 颜色值
     */
    public static int setAlpha(
            final int color,
            final int alpha
    ) {
        return (color & 0x00ffffff) | (alpha << 24);
    }

    /**
     * 设置透明度
     *
     * @param color argb/rgb color
     * @param alpha 透明度 [0-255]
     * @return argb 颜色值
     */
    public static int setAlpha(
            final int color,
            final float alpha
    ) {
        return (color & 0x00ffffff) | ((int) (alpha * 255.0f + 0.5f) << 24);
    }

    /**
     * 改变颜色值中的红色色值
     *
     * @param color argb/rgb color
     * @param red   红色值 [0-255]
     * @return argb/rgb 颜色值
     */
    public static int setRed(
            final int color,
            final int red
    ) {
        return (color & 0xff00ffff) | (red << 16);
    }

    /**
     * 改变颜色值中的红色色值
     *
     * @param color argb/rgb color
     * @param red   红色值 [0-255]
     * @return argb/rgb 颜色值
     */
    public static int setRed(
            final int color,
            final float red
    ) {
        return (color & 0xff00ffff) | ((int) (red * 255.0f + 0.5f) << 16);
    }

    /**
     * 改变颜色值中的绿色色值
     *
     * @param color argb/rgb color
     * @param green 绿色值 [0-255]
     * @return argb/rgb 颜色值
     */
    public static int setGreen(
            final int color,
            final int green
    ) {
        return (color & 0xffff00ff) | (green << 8);
    }


    /**
     * 改变颜色值中的绿色色值
     *
     * @param color argb/rgb color
     * @param green 绿色值 [0-255]
     * @return argb/rgb 颜色值
     */
    public static int setGreen(
            final int color,
            final float green
    ) {
        return (color & 0xffff00ff) | ((int) (green * 255.0f + 0.5f) << 8);
    }

    /**
     * 改变颜色值中的蓝色色值
     *
     * @param color argb/rgb color
     * @param blue  蓝色值 [0-255]
     * @return argb/rgb 颜色值
     */
    public static int setBlue(
            final int color,
            final int blue
    ) {
        return (color & 0xffffff00) | blue;
    }

    /**
     * 改变颜色值中的蓝色色值
     *
     * @param color argb/rgb color
     * @param blue  蓝色值 [0-255]
     * @return argb/rgb 颜色值
     */
    public static int setBlue(
            final int color,
            final float blue
    ) {
        return (color & 0xffffff00) | (int) (blue * 255.0f + 0.5f);
    }

    /**
     * 解析颜色字符串, 返回对应的颜色值
     *
     * @param colorStr argb/rgb color String
     * @return argb/rgb 颜色值
     */
    private static int priParseColor(final String colorStr) {
        if (colorStr.charAt(0) == '#') {
            // Use a long to avoid rollovers on #ffXXXXXX
            long color = Long.parseLong(colorStr.substring(1), 16);
            if (colorStr.length() == 7) {
                // Set the alpha value
                color |= 0x00000000ff000000;
            } else if (colorStr.length() != 9) {
                throw new IllegalArgumentException("Unknown color");
            }
            return (int) color;
        }
        throw new IllegalArgumentException("Unknown color");
    }


    /**
     * 解析颜色字符串, 返回对应的颜色值
     * <pre>
     *     支持的格式:
     *     #RRGGBB
     *     #AARRGGBB
     * </pre>
     *
     * @param colorStr argb/rgb color String
     * @return argb/rgb 颜色值
     */
    public static int parseColor(final String colorStr) {
        if (colorStr != null) {
            try {
                return priParseColor(colorStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Integer.MAX_VALUE;
    }

    /**
     * 颜色值 转换 RGB 颜色字符串
     *
     * @param colorInt rgb int color
     * @return rgb color String
     */
    public static String intToRgbString(final int colorInt) {
        try {
            int color = colorInt;
            color = color & 0x00ffffff;
            String colorStr = Integer.toHexString(color);
            while (colorStr.length() < 6) {
                colorStr = "0" + colorStr;
            }
            return "#" + colorStr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 颜色值 转换 ARGB 颜色字符串
     *
     * @param colorInt argb int color
     * @return argb color String
     */
    public static String intToArgbString(final int colorInt) {
        try {
            String colorString = Integer.toHexString(colorInt);
            while (colorString.length() < 6) {
                colorString = "0" + colorString;
            }
            while (colorString.length() < 8) {
                colorString = "f" + colorString;
            }
            return "#" + colorString;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取随机颜色值
     *
     * @return 随机颜色值
     */
    public static int getRandomColor() {
        return getRandomColor(true);
    }


    /**
     * 获取随机颜色值
     *
     * @param supportAlpha 是否支持透明度
     * @return argb/rgb 颜色值
     */
    public static int getRandomColor(final boolean supportAlpha) {
        int high = supportAlpha ? (int) (Math.random() * 0x100) << 24 : 0xFF000000;
        return high | (int) (Math.random() * 0x1000000);
    }


    /**
     * 获取随机颜色值字符串
     *
     * @return 随机颜色值
     */
    public static String getRandomColorString() {
        return getRandomColorString(true);
    }

    /**
     * 获取随机颜色值字符串
     *
     * @param supportAlpha 是否支持透明度
     * @return 随机颜色值
     */
    public static String getRandomColorString(final boolean supportAlpha) {
        if (supportAlpha) {
            return intToArgbString(getRandomColor(supportAlpha));
        } else {
            return intToRgbString(getRandomColor(supportAlpha));
        }
    }

    /**
     * 判断是否为 ARGB 格式的十六进制颜色, 例如: FF990587
     *
     * @param colorStr color String
     * @return {@code true} yes, {@code false} no
     */
    public static boolean judgeColorString(final String colorStr) {
        if (colorStr != null && colorStr.length() == 8) {
            for (int i = 0; i < colorStr.length(); i++) {
                char cc = colorStr.charAt(i);
                return !(cc != '0' && cc != '1' && cc != '2' && cc != '3' && cc != '4' && cc != '5' && cc != '6' && cc != '7' && cc != '8' && cc != '9'
                        && cc != 'A' && cc != 'B' && cc != 'C' && cc != 'D' && cc != 'E' && cc != 'F'
                        && cc != 'a' && cc != 'b' && cc != 'c' && cc != 'd' && cc != 'e' && cc != 'f');
            }
        }
        return false;
    }

    /**
     * 颜色加深 ( 单独修改 RGB 值, 不变动透明度 )
     *
     * @param colorStr  color String
     * @param darkValue 加深值
     * @return 加深后的颜色值
     */
    public static int setDark(
            final String colorStr,
            final int darkValue
    ) {
        int color = parseColor(colorStr);
        return setDark(color, darkValue);
    }

    /**
     * 颜色加深 ( 单独修改 RGB 值, 不变动透明度 )
     *
     * @param color     int color
     * @param darkValue 加深值
     * @return 加深后的颜色值
     */
    public static int setDark(
            final int color,
            final int darkValue
    ) {
        int red = red(color);
        int green = green(color);
        int blue = blue(color);
        // 进行加深 ( 累减 )
        red -= darkValue;
        green -= darkValue;
        blue -= darkValue;
        // 颜色值
        int colorTemp = color;
        // 进行设置
        colorTemp = setRed(colorTemp, clamp(red, 255, 0));
        colorTemp = setGreen(colorTemp, clamp(green, 255, 0));
        colorTemp = setBlue(colorTemp, clamp(blue, 255, 0));
        return colorTemp;
    }

    /**
     * 返回的 value 介于 max、min 之间, 若 value 小于 min, 返回 min, 若大于 max, 返回 max
     *
     * @param value 指定值
     * @param max   最大值
     * @param min   最小值
     * @return 介于 max、min 之间的 value
     */
    private static int clamp(
            final int value,
            final int max,
            final int min
    ) {
        return value > max ? max : Math.max(value, min);
    }

    /**
     * 颜色变浅, 变亮 ( 单独修改 RGB 值, 不变动透明度 )
     *
     * @param colorStr   color String
     * @param lightValue 变亮 ( 变浅 ) 值
     * @return 变亮 ( 变浅 ) 后的颜色值
     */
    public static int setLight(
            final String colorStr,
            final int lightValue
    ) {
        int color = parseColor(colorStr);
        return setLight(color, lightValue);
    }

    /**
     * 颜色变浅, 变亮 ( 单独修改 RGB 值, 不变动透明度 )
     *
     * @param color      int color
     * @param lightValue 变亮 ( 变浅 ) 值
     * @return 变亮 ( 变浅 ) 后的颜色值
     */
    public static int setLight(
            final int color,
            final int lightValue
    ) {
        int red = red(color);
        int green = green(color);
        int blue = blue(color);
        // 进行变浅, 变亮 ( 累加 )
        red += lightValue;
        green += lightValue;
        blue += lightValue;
        // 颜色值
        int colorTemp = color;
        // 进行设置
        colorTemp = setRed(colorTemp, clamp(red, 255, 0));
        colorTemp = setGreen(colorTemp, clamp(green, 255, 0));
        colorTemp = setBlue(colorTemp, clamp(blue, 255, 0));
        return colorTemp;
    }

    /**
     * 设置透明度加深
     *
     * @param colorStr  color String
     * @param darkValue 加深值
     * @return 透明度加深后的颜色值
     */
    public static int setAlphaDark(
            final String colorStr,
            final int darkValue
    ) {
        int color = parseColor(colorStr);
        return setAlphaDark(color, darkValue);
    }


    /**
     * 设置透明度加深
     *
     * @param color     int color
     * @param darkValue 加深值
     * @return 透明度加深后的颜色值
     */
    public static int setAlphaDark(
            final int color,
            final int darkValue
    ) {
        int alpha = alpha(color);
        // 透明度加深
        alpha += darkValue;
        // 进行设置
        return setAlpha(color, clamp(alpha, 255, 0));
    }

    /**
     * 设置透明度变浅
     *
     * @param colorStr   color String
     * @param lightValue 变浅值
     * @return 透明度变浅后的颜色值
     */
    public static int setAlphaLight(
            final String colorStr,
            final int lightValue
    ) {
        int color = parseColor(colorStr);
        return setAlphaLight(color, lightValue);
    }


    /**
     * 设置透明度变浅
     *
     * @param color      int color
     * @param lightValue 变浅值
     * @return 透明度变浅后的颜色值
     */
    public static int setAlphaLight(
            final int color,
            final int lightValue
    ) {
        int alpha = alpha(color);
        // 透明度变浅
        alpha -= lightValue;
        // 进行设置
        return setAlpha(color, clamp(alpha, 255, 0));
    }

    /**
     * 获取灰度值
     *
     * @param colorStr color String
     * @return 灰度值
     */
    public static int grayLevel(final String colorStr) {
        int color = parseColor(colorStr);
        int[] argb = getARGB(color);
        return (int) (argb[1] * 0.299f + argb[2] * 0.587f + argb[3] * 0.114f);
    }

    /**
     * 获取灰度值
     *
     * @param color int color
     * @return 灰度值
     */
    public static int grayLevel(final int color) {
        // [] { alpha, red, green, blue }
        int[] argb = getARGB(color);
        return (int) (argb[1] * 0.299f + argb[2] * 0.587f + argb[3] * 0.114f);
    }

    /**
     * 设置 Color 解析器
     *
     * @param parser {@link ColorInfo.Parser}
     */
    public static void setParser(final ColorInfo.Parser parser) {
        ColorUtils.sParser = parser;
    }

    /**
     * 灰度值排序
     *
     * @param lists 待排序颜色集合
     */
    public static void sortGray(final List<ColorInfo> lists) {
        Collections.sort(lists, new Comparator<ColorInfo>() {
            @Override
            public int compare(
                    ColorUtils.ColorInfo c1,
                    ColorUtils.ColorInfo c2
            ) {
                long diff = c1.getGrayLevel() - c2.getGrayLevel();
                if (diff < 0) {
                    return 1;
                } else if (diff > 0) {
                    return -1;
                }
                return 0;
            }
        });
    }

    /**
     * HSB ( HSV ) HUE 色相排序
     *
     * @param lists 待排序颜色集合
     */
    public static void sortHUE(final List<ColorInfo> lists) {
        Collections.sort(lists, new Comparator<ColorUtils.ColorInfo>() {
            @Override
            public int compare(
                    ColorUtils.ColorInfo c1,
                    ColorUtils.ColorInfo c2
            ) {
                float diff = c1.getHue() - c2.getHue();
                if (diff > 0) {
                    return 1;
                } else if (diff < 0) {
                    return -1;
                }
                return 0;
            }
        });
    }

    /**
     * HSB ( HSV ) Saturation 饱和度排序
     *
     * @param lists 待排序颜色集合
     */
    public static void sortSaturation(final List<ColorInfo> lists) {
        Collections.sort(lists, new Comparator<ColorUtils.ColorInfo>() {
            @Override
            public int compare(
                    ColorUtils.ColorInfo c1,
                    ColorUtils.ColorInfo c2
            ) {
                float diff = c1.getSaturation() - c2.getSaturation();
                if (diff > 0) {
                    return 1;
                } else if (diff < 0) {
                    return -1;
                }
                return 0;
            }
        });
    }

    /**
     * HSB ( HSV ) Brightness 亮度排序
     *
     * @param lists 待排序颜色集合
     */
    public static void sortBrightness(final List<ColorInfo> lists) {
        Collections.sort(lists, new Comparator<ColorUtils.ColorInfo>() {
            @Override
            public int compare(
                    ColorUtils.ColorInfo c1,
                    ColorUtils.ColorInfo c2
            ) {
                float diff = c1.getBrightness() - c2.getBrightness();
                if (diff > 0) {
                    return 1;
                } else if (diff < 0) {
                    return -1;
                }
                return 0;
            }
        });
    }

    /**
     * 使用给定的比例在两种 ARGB 颜色之间进行混合
     *
     * @param color1 第一种 ARGB 颜色
     * @param color2 第二种 ARGB 颜色
     * @param ratio  两种颜色混合比例
     * @return 混合后的颜色
     */
    public static int blendColor(
            final String color1,
            final String color2,
            final float ratio
    ) {
        return blendColor(parseColor(color1), parseColor(color2), ratio);
    }

    /**
     * 使用给定的比例在两种 ARGB 颜色之间进行混合
     * <pre>
     *     android.support.v4.graphics.ColorUtils#blendARGB
     *     混合比:
     *     0.0 将产生 color1
     *     0.5 将产生均匀的混合
     *     1.0 将产生 color2
     * </pre>
     *
     * @param color1 第一种 ARGB 颜色
     * @param color2 第二种 ARGB 颜色
     * @param ratio  两种颜色混合比例
     * @return 混合后的颜色
     */
    public static int blendColor(
            final int color1,
            final int color2,
            final float ratio
    ) {
        int[] color1Argb = getARGB(color1);
        int[] color2Argb = getARGB(color2);

        final float inverseRatio = 1 - ratio;
        float a = color1Argb[0] * inverseRatio + color2Argb[0] * ratio;
        float r = color1Argb[1] * inverseRatio + color2Argb[1] * ratio;
        float g = color1Argb[2] * inverseRatio + color2Argb[2] * ratio;
        float b = color1Argb[3] * inverseRatio + color2Argb[3] * ratio;
        return argb((int) a, (int) r, (int) g, (int) b);
    }

    /**
     * 计算从 startColor 过渡到 endColor 过程中百分比为 ratio 时的颜色值
     *
     * @param startColor 开始颜色值
     * @param endColor   结束颜色值
     * @param ratio      过渡百分比
     * @return 计算后颜色值
     */
    public static int transitionColor(
            final String startColor,
            final String endColor,
            final float ratio
    ) {
        return transitionColor(parseColor(startColor), parseColor(endColor), ratio);
    }

    /**
     * 计算从 startColor 过渡到 endColor 过程中百分比为 ratio 时的颜色值
     *
     * @param startColor 开始颜色值
     * @param endColor   结束颜色值
     * @param ratio      过渡百分比
     * @return 计算后颜色值
     */
    public static int transitionColor(
            final int startColor,
            final int endColor,
            final float ratio
    ) {
        int[] startArgb = getARGB(startColor);
        int[] endArgb = getARGB(endColor);

        int startAlpha = startArgb[0];
        int startRed = startArgb[1];
        int startGreen = startArgb[2];
        int startBlue = startArgb[3];

        int endAlpha = endArgb[0];
        int endRed = endArgb[1];
        int endGreen = endArgb[2];
        int endBlue = endArgb[3];

        float a = (endAlpha - startAlpha) * ratio + startAlpha;
        float r = (endRed - startRed) * ratio + startRed;
        float g = (endGreen - startGreen) * ratio + startGreen;
        float b = (endBlue - startBlue) * ratio + startBlue;
        return argb((int) a, (int) r, (int) g, (int) b);
    }

    /**
     * 颜色信息
     */
    public static class ColorInfo {

        // key
        private final String key;
        // value ( 如: #000000 )
        private final String value;
        // value 解析后的值 ( 如: #000 => #000000 )
        private String valueParser;
        // ARGB/RGB color
        private long valueColor;
        // A、R、G、B
        private int alpha = 255, red = 0, green = 0, blue = 0;
        // 灰度值
        private int grayLevel;
        // H、S、B ( V )
        private float hue, saturation, brightness;

        /**
         * 构造函数
         *
         * @param key   Key
         * @param value Value ( 如: #000000 )
         */
        public ColorInfo(
                final String key,
                final String value
        ) {
            this.key = key;
            this.value = value;
            priConvert();
        }

        /**
         * 构造函数
         *
         * @param key        Key
         * @param valueColor ARGB/RGB color
         */
        public ColorInfo(
                final String key,
                final int valueColor
        ) {
            this(key, ColorUtils.intToArgbString(valueColor));
        }

        /**
         * RGB 转换 HSB
         * <pre>
         *     HSB 等于 HSV, 不同的叫法
         *     {@link android.graphics.Color#RGBToHSV}
         * </pre>
         *
         * @param r       红色值 [0-255]
         * @param g       绿色值 [0-255]
         * @param b       蓝色值 [0-255]
         * @param hsbvals HSB 数组
         * @return [] { hue, saturation, brightness }
         */
        private static float[] RGBtoHSB(
                int r,
                int g,
                int b,
                float[] hsbvals
        ) {
            float hue, saturation, brightness;
            if (hsbvals == null) {
                hsbvals = new float[3];
            }
            int cmax = (r > g) ? r : g;
            if (b > cmax) cmax = b;
            int cmin = (r < g) ? r : g;
            if (b < cmin) cmin = b;

            brightness = ((float) cmax) / 255.0f;
            if (cmax != 0) {
                saturation = ((float) (cmax - cmin)) / ((float) cmax);
            } else {
                saturation = 0;
            }
            if (saturation == 0) {
                hue = 0;
            } else {
                float redc = ((float) (cmax - r)) / ((float) (cmax - cmin));
                float greenc = ((float) (cmax - g)) / ((float) (cmax - cmin));
                float bluec = ((float) (cmax - b)) / ((float) (cmax - cmin));
                if (r == cmax) {
                    hue = bluec - greenc;
                } else if (g == cmax) {
                    hue = 2.0f + redc - bluec;
                } else {
                    hue = 4.0f + greenc - redc;
                }
                hue = hue / 6.0f;
                if (hue < 0) {
                    hue = hue + 1.0f;
                }
            }
            hsbvals[0] = hue;
            hsbvals[1] = saturation;
            hsbvals[2] = brightness;
            return hsbvals;
        }

        /**
         * 获取 Key
         *
         * @return key String
         */
        public String getKey() {
            return key;
        }

        /**
         * 获取 Value
         *
         * @return value String
         */
        public String getValue() {
            return value;
        }

        /**
         * 获取 Value 解析后的值 ( 如: #000 => #000000 )
         *
         * @return value 解析后的值 ( 如: #000 => #000000 )
         */
        public String getValueParser() {
            return valueParser;
        }

        /**
         * 获取 ARGB/RGB color
         *
         * @return ARGB/RGB color
         */
        public long getValueColor() {
            return valueColor;
        }

        /**
         * 返回颜色中的透明度值 ( 返回十进制 )
         *
         * @return alpha 值
         */
        public int getAlpha() {
            return alpha;
        }

        /**
         * 返回颜色中红色的色值 ( 返回十进制 )
         *
         * @return red 值
         */
        public int getRed() {
            return red;
        }

        /**
         * 返回颜色中绿色的色值 ( 返回十进制 )
         *
         * @return green 值
         */
        public int getGreen() {
            return green;
        }

        /**
         * 返回颜色中蓝色的色值 ( 返回十进制 )
         *
         * @return blue 值
         */
        public int getBlue() {
            return blue;
        }

        /**
         * 获取灰度值
         *
         * @return 灰度值
         */
        public int getGrayLevel() {
            return grayLevel;
        }

        /**
         * 获取颜色色调
         *
         * @return 颜色色调
         */
        public float getHue() {
            return hue;
        }

        /**
         * 获取颜色饱和度
         *
         * @return 颜色饱和度
         */
        public float getSaturation() {
            return saturation;
        }

        /**
         * 获取颜色亮度
         *
         * @return 颜色亮度
         */
        public float getBrightness() {
            return brightness;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("key : ").append(key);
            builder.append("\nvalue : ").append(value);
            builder.append("\nvalueParser : ").append(valueParser);
            builder.append("\nalpha : ").append(alpha);
            builder.append("\nred : ").append(red);
            builder.append("\ngreen : ").append(green);
            builder.append("\nblue : ").append(blue);
            builder.append("\ngrayLevel : ").append(grayLevel);
            builder.append("\nintToRgbString : ").append(ColorUtils.intToRgbString((int) valueColor));
            builder.append("\nintToArgbString : ").append(ColorUtils.intToArgbString((int) valueColor));
            return builder.toString();
        }

        /**
         * 内部转换处理
         */
        private void priConvert() {
            String temp = value;
            if (sParser != null) {
                temp = sParser.handleColor(value);
                // 保存解析后的值
                valueParser = temp;
            }
            if (temp == null) return;
            // 转换 long 颜色值
            valueColor = ColorUtils.parseColor(temp);

            int[] argb = ColorUtils.getARGB((int) valueColor);
            // 获取 ARGB
            alpha = argb[0];
            red = argb[1];
            green = argb[2];
            blue = argb[3];
            // 获取灰度值
            grayLevel = (int) (argb[1] * 0.299f + argb[2] * 0.587f + argb[3] * 0.114f);
            // 获取 HSB
            float[] hsbvals = RGBtoHSB(red, green, blue, null);
            hue = hsbvals[0]; // 色调
            saturation = hsbvals[1]; // 饱和度
            brightness = hsbvals[2]; // 亮度
        }

        /**
         * Color 解析器
         */
        public interface Parser {

            /**
             * 处理 color
             *
             * @param value 如: #000000
             * @return 处理后的 value
             */
            String handleColor(String value);
        }

        /**
         * Color 解析器
         */
        public static class ColorParser
                implements Parser {
            @Override
            public String handleColor(String value) {
                if (value == null) return null;
                String color = StringUtils.clearSpace(value);
                char[] chars = color.toCharArray();
                int length = chars.length;
                if (length != 0 && chars[0] == '#') {
                    if (length == 4) {
                        String colorsub = color.substring(1);
                        // #000 => #000000
                        return color + colorsub;
                    } else if (length == 5) {
                        String colorsub = color.substring(3);
                        // #0011 => #00111111
                        return color + colorsub + colorsub;
                    }
                }
                return color;
            }
        }
    }
}