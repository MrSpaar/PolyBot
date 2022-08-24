# Projet

Ce projet nécessite :
- [Python 3.9](https://www.python.org/downloads/) minimum
- [Java 11](https://adoptium.net/) minimum
- [GTK](https://www.gtk.org/) pour les échecs

Pour installer les dépendances : `pip install -U -r requirements.txt`.<br>
Pour lancer le bot :
- `java -jar lavalink/Lavalink.jar`
- `python main.py`

# Commandes

### • 🧍 Commandes utilisateur

| Categorie                                                                            | Commandes                                      |
|--------------------------------------------------------------------------------------|------------------------------------------------|
| [Fun](https://github.com/MrSpaar/Hikari-PolyBot/blob/master/plugins/fun.py)          | `chess` `pendu` `coinflip` `roll` `reaction`   |
| [Musique](https://github.com/MrSpaar/Hikari-PolyBot/blob/master/plugins/music.py)    | `play` `leave` `skip` `pause` `resume`         |
| [Recherche](https://github.com/MrSpaar/Hikari-PolyBot/blob/master/plugins/search.py) | `twitch` `youtube` `wikipedia` `anime` `meteo` |
| [Infos](https://github.com/MrSpaar/Hikari-PolyBot/blob/master/plugins/infos.py)      | `info server` `info user` `info role`          |
| [Divers](https://github.com/MrSpaar/Hikari-PolyBot/blob/master/plugins/misc.py)      | `poll` `pp` `emoji`                            |
| [Niveaux](https://github.com/MrSpaar/Hikari-PolyBot/blob/master/plugins/levels.py)   | `rank` `levels`                                |

### • 🔒 Commandes admin

| Categorie                                                                                 | Commandes                    |
|-------------------------------------------------------------------------------------------|------------------------------|
| [Modération](https://github.com/MrSpaar/Hikari-PolyBot/blob/master/plugins/moderation.py) | `clear` `kick` `ban` `unban` |
| [Menus](https://github.com/MrSpaar/Hikari-PolyBot/blob/master/plugins/utility.py)         | `menu`                       |

# Modules supplémentaires

### • 📈 [Système d'expérience](https://github.com/MrSpaar/Hikari-PolyBot/blob/master/plugins/levels.py)

Le système a la **même courbe d'xp que [Mee6](https://mee6.xyz/)**. <br>
Ecrivez `!set channel <#channel>` pour définir le salon où le bot fait ses annonces de level up.<br>
`!rank` vous montrera votre niveau, expérience et position dans le classement du serveur.<br>
`!levels` vous montrera le classement du serveur par page de 10.

### • ⏲️ [Channels temporaires](https://github.com/MrSpaar/Hikari-PolyBot/blob/master/plugins/channels.py)

Ce module permet d'avoir des channels vocaux temporaires :

- Chaque channel contenant [ce prefix](https://github.com/MrSpaar/Hikari-PolyBot/blob/master/plugins/channels.py#L18) génèrera un channel tempaire dès que quelqu'un le rejoindra.
- Un channel écrit est généré et lié avec le channel temporaire.
- Les deux sont supprimés dès que le channel vocal est vide.

### • 📝 [Logs](https://github.com/MrSpaar/Hikari-PolyBot/blob/master/plugins/logs.py)

Ecrivez `!set logs <#channel>` pour définir le channel contenant les logs.

| Log                        | Informations affichées                                  |
|----------------------------|---------------------------------------------------------|
| Nouveau membre             | Mention                                                 |
| Départ d'un membre         | Pseudo, ID et raison (ban, kick, ...)                   |
| Membre unban               | Pseudo, par qui et raison                               |
| Changement de surnom       | Ancien et nouveau surnom et par qui                     |
| Ajout/Suppression de rôles | Rôle ajouté ou enlevé, de qui et par qui                |
| Message supprimé           | Contenu du message, images, auteur et salon             |
| Création d'invitation      | Lien, autheur, date d'expiration, nombre d'utilisations |

### • ❌ [Gestion d'erreurs](https://github.com/MrSpaar/PolyBot/blob/master/modules/errors.py)

Ce module permet d'afficher des messages d'erreurs. A chaque erreur, un message suivi d'un exemple est envoyé.<br>
S'il s'agit d'une commande inconnue, la commande la plus proche apparait également dans le message d'erreur.
