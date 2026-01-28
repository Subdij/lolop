package com.example.lolop.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class FavoriteDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "lol_favs.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_FAVORITES = "favorites";
    private static final String COLUMN_ID = "id";

    public FavoriteDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Appelé lors de la création de la base de données.
     * Crée la table des favoris.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_FAVORITES + " (" + COLUMN_ID + " TEXT PRIMARY KEY)";
        db.execSQL(createTable);
    }

    /**
     * Appelé lors de la mise à jour de la version de la base de données.
     * Supprime l'ancienne table et la recrée.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        onCreate(db);
    }

    /**
     * Ajoute un champion aux favoris.
     */
    public void addFavorite(String championId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, championId);
        db.insert(TABLE_FAVORITES, null, values);
        db.close();
    }

    /**
     * Retire un champion des favoris.
     */
    public void removeFavorite(String championId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FAVORITES, COLUMN_ID + "=?", new String[] { championId });
        db.close();
    }

    /**
     * Vérifie si un champion est déjà dans les favoris.
     */
    public boolean isFavorite(String championId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVORITES, null, COLUMN_ID + "=?", new String[] { championId }, null, null,
                null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    /**
     * Récupère la liste de tous les IDs des champions favoris.
     */
    public java.util.HashSet<String> getAllFavorites() {
        java.util.HashSet<String> favorites = new java.util.HashSet<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVORITES, new String[] { COLUMN_ID }, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                favorites.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return favorites;
    }
}