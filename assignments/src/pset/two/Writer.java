package pset.two;
/*
 * Decompiled with CFR 0_110.
 * 
 * Could not load the following classes:
 *  ReadWriteLock
 */
final class Writer
extends ReaderWriter {
    public Writer(ReadWriteLock readWriteLock, StringBuffer stringBuffer, int n) {
        super(readWriteLock, stringBuffer, n);
    }

    @Override
    protected void beginSession() {
        try {
            this.lock.beginWrite();
        }
        catch (Exception var1_1) {
            var1_1.printStackTrace();
        }
    }

    @Override
    protected void inSession() {
        this.sb.append("Writer writes " + this.id + "\n");
    }

    @Override
    protected void endSession() {
        try {
            this.lock.endWrite();
        }
        catch (Exception var1_1) {
            var1_1.printStackTrace();
        }
    }
}
