package com.xslczx.basis.java.timer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class TimerManager {

    // 内部保存定时器对象 ( 统一管理 )
    protected static final List<BasisTimer> mTimerLists = Collections.synchronizedList(new ArrayList<BasisTimer>());

    private TimerManager() {
    }

    /**
     * 添加包含校验
     *
     * @param timer 定时器
     */
    protected static void addContainsChecker(final BasisTimer timer) {
        synchronized (mTimerLists) {
            if (!mTimerLists.contains(timer)) {
                mTimerLists.add(timer);
            }
        }
    }

    /**
     * 获取全部定时器总数
     *
     * @return 全部定时器总数
     */
    public static int getSize() {
        return mTimerLists.size();
    }

    /**
     * 回收定时器资源
     */
    public static void recycle() {
        synchronized (mTimerLists) {
            try {
                Iterator<BasisTimer> iterator = mTimerLists.iterator();
                while (iterator.hasNext()) {
                    BasisTimer timer = iterator.next();
                    if (timer == null || timer.isMarkSweep()) {
                        iterator.remove();
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 获取对应 TAG 定时器 ( 优先获取符合的 )
     *
     * @param tag 判断 {@link BasisTimer#getTag()}
     * @return {@link BasisTimer}
     */
    public static BasisTimer getTimer(final String tag) {
        if (tag != null) {
            synchronized (mTimerLists) {
                try {
                    for (BasisTimer timer : mTimerLists) {
                        if (timer != null && tag.equals(timer.getTag())) {
                            return timer;
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }

    /**
     * 获取对应 UUID 定时器 ( 优先获取符合的 )
     *
     * @param uuid 判断 {@link BasisTimer#getTag()}
     * @return {@link BasisTimer}
     */
    public static BasisTimer getTimer(final int uuid) {
        synchronized (mTimerLists) {
            try {
                for (BasisTimer timer : mTimerLists) {
                    if (timer != null && uuid == timer.getUUID()) {
                        return timer;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * 获取对应 TAG 定时器集合
     *
     * @param tag 判断 {@link BasisTimer#getTag()}
     * @return 定时器集合
     */
    public static List<BasisTimer> getTimers(final String tag) {
        List<BasisTimer> lists = new ArrayList<>();
        if (tag != null) {
            synchronized (mTimerLists) {
                try {
                    for (BasisTimer timer : mTimerLists) {
                        if (timer != null && tag.equals(timer.getTag())) {
                            lists.add(timer);
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return lists;
    }

    /**
     * 获取对应 UUID 定时器集合
     *
     * @param uuid 判断 {@link BasisTimer#getTag()}
     * @return 定时器集合
     */
    public static List<BasisTimer> getTimers(final int uuid) {
        List<BasisTimer> lists = new ArrayList<>();
        synchronized (mTimerLists) {
            try {
                for (BasisTimer timer : mTimerLists) {
                    if (timer != null && uuid == timer.getUUID()) {
                        lists.add(timer);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return lists;
    }

    /**
     * 关闭全部定时器
     */
    public static void closeAll() {
        synchronized (mTimerLists) {
            try {
                Iterator<BasisTimer> iterator = mTimerLists.iterator();
                while (iterator.hasNext()) {
                    BasisTimer timer = iterator.next();
                    if (timer != null) {
                        timer.stop();
                        iterator.remove();
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 关闭所有未运行的定时器
     */
    public static void closeAllNotRunning() {
        synchronized (mTimerLists) {
            try {
                Iterator<BasisTimer> iterator = mTimerLists.iterator();
                while (iterator.hasNext()) {
                    BasisTimer timer = iterator.next();
                    if (timer != null && !timer.isRunning()) {
                        timer.stop();
                        iterator.remove();
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 关闭所有无限循环的定时器
     */
    public static void closeAllInfinite() {
        synchronized (mTimerLists) {
            try {
                Iterator<BasisTimer> iterator = mTimerLists.iterator();
                while (iterator.hasNext()) {
                    BasisTimer timer = iterator.next();
                    if (timer != null && timer.isInfinite()) {
                        timer.stop();
                        iterator.remove();
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 关闭所有对应 TAG 定时器
     *
     * @param tag 判断 {@link BasisTimer#getTag()}
     */
    public static void closeAllTag(final String tag) {
        if (tag != null) {
            synchronized (mTimerLists) {
                try {
                    Iterator<BasisTimer> iterator = mTimerLists.iterator();
                    while (iterator.hasNext()) {
                        BasisTimer timer = iterator.next();
                        if (timer != null && tag.equals(timer.getTag())) {
                            timer.stop();
                            iterator.remove();
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * 关闭所有对应 UUID 定时器
     *
     * @param uuid 判断 {@link BasisTimer#getUUID()}
     */
    public static void closeAllUUID(final int uuid) {
        synchronized (mTimerLists) {
            try {
                Iterator<BasisTimer> iterator = mTimerLists.iterator();
                while (iterator.hasNext()) {
                    BasisTimer timer = iterator.next();
                    if (timer != null && uuid == timer.getUUID()) {
                        timer.stop();
                        iterator.remove();
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }
}
