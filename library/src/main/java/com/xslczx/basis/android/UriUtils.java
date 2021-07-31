package com.xslczx.basis.android;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import com.xslczx.basis.func.Supplier;
import com.xslczx.basis.java.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;

public final class UriUtils {

    private UriUtils() {
    }

    /**
     * file 转uri
     *
     * @param context 上下文
     * @param file    文件
     * @return uri
     */

    public static Uri file2Uri(Context context, final File file,
                               Supplier<Uri> supplier) {
        if (!FileUtils.isFileExists(file)) return null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return supplier.get();
        } else {
            return Uri.fromFile(file);
        }
    }

    /**
     * uri转file
     *
     * @param uri uri
     * @return file
     */

    public static File uri2File(Context context, final Uri uri) {
        File file = uri2FileReal(context, uri);
        if (file != null) return file;
        return copyUri2Cache(context, uri);
    }

    private static File uri2FileReal(Context context, final Uri uri) {
        String authority = uri.getAuthority();
        String scheme = uri.getScheme();
        String path = uri.getPath();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && path != null) {
            String[] externals = new String[]{"/external/", "/external_path/"};
            File file;
            for (String external : externals) {
                if (path.startsWith(external)) {
                    file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                            + path.replace(external, "/"));
                    if (file.exists()) {
                        return file;
                    }
                }
            }
            file = null;
            if (path.startsWith("/files_path/")) {
                file = new File(context.getFilesDir().getAbsolutePath()
                        + path.replace("/files_path/", "/"));
            } else if (path.startsWith("/cache_path/")) {
                file = new File(context.getCacheDir().getAbsolutePath()
                        + path.replace("/cache_path/", "/"));
            } else if (path.startsWith("/external_files_path/")) {
                file = new File(context.getExternalFilesDir(null).getAbsolutePath()
                        + path.replace("/external_files_path/", "/"));
            } else if (path.startsWith("/external_cache_path/")) {
                file = new File(context.getExternalCacheDir().getAbsolutePath()
                        + path.replace("/external_cache_path/", "/"));
            }
            if (file != null && file.exists()) {
                return file;
            }
        }
        if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            if (path != null) return new File(path);
            return null;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && DocumentsContract.isDocumentUri(context, uri)) {
            if ("com.android.externalstorage.documents".equals(authority)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return new File(Environment.getExternalStorageDirectory() + "/" + split[1]);
                } else {
                    StorageManager mStorageManager =
                            (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
                    try {
                        Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
                        Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
                        Method getUuid = storageVolumeClazz.getMethod("getUuid");
                        Method getState = storageVolumeClazz.getMethod("getState");
                        Method getPath = storageVolumeClazz.getMethod("getPath");
                        Method isPrimary = storageVolumeClazz.getMethod("isPrimary");
                        Method isEmulated = storageVolumeClazz.getMethod("isEmulated");
                        Object result = getVolumeList.invoke(mStorageManager);
                        final int length = Array.getLength(result);
                        for (int i = 0; i < length; i++) {
                            Object storageVolumeElement = Array.get(result, i);
                            final boolean mounted =
                                    Environment.MEDIA_MOUNTED.equals(getState.invoke(storageVolumeElement))
                                            || Environment.MEDIA_MOUNTED_READ_ONLY.equals(
                                            getState.invoke(storageVolumeElement));
                            if (!mounted) continue;
                            if ((Boolean) isPrimary.invoke(storageVolumeElement)
                                    && (Boolean) isEmulated.invoke(storageVolumeElement)) {
                                continue;
                            }

                            String uuid = (String) getUuid.invoke(storageVolumeElement);

                            if (uuid != null && uuid.equals(type)) {
                                return new File(getPath.invoke(storageVolumeElement) + "/" + split[1]);
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                return null;
            } else if ("com.android.providers.downloads.documents".equals(authority)) {
                String id = DocumentsContract.getDocumentId(uri);
                if (TextUtils.isEmpty(id)) {
                    return null;
                }
                if (id.startsWith("raw:")) {
                    return new File(id.substring(4));
                } else if (id.startsWith("msf:")) {
                    id = id.split(":")[1];
                }
                long availableId = 0;
                try {
                    availableId = Long.parseLong(id);
                } catch (Exception e) {
                    return null;
                }
                String[] contentUriPrefixesToTry = new String[]{
                        "content://downloads/public_downloads",
                        "content://downloads/all_downloads",
                        "content://downloads/my_downloads"
                };
                for (String contentUriPrefix : contentUriPrefixesToTry) {
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), availableId);
                    try {
                        File file = getFileFromUri(context, contentUri, "1_1");
                        if (file != null) {
                            return file;
                        }
                    } catch (Exception ignore) {
                    }
                }
                return null;
            } else if ("com.android.providers.media.documents".equals(authority)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                } else {
                    return null;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};
                return getFileFromUri(context, contentUri, selection, selectionArgs, "1_2");
            } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
                return getFileFromUri(context, uri, "1_3");
            } else {
                return null;
            }
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            return getFileFromUri(context, uri, "2");
        } else {
            return null;
        }
    }

    private static File getFileFromUri(Context context, final Uri uri, final String code) {
        return getFileFromUri(context, uri, null, null, code);
    }

    private static File getFileFromUri(Context context, final Uri uri,
                                       final String selection,
                                       final String[] selectionArgs,
                                       final String code) {
        if ("com.google.android.apps.photos.content".equals(uri.getAuthority())) {
            if (!TextUtils.isEmpty(uri.getLastPathSegment())) {
                return new File(uri.getLastPathSegment());
            }
        } else if ("com.tencent.mtt.fileprovider".equals(uri.getAuthority())) {
            String path = uri.getPath();
            if (!TextUtils.isEmpty(path)) {
                File fileDir = Environment.getExternalStorageDirectory();
                return new File(fileDir, path.substring("/QQBrowser".length(), path.length()));
            }
        } else if ("com.huawei.hidisk.fileprovider".equals(uri.getAuthority())) {
            String path = uri.getPath();
            if (!TextUtils.isEmpty(path)) {
                return new File(path.replace("/root", ""));
            }
        }

        final Cursor cursor = context.getContentResolver().query(
                uri, new String[]{"_data"}, selection, selectionArgs, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                final int columnIndex = cursor.getColumnIndex("_data");
                if (columnIndex > -1) {
                    return new File(cursor.getString(columnIndex));
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        } finally {
            cursor.close();
        }
    }

    private static File copyUri2Cache(Context context, Uri uri) {
        InputStream is = null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            File file = new File(context.getCacheDir(), "" + System.currentTimeMillis());
            FileUtils.writeFileFromIS(file, is);
            return file;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
