package com.example.lolop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.lolop.R;
import com.example.lolop.model.Item;
import java.util.List;

public class GridItemAdapter extends RecyclerView.Adapter<GridItemAdapter.ViewHolder> {
    private Context context;
    private List<Item> items;
    private String version;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Item item);
    }

    public GridItemAdapter(Context context, List<Item> items, String version) {
        this.context = context;
        this.items = items;
        this.version = version;
        if (context instanceof OnItemClickListener) {
            this.listener = (OnItemClickListener) context;
        }
    }

    /**
     * Définit l'écouteur pour les clics sur les items de la grille.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * Crée la vue pour un objet dans la grille.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_icon, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Affiche l'icône de l'objet et configure le clic.
     * Charge l'image en tenant compte du mode économie d'énergie.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = items.get(position);

        // Load image
        if (item.getImage() != null) {
            String imageUrl = "https://ddragon.leagueoflegends.com/cdn/" + version + "/img/item/"
                    + item.getImage().getFull();
            if (com.example.lolop.utils.PowerSavingManager.getInstance().isPowerSavingMode()) {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.color.lol_blue_light)
                        .format(com.bumptech.glide.load.DecodeFormat.PREFER_RGB_565)
                        .dontAnimate()
                        .into(holder.ivIcon);
            } else {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.color.lol_blue_light)
                        .into(holder.ivIcon);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivItemIcon);
        }
    }
}
