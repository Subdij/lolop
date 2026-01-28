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

/**
 * Activité affichant les détails complets d'un champion.
 * Affiche les informations de base, les sorts, les tips et la gestion des favoris.
 */
public class DetailChampion extends AppCompatActivity {

    // Constantes pour passer les données via Intent
    public static final String EXTRA_CHAMPION = "extra_champion";
    public static final String EXTRA_VERSION = "extra_version";
    private String currentVersion = "14.5.1";

    // Éléments d'interface utilisateur
    private ImageView championSplash;          // Image de splash du champion
    private TextView championName;             // Nom du champion
    private TextView championTitle;            // Titre du champion
    private TextView championTags;             // Tags/rôles du champion
    private LinearLayout difficultyBarsContainer; // Conteneur des barres de difficulté
    private TextView championLore;             // Histoire du champion
    private ImageView passiveImage;            // Image de la capacité passive
    private TextView passiveName;              // Nom de la capacité passive
    private TextView passiveDescription;       // Description de la capacité passive
    private LinearLayout spellsContainer;      // Conteneur des 4 sorts actifs
    private TextView allyTips;                 // Conseils pour les alliés
    private TextView enemyTips;                // Conseils contre les ennemis
    private TextView allyTipsHeader;           // En-tête des conseils alliés
    private TextView enemyTipsHeader;          // En-tête des conseils ennemis
    private View tipsSeparator;                // Séparateur visuel entre les deux sections de tips
    private ImageView ivFavorite;              // Bouton favoris (étoile)
    
    // Base de données pour gérer les champions favoris
    private com.example.lolop.database.FavoriteDatabase db;
    
    // État du favoris pour ce champion
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailchampion);

        // Initialiser la base de données pour les favoris
        db = new com.example.lolop.database.FavoriteDatabase(this);

        // Configuration de la barre d'outils avec le bouton retour
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Initialiser toutes les références aux vues
        initViews();

        if (getIntent().hasExtra(EXTRA_VERSION)) {
            currentVersion = getIntent().getStringExtra(EXTRA_VERSION);
        }

        // Récupérer le champion passé via l'Intent
        Champion champion = getIntent().getParcelableExtra(EXTRA_CHAMPION);

        // Si un champion est présent, afficher ses informations
        if (champion != null) {
            displayBasicInfo(champion);              // Afficher les infos de base
            fetchChampionDetail(champion.getId());   // Récupérer les détails complets via API
            setupFavoriteButton(champion.getId());   // Configurer le bouton favoris
        }
    }

    /**
     * Initialiser toutes les références aux vues de l'activité.
     * Récupère les éléments du fichier layout.
     */
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
        ivFavorite = findViewById(R.id.ivFavorite);
    }

    /**
     * Configurer le bouton favoris avec son comportement au clic.
     * Vérifie si le champion est déjà en favori et met à jour l'icône.
     */
    private void setupFavoriteButton(String championId) {
        // Vérifier si ce champion est déjà dans les favoris
        isFavorite = db.isFavorite(championId);
        updateFavoriteIcon();

        // Gérer le clic sur le bouton favoris
        ivFavorite.setOnClickListener(v -> {
            if (isFavorite) {
                // Si favori, retirer de la base de données
                db.removeFavorite(championId);
                isFavorite = false;
            } else {
                // Si non favori, ajouter à la base de données
                db.addFavorite(championId);
                isFavorite = true;
            }
            // Mettre à jour l'apparence de l'icône
            updateFavoriteIcon();
        });
    }

    /**
     * Mettre à jour l'icône du bouton favoris en fonction de l'état.
     * Affiche une étoile remplie si favori, vide sinon.
     */
    private void updateFavoriteIcon() {
        if (isFavorite) {
            ivFavorite.setImageResource(R.drawable.ic_star_filled);      // Étoile remplie
        } else {
            ivFavorite.setImageResource(R.drawable.ic_star_outline);    // Étoile vide
        }
    }

    /**
     * Afficher les informations de base du champion.
     * Remplit les champs de texte et charge l'image de splash.
     */
    private void displayBasicInfo(Champion champion) {
        // Définir le titre de l'activité
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(champion.getName());
        }
        
        // Remplir les champs de texte
        championName.setText(champion.getName());
        championTitle.setText(champion.getTitle());
        championLore.setText(champion.getLore());

        // Construire l'URL de l'image de splash
        String splashUrl = "https://ddragon.leagueoflegends.com/cdn/img/champion/splash/" + champion.getId() + "_0.jpg";
        
        // Charger l'image avec Glide
        // En mode économie d'énergie : compression et pas d'animation
        if (com.example.lolop.utils.PowerSavingManager.getInstance().isPowerSavingMode()) {
            Glide.with(this)
                 .load(splashUrl)
                 .format(com.bumptech.glide.load.DecodeFormat.PREFER_RGB_565)
                 .dontAnimate()
                 .into(championSplash);
        } else {
            // Mode normal : charger l'image avec les animations
            Glide.with(this).load(splashUrl).into(championSplash);
        }
    }

    /**
     * Récupérer les détails complets du champion via l'API.
     * Effectue un appel réseau asynchrone pour obtenir toutes les informations.
     */
    private void fetchChampionDetail(String championId) {
        // Récupérer la langue de l'API en fonction de la locale
        String apiLang = com.example.lolop.utils.LocaleHelper.getApiLanguage(this);
        
        // Faire l'appel API asynchrone
        RetrofitClient.getApiService().getChampionDetail(currentVersion, apiLang, championId).enqueue(new Callback<ChampionListResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChampionListResponse> call, @NonNull Response<ChampionListResponse> response) {
                // Si la réponse est réussie et contient des données
                if (response.isSuccessful() && response.body() != null) {
                    // Récupérer le champion des données de réponse
                    Champion detailedChampion = response.body().getData().get(championId);
                    if (detailedChampion != null) {
                        // Mettre à jour l'interface avec les détails
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

    /**
     * Mettre à jour l'interface avec tous les détails du champion.
     * Affiche les infos, barres de difficulté, capacités et conseils.
     */
    private void updateUI(Champion champion) {
        // Mettre à jour les champs de texte
        championLore.setText(champion.getLore());
        championTitle.setText(champion.getTitle());

        // Tags/Rôles du champion
        // Joindre les tags avec un séparateur "|"
        String tags = champion.getTags() != null ? String.join(" | ", champion.getTags()) : "";
        championTags.setText(tags);

        // Barres de difficulté
        // Récupérer le niveau de difficulté (0-10)
        int difficulty = champion.getInfo() != null ? champion.getInfo().getDifficulty() : 0;
        setupDifficultyBars(difficulty);

        // Capacité passive
        if (champion.getPassive() != null) {
            passiveName.setText(champion.getPassive().getName());
            // Convertir le HTML en texte formaté
            passiveDescription.setText(Html.fromHtml(champion.getPassive().getDescription(), Html.FROM_HTML_MODE_COMPACT));
            
            // Charger l'image du passif
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

        // Sorts (Q, W, E, R)
        spellsContainer.removeAllViews();
        if (champion.getSpells() != null) {
            // Ajouter chaque sort à l'interface
            for (Champion.Spell spell : champion.getSpells()) {
                addSpellView(spell);
            }
        }

        // Conseils (Tips)
        // Vérifier si des conseils existent
        boolean hasAllyTips = champion.getAllytips() != null && !champion.getAllytips().isEmpty();
        boolean hasEnemyTips = champion.getEnemytips() != null && !champion.getEnemytips().isEmpty();

        // Mettre à jour les sections de conseils
        updateTipsSection(champion.getAllytips(), allyTipsHeader, allyTips);
        updateTipsSection(champion.getEnemytips(), enemyTipsHeader, enemyTips);

        // Afficher ou masquer le séparateur selon si des tips existent
        if (!hasAllyTips && !hasEnemyTips) {
            tipsSeparator.setVisibility(View.GONE);
        } else {
            tipsSeparator.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Mettre à jour une section de conseils (alliés ou ennemis).
     * Affiche ou masque la section selon si des conseils existent.
     */
    private void updateTipsSection(List<String> tips, TextView header, TextView content) {
        // Si pas de conseils, masquer la section
        if (tips == null || tips.isEmpty()) {
            header.setVisibility(View.GONE);
            content.setVisibility(View.GONE);
        } else {
            // Sinon, afficher la section avec les conseils formatés
            header.setVisibility(View.VISIBLE);
            content.setVisibility(View.VISIBLE);
            content.setText(formatTips(tips));
        }
    }

    /**
     * Créer les barres de difficulté du champion.
     * Affiche un nombre de barres remplies selon le niveau de difficulté.
     */
    private void setupDifficultyBars(int difficulty) {
        // Vider le conteneur avant d'ajouter de nouvelles barres
        difficultyBarsContainer.removeAllViews();
        
        // Constantes pour le rendu
        int maxBars = 10;                                   // Nombre total de barres
        int barWidth = (int) (12 * getResources().getDisplayMetrics().density);
        int barHeight = (int) (4 * getResources().getDisplayMetrics().density);
        int margin = (int) (4 * getResources().getDisplayMetrics().density);

        // Créer et ajouter les barres
        for (int i = 0; i < maxBars; i++) {
            View bar = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(barWidth, barHeight);
            params.setMargins(0, 0, margin, 0);
            bar.setLayoutParams(params);

            // Colorer les barres selon la difficulté
            if (i < difficulty) {
                // Barres remplies : couleur or
                bar.setBackgroundColor(ContextCompat.getColor(this, R.color.lol_gold));
            } else {
                // Barres vides : couleur grise
                bar.setBackgroundColor(ContextCompat.getColor(this, R.color.lol_grey));
            }
            difficultyBarsContainer.addView(bar);
        }
    }

    /**
     * Ajouter un sort (capacité) à l'interface.
     * Charge l'icône du sort via Glide et remplit les informations.
     */
    private void addSpellView(Champion.Spell spell) {
        // Créer une vue à partir du layout item_spell
        View spellView = LayoutInflater.from(this).inflate(R.layout.item_spell, spellsContainer, false);
        
        // Récupérer les éléments de la vue
        ImageView img = spellView.findViewById(R.id.ivSpellIcon);
        TextView name = spellView.findViewById(R.id.tvSpellName);
        TextView desc = spellView.findViewById(R.id.tvSpellDescription);

        // Remplir les champs
        name.setText(spell.getName());
        // Utiliser la description directement au lieu du tooltip
        desc.setText(Html.fromHtml(spell.getDescription(), Html.FROM_HTML_MODE_COMPACT));

        // Construire l'URL de l'icône du sort
        String spellUrl = "https://ddragon.leagueoflegends.com/cdn/" + currentVersion + "/img/spell/" + spell.getImage().getFull();
        
        // Charger l'image avec Glide
        if (com.example.lolop.utils.PowerSavingManager.getInstance().isPowerSavingMode()) {
            // Mode économie d'énergie : compression et pas d'animation
            Glide.with(this)
                 .load(spellUrl)
                 .format(com.bumptech.glide.load.DecodeFormat.PREFER_RGB_565)
                 .dontAnimate()
                 .into(img);
        } else {
            // Mode normal : charger l'image avec animations
            Glide.with(this).load(spellUrl).into(img);
        }

        // Ajouter la vue au conteneur
        spellsContainer.addView(spellView);
    }

    /**
     * Formater une liste de conseils en texte lisible.
     * Ajoute un puces (•) avant chaque conseil.
     */
    private String formatTips(List<String> tips) {
        // Vérifier que la liste n'est pas vide
        if (tips == null || tips.isEmpty()) return "Aucun conseil disponible.";
        
        // Construire le texte formaté
        StringBuilder sb = new StringBuilder();
        for (String tip : tips) {
            // Ajouter une puce avant chaque conseil
            sb.append("• ").append(tip).append("\n\n");
        }
        // Retirer les espaces inutiles à la fin
        return sb.toString().trim();
    }

    /**
     * Gérer les éléments du menu.
     * Permet de revenir à l'activité précédente avec le bouton retour.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Vérifier si c'est le bouton retour
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
