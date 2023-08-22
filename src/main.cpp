#include "commands.h"
#include "listeners.h"


int main() {
    Env::load();
    Env::init(Env::get("DISCORD_TOKEN"), Env::get("DB_PATH"));

    Env::SQL << "CREATE TABLE IF NOT EXISTS guilds ("
           "    id STRING PRIMARY KEY NOT NULL,"
           "    announce_channel TEST,"
           "    logs_channel TEXT,"
           "    newcomer_role TEXT,"
           "    welcome_channel TEXT,"
           "    welcome_message TEXT"
           ");";

    Env::SQL << "CREATE TABLE IF NOT EXISTS users ("
           "    id STRING PRIMARY KEY NOT NULL,"
           "    guild STRING PRIMARY KEY NOT NULL,"
           "    xp INTEGER NOT NULL DEFAULT 0,"
           "    level INTEGER NOT NULL DEFAULT 0"
           ");", sqlite::run;

    Env::BOT.on_ready(Listeners::onReady);
    Env::BOT.on_button_click(Listeners::onButtonClick);
    Env::BOT.on_select_click(Listeners::onSelectClick);
    Env::BOT.on_message_create(Listeners::onMessageCreate);
    Env::BOT.on_message_reaction_add(Listeners::onReactionAdd);
    Env::BOT.on_guild_member_add(Listeners::onGuildMemberAdd);
    Env::BOT.on_guild_member_remove(Listeners::onGuildMemberRemove);
    Env::BOT.on_guild_ban_add(Listeners::onGuildBanAdd);
    Env::BOT.on_guild_ban_remove(Listeners::onGuildBanRemove);
    Env::BOT.on_voice_state_update(Listeners::onVoiceStateUpdate);

    Command("config", "Base des commandes de configuration")
            .add_subcommand("logs", "Définir le salon des logs", Commands::logsHandler, {
                    {dpp::co_channel, "salon", "Le salon où envoyer les logs"}
            })
            .add_subcommand("bienvenue", "Définir le salon de bienvenue", Commands::welcomeHandler, {
                    {dpp::co_channel, "salon", "Le salon où envoyer les messages de bienvenue"},
                    {dpp::co_string, "message", "Le message à envoyer (\"<mention>\" sera remplacé par la mention du nouveau membre)"}
            })
            .add_subcommand("nouveau", "Définir le rôle des nouveaux", Commands::newcomerHandler, {
                    {dpp::co_role, "role", "Le rôle reçu par les nouveaux"}
            })
            .add_subcommand("annonce", "Définir le salon d'annonce de niveaux", Commands::announceHandler, {
                    {dpp::co_channel, "salon", "Le salon où envoyer les annonces de niveaux"}
            });

    Command("rang", "Base des commandes de niveaux")
            .add_subcommand("global", "Afficher le classement du serveur", Commands::leaderboardHandler)
            .add_subcommand("perso", "Afficher son classement ou celui d'un autre", Commands::rankHandler, {
                    {dpp::co_user, "membre", "Le membre dont afficher le classement"}
            });

    Command("menu", "Base des commandes de menus")
            .add_subcommand("boutons", "Créer un menu de rôles avec des boutons", Commands::buttonsHandler, {
                    {dpp::co_string, "titre", "Le titre du menu", true}
            })
            .add_subcommand("liste", "Créer un menu de rôles avec une liste déroulante", Commands::selectHandler, {
                    {dpp::co_string, "titre", "Le titre du menu", true}
            });

    Command("recherche", "Base des commandes de recherche")
            .add_subcommand("twitch", "Rechercher des streams", Commands::twitchHandler, {
                    {dpp::co_string, "categorie", "La catégorie des streams à rechercher", true},
                    {dpp::co_string, "filtres", "Mots-clés pour filtrer les résultats"}
            })
            .add_subcommand("wiki", "Rechercher un article Wikipedia", Commands::wikiHandler, {
                    {dpp::co_string, "titre", "Le nom de l'article à rechercher", true}
            });

    Command("kick", "Expulser un membre du serveur", Commands::kickHandler)
            .add_option(dpp::co_user, "membre", "Le membre à expulser", true)
            .add_option(dpp::co_string, "raison", "La raison de l'expulsion");
    Command("ban", "Bannir un membre du serveur", Commands::banHandler)
            .add_option(dpp::co_user, "membre", "Le membre à bannir", true)
            .add_option(dpp::co_string, "raison", "La raison du bannissement");
    Command("unban", "Débannir un membre du serveur", Commands::unbanHandler)
            .add_option(dpp::co_user, "membre", "Le membre à débannir", true)
            .add_option(dpp::co_string, "raison", "La raison du débannissement");
    Command("clear", "Supprimer un nombre de messages (jusqu'à 2 semaines d'ancienneté)", Commands::clearHandler)
            .add_option(dpp::co_integer, "nombre", "Le nombre de messages à supprimer (2 à 100)", true);

    Env::BOT.intents = dpp::i_default_intents | dpp::i_guild_members;
    Env::BOT.set_presence(dpp::presence(dpp::ps_online, dpp::at_game, "vous observer"));
    Env::BOT.start(dpp::st_wait);
    return 0;
}
