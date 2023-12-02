//
// Created by mrspaar on 3/18/23.
//

#include "logger.h"
#include "paginator.h"


void Bot::rankHandler(const dpp::slashcommand_t &event) {
    dpp::command_value param = event.get_parameter("membre");
    bool has_param = std::holds_alternative<dpp::snowflake>(param);

    std::string user_id = has_param ?
            std::to_string(std::get<dpp::snowflake>(param)) : std::to_string(event.command.member.user_id);

    SQLRow row;
    std::string guild_id = std::to_string(event.command.guild_id);

    int rc = SQLQuery(db,
            "SELECT level, xp, rank FROM ("
            "   SELECT level, xp, id, ROW_NUMBER() OVER (ORDER BY xp DESC) AS rank FROM users WHERE guild = ?"
            ") WHERE id = ?;"
    ).bind(guild_id)
     .bind(user_id)
     .step(row);

    if (rc != SQLITE_ROW) {
        logger(WARNING) << "User " << user_id << " not found in guild " << guild_id << std::endl;

        return Bot::reply(event, dpp::embed()
                .set_color(RED)
                .set_description("❌ L'utilisateur n'est pas enregistré ou n'a jamais parlé"), true
        );
    }

    auto xp = row.get<int>("xp");
    auto level = row.get<int>("level");
    auto rank = row.get<int>("rank");

    dpp::guild_member member = has_param ? event.command.get_resolved_member(user_id) : event.command.member;
    std::string effective_name = member.nickname, effective_avatar = member.get_avatar_url();

    if (member.get_user() == nullptr) {
        dpp::user_identified user = user_get_sync(user_id);
        effective_name = user.username;
        effective_avatar = user.get_avatar_url();
    } else if (effective_avatar.empty())
        effective_avatar = member.get_user()->get_avatar_url();

    logger(INFO) << "User " << user_id << " used rank command" << std::endl;

    Bot::reply(event, dpp::embed()
            .set_color(BLUE)
            .add_field(
                    "Niveau " + std::to_string(level) + " • Rang " + std::to_string(rank),
                    Paginator::to_progress_bar(level, xp, 14), false
            )
            .set_author("Progression de " + effective_name, "", effective_avatar)
    );
}


void Bot::leaderboardHandler(const dpp::slashcommand_t &event) {
    dpp::embed embed = dpp::embed()
            .set_color(BLUE)
            .set_footer("Page 1", "")
            .set_author("Classement du serveur", "", event.command.get_guild().get_icon_url());

    Bot::reply(event, embed);
    logger(INFO) << "User " << event.command.member.user_id << " used leaderboard command" << std::endl;

    event.get_original_response([&](const dpp::confirmation_callback_t &callback) {
        if (callback.is_error())
            return;

        dpp::message msg = get<dpp::message>(callback.value);
        Paginator::CACHE[msg.id] = new Paginator(this, msg);
    });
}
