package com.example.lolop;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.lolop.model.Champion;

public class DetailChampion extends AppCompatActivity {

    public static final String EXTRA_CHAMPION = "extra_champion";

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

        ImageView championSplash = findViewById(R.id.champion_splash);
        TextView championName = findViewById(R.id.champion_name);
        TextView championTitle = findViewById(R.id.champion_title);
        TextView championLore = findViewById(R.id.champion_lore);

        Champion champion = getIntent().getParcelableExtra(EXTRA_CHAMPION);

        if (champion != null) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(champion.getName());
            }
            championName.setText(champion.getName());
            championTitle.setText(champion.getTitle());
            championLore.setText(champion.getLore());

            String splashUrl = "https://ddragon.leagueoflegends.com/cdn/img/champion/splash/" + champion.getId() + "_0.jpg";

            Glide.with(this)
                    .load(splashUrl)
                    .into(championSplash);
        }
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
