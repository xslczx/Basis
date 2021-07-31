package com.xslczx.basis.android;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.webkit.MimeTypeMap;

import java.io.File;

public final class IntentUtils {
    private IntentUtils() {
    }

    /**
     * 获取跳转「选择文件」的意图
     *
     * @return 意图
     */
    public static Intent getPickFileIntent() {
        return getPickIntent("file/*");
    }

    /**
     * 获取跳转「选择文件」的意图, 指定文件扩展名
     *
     * @param fileExtension 文件扩展名
     * @return 意图
     */
    public static Intent getPickFileIntent(String fileExtension) {
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
        if (type == null) {
            return null;
        }
        return getPickIntent(type);
    }

    /**
     * 获取跳转「选择图片」的意图
     *
     * @return 意图
     */
    public static Intent getPickPhotoIntent() {
        return getPickIntent("image/*");
    }

    /**
     * 获取跳转「选择视频」的意图
     *
     * @return 意图
     */
    public static Intent getPickVideoIntent() {
        return getPickIntent("video/*");
    }

    /**
     * 获取跳转「选择音频」的意图
     *
     * @return 意图
     */
    public static Intent getPickAudioIntent() {
        return getPickIntent("audio/*");
    }

    /**
     * 获取跳转「选择...」的意图
     *
     * @param type mimeType 类型
     * @return 意图
     */
    public static Intent getPickIntent(String type) {
        return new Intent(Intent.ACTION_GET_CONTENT)
                .setType(type)
                .addCategory(Intent.CATEGORY_OPENABLE);
    }

    /**
     * 获取跳转「系统相机」的意图
     *
     * @param uriGeN     Build.VERSION_CODES.N Uri
     * @param outputFile 拍摄图片的输入文件对象
     * @return 意图
     */
    public static Intent getTakePhotoIntent(Context context, Uri uriGeN, File outputFile) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uriGeN);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outputFile));
        }
        return intent;
    }

    /**
     * 获取跳转「系统剪裁」的意图
     *
     * @param uriGeN     Build.VERSION_CODES.N Uri
     * @param inputFile  剪裁图片文件
     * @param outputFile 输出图片文件
     * @param aspectX    输出图片宽高比中的宽
     * @param aspectY    输出图片宽高比中的高
     * @param outputX    输出图片的宽
     * @param outputY    输出图片的高
     * @return 意图
     */
    public static Intent getCropPhotoIntent(Uri uriGeN, File inputFile,
                                            File outputFile, int aspectX, int aspectY, int outputX, int outputY) {
        Uri inputUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            inputUri = uriGeN;
        } else {
            inputUri = Uri.fromFile(inputFile);
        }
        return getCropPhotoIntent(inputUri, Uri.fromFile(outputFile), aspectX, aspectY, outputX,
                outputY);
    }

    /**
     * 获取跳转「系统剪裁」的意图
     *
     * @param inputUri  剪裁图片文件的 Uri
     * @param outputUri 输出图片文件的 Uri
     * @param aspectX   输出图片宽高比中的宽
     * @param aspectY   输出图片宽高比中的高
     * @param outputX   输出图片的宽
     * @param outputY   输出图片的高
     * @return 意图
     */
    public static Intent getCropPhotoIntent(Uri inputUri, Uri outputUri, int aspectX, int aspectY,
                                            int outputX, int outputY) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(inputUri, "image/*");
        intent.putExtra("crop", "true");
        // 指定输出宽高比
        intent.putExtra("aspectX", aspectX);
        intent.putExtra("aspectY", aspectY);
        // 指定输出宽高
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        // 指定输出路径和文件类型
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        if (outputUri.getPath().endsWith(".jpg") || outputUri.getPath().endsWith(".jpeg")) {
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        } else if (outputUri.getPath().endsWith(".png") || inputUri.getPath().endsWith(".png")) {
            intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
        } else {
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        }
        intent.putExtra("return-data", false);
        return intent;
    }

    /**
     * 获取跳转「应用」的意图
     *
     * @param packageName 应用包名
     * @return 意图
     */
    public static Intent getLaunchAppIntent(Context context, String packageName) {
        return context.getPackageManager().getLaunchIntentForPackage(packageName);
    }

    /**
     * 获取跳转「应用组件」的意图
     *
     * @param packageName 应用包名
     * @param className   应用组件的类名
     * @return 意图
     */
    public static Intent getComponentIntent(String packageName, String className) {
        return new Intent(Intent.ACTION_VIEW)
                .setComponent(new ComponentName(packageName, className))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    /**
     * 获取跳转「拨号界面」的意图
     *
     * @return 意图
     */
    public static Intent getDialIntent() {
        return new Intent(Intent.ACTION_DIAL)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    /**
     * 获取跳转「拨号界面」的意图
     *
     * @param phoneNumber 电话号码
     * @return 意图
     */
    public static Intent getDialIntent(String phoneNumber) {
        return new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    /**
     * 获取跳转「拨打电话」的意图, 即直接拨打电话
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.CALL_PHONE"/>}</p>
     *
     * @param phoneNumber 电话号码
     * @return 意图
     */
    public static Intent getCallIntent(String phoneNumber) {
        return new Intent("android.intent.action.CALL", Uri.parse("tel:" + phoneNumber))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    /**
     * 获取跳转「发送短信」的意图
     *
     * @param phoneNumber 电话号码
     * @param content     预设内容
     * @return 意图
     */
    public static Intent getSendSmsIntent(String phoneNumber, String content) {
        return new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phoneNumber))
                .putExtra("sms_body", content)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    /**
     * 获取跳转「联系人」的意图
     *
     * @return 意图
     */
    public static Intent getContactsIntent() {
        return new Intent(Intent.ACTION_VIEW)
                .setData(ContactsContract.Contacts.CONTENT_URI);
    }

    /**
     * 获取跳转「联系人详情」的意图
     *
     * @param contactId 联系人的 contactId
     * @param lookupKey 联系人的 lookupKey
     * @return 意图
     */
    public static Intent getContactDetailIntent(long contactId, String lookupKey) {
        Uri data = ContactsContract.Contacts.getLookupUri(contactId, lookupKey);
        return new Intent(Intent.ACTION_VIEW)
                .setDataAndType(data, ContactsContract.Contacts.CONTENT_ITEM_TYPE);
    }

    /**
     * 获取跳转「设置界面」的意图
     *
     * @return 意图
     */
    public static Intent getSettingIntent() {
        return new Intent(Settings.ACTION_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    /**
     * 获取跳转「应用详情」的意图
     *
     * @param packageName 应用包名
     * @return 意图
     */
    public static Intent getAppDetailsSettingsIntent(String packageName) {
        return new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.parse("package:" + packageName))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    /**
     * 获取跳转「应用列表」的意图
     *
     * @return 意图
     */
    public static Intent getAppsIntent() {
        return new Intent(Settings.ACTION_APPLICATION_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    /**
     * 获取跳转「 Wifi 设置」的意图
     *
     * @return 意图
     */
    public static Intent getWifiSettingIntent() {
        return new Intent(Settings.ACTION_WIFI_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    /**
     * 获取跳转「网络设置」的意图
     *
     * @return 意图
     */
    public static Intent getWirelessSettingIntent() {
        return new Intent(Settings.ACTION_WIRELESS_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    /**
     * 获取跳转「无障碍服务设置」的意图
     *
     * @return 意图
     */
    public static Intent getAccessibilitySettingIntent() {
        return new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
}
