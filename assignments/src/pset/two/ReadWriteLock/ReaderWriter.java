package pset.two.ReadWriteLock;

/*
 * Decompiled with CFR 0_110.
 * 
 * Could not load the following classes:
 *  ReadWriteLock
 */
import java.util.Random;

abstract class ReaderWriter
    implements Runnable {
    static Random ran = new Random();
    static final int SLEEP_RANGE = 50;
    final int id;
    protected final ReadWriteLock lock;
    protected final StringBuffer sb;

    public ReaderWriter(ReadWriteLock readWriteLock, StringBuffer stringBuffer, int n) {
        this.lock = readWriteLock;
        this.sb = stringBuffer;
        this.id = n;
    }

    @Override
    public void run() {
        this.sleep();
        try {
            this.beginSession();
            this.inSession();
            this.endSession();
        }
        catch (Exception var1_1) {
            var1_1.printStackTrace();
        }
    }

    void sleep() {
        try {
            Thread.sleep(1 + ran.nextInt(50));
        }
        catch (InterruptedException var1_1) {
            var1_1.printStackTrace();
        }
    }

    protected abstract void beginSession();

    protected abstract void inSession();

    protected abstract void endSession();
}
