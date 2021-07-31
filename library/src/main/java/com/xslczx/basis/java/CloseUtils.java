package com.xslczx.basis.java;

import java.io.Closeable;
import java.io.IOException;

public final class CloseUtils {

  private CloseUtils() {
  }

  /**
   * 关闭IO
   *
   * @param closeables Closeable
   */
  public static void closeIO(final Closeable... closeables) {
    if (closeables == null) return;
    for (Closeable closeable : closeables) {
      if (closeable != null) {
        try {
          closeable.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * 静默关闭IO，不输出异常信息
   *
   * @param closeables Closeable
   */
  public static void closeIOQuietly(final Closeable... closeables) {
    if (closeables == null) return;
    for (Closeable closeable : closeables) {
      if (closeable != null) {
        try {
          closeable.close();
        } catch (IOException ignored) {
        }
      }
    }
  }
}
