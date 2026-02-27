package com.azerl0.gutitubow.util;

public class MathUtil {
    public static double getLerp(double value, double low1, double high1, double low2, double high2) {
        return low2 + (high2 - low2) * (value - low1) / (high1 - low1);
    }
}
