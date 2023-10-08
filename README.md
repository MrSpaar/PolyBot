# PolyBot

La configuration du bot se fait avec un fichier `.env` :
```dotenv
GEN_CHANNEL_PREFIX="..."
TEMP_CHANNEL_PREFIX="..."

DB_PATH="..."
JSON_PATH="..."
DISCORD_TOKEN="..."

TWITCH_TOKEN="..."
TWITCH_CLIENT="..."
```

Une fois configuré, il suffit de déployer le bot avec `docker-compose up -d`.

# Commandes

### • 🧍 Commandes utilisateur

| Categorie                               | Commandes         |
|-----------------------------------------|-------------------|
| [Niveaux](src/commands/levels.cpp)      | `perso` `global`  |
| [Recherche](src/commands/search.cpp)    | `twitch` `wiki`   |

### • 🔒 Commandes admin

| Categorie                                 | Commandes                              |
|-------------------------------------------|----------------------------------------|
| [Modération](src/commands/moderation.cpp) | `clear` `unban`                        |
| [Configuration](src/commands/config.cpp)  | `logs` `bienvenue` `nouveau` `annonce` |


# Modules supplémentaires

### • 📈 [Système d'expérience](./src/listeners/levels.cpp)

Le système a la **même courbe d'xp que [Mee6](https://mee6.xyz/)**. <br>
Il est possible d'envoyer des messages de level up avec `/config`.
- `/rang perso` vous montrera votre niveau et votre progression vers le prochain niveau<br>
- `/rang global` vous montrera le classement du serveur par pages de 10.

### • ⏲️ [Salons temporaires](./src/listeners/channels.cpp)

Ce module permet d'avoir des channels vocaux temporaires :

- Chaque générateur de salon temporaire doit contenir le `GEN_CHANNEL_PREFIX` du `.env` dans son nom.
- Un channel écrit est généré et lié avec le channel temporaire.
- Les deux sont supprimés dès que le channel vocal est vide.

### • 📝 [Logs](./src/listeners/logs.cpp)

Ce module permet d'envoyer automatiquement des messages de bienvenue.<br>
Utilisez `/config` pour choisir le salon et le message de bienvenue, chaque `<mention>` sera remplacé par le nouveau membre.

| Log                                    | Informations affichées                                  |
|----------------------------------------|---------------------------------------------------------|
| Nouveau membre                         | Mention                                                 |
| Départ d'un membre                     | Pseudo, ID et raison (ban, kick, ...)                   |
| Membre banni                           | Pseudo, par qui et raison                               |
| Membre unban                           | Pseudo, par qui et raison                               |