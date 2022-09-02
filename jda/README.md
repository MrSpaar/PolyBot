# Projet

Build le projet nécessite [`Java 17`](https://adoptium.net/) minimum.<br>
Deux possibilités pour lancer le bot (à partir de la racine du projet) :
- Exécuter `./gradlew run` dans un terminal
- Installer [`Gradle`](https://gradle.org/install/) et exécuter `gradle run` dans un terminal

# Commandes

### • 🧍 Commandes utilisateur

| Categorie                                       | Commandes                            |
|-------------------------------------------------|--------------------------------------|
| [Jeux](./src/main/kotlin/commands/Games.kt)     | `coinflip` `pendu` `roll`            |
| [Divers](./src/main/kotlin/commands/Utility.kt) | `sondage` `pfp` `emoji`              |

### • 🔒 Commandes admin

| Categorie                                              | Commandes                              |
|--------------------------------------------------------|----------------------------------------|
| [Modération](./src/main/kotlin/commands/Moderation.kt) | `clear` `kick` `ban` `unban`           |
| [Configuration](./src/main/kotlin/commands/Config.kt)  | `logs` `bienvenue` `nouveau` `annonce` |
