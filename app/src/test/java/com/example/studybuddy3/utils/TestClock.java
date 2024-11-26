package com.example.studybuddy3.utils;

public class TestClock implements Clock {
    private long currentTime;

    public TestClock(long initialTime) {
        this.currentTime = initialTime;
    }

    @Override
    public long currentTimeMillis() {
        return currentTime;
    }

    public void setCurrentTime(long newTime) {
        this.currentTime = newTime;
    }

    public void advanceBy(long milliseconds) {
        this.currentTime += milliseconds;
    }
}