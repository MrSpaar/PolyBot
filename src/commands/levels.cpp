//
// Created by mrspaar on 3/18/23.
//

#include "pages.h"
#include "commands.h"


void rank_handler(const dpp::slashcommand_t &event) {
    dpp::guild_member member = event.command.member;
    auto subcommand = event.command.get_command_interaction().options[0];

    if (subcommand.options.size() > 1)
        member = event.command.get_resolved_member(subcommand.get_value<dpp::snowflake>(0));

    int level, xp, rank;
    Env::SQL << "SELECT level, xp, rank FROM ("
                    "   SELECT level, xp, user_id, ROW_NUMBER() OVER (ORDER BY xp DESC) AS rank FROM users WHERE guild_id = ?"
                    ") WHERE user_id = ?",
            soci::use(uint64_t(event.command.guild_id)), soci::use(uint64_t(member.user_id)),
            soci::into(level), soci::into(xp), soci::into(rank);


    if (xp == 0)
        return Command::reply(event, dpp::embed()
                .set_color(colors::RED)
                .set_description("❌ L'utilisateur n'est pas enregistré ou n'a jamais parlé"), true
        );

    std::string effective_name = member.nickname.empty() ? member.get_user()->username : member.nickname;

    Command::reply(event, dpp::embed()
            .set_color(colors::GREEN)
            .add_field("Niveau" + std::to_string(level), Pages::to_progress_bar(level, xp, 10), false)
            .set_author("Progression de " + effective_name, "", member.get_avatar_url())
    );
}


void leaderboard_handler(const dpp::slashcommand_t &event) {
    soci::rowset<soci::row> rows = (
            Env::SQL.prepare << "SELECT user_id, level, xp, ROW_NUMBER() OVER (ORDER BY xp DESC) AS rank FROM users WHERE guild_id = ? LIMIT 10",
                    soci::use(uint64_t(event.command.guild_id))
    );

    std::vector<std::string> values = Pages::process_rows(rows, event.command.guild_id);

    Command::reply(event, dpp::embed()
            .set_color(colors::BLUE)
            .set_footer("Page 1", "")
            .add_field("Nom", values[0], true)
            .add_field("Niveau", values[1], true)
            .add_field("Progression", values[2], true)
    );

    event.get_original_response([](const dpp::confirmation_callback_t &callback) {
        if (callback.is_error())
            return;

        Pages::create(get<dpp::message>(callback.value));
    });
}


Command level = Command("rang", "Commande de base pour les niveaux")
        .add_subcommand(
                Subcommand("perso", "Afficher la progression d'un membre", rank_handler)
                .add_option(dpp::co_user, "membre", "Membre à afficher", false)
        )
        .add_subcommand(
                Subcommand("classement", "Afficher le classement du serveur", leaderboard_handler)
        );
