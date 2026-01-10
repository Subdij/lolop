package com.example.lolop;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lolop.adapter.ItemAdapter;
import com.example.lolop.api.RetrofitClient;
import com.example.lolop.databinding.ActivityItemsBinding;
import com.example.lolop.model.Item;
import com.example.lolop.model.ItemResponse;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItemsActivity extends AppCompatActivity {

    private ActivityItemsBinding binding;
    private ItemAdapter itemAdapter;
    private final Map<String, String> englishItemNames = new HashMap<>();
    private static final Map<String, String> TAG_TRANSLATIONS = new HashMap<>();
    
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
        TAG_TRANSLATIONS.put("SpellVamp", "Sort Vampirique");
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

    private static final List<String> CATEGORY_ORDER = Arrays.asList(
        "Bottes",
        "Consommable",
        "Relique",
        "Dégâts Physiques",
        "Puissance",
        "PV",
        "Armure",
        "Résistance Magique",
        "Vitesse d'Attaque",
        "Coup Critique",
        "Accélération de Compétence",
        "Pénétration d'Armure",
        "Pénétration Magique",
        "Vol de Vie",
        "Omnivampirisme", 
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
        "Furtivité",
        "Revenus",
        "Vision",
        "Jungle",
        "Voie"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityItemsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupNavigation();
        setupSearch();
        fetchLatestVersion();
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
        if (query.isEmpty()) {
            itemAdapter.updateData(originalDataHeader, originalDataChild);
            for(int i=0; i < originalDataHeader.size(); i++) {
                binding.elvItems.collapseGroup(i);
            }
            return;
        }

        String normalizedQuery = stripAccents(query.toLowerCase());
        List<String> filteredHeaders = new ArrayList<>();
        HashMap<String, List<Item>> filteredChild = new HashMap<>();
        
        Map<String, Item> allMatchesMap = new HashMap<>();

        for (String header : originalDataHeader) {
            List<Item> originalItems = originalDataChild.get(header);
            List<Item> filteredItems = new ArrayList<>();
            
            // Check French category name (header)
            String normalizedHeader = stripAccents(header.toLowerCase());
            boolean headerMatches = normalizedHeader.contains(normalizedQuery);
            
            // Check English category name mappings
            if (!headerMatches) {
                for (Map.Entry<String, String> entry : TAG_TRANSLATIONS.entrySet()) {
                    // entry.getValue() == header (French)
                    // entry.getKey() == English Tag
                    if (entry.getValue().equals(header)) {
                         String normalizedEnglishTag = stripAccents(entry.getKey().toLowerCase());
                         if (normalizedEnglishTag.contains(normalizedQuery)) {
                             headerMatches = true;
                             break;
                         }
                    }
                }
            }

            if (originalItems != null) {
                if (headerMatches) {
                    filteredItems.addAll(originalItems);
                } else {
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

        itemAdapter.updateData(filteredHeaders, filteredChild);
        
        if (!filteredHeaders.isEmpty()) {
             binding.elvItems.expandGroup(0);
             for(int i=1; i < filteredHeaders.size(); i++) {
                 binding.elvItems.collapseGroup(i);
             }
        }
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
        }

        // Sort headers by custom order
        listDataHeader = new ArrayList<>(tempMap.keySet());
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
            // Replaced List.sort with Collections.sort for compatibility
            Collections.sort(items, (i1, i2) -> {
                String n1 = i1.getName();
                String n2 = i2.getName();
                if (n1 == null) return -1;
                if (n2 == null) return 1;
                return n1.compareToIgnoreCase(n2);
            });
            listDataChild.put(header, items);
        }

        // Save original for search
        originalDataHeader = new ArrayList<>(listDataHeader);
        originalDataChild = new HashMap<>(listDataChild);

        // Set adapter
        itemAdapter = new ItemAdapter(this, listDataHeader, listDataChild, currentVersion);
        binding.elvItems.setAdapter(itemAdapter);
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
