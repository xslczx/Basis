package com.xslczx.basis.android;

import android.util.Log;

import java.text.NumberFormat;

public class TimeWatcher {
    private final String name;
    private final NumberFormat numberFormat;
    private long startTime;
    private long endTime;
    private long elapsedTime;

    public TimeWatcher(String watcherName) {
        numberFormat = NumberFormat.getNumberInstance();
        this.name = watcherName;
    }

    public static TimeWatcher obtainAndStart(String name) {
        TimeWatcher watcher = new TimeWatcher(name);
        watcher.start();
        return watcher;
    }

    public final void reset() {
        startTime = 0;
        endTime = 0;
        elapsedTime = 0;
    }

    public final void start() {
        reset();
        startTime = System.nanoTime();
    }

    public final void stop() {
        if (startTime != 0) {
            endTime = System.nanoTime();
            elapsedTime = endTime - startTime;
        } else {
            reset();
        }
    }

    public final long getTotalTimeAsNano() {
        return elapsedTime;
    }

    public final long getTotalTimeAsMillis() {
        return elapsedTime / 1000000;
    }

    public final String getTotalTimeAsString() {
        long ms = elapsedTime / 1000000;
        if (ms > 0) {
            return ms + " ms";
        } else {
            long ns = elapsedTime % 1000000;
            String format = numberFormat.format(ns);
            return format + " ns";
        }
    }

    public final String stopAndPrint() {
        stop();
        String msg = name + " cost time:" + getTotalTimeAsString();
        Log.i("TimeWatcher", msg);
        return msg;
    }
}
