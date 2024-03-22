package io.github.itzispyder.crosshairtarget.util;

public class Animator {

    private long start, length;
    private boolean reversed;

    public Animator(long length) {
        this.start = System.currentTimeMillis();
        this.length = length;
        this.reversed = false;
    }

    public double getProgress() {
        long pass = System.currentTimeMillis() - start;
        double rat = pass / (double)length;
        return reversed ? 1 - rat : rat;
    }

    public double getProgressClamped() {
        return Math.max(0.0, Math.min(1.0, getProgress()));
    }

    public boolean isFinished() {
        double p = getProgress();
        return reversed ? p <= 0.0 : p >= 1.0;
    }

    public void reverse() {
        reversed = !reversed;
    }

    public void setReversed(boolean reversed) {
        this.reversed = reversed;
    }

    public boolean isReversed() {
        return reversed;
    }

    public void reset(long length) {
        this.start = System.currentTimeMillis();
        this.length = length;
    }

    public void reset() {
        this.start = System.currentTimeMillis();
    }
}
