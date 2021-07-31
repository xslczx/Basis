package com.xslczx.basis.java;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public final class ThrowableUtils {

    private static final String LINE_SEP = System.getProperty("line.separator");

    private ThrowableUtils() {
    }

    /**
     * 获取异常的堆栈完整信息
     *
     * @param throwable 异常
     * @return 堆栈信息
     */
    public static String getFullStackTrace(Throwable throwable) {
        final List<Throwable> throwableList = new ArrayList<>();
        Throwable t = throwable;
        while (t != null && !throwableList.contains(t)) {
            throwableList.add(t);
            t = t.getCause();
        }
        final int size = throwableList.size();
        final List<String> frames = new ArrayList<>();
        List<String> nextTrace = getStackFrameList(throwableList.get(size - 1));
        for (int i = size; --i >= 0; ) {
            final List<String> trace = nextTrace;
            if (i != 0) {
                nextTrace = getStackFrameList(throwableList.get(i - 1));
                removeCommonFrames(trace, nextTrace);
            }
            if (i == size - 1) {
                frames.add(throwableList.get(i).toString());
            } else {
                frames.add(" Caused by: " + throwableList.get(i).toString());
            }
            frames.addAll(trace);
        }
        StringBuilder sb = new StringBuilder();
        for (final String element : frames) {
            sb.append(element).append(LINE_SEP);
        }
        return sb.toString();
    }

    private static List<String> getStackFrameList(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        final String stackTrace = sw.toString();
        final StringTokenizer frames = new StringTokenizer(stackTrace, LINE_SEP);
        final List<String> list = new ArrayList<>();
        boolean traceStarted = false;
        while (frames.hasMoreTokens()) {
            final String token = frames.nextToken();
            final int at = token.indexOf("at");
            if (at != -1 && token.substring(0, at).trim().isEmpty()) {
                traceStarted = true;
                list.add(token);
            } else if (traceStarted) {
                break;
            }
        }
        return list;
    }

    private static void removeCommonFrames(final List<String> causeFrames,
                                           final List<String> wrapperFrames) {
        int causeFrameIndex = causeFrames.size() - 1;
        int wrapperFrameIndex = wrapperFrames.size() - 1;
        while (causeFrameIndex >= 0 && wrapperFrameIndex >= 0) {
            final String causeFrame = causeFrames.get(causeFrameIndex);
            final String wrapperFrame = wrapperFrames.get(wrapperFrameIndex);
            if (causeFrame.equals(wrapperFrame)) {
                causeFrames.remove(causeFrameIndex);
            }
            causeFrameIndex--;
            wrapperFrameIndex--;
        }
    }
}
