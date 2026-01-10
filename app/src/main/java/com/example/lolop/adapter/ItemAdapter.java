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

    public ItemAdapter(Context context, List<String> listDataHeader, HashMap<String, List<Item>> listChildData, String currentVersion) {
        this.context = context;
        this.listDataHeader = listDataHeader;
        this.listDataChild = listChildData;
        this.currentVersion = currentVersion;
    }

    public void updateData(List<String> listDataHeader, HashMap<String, List<Item>> listChildData) {
        this.listDataHeader = listDataHeader;
        this.listDataChild = listChildData;
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return this.listDataHeader != null ? this.listDataHeader.size() : 0;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (listDataHeader == null || groupPosition >= listDataHeader.size()) return 0;
        
        String header = listDataHeader.get(groupPosition);
        
        // If "Tout", we only have 1 child (the grid container)
        if ("Tout".equals(header)) {
            return 1;
        }
        
        List<Item> items = this.listDataChild.get(header);
        return items != null ? items.size() : 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listDataHeader.get(groupPosition);
    }

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
    
    @Override
    public int getChildTypeCount() {
        return 2; // Normal Item and Grid Container
    }

    @Override
    public int getChildType(int groupPosition, int childPosition) {
        return "Tout".equals(listDataHeader.get(groupPosition)) ? 1 : 0;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.item_list_group, parent, false);
        }

        TextView lblListHeader = convertView.findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        int type = getChildType(groupPosition, childPosition);
        
        if (type == 1) { // Grid Layout for "Tout"
            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.item_grid_container, parent, false);
            }
            
            androidx.recyclerview.widget.RecyclerView rv = convertView.findViewById(R.id.rvItemGrid);
            // Setup Grid
            // Use AutoFit matches 5 columns
            rv.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(context, 5));
            
            List<Item> allItems = listDataChild.get("Tout");
            if (allItems != null) {
                GridItemAdapter gridAdapter = new GridItemAdapter(context, allItems, currentVersion);
                rv.setAdapter(gridAdapter);
            }
            
            return convertView;
            
        } else { // Normal List Layout
            final Item item = (Item) getChild(groupPosition, childPosition);
            
            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.item_list_item, parent, false);
            }
            
            if (item != null) {
                TextView txtListChild = convertView.findViewById(R.id.lblListItem);
                ImageView imgListChild = convertView.findViewById(R.id.ivItemIcon);
                
                txtListChild.setText(item.getName());
                
                if (item.getImage() != null) {
                    String imageUrl = "https://ddragon.leagueoflegends.com/cdn/" + currentVersion + "/img/item/" + item.getImage().getFull();
                    Glide.with(context)
                         .load(imageUrl)
                         .placeholder(R.color.lol_blue_light)
                         .into(imgListChild);
                }
            }

            return convertView;
        }
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
