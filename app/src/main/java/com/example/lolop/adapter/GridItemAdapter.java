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

    public GridItemAdapter(Context context, List<Item> items, String version) {
        this.context = context;
        this.items = items;
        this.version = version;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_icon, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = items.get(position);
        
        // Load image
        if (item.getImage() != null) {
            String imageUrl = "https://ddragon.leagueoflegends.com/cdn/" + version + "/img/item/" + item.getImage().getFull();
            Glide.with(context)
                 .load(imageUrl)
                 .placeholder(R.color.lol_blue_light)
                 .into(holder.ivIcon);
        }
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
