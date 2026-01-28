package com.example.lolop;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.lolop.adapter.ChampionAdapter;
import com.example.lolop.api.RetrofitClient;
import com.example.lolop.database.FavoriteDatabase;
import com.example.lolop.databinding.ActivityMainBinding;
import com.example.lolop.model.Champion;
import com.example.lolop.model.ChampionListResponse;
import com.example.lolop.utils.LocaleHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.content.Context;
import android.content.pm.PackageManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.widget.Toast;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.example.lolop.utils.FileUtils;
import com.example.lolop.utils.PreferenceHelper;
import java.util.concurrent.TimeUnit;
import com.example.lolop.worker.PatchUpdateWorker;
import com.google.gson.Gson;

public class MainActivity extends BaseActivity implements ChampionAdapter.OnChampionClickListener {

    private ActivityMainBinding binding;
    private ChampionAdapter adapter;
    private ArrayList<Champion> championList = new ArrayList<>();
    private String currentVersion = "14.5.1"; // Default fallback
    private FavoriteDatabase db;
    private java.util.Set<String> favoriteIds = new java.util.HashSet<>();
    private String currentRoleFilter = "All";
    private View currentSelectedView = null;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private static final int RECORD_AUDIO_REQUEST_CODE = 101;
    private boolean isListening = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = new FavoriteDatabase(this);
        updateNavbarVersion();
        setupRecyclerView();
        setupSearch();
        setupVoiceSearch();
        setupRoleIcons();
        setupStickyAnimation();
        setupLanguageButtons();
        setupBackgroundWork();

        if (savedInstanceState != null) {
            // noinspection unchecked
            championList = (ArrayList<Champion>) savedInstanceState.getSerializable("CHAMP_LIST");
            if (savedInstanceState.containsKey("CURRENT_VERSION")) {
                currentVersion = savedInstanceState.getString("CURRENT_VERSION");
            }
            if (adapter != null) {
                adapter.setVersion(currentVersion);
            }
            updateNavbarVersion();
            if (championList != null && !championList.isEmpty()) {
                sortAndDisplayChampions();
            } else {
                fetchLatestVersion();
            }
        } else {
            fetchLatestVersion();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshFavorites();
    }

    private void refreshFavorites() {
        if (db != null) {
            favoriteIds = db.getAllFavorites();
            if (adapter != null) {
                adapter.setFavorites(favoriteIds);
                sortAndDisplayChampions();
            }
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private void fetchLatestVersion() {
        binding.progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService().getVersions().enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(@NonNull Call<List<String>> call, @NonNull Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    currentVersion = response.body().get(0);
                    if (adapter != null) {
                        adapter.setVersion(currentVersion);
                    }
                    updateNavbarVersion();

                    PreferenceHelper prefs = new PreferenceHelper(MainActivity.this);
                    String savedVersion = prefs.getLastKnownVersion();
                    if (currentVersion.equals(savedVersion)
                            && FileUtils.fileExists(MainActivity.this, "champions.json")) {
                        loadLocalChampions();
                    } else {
                        fetchChampions();
                    }
                } else {
                    fetchChampions();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<String>> call, @NonNull Throwable t) {
                // If version fetch fails, try to fetch champions with default fallback version
                fetchChampions();
            }
        });
    }

    private void setupLanguageButtons() {
        binding.btnLangFr.setOnClickListener(v -> setLanguage("fr"));
        binding.btnLangEn.setOnClickListener(v -> setLanguage("en"));

        updateLanguageUI(LocaleHelper.getLanguage(this));
    }

    private void updateLanguageUI(String currentLanguage) {
        float activeScale = 1.0f;
        float inactiveScale = 0.75f;
        float activeAlpha = 1.0f;
        float inactiveAlpha = 0.5f;

        if ("fr".equals(currentLanguage)) {
            binding.btnLangFr.setAlpha(activeAlpha);
            binding.btnLangFr.setScaleX(activeScale);
            binding.btnLangFr.setScaleY(activeScale);

            binding.btnLangEn.setAlpha(inactiveAlpha);
            binding.btnLangEn.setScaleX(inactiveScale);
            binding.btnLangEn.setScaleY(inactiveScale);
        } else {
            // Default to English if not French
            binding.btnLangEn.setAlpha(activeAlpha);
            binding.btnLangEn.setScaleX(activeScale);
            binding.btnLangEn.setScaleY(activeScale);

            binding.btnLangFr.setAlpha(inactiveAlpha);
            binding.btnLangFr.setScaleX(inactiveScale);
            binding.btnLangFr.setScaleY(inactiveScale);
        }
    }

    private void setLanguage(String language) {
        LocaleHelper.setLocale(this, language);
        recreate();
    }

    private void setupStickyAnimation() {
        binding.appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            // Disable complex animation calculation in power saving mode to save CPU
            if (com.example.lolop.utils.PowerSavingManager.getInstance().isPowerSavingMode()) {
                return;
            }

            float totalScrollRange = appBarLayout.getTotalScrollRange();
            if (totalScrollRange == 0)
                return;

            float percentage = (float) Math.abs(verticalOffset) / totalScrollRange;

            // Définir le pivot en haut au centre pour que le logo rétrécisse vers le haut
            binding.ivLogoLarge.setPivotY(0f);
            binding.ivLogoLarge.setPivotX(binding.ivLogoLarge.getWidth() / 2f);

            // On réduit le logo jusqu'à 40% de sa taille originale (augmenté de 30% à 40%)
            float minScale = 0.5f;
            float scale = 1.0f - (percentage * (1.0f - minScale));

            binding.ivLogoLarge.setScaleX(scale);
            binding.ivLogoLarge.setScaleY(scale);

            // Applique un décalage vertical progressif de 0dp à 20dp
            float maxTranslationY = 10 * getResources().getDisplayMetrics().density;
            binding.ivLogoLarge.setTranslationY(percentage * maxTranslationY);

            // Le logo reste visible et sticky grâce au mode 'pin' dans le XML
            binding.ivLogoLarge.setAlpha(1.0f);
        });
    }

    private void setupRoleIcons() {
        binding.ivRoleFavorites.setOnClickListener(v -> toggleRoleFilter(v, "Favorites"));
        binding.ivRoleTop.setOnClickListener(v -> toggleRoleFilter(v, "Top"));
        binding.ivRoleJungle.setOnClickListener(v -> toggleRoleFilter(v, "Jungle"));
        binding.ivRoleMid.setOnClickListener(v -> toggleRoleFilter(v, "Mid"));
        binding.ivRoleBot.setOnClickListener(v -> toggleRoleFilter(v, "Bot"));
        binding.ivRoleSupport.setOnClickListener(v -> toggleRoleFilter(v, "Support"));
    }

    private void toggleRoleFilter(View view, String role) {
        if (currentRoleFilter.equals(role)) {
            currentRoleFilter = "All";
            view.setBackgroundResource(R.drawable.role_border);
            currentSelectedView = null;
        } else {
            if (currentSelectedView != null) {
                currentSelectedView.setBackgroundResource(R.drawable.role_border);
            }
            currentRoleFilter = role;
            view.setBackgroundResource(R.drawable.role_icon_selected);
            currentSelectedView = view;
        }
        adapter.setRoleFilter(currentRoleFilter);
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null)
                    adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupVoiceSearch() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                binding.ivMic.setImageResource(R.drawable.ic_mic_active);
                isListening = true;
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEndOfSpeech() {
                binding.ivMic.setImageResource(R.drawable.ic_mic_inactive);
                isListening = false;
            }

            @Override
            public void onError(int error) {
                binding.ivMic.setImageResource(R.drawable.ic_mic_inactive);
                isListening = false;
            }

            @Override
            public void onResults(Bundle results) {
                binding.ivMic.setImageResource(R.drawable.ic_mic_inactive);
                isListening = false;
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    processVoiceResult(matches.get(0));
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });

        binding.ivMic.setOnClickListener(v -> checkPermissionAndStartVoiceInput());
    }

    private void checkPermissionAndStartVoiceInput() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.RECORD_AUDIO },
                    RECORD_AUDIO_REQUEST_CODE);
        } else {
            if (isListening) {
                speechRecognizer.stopListening();
                binding.ivMic.setImageResource(R.drawable.ic_mic_inactive);
                isListening = false;
            } else {
                speechRecognizer.startListening(speechRecognizerIntent);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                speechRecognizer.startListening(speechRecognizerIntent);
            } else {
                Toast.makeText(this, "Permission denied to record audio", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void processVoiceResult(String query) {
        binding.etSearch.setText(query);
        // Clean query for comparison
        String normalizedQuery = query.trim().toLowerCase();

        // Check for exact match in current list
        if (championList != null) {
            for (Champion champion : championList) {
                if (champion.getName().equalsIgnoreCase(normalizedQuery)) {
                    // Exact match found!
                    onChampionClick(champion);
                    return;
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }

    private void setupRecyclerView() {
        adapter = new ChampionAdapter(currentVersion);
        adapter.setFavorites(favoriteIds);
        adapter.setOnChampionClickListener(this);
        binding.rvChampions.setLayoutManager(new GridLayoutManager(this, 3));
        binding.rvChampions.setAdapter(adapter);
    }

    private void fetchChampions() {
        binding.progressBar.setVisibility(View.VISIBLE);
        String apiLang = LocaleHelper.getApiLanguage(this);
        RetrofitClient.getApiService().getChampions(currentVersion, apiLang)
                .enqueue(new Callback<ChampionListResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ChampionListResponse> call,
                            @NonNull Response<ChampionListResponse> response) {
                        binding.progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            championList = new ArrayList<>(response.body().getData().values());
                            sortAndDisplayChampions();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ChampionListResponse> call, @NonNull Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void sortAndDisplayChampions() {
        if (championList == null || championList.isEmpty())
            return;
        ArrayList<Champion> favorites = new ArrayList<>();
        ArrayList<Champion> others = new ArrayList<>();
        if (favoriteIds == null)
            refreshFavorites();

        for (Champion champion : championList) {
            if (favoriteIds.contains(champion.getId()))
                favorites.add(champion);
            else
                others.add(champion);
        }
        Collections.sort(favorites, (c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));
        Collections.sort(others, (c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));
        ArrayList<Champion> sortedList = new ArrayList<>();
        sortedList.addAll(favorites);
        sortedList.addAll(others);
        adapter.setChampions(sortedList);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("CHAMP_LIST", championList);
        outState.putString("CURRENT_VERSION", currentVersion);
    }

    private void updateNavbarVersion() {
        com.example.lolop.fragments.NavbarFragment navbar = (com.example.lolop.fragments.NavbarFragment) getSupportFragmentManager()
                .findFragmentById(R.id.bottomNavigation);
        if (navbar != null) {
            navbar.setCurrentVersion(currentVersion);
        }
    }

    @Override
    public void onChampionClick(Champion champion) {
        Intent intent = new Intent(this, DetailChampion.class);
        intent.putExtra(DetailChampion.EXTRA_CHAMPION, champion);
        intent.putExtra(DetailChampion.EXTRA_VERSION, currentVersion);
        startActivity(intent);
    }

    private void setupBackgroundWork() {
        PeriodicWorkRequest patchWorkRequest = new PeriodicWorkRequest.Builder(PatchUpdateWorker.class, 6,
                TimeUnit.HOURS)
                .setInitialDelay(15, TimeUnit.MINUTES) // Un petit délai au premier lancement
                .addTag("patch_update_work")
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "patch_update_work",
                ExistingPeriodicWorkPolicy.KEEP,
                patchWorkRequest);
    }

    private void loadLocalChampions() {
        new Thread(() -> {
            String json = FileUtils.readStringFromFile(this, "champions.json");
            if (json != null) {
                ChampionListResponse response = new Gson().fromJson(json, ChampionListResponse.class);
                if (response != null && response.getData() != null) {
                    championList = new ArrayList<>(response.getData().values());
                    runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        sortAndDisplayChampions();
                    });
                } else {
                    runOnUiThread(this::fetchChampions);
                }
            } else {
                runOnUiThread(this::fetchChampions);
            }
        }).start();
    }
}