package ki.rage.client.util.math;

import lombok.Getter;

import java.time.Instant;

@Getter
public class TimeUtil {
    private long lastMS = System.currentTimeMillis();
    private long startTime;

    public void reset() {
        lastMS = Instant.now().toEpochMilli();
    }

    public TimeUtil() {
        this.resetCounter();
    }

    public static TimeUtil create() {
        return new TimeUtil();
    }

    public void resetCounter() {
        lastMS = System.currentTimeMillis();
    }

    public boolean isReached(long time) {
        return System.currentTimeMillis() - lastMS > time;
    }

    public void setLastMS(long newValue) {
        lastMS = System.currentTimeMillis() + newValue;
    }

    public void setTime(long time) {
        lastMS = time;
    }

    public long getTime() {
        return System.currentTimeMillis() - lastMS;
    }

    public boolean isRunning() {
        return System.currentTimeMillis() - lastMS <= 0;
    }

    public boolean hasTimeElapsed(long time) {
        return System.currentTimeMillis() - lastMS > time;
    }

    public boolean finished(final double delay) {
        return System.currentTimeMillis() - delay >= startTime;
    }

    public boolean hasTimeElapsed() {
        return lastMS < System.currentTimeMillis();
    }
}
