package com.example.lolop.utils;

public class PowerSavingManager {
    private static final PowerSavingManager instance = new PowerSavingManager();
    private boolean isPowerSavingMode = false;

    private PowerSavingManager() {
    }

    /**
     * Retourne l'instance unique du gestionnaire d'économie d'énergie.
     */
    public static PowerSavingManager getInstance() {
        return instance;
    }

    /**
     * Active ou désactive le mode économie d'énergie.
     * Retourne true si l'état a changé, sinon false.
     */
    public boolean setPowerSavingMode(boolean enabled) {
        if (this.isPowerSavingMode != enabled) {
            this.isPowerSavingMode = enabled;
            return true;
        }
        return false;
    }

    /**
     * Vérifie si le mode économie d'énergie est actuellement activé.
     */
    public boolean isPowerSavingMode() {
        return isPowerSavingMode;
    }
}
