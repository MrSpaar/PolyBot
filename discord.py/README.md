# Projet

Ce projet nécessite :
- [Python 3.8](https://www.python.org/downloads/) minimum
- [GTK](https://www.gtk.org/) pour les échecs

Pour installer les dépendances : `pip install -U -r requirements.txt`<br>
Pour lancer le bot : `python main.py`

# Commandes

### • 🧍 Commandes utilisateur

| Categorie                                     | Commandes                                                |
|-----------------------------------------------|----------------------------------------------------------|
| [Fun](./modules/fun.py)                       | `chess` `hangman` `minesweeper` `toss` `roll` `reaction` |
| [Musique](./modules/music.py)                 | `play` `pause` `skip` `remove` `leave`                   |
| [Recherche](./modules/search.py)              | `twitch` `youtube` `wikipedia` `anime` `weather`         |
| [Divers](./modules/misc.py)                   | `help` `poll` `source` `pfp` `emoji`                     |
| [Maths](./modules/maths.py)                   | `base` `binary` `hexadecimal` `calcul `                  |
| [Niveaux](./modules/levels.py)                | `rank` `levels`                                          |
| [Channels Temporaires](./modules/channels.py) | `voc rename` `voc private` `voc owner`                   |

### • 🔒 Commandes admin

| Categorie                             |                                   Commandes                             |
|---------------------------------------|-------------------------------------------------------------------------|
| [Modération](./modules/moderation.py) | `mute` `unmute` `clear` `kick` `ban` `unban`                            |
| [Infos](./modules/informations.py)    | `serverinfo` `userinfo` `roleinfo`                                      |
| [Setup](./modules/setup.py)           | `set` `settings`                                                        |

⚠️ La création d'un rôle `muted` est automatique mais si vous en voulez un spécifique : `!set mute <@role>`

# Modules supplémentaires

### • 📈 [Système d'expérience](./modules/levels.py)

Le système a la **même courbe d'xp que [Mee6](https://mee6.xyz/)**. <br>
Ecrivez `!set channel <#channel>` pour définir le salon où le bot fait ses annonces de level up.<br>
`!rank` vous montrera votre niveau, expérience et position dans le classement du serveur.<br>
`!levels` vous montrera le classement du serveur par page de 10.

### • 💬 [Chatbot OpenAI](./modules/openai.py)

Ce module est en cours d'affinage mais vous permet de "parler" avec PolyBot.
Pour obtenir une clé API, vous devrez rejoindre la [liste d'attente OpenAI](https://share.hsforms.com/1Lfc7WtPLRk2ppXhPjcYY-A4sk30). <br>
⚠️ Les réponses peuvent être répétitives ou imprécises. Je ne suis en aucun cas responsable des réponses données par le bot.

### • ⏲️ [Channels temporaires](./modules/channels.py)

Ce module permet d'avoir des channels vocaux temporaires :

- Chaque channel contenant [ce prefix](https://github.com/MrSpaar/PolyBot/blob/master/discord.py/modules/channels.py#L18) génèrera un channel tempaire dès que quelqu'un le rejoindra.
- Un channel écrit est généré et lié avec le channel temporaire.
- Les deux sont supprimés dès que le channel vocal est vide.

### • 📝 [Logs](./modules/logs.py)

Ecrivez `!set logs <#channel>` pour définir le channel contenant les logs.

| Log                        | Informations affichées                                   |
|----------------------------|----------------------------------------------------------|
| Messages supprimés         | Autheur, contenu et images (si il y en a)                |
| Nouveau membre             | Mention                                                  |
| Départ d'un membre         | Pseudo, ID et raison (ban, kick, ...)                    |
| Membre unban               | Pseudo, par qui et raison                                |
| Changement de surnom       | Ancien et nouveau surnom et par qui                      |
| Ajout/Suppression de rôles | Rôle ajouté ou enlevé, de qui et par qui                 |
| Modification de profile    | Ancien et nouveau pseudo et/ou tag                       |
| Création d'invitation      | Lien, autheur, date d'expiration, nombre d'utilisations  |

### • ❌ [Gestion d'erreurs](./modules/errors.py)

Ce module permet d'afficher des messages d'erreurs. A chaque erreur, un message suivi d'un exemple est envoyé.<br>
S'il s'agit d'une commande inconnue, la commande la plus proche apparait également dans le message d'erreur.
