package com.example.lolop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.lolop.R;
import com.example.lolop.model.Item;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_TOUT = 0;
    private static final int VIEW_TYPE_CARD = 1;

    private final Context context;
    private List<String> categories;
    private HashMap<String, List<Item>> categoryItems;
    private final String currentVersion;
    private final OnCategoryClickListener listener;
    private final Map<String, Item> representativeItems; // Stable images
    private final Map<String, Integer> categoryDrawableImages;

    public interface OnCategoryClickListener {
        void onCategoryClick(String category);
    }

    public CategoryAdapter(Context context, List<String> categories, HashMap<String, List<Item>> categoryItems,
            String currentVersion, Map<String, Item> representativeItems, Map<String, Integer> categoryDrawableImages,
            OnCategoryClickListener listener) {
        this.context = context;
        this.categories = categories;
        this.categoryItems = categoryItems;
        this.currentVersion = currentVersion;
        this.representativeItems = representativeItems;
        this.categoryDrawableImages = categoryDrawableImages;
        this.listener = listener;
    }

    public void updateData(List<String> categories, HashMap<String, List<Item>> categoryItems) {
        this.categories = categories;
        this.categoryItems = categoryItems;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (context.getString(R.string.category_all).equals(categories.get(position))) {
            return VIEW_TYPE_TOUT;
        }
        return VIEW_TYPE_CARD;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_TOUT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_tout_section, parent, false);
            return new ToutViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_category_card, parent, false);
            return new CategoryViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        String category = categories.get(position);

        if (holder instanceof ToutViewHolder) {
            ((ToutViewHolder) holder).bind(category);
        } else if (holder instanceof CategoryViewHolder) {
            ((CategoryViewHolder) holder).bind(category);
        }
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    // ViewHolder for "Tout" - Shows as a full section with grid
    class ToutViewHolder extends RecyclerView.ViewHolder {
        RecyclerView rvGrid;
        TextView tvHeader;

        ToutViewHolder(@NonNull View itemView) {
            super(itemView);
            rvGrid = itemView.findViewById(R.id.rvToutGrid);
            tvHeader = itemView.findViewById(R.id.lblListHeader);

            // Inner grid setup (5 columns to match requirement)
            rvGrid.setLayoutManager(new GridLayoutManager(context, 5));
        }

        void bind(String category) {
            tvHeader.setText(category);

            List<Item> items = categoryItems.get(category);
            if (items != null) {
                GridItemAdapter adapter = new GridItemAdapter(context, items, currentVersion);
                rvGrid.setAdapter(adapter);
            } else {
                rvGrid.setAdapter(null);
            }
        }
    }

    // ViewHolder for Categories - Shows as a clickable card
    class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvName = itemView.findViewById(R.id.tvCategoryName);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onCategoryClick(categories.get(getAdapterPosition()));
                }
            });
        }

        void bind(String category) {
            tvName.setText(category);

            // Check if there's a drawable resource for this category
            if (categoryDrawableImages != null && categoryDrawableImages.containsKey(category)) {
                ivIcon.setImageResource(categoryDrawableImages.get(category));
            } else {
                // Fallback to loading item image from API
                // Use stable representative item if available, otherwise fallback to current
                // list
                Item representativeItem = representativeItems.get(category);

                // Fallback logic if map doesn't have it (should be covered by ItemsActivity
                // logic)
                if (representativeItem == null) {
                    List<Item> items = categoryItems.get(category);
                    if (items != null && !items.isEmpty()) {
                        representativeItem = items.get(0);
                    }
                }

                if (representativeItem != null && representativeItem.getImage() != null) {
                    String imageUrl = "https://ddragon.leagueoflegends.com/cdn/" + currentVersion + "/img/item/"
                            + representativeItem.getImage().getFull();

                    if (com.example.lolop.utils.PowerSavingManager.getInstance().isPowerSavingMode()) {
                        Glide.with(context)
                                .load(imageUrl)
                                .placeholder(R.color.lol_blue_light)
                                .format(com.bumptech.glide.load.DecodeFormat.PREFER_RGB_565)
                                .dontAnimate()
                                .into(ivIcon);
                    } else {
                        Glide.with(context)
                                .load(imageUrl)
                                .placeholder(R.color.lol_blue_light)
                                .into(ivIcon);
                    }
                } else {
                    ivIcon.setImageResource(R.mipmap.ic_launcher);
                }
            }
        }
    }
}
