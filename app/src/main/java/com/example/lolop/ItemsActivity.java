package com.example.lolop;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lolop.adapter.CategoryAdapter;
import com.example.lolop.adapter.OverlayItemAdapter;
import com.example.lolop.api.RetrofitClient;
import com.example.lolop.databinding.ActivityItemsBinding;
import com.example.lolop.model.Item;
import com.example.lolop.model.ItemResponse;
import androidx.recyclerview.widget.GridLayoutManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.OnBackPressedCallback;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItemsActivity extends AppCompatActivity {

    private ActivityItemsBinding binding;
    private CategoryAdapter categoryAdapter;
    private final Map<String, String> englishItemNames = new HashMap<>();
    private static final Map<String, String> TAG_TRANSLATIONS = new HashMap<>();
    private final Map<String, Item> representativeItems = new HashMap<>(); // Store stable representative item for each category
    
    private String currentVersion = "14.5.1"; // Default fallback
    private List<String> listDataHeader = new ArrayList<>();
    private final HashMap<String, List<Item>> listDataChild = new HashMap<>();
    private List<String> originalDataHeader = new ArrayList<>();
    private HashMap<String, List<Item>> originalDataChild = new HashMap<>();

    static {
        TAG_TRANSLATIONS.put("Boots", "Bottes");
        TAG_TRANSLATIONS.put("ManaRegen", "Régén. Mana");
        TAG_TRANSLATIONS.put("HealthRegen", "Régén. PV");
        TAG_TRANSLATIONS.put("Health", "PV");
        TAG_TRANSLATIONS.put("CriticalStrike", "Coup Critique");
        TAG_TRANSLATIONS.put("SpellDamage", "Puissance");
        TAG_TRANSLATIONS.put("Armor", "Armure");
        TAG_TRANSLATIONS.put("SpellBlock", "Résistance Magique");
        TAG_TRANSLATIONS.put("LifeSteal", "Vol de Vie");
        TAG_TRANSLATIONS.put("Damage", "Dégâts Physiques");
        TAG_TRANSLATIONS.put("AttackSpeed", "Vitesse d'Attaque");
        TAG_TRANSLATIONS.put("CooldownReduction", "Accélération de Compétence");
        TAG_TRANSLATIONS.put("MagicPenetration", "Pénétration Magique");
        TAG_TRANSLATIONS.put("ArmorPenetration", "Pénétration d'Armure");
        TAG_TRANSLATIONS.put("Consumable", "Consommable");
        TAG_TRANSLATIONS.put("GoldPer", "Revenus");
        TAG_TRANSLATIONS.put("Vision", "Vision");
        TAG_TRANSLATIONS.put("Active", "Actif");
        TAG_TRANSLATIONS.put("Movement", "Déplacement");
        TAG_TRANSLATIONS.put("SpellVamp", "Omnivampirisme"); // Updated to Omnivampirisme as per user context usually
        TAG_TRANSLATIONS.put("Stealth", "Furtivité");
        TAG_TRANSLATIONS.put("NonbootsMovement", "Vitesse (Hors Bottes)"); 
        TAG_TRANSLATIONS.put("Aura", "Aura"); 
        TAG_TRANSLATIONS.put("Slow", "Ralentissement");
        TAG_TRANSLATIONS.put("Tenacity", "Ténacité");
        TAG_TRANSLATIONS.put("MagicResist", "Résistance Magique");
        TAG_TRANSLATIONS.put("AbilityHaste", "Accélération de Compétence");
        TAG_TRANSLATIONS.put("OnHit", "À l'impact");
        TAG_TRANSLATIONS.put("Trinket", "Relique");
        TAG_TRANSLATIONS.put("Jungle", "Jungle");
        TAG_TRANSLATIONS.put("Lane", "Voie");
    }

    private static final Map<String, String> CATEGORY_ALIASES = new HashMap<>();
    static {
        CATEGORY_ALIASES.put("on hit", "À l'impact");
        CATEGORY_ALIASES.put("on-hit", "À l'impact");
        CATEGORY_ALIASES.put("ad", "Dégâts Physiques");
        CATEGORY_ALIASES.put("attack damage", "Dégâts Physiques");
        CATEGORY_ALIASES.put("ap", "Puissance");
        CATEGORY_ALIASES.put("ability power", "Puissance");
        CATEGORY_ALIASES.put("mr", "Résistance Magique");
        CATEGORY_ALIASES.put("magic resist", "Résistance Magique");
        CATEGORY_ALIASES.put("magic resistance", "Résistance Magique");
        CATEGORY_ALIASES.put("cdr", "Accélération de Compétence");
        CATEGORY_ALIASES.put("cooldown reduction", "Accélération de Compétence");
        CATEGORY_ALIASES.put("ah", "Accélération de Compétence");
        CATEGORY_ALIASES.put("ability haste", "Accélération de Compétence");
        CATEGORY_ALIASES.put("speed", "Déplacement");
        CATEGORY_ALIASES.put("move speed", "Déplacement");
        CATEGORY_ALIASES.put("movement speed", "Déplacement");
        CATEGORY_ALIASES.put("ms", "Déplacement");
        CATEGORY_ALIASES.put("vamp", "Omnivampirisme");
        CATEGORY_ALIASES.put("omnivamp", "Omnivampirisme");
        CATEGORY_ALIASES.put("lifesteal", "Vol de Vie");
        CATEGORY_ALIASES.put("life steal", "Vol de Vie");
        CATEGORY_ALIASES.put("crit", "Coup Critique");
        CATEGORY_ALIASES.put("critical strike", "Coup Critique");
        CATEGORY_ALIASES.put("as", "Vitesse d'Attaque");
        CATEGORY_ALIASES.put("attack speed", "Vitesse d'Attaque");
        CATEGORY_ALIASES.put("lethality", "Létalité");
        CATEGORY_ALIASES.put("gw", "Anti-soin");
        CATEGORY_ALIASES.put("anti heal", "Anti-soin");
        CATEGORY_ALIASES.put("grievous wounds", "Anti-soin");
        CATEGORY_ALIASES.put("shield reduce", "Anti-bouclier");
        CATEGORY_ALIASES.put("anti shield", "Anti-bouclier");
        CATEGORY_ALIASES.put("armor", "Armure");
        CATEGORY_ALIASES.put("health", "PV");
        CATEGORY_ALIASES.put("hp", "PV");
        CATEGORY_ALIASES.put("mana", "Mana");
        CATEGORY_ALIASES.put("mana regen", "Régén. Mana");
        CATEGORY_ALIASES.put("health regen", "Régén. PV");
        CATEGORY_ALIASES.put("hp regen", "Régén. PV");
        CATEGORY_ALIASES.put("armor pen", "Pénétration d'Armure");
        CATEGORY_ALIASES.put("armor penetration", "Pénétration d'Armure");
        CATEGORY_ALIASES.put("magic pen", "Pénétration Magique");
        CATEGORY_ALIASES.put("magic penetration", "Pénétration Magique");
        CATEGORY_ALIASES.put("tenacity", "Ténacité");
        CATEGORY_ALIASES.put("spellblade", "Lame enchantée");
        CATEGORY_ALIASES.put("sheen", "Lame enchantée");
        CATEGORY_ALIASES.put("lifeline", "Lien vital");
        CATEGORY_ALIASES.put("shield", "Bouclier");
        CATEGORY_ALIASES.put("slow", "Ralentissement");
    }

    private static final Map<String, String> preferredCategoryImages = new HashMap<>();
    static {
        preferredCategoryImages.put("Dégâts Physiques", "Épée longue");
        preferredCategoryImages.put("Puissance", "Coiffe de Rabadon");
        preferredCategoryImages.put("PV", "Cristal de rubis");
        preferredCategoryImages.put("Pénétration d'Armure", "Dernier souffle");
        preferredCategoryImages.put("Pénétration Magique", "Joyau putréfiant");
        preferredCategoryImages.put("Vol de Vie", "Soif-de-sang");
        preferredCategoryImages.put("Régén. PV", "Armure de Warmog");
        preferredCategoryImages.put("Vitesse (Hors Bottes)", "Plaque de lune ailée");
        preferredCategoryImages.put("Ténacité", "Gage de Sterak");
        preferredCategoryImages.put("Actif", "Sablier de Zhonya");
        preferredCategoryImages.put("Aura", "Égide solaire");
        preferredCategoryImages.put("À l'impact", "Tueur de krakens");
        preferredCategoryImages.put("Jungle", "Bébé Ixamandre"); // Assuming name match
        preferredCategoryImages.put("Omnivampirisme", "Créateur de failles");
        preferredCategoryImages.put("Anti-soin", "Morellonomicon");
        preferredCategoryImages.put("Anti-bouclier", "Crochet de serpent");
        preferredCategoryImages.put("Létalité", "Dague dentelée");
        preferredCategoryImages.put("Lame enchantée", "Brillance");
        preferredCategoryImages.put("Lien vital", "Arc-bouclier immortel");
        preferredCategoryImages.put("Bouclier", "Médaillon de l'Iron Solari");
    }

    private static final Map<String, String> categoryDescriptions = new HashMap<>();
    static {
        categoryDescriptions.put("Dégâts Physiques", "Augmente la puissance de vos attaques de base et de vos compétences physiques.");
        categoryDescriptions.put("Puissance", "Augmente les dégâts de vos compétences magiques.");
        categoryDescriptions.put("PV", "Augmente votre santé maximale pour subir plus de dégâts.");
        categoryDescriptions.put("Armure", "Réduit les dégâts physiques subis.");
        categoryDescriptions.put("Résistance Magique", "Réduit les dégâts magiques subis.");
        categoryDescriptions.put("Vitesse d'Attaque", "Augmente la fréquence de vos attaques de base.");
        categoryDescriptions.put("Accélération de Compétence", "Réduit le temps de récupération de vos compétences.");
        categoryDescriptions.put("Coup Critique", "Augmente la chance que vos attaques infligent des dégâts doublés.");
        categoryDescriptions.put("Mana", "Réserve d'énergie pour lancer des sorts.");
        categoryDescriptions.put("Régén. Mana", "Vitesse à laquelle votre mana se recharge.");
        categoryDescriptions.put("Régén. PV", "Vitesse à laquelle vos PV remontent hors combat.");
        categoryDescriptions.put("Vol de Vie", "Vous soigne d'un pourcentage des dégâts physiques infligés par vos attaques.");
        categoryDescriptions.put("Omnivampirisme", "Vous soigne d'un pourcentage de TOUS les dégâts infligés (sorts inclus).");
        categoryDescriptions.put("Létalité", "Ignore une quantité fixe d'armure. Très efficace contre les cibles fragiles.");
        categoryDescriptions.put("Pénétration d'Armure", "Ignore un pourcentage d'armure. Efficace contre les tanks.");
        categoryDescriptions.put("Pénétration Magique", "Ignore de la résistance magique. Indispensable contre les tanks.");
        categoryDescriptions.put("Déplacement", "Augmente votre vitesse de déplacement.");
        categoryDescriptions.put("Vitesse (Hors Bottes)", "Objets de déplacement autres que les bottes.");
        categoryDescriptions.put("Consommable", "Objets à usage unique (potions, balises).");
        categoryDescriptions.put("Relique", "Objets de vision gratuits (Totem, Brouilleur).");
        categoryDescriptions.put("Ténacité", "Réduit la durée des contrôles de foule (étourdissements, etc.).");
        categoryDescriptions.put("Actif", "Objets possédant une compétence activable manuellement.");
        categoryDescriptions.put("Aura", "Objets conférant un bonus passif aux alliés proches.");
        categoryDescriptions.put("À l'impact", "Effets supplémentaires appliqués à chaque attaque de base.");
        categoryDescriptions.put("Ralentissement", "Objets capables de ralentir les ennemis.");
        categoryDescriptions.put("Revenus", "Dédiés aux supports pour gagner de l'or sans tuer de sbires.");
        categoryDescriptions.put("Vision", "Tout ce qui concerne la pose ou destruction de balises.");
        categoryDescriptions.put("Jungle", "Objets de départ pour les junglers.");
        categoryDescriptions.put("Voie", "Objets de départ pour les laners (Doran, Bouclier...).");
        
        categoryDescriptions.put("Anti-soin", "Applique 'Hémorragie' pour réduire les soins reçus par les ennemis.");
        categoryDescriptions.put("Anti-bouclier", "Réduit l'efficacité des boucliers ennemis.");
        categoryDescriptions.put("Lame enchantée", "Renforce la prochaine attaque après avoir lancé un sort (Effet Brillance).");
        categoryDescriptions.put("Lien vital", "Déclenche un bouclier de survie quand vos PV tombent bas.");
        categoryDescriptions.put("Bouclier", "Objets capables d'octroyer un bouclier à vous-même ou un allié.");
        categoryDescriptions.put("Stase", "Vous rend invulnérable et incapable d'agir pendant une courte durée.");
    }

    private static final List<String> CATEGORY_ORDER = Arrays.asList(
        "Bottes",
        "Dégâts Physiques",
        "Puissance",
        "Létalité",       // New
        "Lame enchantée", // New
        "PV",
        "Bouclier",       // New
        "Lien vital",     // New
        "Pénétration d'Armure",
        "Pénétration Magique",
        "Vol de Vie",
        "Omnivampirisme", 
        "Anti-soin",     // New
        "Anti-bouclier", // New
        "Mana",
        "Régén. Mana",
        "Régén. PV",
        "Déplacement",
        "Vitesse (Hors Bottes)",
        "Ténacité",
        "Actif",
        "Aura",
        "Ralentissement",
        "À l'impact",
        "Revenus",
        "Vision",
        "Jungle",
        "Voie",
        "Relique"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityItemsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupNavigation();
        setupSearch();
        setupOverlay();
        fetchLatestVersion();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isOverlayVisible()) {
                    hideOverlay();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    // Overlay Fields
    private FrameLayout overlayContainer;
    private View overlayBackground;
    private ImageView btnCloseOverlay;
    private ImageView btnInfoOverlay; // New
    private TextView tvOverlayTitle;
    private TextView tvOverlayDescription; // New
    private RecyclerView rvOverlayItems;

    private void setupOverlay() {
        overlayContainer = findViewById(R.id.overlayContainer);
        overlayBackground = findViewById(R.id.overlayBackground);
        btnCloseOverlay = findViewById(R.id.btnCloseOverlay);
        btnInfoOverlay = findViewById(R.id.btnInfoOverlay);
        tvOverlayTitle = findViewById(R.id.tvOverlayTitle);
        tvOverlayDescription = findViewById(R.id.tvOverlayDescription);
        rvOverlayItems = findViewById(R.id.rvOverlayItems);

        rvOverlayItems.setLayoutManager(new GridLayoutManager(this, 3)); // 3 columns for items as requested
        
        // Disable nested scrolling for the overlay list so it doesn't propagate scroll events to the CoordinatorLayout/AppBar
        rvOverlayItems.setNestedScrollingEnabled(false);

        // Consume all touch events on the background to prevent them from passing through to the list behind
        overlayBackground.setOnTouchListener((v, event) -> {
            hideOverlay();
            return true; // Consume the event
        });
        
        btnCloseOverlay.setOnClickListener(v -> hideOverlay());
        
        // Toggle description
        btnInfoOverlay.setOnClickListener(v -> {
            if (tvOverlayDescription.getVisibility() == View.VISIBLE) {
                tvOverlayDescription.setVisibility(View.GONE);
            } else {
                tvOverlayDescription.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showOverlay(String category) {
        if (overlayContainer == null) return;
        
        tvOverlayTitle.setText(category);
        
        // Set info text
        String desc = categoryDescriptions.get(category);
        if (desc != null) {
            tvOverlayDescription.setText(desc);
            btnInfoOverlay.setVisibility(View.VISIBLE);
        } else {
            btnInfoOverlay.setVisibility(View.GONE);
        }
        // Always start hidden
        tvOverlayDescription.setVisibility(View.GONE);
        
        List<Item> items = listDataChild.get(category);
        if (items != null) {
            OverlayItemAdapter adapter = new OverlayItemAdapter(this, items, currentVersion);
            rvOverlayItems.setAdapter(adapter);
        } else {
             rvOverlayItems.setAdapter(null);
        }

        overlayContainer.setVisibility(View.VISIBLE);
        overlayContainer.setAlpha(0f);
        overlayContainer.animate().alpha(1f).setDuration(200).start();
        
        // Disable scrolling on the main list
        binding.rvCategories.setNestedScrollingEnabled(false);
        // Also suppress layout to be extremely safe, although nestedScrollingEnabled usually suffices for Grid
        // binding.rvCategories.suppressLayout(true);
    }

    private void hideOverlay() {
        if (overlayContainer == null) return;
        
        // Re-enable scrolling on the main list
        binding.rvCategories.setNestedScrollingEnabled(true);
        // binding.rvCategories.suppressLayout(false);

        overlayContainer.animate().alpha(0f).setDuration(200).withEndAction(() -> 
            overlayContainer.setVisibility(View.GONE)
        ).start();
    }
    
    private boolean isOverlayVisible() {
        return overlayContainer != null && overlayContainer.getVisibility() == View.VISIBLE;
    }

    private void fetchLatestVersion() {
        binding.progressBarItems.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService().getVersions().enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(@NonNull Call<List<String>> call, @NonNull Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    currentVersion = response.body().get(0);
                }
                fetchItems();
            }

            @Override
            public void onFailure(@NonNull Call<List<String>> call, @NonNull Throwable t) {
                fetchItems();
            }
        });
    }

    private void setupNavigation() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_items);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_champions) {
                startActivity(new Intent(ItemsActivity.this, MainActivity.class));
                overridePendingTransition(0, 0); // No animation
                return true;
            }
            return item.getItemId() == R.id.nav_items;
        });
    }

    private void setupSearch() {
        binding.etSearchItems.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(s.toString());
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filterData(String query) {
        if (categoryAdapter == null) return;

        if (query.isEmpty()) {
            listDataHeader = new ArrayList<>(originalDataHeader);
            listDataChild.clear();
            listDataChild.putAll(originalDataChild);
            categoryAdapter.updateData(originalDataHeader, originalDataChild);
            return;
        }

        String normalizedQuery = stripAccents(query.toLowerCase());
        List<String> filteredHeaders = new ArrayList<>();
        HashMap<String, List<Item>> filteredChild = new HashMap<>();
        
        Map<String, Item> allMatchesMap = new HashMap<>();

        for (String header : originalDataHeader) {
            List<Item> originalItems = originalDataChild.get(header);
            List<Item> filteredItems = new ArrayList<>();
            
            boolean headerMatches = stripAccents(header.toLowerCase()).contains(normalizedQuery);

            if (!headerMatches) {
                for (Map.Entry<String, String> entry : CATEGORY_ALIASES.entrySet()) {
                    if (entry.getKey().contains(normalizedQuery)) {
                        if (header.equalsIgnoreCase(entry.getValue())) {
                            headerMatches = true;
                            break;
                        }
                    }
                }
            }

            if (headerMatches) {
                // If header matches, show ALL items in this category
                if (originalItems != null) {
                    filteredItems.addAll(originalItems);
                }
            } else if (originalItems != null) {
                // Iterate through items individually to check for matches
                for (Item item : originalItems) {
                    boolean match = false;
                    
                    // Check French Name
                    if (item.getName() != null) {
                        String normalizedItemName = stripAccents(item.getName().toLowerCase());
                        if (normalizedItemName.contains(normalizedQuery)) {
                            match = true;
                        }
                    }
                    
                    // Check English Name
                    if (!match && item.getId() != null) {
                         String enName = englishItemNames.get(item.getId());
                         if (enName != null) {
                             String normalizedEnName = stripAccents(enName.toLowerCase());
                             if (normalizedEnName.contains(normalizedQuery)) {
                                 match = true;
                             }
                         }
                    }

                    if (match) {
                        filteredItems.add(item);
                    }
                }
            }

            if (!filteredItems.isEmpty()) {
                filteredHeaders.add(header);
                filteredChild.put(header, filteredItems);
                
                for (Item item : filteredItems) {
                    if (item.getId() != null) {
                        allMatchesMap.put(item.getId(), item);
                    }
                }
            }
        }
        
        if (!allMatchesMap.isEmpty()) {
            String toutHeader = "Tout";
            List<Item> allItems = new ArrayList<>(allMatchesMap.values());
            Collections.sort(allItems, (i1, i2) -> {
                String n1 = i1.getName();
                String n2 = i2.getName();
                if (n1 == null) return -1;
                if (n2 == null) return 1;
                return n1.compareToIgnoreCase(n2);
            });
            
            filteredHeaders.add(0, toutHeader);
            filteredChild.put(toutHeader, allItems);
        }

        listDataHeader = filteredHeaders;
        listDataChild.clear();
        listDataChild.putAll(filteredChild);
        categoryAdapter.updateData(filteredHeaders, filteredChild);
    }

    private String stripAccents(String s) {
        if (s == null) return null;
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return s;
    }

    private void fetchItems() {
        binding.progressBarItems.setVisibility(View.VISIBLE);
        // Fetch French Data first (Primary)
        RetrofitClient.getApiService().getItemsByLanguage(currentVersion, "fr_FR").enqueue(new Callback<ItemResponse>() {
            @Override
            public void onResponse(@NonNull Call<ItemResponse> call, @NonNull Response<ItemResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                     processItems(response.body().getData());
                     // After French data loaded, fetch English data for search indexing
                     fetchEnglishItems(); 
                } else {
                    binding.progressBarItems.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ItemResponse> call, @NonNull Throwable t) {
                binding.progressBarItems.setVisibility(View.GONE);
            }
        });
    }
    
    private void fetchEnglishItems() {
        RetrofitClient.getApiService().getItemsByLanguage(currentVersion, "en_US").enqueue(new Callback<ItemResponse>() {
            @Override
            public void onResponse(@NonNull Call<ItemResponse> call, @NonNull Response<ItemResponse> response) {
                binding.progressBarItems.setVisibility(View.GONE); // Hide progress only after secondary fetch (or primary if parallel)
                
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    for (Map.Entry<String, Item> entry : response.body().getData().entrySet()) {
                        if (entry.getValue().getName() != null) {
                            englishItemNames.put(entry.getKey(), entry.getValue().getName());
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ItemResponse> call, @NonNull Throwable t) {
                binding.progressBarItems.setVisibility(View.GONE);
            }
        });
    }

    private void processItems(Map<String, Item> data) {
        if (data == null) return;

        // Reset data
        listDataHeader.clear();
        listDataChild.clear();

        // Temporary map to group items by tag
        HashMap<String, List<Item>> tempMap = new HashMap<>();

        // Pre-process: Sort by ID to prioritize standard items and Deduplicate by Name
        List<Item> sortedItems = new ArrayList<>();
        for (Map.Entry<String, Item> entry : data.entrySet()) {
            Item item = entry.getValue();
            item.setId(entry.getKey());
            sortedItems.add(item);
        }
        
        // Sort by ID (numerical) to prioritize lower IDs (standard items) over higher IDs (special modes)
        Collections.sort(sortedItems, (i1, i2) -> {
            try {
                int id1 = Integer.parseInt(i1.getId());
                int id2 = Integer.parseInt(i2.getId());
                return Integer.compare(id1, id2);
            } catch (NumberFormatException e) {
                return i1.getId().compareTo(i2.getId());
            }
        });

        Set<String> seenNames = new HashSet<>();

        for (Item item : sortedItems) {
            // Filter out items without name or generic/internal items
            if (item.getName() == null || item.getName().isEmpty()) continue;

            // Deduplicate by Name (Case insensitive logic if needed, but strict name usually works)
            if (seenNames.contains(item.getName())) continue;
            seenNames.add(item.getName());

            // map 11 is Summoner's Rift
            if (item.getMaps() == null || !Boolean.TRUE.equals(item.getMaps().get("11"))) {
                continue;
            }

            // Exclude non-purchasable items or hidden items
            if (!item.isInStore()) continue;
            if (item.getGold() != null && !item.getGold().isPurchasable()) continue;

            // Ensure we don't show required champion items unless necessary
            if (item.getRequiredChampion() != null) continue;

            // Filter out Ornn upgrades (they contain <ornnBonus> in description)
            if (item.getDescription() != null && item.getDescription().contains("<ornnBonus>")) {
                continue;
            }

            // Explicitly filter "obsidienne" items as requested
            if (item.getName().toLowerCase().contains("obsidienne")) {
                continue;
            }
            
            // Map items to tags
            List<String> tags = item.getTags();
            if (tags == null || tags.isEmpty()) continue;

            Set<String> uniqueCategories = new HashSet<>();
            for (String tag : tags) {
                uniqueCategories.add(formatTag(tag));
            }
            for (String category : uniqueCategories) {
                addToMap(tempMap, category, item);
            }
            
            // Custom Logic: Anti-heal (Hémorragie)
            // Check description for keywords
            if (item.getDescription() != null) {
                String lowerDesc = item.getDescription().toLowerCase();
                // Keep Anti-heal logic
                if (lowerDesc.contains("hémorragie") || lowerDesc.contains("grievous wounds") || lowerDesc.contains("réduit les soins")) {
                    addToMap(tempMap, "Anti-soin", item);
                }
                
                // Consolided Anti-shield Logic to prevent duplicates
                boolean isAntiShield = false;
                if (lowerDesc.contains("réduit les boucliers") || lowerDesc.contains("shield reave")) {
                    isAntiShield = true;
                } else if (item.getName() != null && item.getName().equalsIgnoreCase("Crochet de serpent")) {
                    isAntiShield = true;
                }
                
                if (isAntiShield) {
                     addToMap(tempMap, "Anti-bouclier", item);
                }

                // Létalité
                if (lowerDesc.contains("létalité") || lowerDesc.contains("lethality")) {
                    addToMap(tempMap, "Létalité", item);
                }

                // Lame enchantée (Spellblade)
                if (lowerDesc.contains("lame enchantée") || lowerDesc.contains("spellblade")) {
                    addToMap(tempMap, "Lame enchantée", item);
                }

                // Lien vital (Lifeline)
                if (lowerDesc.contains("lien vital") || lowerDesc.contains("lifeline")) {
                    addToMap(tempMap, "Lien vital", item);
                }

                // Bouclier (Donne un bouclier / Shielding)
                // Use keywords like "confère un bouclier" (grants a shield) to avoid anti-shield items
                if (lowerDesc.contains("confère un bouclier") || lowerDesc.contains("octroie un bouclier") || lowerDesc.contains("grants a shield")) {
                    addToMap(tempMap, "Bouclier", item);
                }
            }
        }

        // Sort headers by custom order
        listDataHeader = new ArrayList<>(tempMap.keySet());
        // Explicitly remove "Furtivité" if it exists in the keys
        listDataHeader.remove("Furtivité");

        Collections.sort(listDataHeader, (o1, o2) -> {
            int index1 = CATEGORY_ORDER.indexOf(o1);
            int index2 = CATEGORY_ORDER.indexOf(o2);
            
            // If both are in the list, compare indices
            if (index1 != -1 && index2 != -1) {
                return Integer.compare(index1, index2);
            }
            // If only one is in the list, it comes first
            if (index1 != -1) return -1;
            if (index2 != -1) return 1;
            
            // If neither is in the list, sort alphabetically
            return o1.compareToIgnoreCase(o2);
        });

        // Sort items within each header and populate child map
        for (String header : listDataHeader) {
            List<Item> items = tempMap.get(header);
            
            if (items != null) {
                // Sort by gold, then name
                Collections.sort(items, (i1, i2) -> {
                     int price1 = (i1.getGold() != null) ? i1.getGold().getTotal() : 0;
                     int price2 = (i2.getGold() != null) ? i2.getGold().getTotal() : 0;
                     if (price1 != price2) return Integer.compare(price1, price2);
                     
                     String n1 = i1.getName();
                     String n2 = i2.getName();
                     if (n1 == null) return -1;
                     if (n2 == null) return 1;
                     return n1.compareToIgnoreCase(n2);
                });
            }

            listDataChild.put(header, items);
            
            // Capture representative item for this header (First item)
            // Only do this on initial load (when representativeItems is empty or we want to ensure stability)
            // Capture representative item for this header
            if (items != null && !items.isEmpty() && !representativeItems.containsKey(header)) {
                Item repItem = items.get(0); // Default to first item
                
                // Check for preferred custom image
                if (preferredCategoryImages.containsKey(header)) {
                    String preferredName = preferredCategoryImages.get(header);
                    // Search in ALL items to find the preferred one, not just this category's items 
                    // (though it likely should be in this category, scanning global sorted set is safer if we want strict match)
                    // For improved performance, we check items in this category first.
                    
                    boolean found = false;
                    for (Item item : items) {
                         if (item.getName() != null && item.getName().equalsIgnoreCase(preferredName)) {
                             repItem = item;
                             found = true;
                             break;
                         }
                    }
                    
                    if (!found) {
                        // Fallback: search in all sortedItems if not found in category (rare case)
                         for (Item item : sortedItems) { // sortedItems is available in this scope
                             if (item.getName() != null && item.getName().equalsIgnoreCase(preferredName)) {
                                 repItem = item;
                                 break;
                             }
                         }
                    }
                }
                
                representativeItems.put(header, repItem);
            }
        }

        // Save original for search
        originalDataHeader = new ArrayList<>(listDataHeader);
        originalDataChild = new HashMap<>(listDataChild);

        // Set adapter
        categoryAdapter = new CategoryAdapter(this, listDataHeader, listDataChild, currentVersion, representativeItems, category -> {
             showOverlay(category);
        });
        
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                // "Tout" takes 2 spans (full width), others take 1
                if ("Tout".equals(categoryAdapter.getItemCount() > position ? listDataHeader.get(position) : "")) {
                    return 2;
                }
                return 1;
            }
        });
        
        binding.rvCategories.setLayoutManager(layoutManager);
        binding.rvCategories.setAdapter(categoryAdapter);
    }

    private void addToMap(HashMap<String, List<Item>> map, String key, Item item) {
        if (!map.containsKey(key)) {
            map.put(key, new ArrayList<>());
        }
        // map.get(key) is guaranteed not null because we just put it if it was missing
        List<Item> list = map.get(key);
        if (list != null) {
            list.add(item);
        }
    }

    private String formatTag(String tag) {
        if (TAG_TRANSLATIONS.containsKey(tag)) {
            return TAG_TRANSLATIONS.get(tag);
        }
        // Fallback: Split CamelCase (e.g., "AbilityHaste" -> "Ability Haste")
        return tag.replaceAll(
            String.format("%s|%s|%s",
                "(?<=[A-Z])(?=[A-Z][a-z])",
                "(?<=[^A-Z])(?=[A-Z])",
                "(?<=[A-Za-z])(?=[^A-Za-z])"
            ),
            " "
        );
    }
}
