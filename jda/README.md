# Projet

Build le projet nécessite [`Java 17`](https://adoptium.net/) minimum.<br>
Deux possibilités pour lancer le bot (à partir de la racine du projet) :
- Exécuter `./gradlew run` dans un terminal
- Installer [`Gradle`](https://gradle.org/install/) et exécuter `gradle run` dans un terminal

# Commandes

### • 🧍 Commandes utilisateur

| Categorie                                      | Commandes                            |
|------------------------------------------------|--------------------------------------|
| [Jeux](./src/main/kotlin/commands/games)       | `coinflip` `pendu` `roll`            |
| [Recherche](./src/main/kotlin/commands/search) | `twitch` `anime` `wikipedia` `meteo` |
| [Divers](./src/main/kotlin/commands/utility)   | `sondage` `pfp` `emoji`              |
| [Infos](./src/main/kotlin/commands/info)       | `serveur` `membre` `role`            |

### • 🔒 Commandes admin

| Categorie                                             | Commandes                              |
|-------------------------------------------------------|----------------------------------------|
| [Modération](./src/main/kotlin/commands/moderation)   | `clear` `kick` `ban` `unban`           |
| [Configuration](./src/main/kotlin/commands/Config.kt) | `logs` `bienvenue` `nouveau` `annonce` |
