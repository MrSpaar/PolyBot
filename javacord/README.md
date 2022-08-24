# Projet

Build le projet nécessite [`Java 11`](https://adoptium.net/) minimum.<br>
Deux possibilités pour lancer le bot (à partir de la racine du projet) :
- Exécuter `./gradlew run` dans un terminal
- Installer [`Gradle`](https://gradle.org/install/) et exécuter `gradle run` dans un terminal

# Commandes

### • 🧍 Commandes utilisateur

| Categorie                                                                                          | Commandes                            |
|----------------------------------------------------------------------------------------------------|--------------------------------------|
| [Musique](https://github.com/MrSpaar/Polybot/tree/master/javacord/src/main/java/audio)             | `play` `skip`                        |
| [Jeux](https://github.com/MrSpaar/Polybot/tree/master/javacord/src/main/java/commands/games)       | `coinflip` `pendu` `roll`            |
| [Niveaux](https://github.com/MrSpaar/Polybot/tree/master/javacord/src/main/java/commands/xp)       | `rang` `classement`                  |
| [Recherche](https://github.com/MrSpaar/Polybot/tree/master/javacord/src/main/java/commands/search) | `twitch` `anime` `wikipedia` `meteo` |
| [Infos](https://github.com/MrSpaar/Polybot/tree/master/javacord/src/main/java/commands/infos)      | `serveur` `membre` `role`            |
| [Divers](https://github.com/MrSpaar/Polybot/tree/master/javacord/src/main/java/commands/misc)      | `sondage` `pfp` `emoji`              |

### • 🔒 Commandes admin

| Categorie                                                                                               | Commandes                              |
|---------------------------------------------------------------------------------------------------------|----------------------------------------|
| [Modération](https://github.com/MrSpaar/Polybot/tree/master/javacord/src/main/java/commands/moderation) | `clear` `kick` `ban` `unban`           |
| [Configuration](https://github.com/MrSpaar/Polybot/tree/master/javacord/src/main/java/commands/setup)   | `logs` `bienvenue` `nouveau` `annonce` |

# Modules supplémentaires

### • 📈 [Système d'expérience](https://github.com/MrSpaar/Polybot/tree/master/javacord/src/main/java/events/xp)

Le système a la **même courbe d'xp que [Mee6](https://mee6.xyz/)**. <br>
Ecrivez `/config annonce #channel` pour définir le salon où seront envoyées les annonces de level up.<br>
`/rang` vous montrera votre niveau et votre progression vers le prochain niveau<br>
`/classement` vous montrera le classement du serveur par pages de 10.

### • ⏲️ [Channels temporaires](https://github.com/MrSpaar/Polybot/tree/master/javacord/src/main/java/events/tempchannel)

Ce module permet d'avoir des channels vocaux temporaires :

- Chaque channel contenant [ce prefix](https://github.com/MrSpaar/Polybot/tree/master/javacord/src/main/java/events/tempchannel#L13) génèrera un channel tempaire dès que quelqu'un le rejoindra.
- Un channel écrit est généré et lié avec le channel temporaire.
- Les deux sont supprimés dès que le channel vocal est vide.

###  • 📥 [Accueil des nouveaux membres](https://github.com/MrSpaar/Polybot/tree/master/javacord/src/main/java/events/logs/MemberJoinEvent)

Ce module permet de personnaliser un peu plus l'arrivée de nouveaux membres sur votre serveur :
- `/config bienvenue #channel message` pour définir ce message,  `<mention>` sera remplacé par le nouveau membre en question.
- `/config nouveau @role` pour définir le rôle qui sera ajouté automatiquement à chaque nouveau membre.

### • 📝 [Logs](https://github.com/MrSpaar/Polybot/tree/javacord/master/src/java/events/logs)

Ecrivez `/config logs #channel` pour définir le channel contenant les logs.

| Log                        | Informations affichées                                  |
|----------------------------|---------------------------------------------------------|
| Nouveau membre             | Mention                                                 |
| Départ d'un membre         | Pseudo, ID et raison (ban, kick, ...)                   |
| Membre unban               | Pseudo, par qui et raison                               |
| Changement de surnom       | Ancien et nouveau surnom et par qui                     |
| Ajout/Suppression de rôles | Rôle ajouté ou enlevé, de qui et par qui                |
| Message supprimé           | Contenu du message, images, auteur et salon             |
| Création d'invitation      | Lien, autheur, date d'expiration, nombre d'utilisations |