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
import com.example.lolop.utils.LocaleHelper;
import android.content.Context;
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

public class ItemsActivity extends AppCompatActivity implements OverlayItemAdapter.OnItemClickListener, com.example.lolop.adapter.GridItemAdapter.OnItemClickListener {

    private ActivityItemsBinding binding;
    private CategoryAdapter categoryAdapter;
    private final Map<String, String> englishItemNames = new HashMap<>();
    private final Map<String, String> TAG_TRANSLATIONS = new HashMap<>();
    private final Map<String, Item> representativeItems = new HashMap<>(); // Store stable representative item for each category

    
    private String currentVersion = "14.5.1"; // Default fallback
    private List<String> listDataHeader = new ArrayList<>();
    private final HashMap<String, List<Item>> listDataChild = new HashMap<>();
    private List<String> originalDataHeader = new ArrayList<>();
    private HashMap<String, List<Item>> originalDataChild = new HashMap<>();

    private void setupTagTranslations() {
        TAG_TRANSLATIONS.put("Boots", getString(R.string.cat_boots));
        TAG_TRANSLATIONS.put("ManaRegen", getString(R.string.cat_mana_regen));
        TAG_TRANSLATIONS.put("HealthRegen", getString(R.string.cat_health_regen));
        TAG_TRANSLATIONS.put("Health", getString(R.string.cat_health));
        TAG_TRANSLATIONS.put("CriticalStrike", getString(R.string.cat_crit));
        TAG_TRANSLATIONS.put("SpellDamage", getString(R.string.cat_ap));
        TAG_TRANSLATIONS.put("Armor", getString(R.string.cat_armor));
        TAG_TRANSLATIONS.put("SpellBlock", getString(R.string.cat_mr));
        TAG_TRANSLATIONS.put("LifeSteal", getString(R.string.cat_life_steal));
        TAG_TRANSLATIONS.put("Damage", getString(R.string.cat_ad));
        TAG_TRANSLATIONS.put("AttackSpeed", getString(R.string.cat_as));
        TAG_TRANSLATIONS.put("CooldownReduction", getString(R.string.cat_ability_haste));
        TAG_TRANSLATIONS.put("MagicPenetration", getString(R.string.cat_magic_pen));
        TAG_TRANSLATIONS.put("ArmorPenetration", getString(R.string.cat_armor_pen));
        TAG_TRANSLATIONS.put("Consumable", getString(R.string.cat_consumable));
        TAG_TRANSLATIONS.put("GoldPer", getString(R.string.cat_gold_per));
        TAG_TRANSLATIONS.put("Vision", getString(R.string.cat_vision));
        TAG_TRANSLATIONS.put("Active", getString(R.string.cat_active));
        TAG_TRANSLATIONS.put("Movement", getString(R.string.cat_move_speed));
        TAG_TRANSLATIONS.put("SpellVamp", getString(R.string.cat_omnivamp));
        TAG_TRANSLATIONS.put("Stealth", getString(R.string.cat_stealth));
        TAG_TRANSLATIONS.put("NonbootsMovement", getString(R.string.cat_speed_no_boots)); 
        TAG_TRANSLATIONS.put("Aura", getString(R.string.cat_aura)); 
        TAG_TRANSLATIONS.put("Slow", getString(R.string.cat_slow));
        TAG_TRANSLATIONS.put("Tenacity", getString(R.string.cat_tenacity));
        TAG_TRANSLATIONS.put("MagicResist", getString(R.string.cat_mr));
        TAG_TRANSLATIONS.put("AbilityHaste", getString(R.string.cat_ability_haste));
        TAG_TRANSLATIONS.put("OnHit", getString(R.string.cat_on_hit));
        TAG_TRANSLATIONS.put("Trinket", getString(R.string.cat_trinket));
        TAG_TRANSLATIONS.put("Jungle", getString(R.string.cat_jungle));
        TAG_TRANSLATIONS.put("Lane", getString(R.string.cat_lane));
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

    private final Map<String, String> preferredCategoryImages = new HashMap<>();
    private void setupPreferredImages() {
        preferredCategoryImages.put(getString(R.string.cat_ad), "Épée longue"); // Using implicit internal map, but keys are display
        preferredCategoryImages.put(getString(R.string.cat_ap), "Coiffe de Rabadon");
        preferredCategoryImages.put(getString(R.string.cat_health), "Cristal de rubis");
        preferredCategoryImages.put(getString(R.string.cat_armor_pen), "Dernier souffle");
        preferredCategoryImages.put(getString(R.string.cat_magic_pen), "Joyau putréfiant");
        preferredCategoryImages.put(getString(R.string.cat_life_steal), "Soif-de-sang");
        preferredCategoryImages.put(getString(R.string.cat_health_regen), "Armure de Warmog");
        preferredCategoryImages.put(getString(R.string.cat_speed_no_boots), "Plaque de lune ailée");
        preferredCategoryImages.put(getString(R.string.cat_tenacity), "Gage de Sterak");
        preferredCategoryImages.put(getString(R.string.cat_active), "Sablier de Zhonya");
        preferredCategoryImages.put(getString(R.string.cat_aura), "Égide solaire");
        preferredCategoryImages.put(getString(R.string.cat_on_hit), "Tueur de krakens");
        preferredCategoryImages.put(getString(R.string.cat_jungle), "Bébé Ixamandre");
        preferredCategoryImages.put(getString(R.string.cat_omnivamp), "Créateur de failles");
        preferredCategoryImages.put(getString(R.string.cat_anti_heal), "Morellonomicon");
        preferredCategoryImages.put(getString(R.string.cat_anti_shield), "Crochet de serpent");
        preferredCategoryImages.put(getString(R.string.cat_lethality), "Dague dentelée");
        preferredCategoryImages.put(getString(R.string.cat_spellblade), "Brillance");
        preferredCategoryImages.put(getString(R.string.cat_lifeline), "Arc-bouclier immortel");
        preferredCategoryImages.put(getString(R.string.cat_shield), "Médaillon de l'Iron Solari");
    }

    private final Map<String, String> categoryDescriptions = new HashMap<>();
    private void setupCategoryDescriptions() {
        categoryDescriptions.put(getString(R.string.cat_ad), "Augmente la puissance de vos attaques de base et de vos compétences physiques.");
        categoryDescriptions.put(getString(R.string.cat_ap), "Augmente les dégâts de vos compétences magiques.");
        categoryDescriptions.put(getString(R.string.cat_health), "Augmente votre santé maximale pour subir plus de dégâts.");
        // Descriptions are static in logic but keys must match current language headers
        // Ideally should externalize descriptions too but keeping it simple for now as requested keys fix
    }

    private final List<String> CATEGORY_ORDER = new ArrayList<>();
    
    private void setupCategoryOrder() {
        CATEGORY_ORDER.clear();
        CATEGORY_ORDER.addAll(Arrays.asList(
            getString(R.string.cat_boots),
            getString(R.string.cat_ad),
            getString(R.string.cat_ap),
            getString(R.string.cat_lethality),
            getString(R.string.cat_spellblade),
            getString(R.string.cat_health),
            getString(R.string.cat_shield),
            getString(R.string.cat_lifeline),
            getString(R.string.cat_armor_pen),
            getString(R.string.cat_magic_pen),
            getString(R.string.cat_life_steal),
            getString(R.string.cat_omnivamp),
            getString(R.string.cat_anti_heal),
            getString(R.string.cat_anti_shield),
            getString(R.string.cat_mana),
            getString(R.string.cat_mana_regen),
            getString(R.string.cat_health_regen),
            getString(R.string.cat_move_speed),
            getString(R.string.cat_speed_no_boots),
            getString(R.string.cat_tenacity),
            getString(R.string.cat_active),
            getString(R.string.cat_aura),
            getString(R.string.cat_slow),
            getString(R.string.cat_on_hit),
            getString(R.string.cat_gold_per),
            getString(R.string.cat_vision),
            getString(R.string.cat_jungle),
            getString(R.string.cat_lane),
            getString(R.string.cat_trinket)
        ));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityItemsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        setupNavigation();
        setupTagTranslations();
        setupCategoryOrder();
        setupPreferredImages();
        setupCategoryDescriptions();
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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
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
            } else if (item.getItemId() == R.id.nav_items) {
                return true;
            } else if (item.getItemId() == R.id.nav_patch) {
                Intent intent = new Intent(ItemsActivity.this, PatchNoteActivity.class);
                intent.putExtra("CURRENT_VERSION", currentVersion);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
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
            String toutHeader = getString(R.string.category_all);
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
        String apiLang = LocaleHelper.getApiLanguage(this);
        // Fetch Primary Language Data
        RetrofitClient.getApiService().getItems(currentVersion, apiLang).enqueue(new Callback<ItemResponse>() {
            @Override
            public void onResponse(@NonNull Call<ItemResponse> call, @NonNull Response<ItemResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                     processItems(response.body().getData());
                     // Fetch secondary language for search (if FR, fetch EN. If EN, fetch FR)
                     String secondaryLang = apiLang.equals("fr_FR") ? "en_US" : "fr_FR";
                     fetchSecondaryItems(secondaryLang); 
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
    
    private void fetchSecondaryItems(String lang) {
        RetrofitClient.getApiService().getItems(currentVersion, lang).enqueue(new Callback<ItemResponse>() {
            @Override
            public void onResponse(@NonNull Call<ItemResponse> call, @NonNull Response<ItemResponse> response) {
                binding.progressBarItems.setVisibility(View.GONE);
                
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
                    addToMap(tempMap, getString(R.string.cat_anti_heal), item);
                }
                
                // Consolided Anti-shield Logic to prevent duplicates
                boolean isAntiShield = false;
                if (lowerDesc.contains("réduit les boucliers") || lowerDesc.contains("shield reave")) {
                    isAntiShield = true;
                } else if (item.getName() != null && item.getName().equalsIgnoreCase("Crochet de serpent")) {
                    isAntiShield = true;
                }
                
                if (isAntiShield) {
                     addToMap(tempMap, getString(R.string.cat_anti_shield), item);
                }

                // Létalité
                if (lowerDesc.contains("létalité") || lowerDesc.contains("lethality")) {
                    addToMap(tempMap, getString(R.string.cat_lethality), item);
                }

                // Lame enchantée (Spellblade)
                if (lowerDesc.contains("lame enchantée") || lowerDesc.contains("spellblade")) {
                     addToMap(tempMap, getString(R.string.cat_spellblade), item);
                }

                // Lien vital (Lifeline)
                if (lowerDesc.contains("lien vital") || lowerDesc.contains("lifeline")) {
                    addToMap(tempMap, getString(R.string.cat_lifeline), item);
                }

                // Bouclier (Donne un bouclier / Shielding)
                // Use keywords like "confère un bouclier" (grants a shield) to avoid anti-shield items
                if (lowerDesc.contains("confère un bouclier") || lowerDesc.contains("octroie un bouclier") || lowerDesc.contains("grants a shield")) {
                     addToMap(tempMap, getString(R.string.cat_shield), item);
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
                if (getString(R.string.category_all).equals(categoryAdapter.getItemCount() > position ? listDataHeader.get(position) : "")) {
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
        
        // Fallback or English: Split CamelCase
        return tag.replaceAll(
            String.format("%s|%s|%s",
                "(?<=[A-Z])(?=[A-Z][a-z])",
                "(?<=[^A-Z])(?=[A-Z])",
                "(?<=[A-Za-z])(?=[^A-Za-z])"
            ),
            " "
        );
    }

    private String formatDescription(String description) {
        if (description == null) return "";
        
        // 1. Structural Clean-up & Keywords
        String s = description
             .replaceAll("<mainText>", "")
             .replaceAll("</mainText>", "")
             .replaceAll("<stats>", "<br>")
             .replaceAll("</stats>", "<br>")
             .replaceAll("<attention>", "<font color='#FFD700'>")
             .replaceAll("</attention>", "</font>")
             .replaceAll("<passive>", "<font color='#FFD700'>")
             .replaceAll("</passive>", "</font>")
             .replaceAll("<active>", "<font color='#FFD700'>")
             .replaceAll("</active>", "</font>")
             .replaceAll("<status>", "<font color='#FFFFFF'>")
             .replaceAll("</status>", "</font>")
             .replaceAll("<keywordStealth>", "<font color='#9370DB'>")
             .replaceAll("</keywordStealth>", "</font>")
             .replaceAll("<rarityLegendary>", "<font color='#FFD700'>") 
             .replaceAll("</rarityLegendary>", "</font>")
             .replaceAll("<rarityMythic>", "<font color='#FF4500'>")
             .replaceAll("</rarityMythic>", "</font>");

        // 2. Tag Replacements (Official Riot Tags -> Icons)
        s = s.replaceAll("<attackDamage>", "<img src='ic_stat_ad'/> <font color='#FF8C00'>")
            .replaceAll("</attackDamage>", "</font> ") 
            .replaceAll("<abilityPower>", "<img src='ic_stat_ap'/> <font color='#0099CC'>")
            .replaceAll("</abilityPower>", "</font> ")
            .replaceAll("<armor>", "<img src='ic_stat_armor'/> <font color='#FFFF00'>")
            .replaceAll("</armor>", "</font> ")
            .replaceAll("<spellBlock>", "<img src='ic_stat_mr'/> <font color='#FF00FF'>")
            .replaceAll("</spellBlock>", "</font> ")
            .replaceAll("<magicResistance>", "<img src='ic_stat_mr'/> <font color='#FF00FF'>")
            .replaceAll("</magicResistance>", "</font> ")
            .replaceAll("<attackSpeed>", "<img src='ic_stat_attack_speed'/> <font color='#FFFF00'>")
            .replaceAll("</attackSpeed>", "</font> ")
            .replaceAll("<abilityHaste>", "<img src='ic_stat_haste'/> <font color='#FFFFFF'>")
            .replaceAll("</abilityHaste>", "</font> ")
            .replaceAll("<moveSpeed>", "<img src='ic_stat_move_speed'/> <font color='#FFFFFF'>")
            .replaceAll("</moveSpeed>", "</font> ")
            .replaceAll("<mana>", "<img src='ic_stat_mana'/> <font color='#0099CC'>")
            .replaceAll("</mana>", "</font> ")
            .replaceAll("<health>", "<img src='ic_stat_health'/> <font color='#4B8B3B'>")
            .replaceAll("</health>", "</font> ")
            .replaceAll("<crit>", "<img src='ic_stat_crit'/> <font color='#FF4500'>")
            .replaceAll("</crit>", "</font> ")
            .replaceAll("<lifeSteal>", "<img src='ic_stat_lifesteal'/> <font color='#FF4500'>")
            .replaceAll("</lifeSteal>", "</font> ")
            .replaceAll("<physicalDamage>", "<img src='ic_stat_ad'/> <font color='#FF8C00'>")
            .replaceAll("</physicalDamage>", "</font>")
            .replaceAll("<magicDamage>", "<img src='ic_stat_ap'/> <font color='#0099CC'>")
            .replaceAll("</magicDamage>", "</font>")
            .replaceAll("<trueDamage>", "<font color='#FFFFFF'>") 
            .replaceAll("</trueDamage>", "</font>")
            .replaceAll("<healing>", "<img src='ic_stat_health'/> <font color='#00FF00'>")
            .replaceAll("</healing>", "</font>")
            .replaceAll("<shield>", "<img src='ic_stat_armor'/> <font color='#FFD700'>")
            .replaceAll("</shield>", "</font>")
            .replaceAll("<scaleHealth>", "<img src='ic_stat_health'/> <font color='#4B8B3B'>")
            .replaceAll("</scaleHealth>", "</font>")
            .replaceAll("<scaleMana>", "<img src='ic_stat_mana'/> <font color='#0099CC'>")
            .replaceAll("</scaleMana>", "</font>")
            .replaceAll("<scaleAD>", "<img src='ic_stat_ad'/> <font color='#FF8C00'>")
            .replaceAll("</scaleAD>", "</font>")
            .replaceAll("<scaleAP>", "<img src='ic_stat_ap'/> <font color='#0099CC'>")
            .replaceAll("</scaleAP>", "</font>")
            .replaceAll("<scaleArmor>", "<img src='ic_stat_armor'/> <font color='#FFFF00'>")
            .replaceAll("</scaleArmor>", "</font>")
            .replaceAll("<scaleMR>", "<img src='ic_stat_mr'/> <font color='#FF00FF'>")
            .replaceAll("</scaleMR>", "</font>")
            .replaceAll("<speed>", "<img src='ic_stat_move_speed'/> <font color='#FFFF00'>")
            .replaceAll("</speed>", "</font>");

        // 3. Line-by-Line Processing safely to avoid messing up BR tags
        String[] lines = s.split("<br>");
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            
            // Complex Stats
            line = replaceStat(line, "régén(\\.|ération)?( de base)? des pv", "ic_stat_hp_regen");
            line = replaceStat(line, "régén(\\.|ération)?( de base)? du mana", "ic_stat_mana_regen");
            line = replaceStat(line, "pénétration d'armure", "ic_stat_armor_pen");
            line = replaceStat(line, "pénétration magique", "ic_stat_magic_pen");
            line = replaceStat(line, "efficacité de vos soins et boucliers", "ic_stat_heal_shield");
            line = replaceStat(line, "puissance des soins et boucliers", "ic_stat_heal_shield");
            line = replaceStat(line, "vol de vie", "ic_stat_lifesteal");
            line = replaceStat(line, "omnivampirisme", "ic_stat_vamp");
            line = replaceStat(line, "dégâts (des|de) coups? critiques?", "ic_stat_crit_damage");
            line = replaceStat(line, "chances de coup critique", "ic_stat_crit");
            line = replaceStat(line, "accélération de compétence", "ic_stat_haste");
            line = replaceStat(line, "vitesse de déplacement", "ic_stat_move_speed");
            line = replaceStat(line, "vitesse d'attaque", "ic_stat_attack_speed");
            line = replaceStat(line, "létalité", "ic_stat_armor_pen");
            line = replaceStat(line, "tenacity|ténacité", "ic_stat_tenacity");

            // Simple Stats
            line = replaceStat(line, "dégâts d'attaque", "ic_stat_ad");
            line = replaceStat(line, "puissance", "ic_stat_ap");
            line = replaceStat(line, "(?<!d')armure", "ic_stat_armor"); 
            line = replaceStat(line, "résistance magique", "ic_stat_mr");
            
            // PV / Mana / Gold
            line = replaceStat(line, "(?<!des )\\bPV\\b", "ic_stat_health");
            line = replaceStat(line, "points de vie", "ic_stat_health");
            line = replaceStat(line, "(?<!du )\\bmana\\b", "ic_stat_mana");
            line = replaceStat(line, "\\bPO\\b", "ic_stat_gold");
            
            sb.append(line);
            if (i < lines.length - 1) {
                sb.append("<br>");
            }
        }
             
        return sb.toString();
    }

    /**
     * Helper to replace stats with icons intelligently WITHIN A SINGLE LINE.
     * 1. Detects if an icon is already present (Group 1 Or Group 4).
     * 2. Captures preceding number/unit (Group 2).
     * 3. Reconstructs as [Icon] [Number] [Stat].
     */
    private String replaceStat(String text, String statRegex, String iconName) {
        // Regex Breakdown (Simplified for single line):
        // G1 (Existing Pre-Icon): (?:(<img[^>]+>)(?:\\s*<[^>]+>)*\\s*)?
        // G2 (Number/Unit): Looks for standard pattern "Tags Number Tags Unit", BUT specifically excludes <img...> tags 
        //                   to ensure they are caught by G1 (Pre-Icon) instead of being consumed here.
        // G3 (Stat Name): (STAT_REGEX)
        // G4 (Existing Post-Icon): (?:\s*(<img[^>]+>))?
        
        String preIcon = "(?:(<img[^>]+>)(?:\\s*<[^>]+>)*\\s*)?";
        // Change: Added d' and des to regex to capture preposition properly
        String numberPart = "((?:<(?!img\\b)[^>]+>|\\s)*[-+]?\\s*\\d+(?:(?:%|\\s*%+)|(?:[.,]\\d+))?(?:<(?!img\\b)[^>]+>|\\s)*(?:d'|des\\s+|de\\s+)?)?";
        String postIcon = "(?:\\s*(<img[^>]+>))?";
        
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("(?i)" + preIcon + numberPart + "(" + statRegex + ")" + postIcon);
        java.util.regex.Matcher m = p.matcher(text);
        
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            boolean hasPreIcon = m.group(1) != null;
            boolean hasPostIcon = m.group(4) != null;
            
            String number = m.group(2) != null ? m.group(2) : "";
            String stat = m.group(3);
            
            // Always ensure the icon is at the start (Left aligned)
            String iconTag = "<img src='" + iconName + "'/> ";
            
            if (hasPreIcon) {
                // If icon exists before, verify duplication logic.
                // Simple logic: we are rewriting the block. If we found an icon, we can just rewrite it properly.
                // But to fix "Wrong Icon", we enforce OUR icon.
                // Rebuild: Icon + Number + Stat
                m.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(iconTag + number + stat));
            } else if (hasPostIcon) {
                // Found icon AFTER. Move to FRONT.
                m.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(iconTag + number + stat));
            } else {
                // No icon. Insert it.
                m.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(iconTag + number + stat));
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }


    // Custom Span for vertical alignment
    private static class CenteredImageSpan extends android.text.style.ImageSpan {
        public CenteredImageSpan(android.content.Context context, int drawableRes) {
            super(context, drawableRes);
        }

        public CenteredImageSpan(android.graphics.drawable.Drawable d) {
            super(d);
        }
        
        @Override
        public void draw(@androidx.annotation.NonNull android.graphics.Canvas canvas, CharSequence text,
                         int start, int end, float x, 
                         int top, int y, int bottom, @androidx.annotation.NonNull android.graphics.Paint paint) {
            android.graphics.drawable.Drawable b = getDrawable();
            android.graphics.Paint.FontMetricsInt fm = paint.getFontMetricsInt();
            int transY = (y + fm.descent + y + fm.ascent) / 2 - b.getBounds().bottom / 2;
            
            canvas.save();
            canvas.translate(x, transY);
            b.draw(canvas);
            canvas.restore();
        }
    }

    @Override
    public void onItemClick(Item item) {
        showItemDetail(item);
    }

    private void showItemDetail(Item item) {
        if (isFinishing()) return;

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_item_detail, null);
        builder.setView(dialogView);

        android.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        // Bind Views
        ImageView ivIcon = dialogView.findViewById(R.id.ivDetailIcon);
        TextView tvName = dialogView.findViewById(R.id.tvDetailName);
        TextView tvSubtitle = dialogView.findViewById(R.id.tvDetailSubtitle); // We might construct this from tags
        TextView tvCost = dialogView.findViewById(R.id.tvDetailCost);
        TextView tvDescription = dialogView.findViewById(R.id.tvDetailDescription);

        // Set Data
        tvName.setText(item.getName() != null ? item.getName() : "Unknown");

        // Cost
        if (item.getGold() != null) {
            // Add Gold Icon to Price
            // Use Html.fromHtml with image getter for the gold icon
            String goldHtml = String.valueOf(item.getGold().getTotal()) + " <img src='ic_stat_gold'/>";
            tvCost.setText(android.text.Html.fromHtml(goldHtml, android.text.Html.FROM_HTML_MODE_LEGACY, source -> {
                int resourceId = getResources().getIdentifier(source, "drawable", getPackageName());
                if (resourceId != 0) {
                    android.graphics.drawable.Drawable d = androidx.core.content.ContextCompat.getDrawable(this, resourceId);
                    if (d != null) {
                        d.setBounds(0, 0, 40, 40); // Slightly larger for UI
                        return d;
                    }
                }
                return null;
            }, null));
        } else {
            tvCost.setVisibility(View.GONE);
        }

        // Subtitle (Tags)
        if (item.getTags() != null && !item.getTags().isEmpty()) {
            StringBuilder tagsBuilder = new StringBuilder();
            for (int i = 0; i < item.getTags().size(); i++) {
                String translatedTag = formatTag(item.getTags().get(i));
                tagsBuilder.append(translatedTag);
                if (i < item.getTags().size() - 1) {
                    tagsBuilder.append(", ");
                }
            }
            tvSubtitle.setText(tagsBuilder.toString());
        } else {
            tvSubtitle.setVisibility(View.GONE);
        }

        // Image
        if (item.getImage() != null) {
            String imageUrl = "https://ddragon.leagueoflegends.com/cdn/" + currentVersion + "/img/item/" + item.getImage().getFull();
            com.bumptech.glide.Glide.with(this)
                .load(imageUrl)
                .placeholder(R.color.lol_blue_light)
                .into(ivIcon);
        }

        // Description
        if (item.getDescription() != null) {
            String formattedDescription = formatDescription(item.getDescription());
            
            android.text.Html.ImageGetter imageGetter = source -> {
                int resourceId = getResources().getIdentifier(source, "drawable", getPackageName());
                if (resourceId != 0) {
                    android.graphics.drawable.Drawable drawable = androidx.core.content.ContextCompat.getDrawable(ItemsActivity.this, resourceId);
                    if (drawable != null) {
                        // Scale image to match text size roughly (e.g. 1.0x line height or fixed size)
                        int size = (int) (tvDescription.getTextSize() * 1.1);
                        drawable.setBounds(0, 0, size, size);
                        return drawable;
                    }
                }
                return null;
            };

            android.text.Spanned spanned;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                spanned = android.text.Html.fromHtml(formattedDescription, android.text.Html.FROM_HTML_MODE_LEGACY, imageGetter, null);
            } else {
                spanned = android.text.Html.fromHtml(formattedDescription, imageGetter, null);
            }
            
            // Replace ImageSpans with CenteredImageSpans
            if (spanned instanceof android.text.SpannableStringBuilder) {
                android.text.SpannableStringBuilder ssb = (android.text.SpannableStringBuilder) spanned;
                android.text.style.ImageSpan[] imageSpans = ssb.getSpans(0, ssb.length(), android.text.style.ImageSpan.class);
                for (android.text.style.ImageSpan span : imageSpans) {
                    int start = ssb.getSpanStart(span);
                    int end = ssb.getSpanEnd(span);
                    int flags = ssb.getSpanFlags(span);
                    ssb.removeSpan(span);
                    ssb.setSpan(new CenteredImageSpan(span.getDrawable()), start, end, flags);
                }
                tvDescription.setText(ssb);
            } else {
                tvDescription.setText(spanned);
            }

        } else {
            tvDescription.setVisibility(View.GONE);
        }

        dialog.show();
    }
}
