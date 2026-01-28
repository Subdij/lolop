package com.example.lolop.utils;

import android.content.Context;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;

public class FileUtils {
    /**
     * Sauvegarde une chaîne de caractères dans un fichier local privé.
     */
    public static void saveStringToFile(Context context, String fileName, String content) {
        try (FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)) {
            fos.write(content.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lit le contenu d'un fichier local sous forme de chaîne de caractères.
     */
    public static String readStringFromFile(Context context, String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        try (FileInputStream fis = context.openFileInput(fileName);
                InputStreamReader inputStreamReader = new InputStreamReader(fis);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Vérifie si un fichier existe dans le stockage interne de l'application.
     */
    public static boolean fileExists(Context context, String fileName) {
        File file = context.getFileStreamPath(fileName);
        return file.exists();
    }
}
