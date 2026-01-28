package com.example.lolop.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.lolop.ItemsActivity;
import com.example.lolop.MainActivity;
import com.example.lolop.PatchNoteActivity;
import com.example.lolop.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavbarFragment extends Fragment {

    private String currentVersion = "14.5.1"; // Default
    private BottomNavigationView bottomNavigation;

    public void setCurrentVersion(String version) {
        this.currentVersion = version;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_navbar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bottomNavigation = view.findViewById(R.id.bottomNavigation);
        setupNavigation();
        
        if (getActivity() != null) {
            updateSelectedTab();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSelectedTab();
    }

    private void updateSelectedTab() {
        if (bottomNavigation == null || getActivity() == null) return;

        if (getActivity() instanceof MainActivity) {
            bottomNavigation.getMenu().findItem(R.id.nav_champions).setChecked(true);
        } else if (getActivity() instanceof ItemsActivity) {
            bottomNavigation.getMenu().findItem(R.id.nav_items).setChecked(true);
        } else if (getActivity() instanceof PatchNoteActivity) {
            bottomNavigation.getMenu().findItem(R.id.nav_patch).setChecked(true);
        }
    }

    private void setupNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            if (getActivity() == null) return false;

            int itemId = item.getItemId();
            
            if (getActivity() instanceof MainActivity && itemId == R.id.nav_champions) return true;
            if (getActivity() instanceof ItemsActivity && itemId == R.id.nav_items) return true;
            if (getActivity() instanceof PatchNoteActivity && itemId == R.id.nav_patch) return true;

            Intent intent = null;
            if (itemId == R.id.nav_champions) {
                intent = new Intent(getContext(), MainActivity.class);
            } else if (itemId == R.id.nav_items) {
                intent = new Intent(getContext(), ItemsActivity.class);
            } else if (itemId == R.id.nav_patch) {
                intent = new Intent(getContext(), PatchNoteActivity.class);
            }

            if (intent != null) {
                intent.putExtra("CURRENT_VERSION", currentVersion);
                // Flag to bring existing activity to front if it exists, to match previous behavior partially
                // But generally Standard launch mode creates new.
                // We keep it simple as per original code.
                startActivity(intent);
                getActivity().overridePendingTransition(0, 0);

                // Finish current activity if it's not MainActivity (Standard navigation pattern + matches user request to avoid repeating/stacking too much)
                if (!(getActivity() instanceof MainActivity)) {
                    getActivity().finish();
                }
            }
            return true;
        });
    }
}
