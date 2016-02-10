package pset.two.ReadWriteLock;

/*
 * Decompiled with CFR 0_110.
 * 
 * Could not load the following classes:
 *  ReadWriteLock
 */
public final class Reader
    extends ReaderWriter {
    public Reader(ReadWriteLock readWriteLock, StringBuffer stringBuffer, int n) {
        super(readWriteLock, stringBuffer, n);
    }

    @Override
    public void beginSession() {
        try {
            this.lock.beginRead();
        }
        catch (Exception var1_1) {
            var1_1.printStackTrace();
        }
    }

    @Override
    protected void inSession() {
        this.sb.append("Reader reads " + this.id + "\n");
    }

    @Override
    protected void endSession() {
        try {
            this.lock.endRead();
        }
        catch (Exception var1_1) {
            var1_1.printStackTrace();
        }
    }
}
