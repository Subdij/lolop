# 🏆 Lolop - Compagnon League of Legends

**Lolop** est une application Android native conçue pour aider les joueurs de League of Legends. Elle fournit des informations détaillées sur les champions, les objets et les notes de patch, tout en offrant des fonctionnalités pratiques comme la gestion des favoris et des mises à jour automatisées.

## 📱 Fonctionnalités Principales

*   **Wiki Champions** :
    *   Liste complète des champions avec recherche fluide et animée.
    *   Détails approfondis : Statistiques, Histoire (Lore), Conseils (Ally/Enemy Tips), Sorts et Passifs.
    *   Système de **Favoris** pour épingler et retrouver rapidement vos champions préférés.
*   **Encyclopédie des Objets** :
    *   Catalogue complet des items du jeu.
    *   Filtrage par catégories et recherche textuelle.
    *   Mise en cache locale pour un chargement instantané et une économie de données.
*   **Notes de Patch** :
    *   Consultation des dernières notes de mise à jour directement dans l'application (`PatchNoteActivity`).
*   **Fonctionnalités Système** :
    *   **Notifications** : Alertes pertinentes pour l'utilisateur.
    *   **Mode Hors-ligne (Partiel)** : Consultation des données mises en cache sans connexion.
    *   **Mises à jour en arrière-plan** : Utilisation de `WorkManager` pour garder les données à jour sans impacter l'expérience utilisateur.

## 🛠️ Stack Technique

L'application met en œuvre des concepts clés du développement Android :

*   **Langage** : Java
*   **Architecture** : MVVM (Model-View-ViewModel) avec Repository Pattern.
*   **Interface Utilisateur (UI)** :
    *   XML Layouts adaptatifs.
    *   `Fragments` pour la navigation (ex: `NavbarFragment`).
    *   Animations fluides pour la recherche et les transitions.
*   **Réseau & Données** :
    *   **Retrofit** : Consommation de l'API Riot Games / DataDragon.
    *   **Glide / Picasso** : Chargement et mise en cache des images.
    *   **JSON Parsing** : Gestion efficace des réponses API complexes.
*   **Persistance & Système** :
    *   **SQLite** : Base de données locale pour stocker les favoris (`FavoriteDatabase`).
    *   **SharedPreferences** : Sauvegarde des préférences utilisateur.
    *   **WorkManager** : Tâches de fond fiables (`PatchUpdateWorker`).
    *   **BroadcastReceiver** : Écoute des événements système (`ManaReceiver`).

## 🚀 Installation

1.  **Prérequis** :
    *   Android Studio Ladybug ou version récente.
    *   JDK 11 ou supérieur.
    *   Appareil ou émulateur sous Android 8.0 (Oreo) / API Level 26 minimum.

2.  **Configuration** :
    *   Clonez ce dépôt.
    *   Ouvrez le projet dans Android Studio.
    *   Laissez Gradle synchroniser les dépendances.

3.  **Exécution** :
    *   Appuyez sur `Run` (Shift+F10) pour installer l'application sur votre appareil.

## 🌍 Internationalisation

L'application est conçue pour être multilingue. Elle détecte la langue du système et adapte le contenu (noms des items, descriptions, lore) en conséquence (support actuel : Français, Anglais).
