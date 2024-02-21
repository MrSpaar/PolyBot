# Projet

Build le projet nécessite [`Java 17`](https://adoptium.net/) minimum.<br>
Deux possibilités pour lancer le bot (à partir de la racine du projet) :
- Exécuter `./gradlew run` dans un terminal
- Installer [`Gradle`](https://gradle.org/install/) et exécuter `gradle run` dans un terminal

# Commandes

### • 🧍 Commandes utilisateur

| Categorie                                      | Commandes                                            |
|------------------------------------------------|------------------------------------------------------|
| [Musique](./src/main/kotlin/commands/music)    | `play` `skip`                                        |
| [Jeux](./src/main/kotlin/commands/games)       | `coinflip` `pendu` `roll`                            |
| [Niveaux](./src/main/kotlin/commands/levels)   | `rang` `classement`                                  |
| [Recherche](./src/main/kotlin/commands/search) | `api twitch` `api anime` `api wikipedia` `api meteo` |
| [Divers](./src/main/kotlin/commands/utility)   | `sondage` `pfp` `emoji`                              |
| [Infos](./src/main/kotlin/commands/info)       | `info serveur` `info membre` `info role`             |

### • 🔒 Commandes admin

| Categorie                                             | Commandes                              |
|-------------------------------------------------------|----------------------------------------|
| [Menus](./src/main/kotlin/commands/menu)              | `menu liste` `menu boutons`            |
| [Modération](./src/main/kotlin/commands/moderation)   | `clear` `kick` `ban` `unban`           |
| [Configuration](./src/main/kotlin/commands/Config.kt) | `logs` `bienvenue` `nouveau` `annonce` |


# Modules supplémentaires

### • 📈 [Système d'expérience](./src/main/kotlin/events/levels)

Le système a la **même courbe d'xp que [Mee6](https://mee6.xyz/)**. <br>
Il est possible d'envoyer des messages de level up avec `/config`.
- `/rang` vous montrera votre niveau et votre progression vers le prochain niveau<br>
- `/classement` vous montrera le classement du serveur par pages de 10.

### • ⏲️ [Channels temporaires](./src/main/kotlin/events/channels)

Ce module permet d'avoir des channels vocaux temporaires :

- Chaque channel contenant [ce prefix](https://github.com/MrSpaar/Polybot/tree/master/jda/src/main/kotlin/events/CreateChannel.kt#L9) génèrera un channel tempaire dès que quelqu'un le rejoindra.
- Un channel écrit est généré et lié avec le channel temporaire.
- Les deux sont supprimés dès que le channel vocal est vide.

### • 📌 [Menu de rôles](./src/main/kotlin/events/Roles.kt)

Ce module permet de créer deux types de menu de rôles :
- Avec des boutons, l'utilisateur peut s'ajouter n'importe quel rôle parmi la liste
- Avec un menu déroulant, l'utilisateur ne peut choisir qu'un seul rôle parmi la liste

### • 📝 [Logs](./src/main/kotlin/events/logs)

Ce module permet d'envoyer automatiquement des messages de bienvenue.<br>
Utilisez `/config` pour choisir le salon et le message de bienvenue, chaque `<mention>` sera remplacé par le nouveau membre.

| Log                                    | Informations affichées                                  |
|----------------------------------------|---------------------------------------------------------|
| Nouveau membre                         | Mention                                                 |
| Départ d'un membre                     | Pseudo, ID et raison (ban, kick, ...)                   |
| Membre unban                           | Pseudo, par qui et raison                               |
| Changement de surnom                   | Ancien et nouveau surnom et par qui                     |
| Ajout/Suppression de rôles à un membre | Rôle ajouté ou enlevé, de qui et par qui                |
| Création/Suppression de rôles          | Rôle créé ou supprimé et par qui                        |
| Modification de rôles                  | Ce qui a été modifié et par qui                         |
| Création d'invitation                  | Lien, autheur, date d'expiration, nombre d'utilisations |