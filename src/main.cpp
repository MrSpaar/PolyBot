#include "bot.h"

int main() {
    Bot bot("../.env", dpp::i_default_intents | dpp::i_guild_members);

    bot.on_ready(WRAP(dpp::ready_t, readyHandler));
    bot.on_message_create(WRAP(dpp::message_create_t, messageHandler));
    bot.on_message_reaction_add(WRAP(dpp::message_reaction_add_t, reactionHandler));
    bot.on_guild_member_add(WRAP(dpp::guild_member_add_t, memberJoinHandler));
    bot.on_guild_member_remove(WRAP(dpp::guild_member_remove_t, memberLeaveHandler));
    bot.on_guild_ban_add(WRAP(dpp::guild_ban_add_t, banHandler));
    bot.on_guild_ban_remove(WRAP(dpp::guild_ban_remove_t, unbanHandler));
    bot.on_voice_state_update(WRAP(dpp::voice_state_update_t, voiceHandler));

    bot.command("config", "Base des commandes de configuration", dpp::p_manage_guild)
            .subcommand("logs", "Définir le salon des logs", WRAP(logsHandler), {
                    {dpp::co_channel, "salon", "Le salon où envoyer les logs"}
            })
            .subcommand("bienvenue", "Définir le salon de bienvenue", WRAP(welcomeHandler), {
                    {dpp::co_channel, "salon",   "Le salon où envoyer les messages de bienvenue"},
                    {dpp::co_string,  "message", "Le message à envoyer (\"<mention>\" sera remplacé par la mention du nouveau membre)"}
            })
            .subcommand("nouveau", "Définir le rôle des nouveaux", WRAP(newcomerHandler), {
                    {dpp::co_role, "role", "Le rôle reçu par les nouveaux"}
            })
            .subcommand("annonce", "Définir le salon d'annonce de niveaux", WRAP(announceHandler), {
                    {dpp::co_channel, "salon", "Le salon où envoyer les annonces de niveaux"}
            })
       .command("rang", "Base des commandes de niveaux")
            .subcommand("global", "Afficher le classement du serveur", WRAP(leaderboardHandler))
            .subcommand("perso", "Afficher son classement ou celui d'un autre", WRAP(rankHandler), {
                    {dpp::co_user, "membre", "Le membre dont afficher le classement"}
            })
       .command("recherche", "Base des commandes de recherche")
            .subcommand("twitch", "Rechercher des streams", WRAP(twitchHandler), {
                    {dpp::co_string, "categorie", "La catégorie des streams à rechercher", true},
                    {dpp::co_string, "filtres", "Mots-clés pour filtrer les résultats"}
            })
            .subcommand("wiki", "Rechercher un article Wikipedia", WRAP(wikiHandler), {
                    {dpp::co_string, "titre", "Le nom de l'article à rechercher", true}
            })
       .command("mod", "Base des commandes de modération", dpp::p_ban_members)
            .subcommand("unban", "Débannir un membre du serveur", WRAP(unbanHandler), {
                    {dpp::co_user, "membre", "Le membre à débannir", true},
                    {dpp::co_string, "raison", "La raison du débannissement"}
            })
            .subcommand("clear", "Supprimer un nombre de messages (jusqu'à 2 semaines d'ancienneté)", WRAP(clearHandler), {
                    {dpp::co_integer, "nombre", "Le nombre de messages à supprimer (2 à 100)", true}
            });

    return bot.start(dpp::st_wait), 0;
}
