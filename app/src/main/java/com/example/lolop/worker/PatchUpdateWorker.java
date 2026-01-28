package com.example.lolop.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.lolop.MainActivity;
import com.example.lolop.R;
import com.example.lolop.api.RetrofitClient;
import com.example.lolop.model.ChampionListResponse;
import com.example.lolop.model.ItemResponse;
import com.example.lolop.utils.FileUtils;
import com.example.lolop.utils.LocaleHelper;
import com.example.lolop.utils.PreferenceHelper;
import com.google.gson.Gson;
import java.util.List;
import retrofit2.Response;

public class PatchUpdateWorker extends Worker {

    private static final String CHANNEL_ID = "LOL_UPDATE";

    public PatchUpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        PreferenceHelper prefs = new PreferenceHelper(context);
        String lastVersion = prefs.getLastKnownVersion();

        try {
            // 1. Fetch Version
            Response<List<String>> versionResponse = RetrofitClient.getApiService().getVersions().execute();
            if (!versionResponse.isSuccessful() || versionResponse.body() == null || versionResponse.body().isEmpty()) {
                return Result.retry();
            }

            String currentVersion = versionResponse.body().get(0);

            // 2. Check if update needed
            // If lastVersion is null, it's first run or data cleared. We update data but
            // might skip notification?
            // User request implies notification on "New Patch".
            if (!currentVersion.equals(lastVersion)) {

                // 3. Notify if it's an update (and we had a previous version)
                if (lastVersion != null) {
                    showNotification("Nouveau Patch Disponible !",
                            "Le patch " + currentVersion + " est disponible ! Données mises à jour.");
                }

                // 4. Data Sync (Download & Save)
                String lang = LocaleHelper.getApiLanguage(context);

                // Fetch & Save Champions
                Response<ChampionListResponse> champResponse = RetrofitClient.getApiService()
                        .getChampions(currentVersion, lang).execute();
                if (champResponse.isSuccessful() && champResponse.body() != null) {
                    String json = new Gson().toJson(champResponse.body());
                    FileUtils.saveStringToFile(context, "champions.json", json);
                }

                // Fetch & Save Items
                Response<ItemResponse> itemResponse = RetrofitClient.getApiService().getItems(currentVersion, lang)
                        .execute();
                if (itemResponse.isSuccessful() && itemResponse.body() != null) {
                    String json = new Gson().toJson(itemResponse.body());
                    FileUtils.saveStringToFile(context, "items.json", json);
                }

                // 5. Update Prefs
                prefs.setLastKnownVersion(currentVersion);
                prefs.setLastUpdateCheck(System.currentTimeMillis());
            }

            return Result.success();

        } catch (Exception e) {
            e.printStackTrace();
            return Result.retry();
        }
    }

    private void showNotification(String title, String message) {
        Context context = getApplicationContext();
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "LoL Updates",
                    NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_sync) // Use a system icon or app icon
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        manager.notify(2, builder.build());
    }
}
