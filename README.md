# PolyBot

La configuration du bot se fait avec un fichier `.env` :
```dotenv
GUILD_ID="..."

CHANNEL_PREFIX="..."
DB_PATH="..."
DISCORD_TOKEN="..."

TWITCH_CLIENT="..."
TWITCH_TOKEN="..."
```

Une fois configuré, il suffit de déployer le bot avec `docker-compose up -d`.

⚠️ Le bot est en plein transfert de JDA vers DPP, certaines fonctionnalités ne sont pas encore implémentées.<br>
Certaines fonctionnalités n'ont pas encore été testées, des bugs peuvent donc apparaître.

# Commandes

### • 🧍 Commandes utilisateur

| Categorie                                 | Commandes                 |
|-------------------------------------------|---------------------------|
| [Musique](./src/commands/music.cpp) (WIP) | `play` `skip`             |
| [Niveaux](./src/commands/levels.cpp)      | `perso` `global`          |
| [Recherche](./src/commands/search.cpp)    | `twitch` `wiki` `omgserv` |

### • 🔒 Commandes admin

| Categorie                                   | Commandes                              |
|---------------------------------------------|----------------------------------------|
| [Menu](./src/commands/menu.cpp) (WIP)       | `liste` `boutons`                      |
| [Modération](./src/commands/moderation.cpp) | `clear` `kick` `ban` `unban`           |
| [Configuration](./src/commands/config.cpp)  | `logs` `bienvenue` `nouveau` `annonce` |


# Modules supplémentaires

### • 📈 [Système d'expérience](./src/listeners/levels.cpp)

Le système a la **même courbe d'xp que [Mee6](https://mee6.xyz/)**. <br>
Il est possible d'envoyer des messages de level up avec `/config`.
- `/rang perso` vous montrera votre niveau et votre progression vers le prochain niveau<br>
- `/rang global` vous montrera le classement du serveur par pages de 10.

### • ⏲️ [Salons temporaires](./src/listeners/channels.cpp)

Ce module permet d'avoir des channels vocaux temporaires :

- Chaque générateur de salon temporaire doit contenir le `CHANNEL_PREFIX` du `.env` dans son nom.
- Un channel écrit est généré et lié avec le channel temporaire.
- Les deux sont supprimés dès que le channel vocal est vide.

### • 📌 [Menu de rôles](./src/listeners/menu.cpp)

Ce module permet de créer deux types de menu de rôles :
- Avec des boutons, l'utilisateur peut s'ajouter n'importe quel rôle parmi la liste
- Avec un menu déroulant, l'utilisateur ne peut choisir qu'un seul rôle parmi la liste

### • 📝 [Logs](./src/listeners/logs.cpp)

Ce module permet d'envoyer automatiquement des messages de bienvenue.<br>
Utilisez `/config` pour choisir le salon et le message de bienvenue, chaque `<mention>` sera remplacé par le nouveau membre.

| Log                                    | Informations affichées                                  |
|----------------------------------------|---------------------------------------------------------|
| Nouveau membre                         | Mention                                                 |
| Départ d'un membre                     | Pseudo, ID et raison (ban, kick, ...)                   |
| Membre banni                           | Pseudo, par qui et raison                               |
| Membre unban                           | Pseudo, par qui et raison                               |