package com.xslczx.basis.java;

import com.xslczx.basis.func.Consumer;

import java.io.*;
import java.util.Locale;

public final class FileUtils {

    public static final long KB = 1024;
    public static final long MB = 1048576; // 1024 * 1024
    public static final long GB = 1073741824; // 1024 * 1024 * 1024
    private static final int S_BUFFER_SIZE = 8192;

    private FileUtils() {
    }

    /**
     * 复制文件
     *
     * @param src  源文件
     * @param dest 目标文件
     */
    public static boolean copy(final File src, final File dest) {
        if (src == null) return false;
        if (src.isDirectory()) {
            return copyOrMoveDir(src, dest, false);
        }
        return copyOrMoveFile(src, dest, false);
    }

    /**
     * 移动文件
     *
     * @param src  源文件
     * @param dest 目标文件
     */
    public static boolean move(final File src, final File dest) {
        if (src == null) return false;
        if (src.isDirectory()) {
            return copyOrMoveDir(src, dest, true);
        }
        return copyOrMoveFile(src, dest, true);
    }

    /**
     * 删除文件
     *
     * @param file 源文件
     */
    public static boolean delete(final File file) {
        if (file == null) return false;
        if (file.isDirectory()) {
            return deleteDir(file);
        }
        return deleteFile(file);
    }

    /**
     * 将数据流写入文件
     *
     * @param file 文件
     * @param is   输入流
     * @return 如果成功返回true
     */
    public static boolean writeFileFromIS(final File file, final InputStream is) {
        if (!createOrExistsFile(file) || is == null) return false;
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file));
            byte[] data = new byte[8192];
            for (int len; (len = is.read(data)) != -1; ) {
                os.write(data, 0, len);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 文件大小获取
     *
     * @param file File对象
     * @return 文件大小字符串
     */
    public static String getFileSize(File file) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            int length = fis.available();
            if (length >= GB) {
                return String.format(Locale.getDefault(), "%.2f GB", length * 1.0 / GB);
            } else if (length >= MB) {
                return String.format(Locale.getDefault(), "%.2f MB", length * 1.0 / MB);
            } else {
                return String.format(Locale.getDefault(), "%.2f KB", length * 1.0 / KB);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseUtils.closeIOQuietly(fis);
        }
        return "0 KB";
    }

    private static boolean copyOrMoveDir(final File srcDir,
                                         final File destDir,
                                         final boolean isMove) {
        if (srcDir == null || destDir == null) return false;
        String srcPath = srcDir.getPath() + File.separator;
        String destPath = destDir.getPath() + File.separator;
        if (destPath.contains(srcPath)) return false;
        if (!srcDir.exists() || !srcDir.isDirectory()) return false;
        if (!createOrExistsDir(destDir)) return false;
        File[] files = srcDir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                File oneDestFile = new File(destPath + file.getName());
                if (file.isFile()) {
                    if (!copyOrMoveFile(file, oneDestFile, isMove)) return false;
                } else if (file.isDirectory()) {
                    if (!copyOrMoveDir(file, oneDestFile, isMove)) return false;
                }
            }
        }
        return !isMove || deleteDir(srcDir);
    }

    private static boolean copyOrMoveFile(final File srcFile,
                                          final File destFile,
                                          final boolean isMove) {
        if (srcFile == null || destFile == null) return false;
        if (srcFile.equals(destFile)) return false;
        if (!srcFile.exists() || !srcFile.isFile()) return false;
        if (destFile.exists()) {
            if (!destFile.delete()) {
                return false;
            }
        }
        if (!createOrExistsDir(destFile.getParentFile())) return false;
        try {
            return writeFileFromIS(destFile, new FileInputStream(srcFile))
                    && !(isMove && !deleteFile(srcFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean deleteDir(final File dir) {
        if (dir == null) return false;
        if (!dir.exists()) return true;
        if (!dir.isDirectory()) return false;
        File[] files = dir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isFile()) {
                    if (!file.delete()) return false;
                } else if (file.isDirectory()) {
                    if (!deleteDir(file)) return false;
                }
            }
        }
        return dir.delete();
    }

    private static boolean deleteFile(final File file) {
        return file != null && (!file.exists() || file.isFile() && file.delete());
    }

    static boolean createOrExistsFile(final File file) {
        if (file == null) return false;
        if (file.exists()) return file.isFile();
        if (!createOrExistsDir(file.getParentFile())) return false;
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    static boolean createOrExistsDir(final File file) {
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    public static boolean isFileExists(File file) {
        return file != null && file.exists();
    }

    /**
     * Write file from input stream.
     *
     * @param file     The file.
     * @param is       The input stream.
     * @param append   True to append, false otherwise.
     * @param consumer The progress update listener.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean writeFileFromIS(final File file,
                                          final InputStream is,
                                          final boolean append,
                                          final Consumer<Double> consumer) {
        if (is == null || !FileUtils.createOrExistsFile(file)) {
            return false;
        }
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file, append), S_BUFFER_SIZE);
            if (consumer == null) {
                byte[] data = new byte[S_BUFFER_SIZE];
                for (int len; (len = is.read(data)) != -1; ) {
                    os.write(data, 0, len);
                }
            } else {
                double totalSize = is.available();
                int curSize = 0;
                consumer.accept(0d);
                byte[] data = new byte[S_BUFFER_SIZE];
                for (int len; (len = is.read(data)) != -1; ) {
                    os.write(data, 0, len);
                    curSize += len;
                    consumer.accept(curSize / totalSize);
                }
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 将字符串写入文件
     *
     * @param file    文件
     * @param content 字符串内容
     * @param append  是否为追加
     * @return 如果成功返回true
     */
    public static boolean writeFileFromString(final File file, final String content,
                                              final boolean append) {
        if (file == null || content == null) return false;
        if (!createOrExistsFile(file)) {
            return false;
        }
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file, append));
            bw.write(content);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
