package com.example.lolop;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.lolop.api.RetrofitClient;
import com.example.lolop.model.Champion;
import com.example.lolop.model.ChampionListResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailChampion extends AppCompatActivity {

    public static final String EXTRA_CHAMPION = "extra_champion";
    public static final String EXTRA_VERSION = "extra_version";
    private String currentVersion = "14.5.1";

    private ImageView championSplash;
    private TextView championName;
    private TextView championTitle;
    private TextView championTags;
    private LinearLayout difficultyBarsContainer;
    private TextView championLore;
    private ImageView passiveImage;
    private TextView passiveName;
    private TextView passiveDescription;
    private LinearLayout spellsContainer;
    private TextView allyTips;
    private TextView enemyTips;
    private TextView allyTipsHeader;
    private TextView enemyTipsHeader;
    private View tipsSeparator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailchampion);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        initViews();

        if (getIntent().hasExtra(EXTRA_VERSION)) {
            currentVersion = getIntent().getStringExtra(EXTRA_VERSION);
        }

        Champion champion = getIntent().getParcelableExtra(EXTRA_CHAMPION);

        if (champion != null) {
            displayBasicInfo(champion);
            fetchChampionDetail(champion.getId());
        }
    }

    private void initViews() {
        championSplash = findViewById(R.id.champion_splash);
        championName = findViewById(R.id.champion_name);
        championTitle = findViewById(R.id.champion_title);
        championTags = findViewById(R.id.champion_tags);
        difficultyBarsContainer = findViewById(R.id.difficulty_bars_container);
        championLore = findViewById(R.id.champion_lore);
        passiveImage = findViewById(R.id.passive_image);
        passiveName = findViewById(R.id.passive_name);
        passiveDescription = findViewById(R.id.passive_description);
        spellsContainer = findViewById(R.id.spells_container);
        allyTips = findViewById(R.id.ally_tips);
        enemyTips = findViewById(R.id.enemy_tips);
        allyTipsHeader = findViewById(R.id.tvAllyTipsHeader);
        enemyTipsHeader = findViewById(R.id.tvEnemyTipsHeader);
        tipsSeparator = findViewById(R.id.vTipsSeparator);
    }

    private void displayBasicInfo(Champion champion) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(champion.getName());
        }
        championName.setText(champion.getName());
        championTitle.setText(champion.getTitle());
        championLore.setText(champion.getLore());

        String splashUrl = "https://ddragon.leagueoflegends.com/cdn/img/champion/splash/" + champion.getId() + "_0.jpg";
        
        if (com.example.lolop.utils.PowerSavingManager.getInstance().isPowerSavingMode()) {
            Glide.with(this)
                 .load(splashUrl)
                 .format(com.bumptech.glide.load.DecodeFormat.PREFER_RGB_565)
                 .dontAnimate()
                 .into(championSplash);
        } else {
            Glide.with(this).load(splashUrl).into(championSplash);
        }
    }

    private void fetchChampionDetail(String championId) {
        String apiLang = com.example.lolop.utils.LocaleHelper.getApiLanguage(this);
        RetrofitClient.getApiService().getChampionDetail(currentVersion, apiLang, championId).enqueue(new Callback<ChampionListResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChampionListResponse> call, @NonNull Response<ChampionListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Champion detailedChampion = response.body().getData().get(championId);
                    if (detailedChampion != null) {
                        updateUI(detailedChampion);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChampionListResponse> call, @NonNull Throwable t) {
                // Handle failure
            }
        });
    }

    private void updateUI(Champion champion) {
        championLore.setText(champion.getLore());

        // Tags
        String tags = champion.getTags() != null ? String.join(" | ", champion.getTags()) : "";
        championTags.setText(tags);

        // Difficulty Bars
        int difficulty = champion.getInfo() != null ? champion.getInfo().getDifficulty() : 0;
        setupDifficultyBars(difficulty);

        // Passive
        if (champion.getPassive() != null) {
            passiveName.setText(champion.getPassive().getName());
            passiveDescription.setText(Html.fromHtml(champion.getPassive().getDescription(), Html.FROM_HTML_MODE_COMPACT));
            String passiveUrl = "https://ddragon.leagueoflegends.com/cdn/" + currentVersion + "/img/passive/" + champion.getPassive().getImage().getFull();
            if (com.example.lolop.utils.PowerSavingManager.getInstance().isPowerSavingMode()) {
                Glide.with(this)
                     .load(passiveUrl)
                     .format(com.bumptech.glide.load.DecodeFormat.PREFER_RGB_565)
                     .dontAnimate()
                     .into(passiveImage);
            } else {
                Glide.with(this).load(passiveUrl).into(passiveImage);
            }
        }

        // Spells
        spellsContainer.removeAllViews();
        if (champion.getSpells() != null) {
            for (Champion.Spell spell : champion.getSpells()) {
                addSpellView(spell);
            }
        }

        // Tips
        boolean hasAllyTips = champion.getAllytips() != null && !champion.getAllytips().isEmpty();
        boolean hasEnemyTips = champion.getEnemytips() != null && !champion.getEnemytips().isEmpty();

        updateTipsSection(champion.getAllytips(), allyTipsHeader, allyTips);
        updateTipsSection(champion.getEnemytips(), enemyTipsHeader, enemyTips);

        if (!hasAllyTips && !hasEnemyTips) {
            tipsSeparator.setVisibility(View.GONE);
        } else {
            tipsSeparator.setVisibility(View.VISIBLE);
        }
    }

    private void updateTipsSection(List<String> tips, TextView header, TextView content) {
        if (tips == null || tips.isEmpty()) {
            header.setVisibility(View.GONE);
            content.setVisibility(View.GONE);
        } else {
            header.setVisibility(View.VISIBLE);
            content.setVisibility(View.VISIBLE);
            content.setText(formatTips(tips));
        }
    }

    private void setupDifficultyBars(int difficulty) {
        difficultyBarsContainer.removeAllViews();
        int maxBars = 10;
        int barWidth = (int) (12 * getResources().getDisplayMetrics().density);
        int barHeight = (int) (4 * getResources().getDisplayMetrics().density);
        int margin = (int) (4 * getResources().getDisplayMetrics().density);

        for (int i = 0; i < maxBars; i++) {
            View bar = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(barWidth, barHeight);
            params.setMargins(0, 0, margin, 0);
            bar.setLayoutParams(params);

            if (i < difficulty) {
                bar.setBackgroundColor(ContextCompat.getColor(this, R.color.lol_gold));
            } else {
                bar.setBackgroundColor(ContextCompat.getColor(this, R.color.lol_grey));
            }
            difficultyBarsContainer.addView(bar);
        }
    }

    private void addSpellView(Champion.Spell spell) {
        View spellView = LayoutInflater.from(this).inflate(R.layout.item_spell, spellsContainer, false);
        ImageView img = spellView.findViewById(R.id.ivSpellIcon);
        TextView name = spellView.findViewById(R.id.tvSpellName);
        TextView desc = spellView.findViewById(R.id.tvSpellDescription);

        name.setText(spell.getName());
        // Updated to use description directly instead of tooltip
        desc.setText(Html.fromHtml(spell.getDescription(), Html.FROM_HTML_MODE_COMPACT));

        String spellUrl = "https://ddragon.leagueoflegends.com/cdn/" + currentVersion + "/img/spell/" + spell.getImage().getFull();
        if (com.example.lolop.utils.PowerSavingManager.getInstance().isPowerSavingMode()) {
            Glide.with(this)
                 .load(spellUrl)
                 .format(com.bumptech.glide.load.DecodeFormat.PREFER_RGB_565)
                 .dontAnimate()
                 .into(img);
        } else {
            Glide.with(this).load(spellUrl).into(img);
        }

        spellsContainer.addView(spellView);
    }

    private String formatTips(List<String> tips) {
        if (tips == null || tips.isEmpty()) return "No tips available.";
        StringBuilder sb = new StringBuilder();
        for (String tip : tips) {
            sb.append("• ").append(tip).append("\n\n");
        }
        return sb.toString().trim();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
