package com.example.lolop.utils;

public class PowerSavingManager {
    private static final PowerSavingManager instance = new PowerSavingManager();
    private boolean isPowerSavingMode = false;

    private PowerSavingManager() {}

    public static PowerSavingManager getInstance() {
        return instance;
    }

    public boolean setPowerSavingMode(boolean enabled) {
        if (this.isPowerSavingMode != enabled) {
            this.isPowerSavingMode = enabled;
            return true;
        }
        return false;
    }

    public boolean isPowerSavingMode() {
        return isPowerSavingMode;
    }
}
