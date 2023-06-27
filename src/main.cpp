#include "commands.h"
#include "listeners.h"


void Listeners::bind() {
    Env::BOT.on_ready(onReady);
    Env::BOT.on_button_click(onButtonClick);
    Env::BOT.on_select_click(onSelectClick);
    Env::BOT.on_message_create(onMessageCreate);
    Env::BOT.on_message_reaction_add(onReactionAdd);
    Env::BOT.on_guild_member_add(onGuildMemberAdd);
    Env::BOT.on_guild_member_remove(onGuildMemberRemove);
    Env::BOT.on_guild_ban_add(onGuildBanAdd);
    Env::BOT.on_guild_ban_remove(onGuildBanRemove);
    Env::BOT.on_voice_state_update(onVoiceStateUpdate);
}

void Commands::bind() {
    Command("config", "Base des commandes de configuration")
            .add_subcommand("logs", "Définir le salon des logs", logs_handler, {
                    {dpp::co_channel, "salon", "Le salon où envoyer les logs"}
            })
            .add_subcommand("bienvenue", "Définir le salon de bienvenue", welcome_handler, {
                    {dpp::co_channel, "salon", "Le salon où envoyer les messages de bienvenue"},
                    {dpp::co_string, "message", "Le message à envoyer (\"<mention>\" sera remplacé par la mention du nouveau membre)"}
            })
            .add_subcommand("nouveau", "Définir le rôle des nouveaux", newcomer_handler, {
                    {dpp::co_role, "role", "Le rôle reçu par les nouveaux"}
            })
            .add_subcommand("annonce", "Définir le salon d'annonce de niveaux", announce_handler, {
                    {dpp::co_channel, "salon", "Le salon où envoyer les annonces de niveaux"}
            });

    Command("rang", "Base des commandes de niveaux")
            .add_subcommand("global", "Afficher le classement du serveur", leaderboard_handler)
            .add_subcommand("perso", "Afficher son classement ou celui d'un autre", rank_handler, {
                    {dpp::co_user, "membre", "Le membre dont afficher le classement"}
            });

    Command("menu", "Base des commandes de menus")
            .add_subcommand("boutons", "Créer un menu de rôles avec des boutons", buttons_handler, {
                    {dpp::co_string, "titre", "Le titre du menu", true}
            })
            .add_subcommand("liste", "Créer un menu de rôles avec une liste déroulante", select_handler, {
                    {dpp::co_string, "titre", "Le titre du menu", true}
            });

    Command("recherche", "Base des commandes de recherche")
            .add_subcommand("twitch", "Rechercher des streams", twitch_handler, {
                    {dpp::co_string, "categorie", "La catégorie des streams à rechercher", true},
                    {dpp::co_string, "filtres", "Mots-clés pour filtrer les résultats"}
            })
            .add_subcommand("wiki", "Rechercher un article Wikipedia", wiki_handler, {
                    {dpp::co_string, "titre", "Le nom de l'article à rechercher", true}
            });

    Command("kick", "Expulser un membre du serveur", kick_handler)
            .add_option(dpp::co_user, "membre", "Le membre à expulser", true)
            .add_option(dpp::co_string, "raison", "La raison de l'expulsion");

    Command("ban", "Bannir un membre du serveur", ban_handler)
            .add_option(dpp::co_user, "membre", "Le membre à bannir", true)
            .add_option(dpp::co_string, "raison", "La raison du bannissement");

    Command("unban", "Débannir un membre du serveur", unban_handler)
            .add_option(dpp::co_user, "membre", "Le membre à débannir", true)
            .add_option(dpp::co_string, "raison", "La raison du débannissement");

    Command("clear", "Supprimer un nombre de messages (jusqu'à 2 semaines d'ancienneté)", clear_handler)
            .add_option(dpp::co_integer, "nombre", "Le nombre de messages à supprimer (2 à 100)", true);
}


int main() {
    Env::load();
    Env::init(Env::get("DISCORD_TOKEN"), Env::get("DB_PATH"));

    Listeners::bind();
    Commands::bind();

    Env::BOT.start(dpp::st_wait);
    return 0;
}
