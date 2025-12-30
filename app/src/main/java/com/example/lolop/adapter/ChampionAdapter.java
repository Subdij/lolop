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
    private final String version;
    private FavoriteDatabase db;
    private String currentSearchText = "";
    private String currentRoleFilter = "All";

    public ChampionAdapter(String version) {
        this.version = version;
    }

    public void setDatabase(FavoriteDatabase db) {
        this.db = db;
    }

    public void setChampions(List<Champion> champions) {
        this.champions = new ArrayList<>(champions);
        this.championsFull = new ArrayList<>(champions);
        applyFilters();
    }

    public void filter(String text) {
        this.currentSearchText = text.toLowerCase();
        applyFilters();
    }

    public void setRoleFilter(String role) {
        this.currentRoleFilter = role;
        applyFilters();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void applyFilters() {
        List<Champion> filteredList = new ArrayList<>();
        for (Champion item : championsFull) {
            boolean matchesSearch = item.getName().toLowerCase().contains(currentSearchText);
            
            // UTILISATION DU MAPPAGE MANUEL
            boolean matchesRole = currentRoleFilter.equals("All") || 
                                 ChampionMapper.isChampionInRole(item.getId(), currentRoleFilter);
            
            if (matchesSearch && matchesRole) {
                filteredList.add(item);
            }
        }
        this.champions = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChampionBinding binding = ItemChampionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Champion champion = champions.get(position);
        holder.binding.tvChampionNameDisplay.setText(champion.getName());

        String iconUrl = "https://ddragon.leagueoflegends.com/cdn/" + version + "/img/champion/" + champion.getImage().getFull();
        
        Glide.with(holder.itemView.getContext())
                .load(iconUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.binding.ivChampionIcon);

        if (db != null && db.isFavorite(champion.getId())) {
            holder.binding.ivFavoriteStar.setVisibility(View.VISIBLE);
        } else {
            holder.binding.ivFavoriteStar.setVisibility(View.GONE);
        }
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