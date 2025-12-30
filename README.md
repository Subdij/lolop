# Projet Développement Mobile - Lolop

## Idée de base
**En rapport avec League of Legends : Aide et Infos**

L'objectif est de fournir des informations sur les personnages et les items, ainsi que des astuces en fonction des "matchups". À terme, l'application pourrait se connecter au compte League of Legends pour fournir des astuces en temps réel.

## Fonctionnalités et Rapport de Version

Ce tableau récapitule les fonctionnalités demandées et leur état d'implémentation actuel (v0.1) versus l'objectif final (v1.0).

| Fonctionnalités | v0.1 (Actuel) | v1.0 (Cible) |
| :--- | :---: | :---: |
| **Intégrer au moins 2 activités** | ❌ (1 Activité) | ✅ |
| **Inclure le concept de « sharedPreferences »** | ❌ | ✅ |
| **Intégrer un menu dans la barre des status** | ❌ (Ressource présente) | ✅ |
| **Utiliser la notification** | ✅ | ✅ |
| **Intégrer des fonctionnalités réseau (HTTP/Retrofit)** | ✅ | ✅ |
| **Utiliser un composant du type « BroadcastReceiver »** | ✅ | ✅ |
| **Exploiter l’écran tactile** | ✅ | ✅ |
| **Inclure une Base de données embarquée « SQLite »** | ✅ (FavoriteDatabase) | ✅ |
| **Utiliser les ressources pour le texte, les couleurs, etc.** | ✅ | ✅ |
| **Internationaliser votre application** | ❌ | ✅ |
| **S’adapter à l’orientation de l’écran** | ❌ | ✅ |
| **Gérer la portabilité de votre écran** | ✅ | ✅ |
| **Utiliser un « Bundle » pour la sauvegarde d’état** | ✅ | ✅ |
| **Utiliser le concept de « Service »** | ❌ | ⏳ (Priorité basse) |
| **Inclure des fragments** | ❌ | ⏳ (Priorité basse) |
| **Utiliser la localisation** | ❌ | ⏳ (Priorité basse) |
| **Utiliser un ou plusieurs capteurs (microphone)** | ❌ | ⏳ (Priorité basse) |

## Détails Techniques

### Architecture
-   **Langage** : Java
-   **Architecture** : MVVM (partiel) avec Repository pattern pour l'API.

### Composants Clés
-   **MainActivity** : Point d'entrée principal, gère l'affichage de la grille et la logique principale.
-   **ChampionAdapter** : Gère l'affichage de la liste des champions (RecyclerView).
-   **RetrofitClient** : Client HTTP pour communiquer avec l'API Riot/DataDragon.
-   **FavoriteDatabase** : Base de données locale pour stocker les champions favoris.
-   **ManaReceiver** : BroadcastReceiver écoutant l'état de la batterie (`BATTERY_LOW`) pour envoyer une notification utilisateur.
