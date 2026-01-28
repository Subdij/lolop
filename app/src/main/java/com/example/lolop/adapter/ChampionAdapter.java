package com.example.lolop.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.lolop.database.FavoriteDatabase;
import com.example.lolop.databinding.ItemChampionBinding;
import com.example.lolop.model.Champion;
import com.example.lolop.utils.ChampionMapper;
import java.util.ArrayList;
import java.util.List;

public class ChampionAdapter extends RecyclerView.Adapter<ChampionAdapter.ViewHolder> {
    private List<Champion> champions = new ArrayList<>();
    private List<Champion> championsFull = new ArrayList<>();
    private String version;
    private java.util.Set<String> favoriteIds = new java.util.HashSet<>();
    private String currentSearchText = "";
    private String currentRoleFilter = "All";
    private OnChampionClickListener listener;

    public interface OnChampionClickListener {
        void onChampionClick(Champion champion);
    }

    /**
     * Définit l'écouteur pour les clics sur les items de champion.
     */
    public void setOnChampionClickListener(OnChampionClickListener listener) {
        this.listener = listener;
    }

    public ChampionAdapter(String version) {
        this.version = version;
    }

    /**
     * Met à jour la version de l'API utilisée pour charger les images des
     * champions.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Met à jour la liste des identifiants de champions favoris et rafraîchit
     * l'affichage.
     */
    public void setFavorites(java.util.Set<String> favoriteIds) {
        this.favoriteIds = favoriteIds;
        notifyDataSetChanged();
    }

    /**
     * Définit la liste complète des champions et applique les filtres actuels.
     */
    public void setChampions(List<Champion> champions) {
        this.championsFull = new ArrayList<>(champions);
        applyFilters();
    }

    /**
     * Filtre la liste des champions par nom (recherche textuelle).
     */
    public void filter(String text) {
        this.currentSearchText = text.toLowerCase();
        applyFilters();
    }

    /**
     * Filtre la liste des champions par rôle (Top, Jungle, Mid, Bot, Support, ou
     * Favoris).
     */
    public void setRoleFilter(String role) {
        this.currentRoleFilter = role;
        applyFilters();
    }

    /**
     * Applique les filtres de recherche et de rôle sur la liste complète et met à
     * jour l'affichage.
     */
    private void applyFilters() {
        List<Champion> filteredList = new ArrayList<>();
        for (Champion item : championsFull) {
            boolean matchesSearch = item.getName().toLowerCase().contains(currentSearchText);

            boolean matchesRole = false;
            if (currentRoleFilter.equals("All")) {
                matchesRole = true;
            } else if (currentRoleFilter.equals("Favorites")) {
                matchesRole = favoriteIds != null && favoriteIds.contains(item.getId());
            } else {
                matchesRole = ChampionMapper.isChampionInRole(item.getId(), currentRoleFilter);
            }

            if (matchesSearch && matchesRole) {
                filteredList.add(item);
            }
        }
        updateList(filteredList);
    }

    /**
     * Met à jour la liste affichée en utilisant DiffUtil pour calculer les
     * différences et animer les changements.
     */
    private void updateList(List<Champion> newList) {
        androidx.recyclerview.widget.DiffUtil.DiffResult diffResult = androidx.recyclerview.widget.DiffUtil
                .calculateDiff(new androidx.recyclerview.widget.DiffUtil.Callback() {
                    @Override
                    public int getOldListSize() {
                        return champions.size();
                    }

                    @Override
                    public int getNewListSize() {
                        return newList.size();
                    }

                    @Override
                    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                        return champions.get(oldItemPosition).getId().equals(newList.get(newItemPosition).getId());
                    }

                    @Override
                    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                        Champion oldItem = champions.get(oldItemPosition);
                        Champion newItem = newList.get(newItemPosition);
                        return oldItem.getName().equals(newItem.getName()) &&
                                oldItem.getImage().getFull().equals(newItem.getImage().getFull());
                    }
                });

        this.champions = new ArrayList<>(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    /**
     * Crée le ViewHolder pour un item de champion.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChampionBinding binding = ItemChampionBinding.inflate(LayoutInflater.from(parent.getContext()), parent,
                false);
        return new ViewHolder(binding);
    }

    /**
     * Lie les données du champion à la vue.
     * Affiche le nom, charge l'image, et gère l'état du favori.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Champion champion = champions.get(position);
        holder.binding.tvChampionNameDisplay.setText(champion.getName());

        String iconUrl = "https://ddragon.leagueoflegends.com/cdn/" + version + "/img/champion/"
                + champion.getImage().getFull();

        if (com.example.lolop.utils.PowerSavingManager.getInstance().isPowerSavingMode()) {
            Glide.with(holder.itemView.getContext())
                    .load(iconUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .format(com.bumptech.glide.load.DecodeFormat.PREFER_RGB_565)
                    .dontAnimate()
                    .into(holder.binding.ivChampionIcon);
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(iconUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.binding.ivChampionIcon);
        }

        if (favoriteIds != null && favoriteIds.contains(champion.getId())) {
            holder.binding.ivFavoriteStar.setVisibility(View.VISIBLE);
        } else {
            holder.binding.ivFavoriteStar.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChampionClick(champion);
            }
        });
    }

    @Override
    public int getItemCount() {
        return champions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ItemChampionBinding binding;

        public ViewHolder(ItemChampionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}