package github.hotstu.lame4droid;


import android.util.Log;

import androidx.core.util.Pools;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;


public class Mp3EncodeThread extends Thread {

    private static final String TAG = Mp3EncodeThread.class.getSimpleName();

    private static class Mp3OutputStream extends OutputStream {

        private final OutputStream outputStream;
        private final byte[] mp3Buffer;

        public Mp3OutputStream(OutputStream outputStream, int bufferSize) {
            this.outputStream = outputStream;
            mp3Buffer = new byte[(int) (7200 + (bufferSize * 2 * 1.25))];
        }

        @Override
        public void write(int b) {
            throw new RuntimeException("no support");
        }

        @Override
        public void write(byte[] b) {
            throw new RuntimeException("no support");
        }

        public void write(short[] b, int len) throws IOException {
            int encodedSize = SimpleLame.encode(b, b, len, mp3Buffer);
            if (encodedSize < 0) {
                Log.e(TAG, "Lame encoded size: " + encodedSize);
            }
            this.outputStream.write(mp3Buffer, 0, encodedSize);
        }

        @Override
        public void flush() throws IOException {
            final int flushResult = SimpleLame.flush(mp3Buffer);
            if (flushResult > 0) {
                outputStream.write(mp3Buffer, 0, flushResult);
            }
            outputStream.flush();
        }

        @Override
        public void close() throws IOException {
            SimpleLame.close();
            outputStream.close();
        }
    }

    private final Mp3OutputStream bufferedOutputStream;

    public static final int QUENE_SIZE = 50;
    Pools.SimplePool<ChangeBuffer> bufferPool = new Pools.SimplePool<>(5);

    //用于存取待转换的PCM数据
    ArrayBlockingQueue<ChangeBuffer> mChangeBuffers = new ArrayBlockingQueue<>(QUENE_SIZE);

    public Mp3EncodeThread(File save, int bufferSize) {
        try {
            if (!save.exists()) {
                save.getParentFile().mkdirs();
                save.createNewFile();
            }
            bufferedOutputStream = new Mp3OutputStream(new BufferedOutputStream(new FileOutputStream(save)), bufferSize);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        Log.d(TAG, "start");

        for (; ; ) {
            ChangeBuffer take;
            try {
                take = mChangeBuffers.take();
                if (take.readSize < 0) {
                    //terminate signal
                    break;
                }
                processData(take);
                take.reset();
                bufferPool.release(take);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
        flushAndRelease();
        Log.d(TAG, "terminated");
    }


    //从缓存区ChangeBuffers里获取待转换的PCM数据，转换为MP3数据,并写入文件
    private int processData(ChangeBuffer data) {
        short[] buffer = data.getData();
        int readSize = data.getReadSize();
        //Log.d(TAG, "Read size: " + readSize);
        if (readSize > 0) {
            try {
                bufferedOutputStream.write(buffer, readSize);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Unable to write to file");
            }
            return readSize;
        }

        return 0;
    }


    private void flushAndRelease() {
        try {
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 阻塞线程如果队列已满,（如果使用实时方式，转码太慢，可能会丢失部分录音)
     * @param rawData
     * @param readSize
     */
    public void addChangeBuffer(short[] rawData, int readSize) {
        try {
            ChangeBuffer acquire = bufferPool.acquire();
            if (acquire == null) {
                acquire = new ChangeBuffer(rawData, readSize);
            } else {
                acquire.reset(rawData, readSize);
            }
            mChangeBuffers.put(acquire);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class ChangeBuffer {
        private short[] rawData;
        private int readSize;

        public ChangeBuffer(short[] rawData, int readSize) {
            if (rawData == null) {
                this.rawData = null;
            } else {
                short[] shorts = new short[rawData.length];
                System.arraycopy(rawData, 0, shorts, 0, rawData.length);
                this.rawData = shorts;
            }

            this.readSize = readSize;
        }

        public short[] getData() {
            return rawData;
        }

        public int getReadSize() {
            return readSize;
        }

        public void reset(short[] rawData, int readSize) {
            if (rawData == null) {
                this.rawData = null;
            } else {
                short[] shorts = new short[rawData.length];
                System.arraycopy(rawData, 0, shorts, 0, rawData.length);
                this.rawData = shorts;
            }
            this.readSize = readSize;

        }

        public void reset() {
            this.rawData = null;
            this.readSize = -1;
        }
    }
}