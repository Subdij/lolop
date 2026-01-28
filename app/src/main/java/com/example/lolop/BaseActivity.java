package com.example.lolop;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lolop.receiver.BatteryReceiver;
import com.example.lolop.utils.PowerSavingManager;

public class BaseActivity extends AppCompatActivity {
    private BatteryReceiver batteryReceiver;

    /**
     * Appelé lors de la création de l'activité.
     * Initialise l'activité et vérifie l'état initial de la batterie.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkInitialBatteryState();
    }

    /**
     * Appelé lorsque l'activité devient visible.
     * Enregistre le récepteur pour surveiller les changements d'état de la
     * batterie.
     */
    @Override
    protected void onStart() {
        super.onStart();
        batteryReceiver = new BatteryReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);
    }

    /**
     * Appelé lorsque l'activité n'est plus visible.
     * Désenregistre le récepteur de batterie pour éviter les fuites de mémoire.
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
        }
    }

    /**
     * Vérifie l'état de la batterie au démarrage de l'activité.
     * Active le mode économie d'énergie si le niveau de batterie est faible (<=
     * 15%).
     */
    private void checkInitialBatteryState() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);

        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            float batteryPct = level * 100 / (float) scale;

            // Standard Low Battery threshold is usually 15%
            // But we can just respect the system broadcast.
            // However, to be consistent on startup:
            if (batteryPct <= 15) {
                PowerSavingManager.getInstance().setPowerSavingMode(true);
            } else {
                PowerSavingManager.getInstance().setPowerSavingMode(false);
            }
        }
    }
}
