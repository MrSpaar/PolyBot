//
// Created by mrspaar on 3/18/23.
//

#include "sqlite.h"
#include "pages.h"
#include "commands.h"


void Commands::rank_handler(const dpp::slashcommand_t &event) {
    dpp::command_value param = event.get_parameter("membre");
    bool has_param = std::holds_alternative<dpp::snowflake>(param);

    std::string user_id = has_param ?
            std::to_string(std::get<dpp::snowflake>(param)) : std::to_string(event.command.member.user_id);

    std::string guild_id = std::to_string(event.command.guild_id);
    Env::SQL << "SELECT level, xp, rank FROM ("
                    "   SELECT level, xp, id, ROW_NUMBER() OVER (ORDER BY xp DESC) AS rank FROM users WHERE guild = ?"
                    ") WHERE id = ?", guild_id, user_id;

    if (!Env::SQL.good())
        return;

    auto xp = Env::SQL.get<int>("xp");
    auto level = Env::SQL.get<int>("level");
    auto rank = Env::SQL.get<int>("rank");

    if (xp == 0)
        return Commands::reply(event, dpp::embed()
                .set_color(colors::RED)
                .set_description("❌ L'utilisateur n'est pas enregistré ou n'a jamais parlé"), true
        );

    dpp::guild_member member = has_param ? event.command.get_resolved_member(user_id) : event.command.member;
    std::string effective_name = member.nickname, effective_avatar = member.get_avatar_url();

    if (member.get_user() == nullptr) {
        dpp::user_identified user = Env::BOT.user_get_sync(user_id);
        effective_name = user.username;
        effective_avatar = user.get_avatar_url();
    } else if (effective_avatar.empty())
        effective_avatar = member.get_user()->get_avatar_url();

    Commands::reply(event, dpp::embed()
            .set_color(colors::BLUE)
            .add_field(
                    "Niveau " + std::to_string(level) + " • Rang " + std::to_string(rank),
                    Pages::to_progress_bar(level, xp, 14),
                    false
            )
            .set_author("Progression de " + effective_name, "", effective_avatar)
    );
}


void Commands::leaderboard_handler(const dpp::slashcommand_t &event) {
    std::string guild_id = std::to_string(event.command.guild_id);

    Env::SQL << "SELECT id, level, xp, ROW_NUMBER() OVER (ORDER BY xp DESC) AS rank FROM users WHERE guild = ? LIMIT 10", guild_id;
    if (!Env::SQL.good())
        return Commands::reply(event, dpp::embed()
                .set_color(colors::RED)
                .set_description("❌ Aucun membre n'est enregistré ou n'a jamais parlé"), true
        );

    dpp::embed embed = dpp::embed()
            .set_color(colors::BLUE)
            .set_footer("Page 1", "")
            .set_author("Classement du serveur", "", event.command.get_guild().get_icon_url());

    Pages::process_rows(embed);
    Commands::reply(event, embed);

    event.get_original_response([](const dpp::confirmation_callback_t &callback) {
        if (callback.is_error())
            return;

        Pages::create(get<dpp::message>(callback.value));
    });
}
