package com.example.lolop.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.lolop.R;
import com.example.lolop.model.Item;
import java.util.HashMap;
import java.util.List;

public class ItemAdapter extends BaseExpandableListAdapter {

    private final Context context;
    private List<String> listDataHeader; // Header titles
    private HashMap<String, List<Item>> listDataChild; // Child data in format of header title, child title
    private final String currentVersion;

    public ItemAdapter(Context context, List<String> listDataHeader, HashMap<String, List<Item>> listChildData,
            String currentVersion) {
        this.context = context;
        this.listDataHeader = listDataHeader;
        this.listDataChild = listChildData;
        this.currentVersion = currentVersion;
    }

    /**
     * Met à jour les données de l'adaptateur et rafraîchit la liste.
     */
    public void updateData(List<String> listDataHeader, HashMap<String, List<Item>> listChildData) {
        this.listDataHeader = listDataHeader;
        this.listDataChild = listChildData;
        notifyDataSetChanged();
    }

    /**
     * Retourne le nombre de groupes (catégories) dans la liste.
     */
    @Override
    public int getGroupCount() {
        return this.listDataHeader != null ? this.listDataHeader.size() : 0;
    }

    /**
     * Retourne le nombre d'éléments dans un groupe donné.
     * Pour la catégorie "Tout", retourne 1 car elle contient une grille unique.
     */
    @Override
    public int getChildrenCount(int groupPosition) {
        if (listDataHeader == null || groupPosition >= listDataHeader.size())
            return 0;

        String header = listDataHeader.get(groupPosition);

        // If "All/Tout", we only have 1 child (the grid container)
        if (context.getString(R.string.category_all).equals(header)) {
            return 1;
        }

        List<Item> items = this.listDataChild.get(header);
        return items != null ? items.size() : 0;
    }

    /**
     * Retourne l'objet correspondant au groupe (le titre de la catégorie).
     */
    @Override
    public Object getGroup(int groupPosition) {
        return this.listDataHeader.get(groupPosition);
    }

    /**
     * Retourne l'objet enfant (Item) à une position donnée.
     */
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        String header = listDataHeader.get(groupPosition);
        List<Item> items = listDataChild.get(header);
        if (items != null && childPosition < items.size()) {
            return items.get(childPosition);
        }
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    /**
     * Retourne le nombre de types de vues hétérogènes.
     * 2 types : Item normal et Conteneur Grille (pour "Tout").
     */
    @Override
    public int getChildTypeCount() {
        return 2; // Normal Item and Grid Container
    }

    /**
     * Retourne le type de vue pour un enfant donné.
     * Type 1 pour "Tout" (Grille), Type 0 pour les autres (Liste).
     */
    @Override
    public int getChildType(int groupPosition, int childPosition) {
        return context.getString(R.string.category_all).equals(listDataHeader.get(groupPosition)) ? 1 : 0;
    }

    /**
     * Crée la vue pour l'en-tête de groupe (Catégorie).
     */
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.item_list_group, parent, false);
        }

        TextView lblListHeader = convertView.findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        return convertView;
    }

    /**
     * Crée la vue pour un enfant.
     * Gère soit l'affichage d'un item individuel, soit l'affichage de la grille
     * pour la catégorie "Tout".
     */
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
            ViewGroup parent) {
        int type = getChildType(groupPosition, childPosition);

        if (type == 1) { // Grid Layout for "Tout"
            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this.context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.item_grid_container, parent, false);
            }

            androidx.recyclerview.widget.RecyclerView rv = convertView.findViewById(R.id.rvItemGrid);
            // Setup Grid
            // Use AutoFit matches 5 columns
            rv.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(context, 5));

            List<Item> allItems = listDataChild.get(context.getString(R.string.category_all));
            if (allItems != null) {
                GridItemAdapter gridAdapter = new GridItemAdapter(context, allItems, currentVersion);
                rv.setAdapter(gridAdapter);
            }

            return convertView;

        } else { // Normal List Layout
            final Item item = (Item) getChild(groupPosition, childPosition);

            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this.context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.item_list_item, parent, false);
            }

            if (item != null) {
                TextView txtListChild = convertView.findViewById(R.id.lblListItem);
                ImageView imgListChild = convertView.findViewById(R.id.ivItemIcon);

                txtListChild.setText(item.getName());

                if (item.getImage() != null) {
                    String imageUrl = "https://ddragon.leagueoflegends.com/cdn/" + currentVersion + "/img/item/"
                            + item.getImage().getFull();
                    Glide.with(context)
                            .load(imageUrl)
                            .placeholder(R.color.lol_blue_light)
                            .into(imgListChild);
                }
            }

            return convertView;
        }
    }

    /**
     * Indique si l'enfant est sélectionnable.
     */
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
