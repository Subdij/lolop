package com.example.lolop;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.lolop.model.Champion;

public class DetailChampion extends AppCompatActivity {

    public static final String EXTRA_CHAMPION = "extra_champion";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailchampion);

        ImageView championSplash = findViewById(R.id.champion_splash);
        TextView championName = findViewById(R.id.champion_name);
        TextView championTitle = findViewById(R.id.champion_title);
        TextView championLore = findViewById(R.id.champion_lore);

        Champion champion = getIntent().getParcelableExtra(EXTRA_CHAMPION);

        if (champion != null) {
            championName.setText(champion.getName());
            championTitle.setText(champion.getTitle());
            championLore.setText(champion.getLore());

            String splashUrl = "https://ddragon.leagueoflegends.com/cdn/img/champion/splash/" + champion.getId() + "_0.jpg";

            Glide.with(this)
                    .load(splashUrl)
                    .into(championSplash);
        }
    }
}
