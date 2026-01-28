package com.example.lolop.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.example.lolop.utils.PowerSavingManager;

public class BatteryReceiver extends BroadcastReceiver {
    /**
     * Reçoit les changements d'état de la batterie.
     * Active le mode d'économie d'énergie si la batterie est faible (<= 15%).
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
            int level = intent.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1);
            float batteryPct = level * 100 / (float) scale;

            boolean isLow = batteryPct <= 15;

            if (PowerSavingManager.getInstance().setPowerSavingMode(isLow)) {
                if (isLow) {
                    Toast.makeText(context, "Batterie 15% ou moins: Performances réduites", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "Batterie suffisante: Performances rétablies", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
