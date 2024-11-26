package com.example.studybuddy3.utils;

public class SystemClock implements Clock {
    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}
