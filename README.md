# Tunetalk Android

Application Android Kotlin/Jetpack Compose de Tunetalk : stations de radio par genre, lecture en direct, authentification et chat de salon.

## Fonctionnalités du MVP

- Inscription et connexion par l’API ASP.NET Core ; session JWT locale avec expiration.
- Navigation dans les genres contrôlés par le serveur et les stations exposées par l’annuaire Radio Browser.
- Lecture directe des flux HTTPS avec Media3/MediaSession, y compris lorsque l’écran de la salle n’est plus visible.
- Chat de groupe SignalR avec historique et compteur de connexions actives.
- Thème sombre Tunetalk : fond nuit `#0B0C12`, violet `#9B7BFF`, menthe `#5FE1C0`, corail `#FF8F82`.

Radio Browser est un annuaire communautaire, pas un fournisseur licenciant les morceaux. L’application n’héberge ni ne relaie l’audio. Vérifier les droits de chaque diffuseur et les licences applicables avant une sortie publique ou commerciale. Les flux non HTTPS sont volontairement désactivés dans le lecteur.

## Lancer l’application

Prérequis : Android Studio avec le SDK stable Android 37.2 (API 37), Java 25 et l’API en cours d’exécution. Démarrer l’API en suivant [`../TunetalkApi.Api/README.md`](../TunetalkApi.Api/README.md), puis lancer l’application sur un émulateur Android :

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

Pour vérifier la connexion réelle entre l’émulateur et une API locale, démarrez l’API, gardez un émulateur Android ouvert, puis exécutez aussi :

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

Les tests instrumentés vérifient la lecture des genres par Retrofit, un `401` pour un compte de test inconnu, le décodage/stockage de la session d’inscription et l’ouverture de l’accueil après création du compte.

Sur Android 17 / API 37, Tunetalk demande l’autorisation **Réseau local** avant d’accéder à une URL privée (dont `10.0.2.2`). Autorisez-la pour utiliser l’API de développement ; une URL d’API publique ne déclenche pas cette demande.

L’URL par défaut est `http://10.0.2.2:5269/` (API sur l’hôte Windows, vue depuis l’émulateur). Pour une autre URL :

```powershell
$env:TUNETALK_API_BASE_URL = "https://api.example.com/"
.\gradlew.bat assembleDebug
```

La variante Debug autorise HTTP uniquement pour faciliter le développement local. Release n’autorise que le trafic TLS. La valeur `TUNETALK_API_BASE_URL` doit être configurée avec une adresse HTTPS pour une distribution externe.

## Architecture

Le code reste organisé par couches dans le module `app` :

- `domain` : modèles, contrats repository, cas d’usage et validation pure.
- `data` : clients Retrofit/SignalR, DataStore et implémentations des repositories.
- `presentation` : ViewModel et écrans Compose (authentification, catalogue, salle/chat).
- `di` : composition manuelle des dépendances.
- `playback` : service Media3/MediaSession pour l’audio en arrière-plan.

Les erreurs API et de connexion sont visibles dans l’interface ; aucun catalogue de secours fictif n’est présenté si l’annuaire tiers est indisponible.

## Qualité et livraison

- `Android CI` exécute lint, tests unitaires et assemble Debug sur les pull requests.
- `Android beta release` attache un APK Debug signée par la clé de développement à une GitHub Release publiée. C’est un artefact de test, pas un binaire Play Store.
- Configurez la variable de dépôt `TUNETALK_API_BASE_URL` avec l’URL HTTPS de l’API pour les artefacts de release.
- Le workflow `Dependabot` surveille Gradle et les GitHub Actions.

La signature de production et le déploiement Play Store nécessitent les clés, l’application Play Console et l’environnement de publication du propriétaire ; ils ne sont pas inclus dans le dépôt.
