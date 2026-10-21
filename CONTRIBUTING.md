# Contribuer à Tunetalk Android

## Conventions

- Respecter les couches `presentation -> domain` et `data -> domain`. Le Domain ne dépend pas d’Android, Retrofit, SignalR ou Compose.
- Mettre la validation réutilisable dans `domain`; garder les Composables déclaratifs et le ViewModel propriétaire de l’état d’écran.
- Nommer types, fonctions et commits en anglais ; les libellés statiques sont dans `res/values/strings.xml` en français.
- Ajouter un test unitaire pour toute règle métier modifiée et vérifier `lintDebug`.
- Ne jamais autoriser un flux HTTP dans Release et ne jamais inclure de secret dans Gradle ou le code source.

## Commits

Utiliser [Conventional Commits](https://www.conventionalcommits.org/) :

```text
feat(player): keep radio playback active in background
fix(chat): preserve messages received during history loading
docs(android): describe local API setup
test(auth): validate registration requirements
ci(android): publish beta APK artifacts
```

Types usuels : `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`.

## Vérifications avant push

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

Les pull requests déclenchent `Android CI`. Une GitHub Release publiée produit un APK Debug de test ; aucun secret de signature ou déploiement Play Store n’est stocké ici.
