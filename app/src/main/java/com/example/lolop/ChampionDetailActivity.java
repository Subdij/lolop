package com.example.lolop;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.example.lolop.api.RetrofitClient;
import com.example.lolop.database.FavoriteDatabase;
import com.example.lolop.databinding.ActivityChampionDetailBinding;
import com.example.lolop.model.Champion;
import com.example.lolop.model.ChampionListResponse;
import com.example.lolop.model.Item;
import com.example.lolop.model.ItemResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChampionDetailActivity extends AppCompatActivity {

    private ActivityChampionDetailBinding binding;
    private String championId;
    private String version;
    private Map<String, Item> allItems = new HashMap<>();
    private FavoriteDatabase db;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChampionDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = new FavoriteDatabase(this);

        setSupportActionBar(binding.toolbarDetail);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.toolbarDetail.setNavigationOnClickListener(v -> finish());

        championId = getIntent().getStringExtra("CHAMPION_ID");
        version = getIntent().getStringExtra("VERSION");

        if (championId != null) {
            isFavorite = db.isFavorite(championId);
            updateFavoriteFab();
            fetchAllItems();
        }

        binding.fabFavorite.setOnClickListener(v -> toggleFavorite());
    }

    private void toggleFavorite() {
        if (championId == null) return;
        
        if (isFavorite) {
            db.removeFavorite(championId);
            Toast.makeText(this, "Retiré des favoris", Toast.LENGTH_SHORT).show();
        } else {
            db.addFavorite(championId);
            Toast.makeText(this, "Ajouté aux favoris !", Toast.LENGTH_SHORT).show();
        }
        isFavorite = !isFavorite;
        updateFavoriteFab();
    }

    private void updateFavoriteFab() {
        if (isFavorite) {
            binding.fabFavorite.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            binding.fabFavorite.setImageResource(android.R.drawable.btn_star_big_off);
        }
    }

    private void fetchAllItems() {
        RetrofitClient.getApiService().getItems(version).enqueue(new Callback<ItemResponse>() {
            @Override
            public void onResponse(Call<ItemResponse> call, Response<ItemResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allItems = response.body().getData();
                }
                fetchChampionDetail();
            }

            @Override
            public void onFailure(Call<ItemResponse> call, Throwable t) {
                fetchChampionDetail();
            }
        });
    }

    private void fetchChampionDetail() {
        RetrofitClient.getApiService().getChampionDetail(version, championId).enqueue(new Callback<ChampionListResponse>() {
            @Override
            public void onResponse(Call<ChampionListResponse> call, Response<ChampionListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Champion champion = response.body().getData().get(championId);
                    if (champion != null) {
                        updateUI(champion);
                    }
                }
            }

            @Override
            public void onFailure(Call<ChampionListResponse> call, Throwable t) {
                Toast.makeText(ChampionDetailActivity.this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(Champion champion) {
        binding.toolbarDetail.setTitle(champion.getName());
        binding.tvDetailName.setText(champion.getName());
        binding.tvDetailTitle.setText(champion.getTitle());
        binding.tvLore.setText(champion.getLore());

        String splashUrl = "https://ddragon.leagueoflegends.com/cdn/img/champion/splash/" + champion.getId() + "_0.jpg";
        Glide.with(this).load(splashUrl).into(binding.ivChampionSplash);

        // Compétences
        binding.llSpells.removeAllViews();
        if (champion.getPassive() != null) {
            String passiveUrl = "https://ddragon.leagueoflegends.com/cdn/" + version + "/img/passive/" + champion.getPassive().getImage().getFull();
            addSpellView("Passif: " + champion.getPassive().getName(), champion.getPassive().getDescription(), passiveUrl);
        }
        for (Champion.Spell spell : champion.getSpells()) {
            String spellUrl = "https://ddragon.leagueoflegends.com/cdn/" + version + "/img/spell/" + spell.getImage().getFull();
            addSpellView(spell.getName(), spell.getDescription(), spellUrl);
        }

        // Build Meta Réel Saison 14
        binding.llRecommendedBuild.removeAllViews();
        List<String> coreItems = new ArrayList<>();
        List<String> starters = new ArrayList<>();
        String runeType = "Précision";
        
        if (champion.getTags().contains("Mage")) {
            starters.add("1056"); starters.add("2003");
            coreItems.add("6655");
            coreItems.add("3020");
            coreItems.add("3157");
            coreItems.add("3089");
            runeType = "Sorcellerie (Comète)";
        } else if (champion.getTags().contains("Assassin")) {
            starters.add("1055"); starters.add("2003");
            coreItems.add("6690");
            coreItems.add("3142");
            coreItems.add("6692");
            coreItems.add("3158");
            runeType = "Domination (Electrocution)";
        } else if (champion.getTags().contains("Tank")) {
            starters.add("1054"); starters.add("2003");
            coreItems.add("3068");
            coreItems.add("3075");
            coreItems.add("3110");
            coreItems.add("3111");
            runeType = "Volonté (Poigne)";
        } else if (champion.getTags().contains("Marksman")) {
            starters.add("1055"); starters.add("2003");
            coreItems.add("6672");
            coreItems.add("3031");
            coreItems.add("3046");
            coreItems.add("3006");
            runeType = "Précision (Tempo Mortel)";
        } else {
            starters.add("1055");
            coreItems.add("6631");
            coreItems.add("3053");
            coreItems.add("3047");
            runeType = "Précision (Conquérant)";
        }

        addManualBuildBlock("Objets de départ (Patch 14.5+)", starters);
        addManualBuildBlock("Build Meta Populaire (S14)", coreItems);

        // Runes style Blitz
        binding.llTips.removeAllViews();
        addRuneView(runeType);

        // Conseils tactiques
        addTipsSection("Conseils de jeu", champion.getAllytips());
        addTipsSection("Comment le contrer", champion.getEnemytips());
    }

    private void addRuneView(String runeName) {
        TextView tv = new TextView(this);
        tv.setText("Runes recommandées : " + runeName);
        tv.setTextColor(ContextCompat.getColor(this, R.color.lol_gold_bright));
        tv.setTextSize(18);
        tv.setPadding(0, 30, 0, 10);
        binding.llTips.addView(tv);
    }

    private void addSpellView(String name, String description, String imageUrl) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_spell, binding.llSpells, false);
        ImageView ivIcon = view.findViewById(R.id.ivSpellIcon);
        TextView tvName = view.findViewById(R.id.tvSpellName);
        TextView tvDesc = view.findViewById(R.id.tvSpellDescription);
        
        tvName.setText(name);
        tvDesc.setText(description.replaceAll("<[^>]*>", ""));
        Glide.with(this).load(imageUrl).into(ivIcon);
        binding.llSpells.addView(view);
    }

    private void addManualBuildBlock(String title, List<String> itemIds) {
        View blockView = LayoutInflater.from(this).inflate(R.layout.item_build_block, binding.llRecommendedBuild, false);
        TextView tvTitle = blockView.findViewById(R.id.tvBlockTitle);
        LinearLayout itemsContainer = blockView.findViewById(R.id.llItemsContainer);
        tvTitle.setText(title);

        for (String id : itemIds) {
            addItemToContainer(id, itemsContainer);
        }
        binding.llRecommendedBuild.addView(blockView);
    }

    private void addItemToContainer(String id, LinearLayout container) {
        ImageView ivItem = new ImageView(this);
        int size = (int) (52 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(0, 0, 15, 0);
        ivItem.setLayoutParams(params);
        ivItem.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ivItem.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
        ivItem.setPadding(3, 3, 3, 3);

        String itemUrl = "https://ddragon.leagueoflegends.com/cdn/" + version + "/img/item/" + id + ".png";
        Glide.with(this).load(itemUrl).into(ivItem);

        Item itemDetail = allItems.get(id);
        if (itemDetail != null) {
            ivItem.setOnClickListener(v -> {
                String desc = itemDetail.getDescription().replaceAll("<[^>]*>", "");
                Toast.makeText(this, itemDetail.getName() + ": " + desc, Toast.LENGTH_LONG).show();
            });
        }
        container.addView(ivItem);
    }

    private void addTipsSection(String title, List<String> tips) {
        if (tips == null || tips.isEmpty()) return;
        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextColor(ContextCompat.getColor(this, R.color.lol_gold));
        tvTitle.setTextSize(20);
        tvTitle.setPadding(0, 40, 0, 10);
        binding.llTips.addView(tvTitle);
        for (String tip : tips) {
            TextView tvTip = new TextView(this);
            tvTip.setText("• " + tip);
            tvTip.setTextColor(ContextCompat.getColor(this, R.color.white));
            tvTip.setPadding(0, 10, 0, 10);
            binding.llTips.addView(tvTip);
        }
    }
}