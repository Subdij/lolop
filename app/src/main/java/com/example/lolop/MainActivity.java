package com.example.lolop;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ChampionAdapter adapter;
    private ArrayList<Champion> championList = new ArrayList<>();
    private String currentVersion = "14.5.1"; // Default fallback
    private FavoriteDatabase db;
    private String currentRoleFilter = "All";
    private View currentSelectedView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = new FavoriteDatabase(this);
        setupNavigation();
        setupRecyclerView();
        setupSearch();
        setupRoleIcons();
        setupStickyAnimation();

        if (savedInstanceState != null) {
            //noinspection unchecked
            championList = (ArrayList<Champion>) savedInstanceState.getSerializable("CHAMP_LIST");
            if (championList != null && !championList.isEmpty()) {
                sortAndDisplayChampions();
            } else {
                fetchLatestVersion(); 
            }
        } else {
            fetchLatestVersion();
        }
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
                }
                fetchChampions();
            }

            @Override
            public void onFailure(@NonNull Call<List<String>> call, @NonNull Throwable t) {
                // If version fetch fails, try to fetch champions with default fallback version
                fetchChampions(); 
            }
        });
    }

    private void setupStickyAnimation() {
        binding.appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            float totalScrollRange = appBarLayout.getTotalScrollRange();
            if (totalScrollRange == 0) return;

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
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) adapter.filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupRecyclerView() {
        adapter = new ChampionAdapter(currentVersion);
        adapter.setDatabase(db);
        binding.rvChampions.setLayoutManager(new GridLayoutManager(this, 3));
        binding.rvChampions.setAdapter(adapter);
    }

    private void fetchChampions() {
        binding.progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService().getChampions(currentVersion).enqueue(new Callback<ChampionListResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChampionListResponse> call, @NonNull Response<ChampionListResponse> response) {
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
        if (championList == null || championList.isEmpty()) return;
        ArrayList<Champion> favorites = new ArrayList<>();
        ArrayList<Champion> others = new ArrayList<>();
        for (Champion champion : championList) {
            if (db.isFavorite(champion.getId())) favorites.add(champion);
            else others.add(champion);
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
    }

    private void setupNavigation() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_champions);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_champions) {
                return true;
            } else if (item.getItemId() == R.id.nav_items) {
                startActivity(new android.content.Intent(MainActivity.this, ItemsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }
}