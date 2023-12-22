//
// Created by mrspaar on 3/3/23.
//

#include <ranges>
#include "bot.h"
#include "request.h"


struct {
    std::string token;
    std::time_t expires{};
} twitch_oauth;


dpp::embed fetch_streams(const std::string &clientID, const std::string &category, const std::string &limit, std::vector<std::string> &filters) {
    dpp::json j = Request("https://api.twitch.tv/helix/search/channels?live_only=true&query=" + category + "&first=" + limit)
            .add_header("Client-ID", clientID)
            .add_header("Authorization", "Bearer " + twitch_oauth.token)
            .get();

    if (j.contains("error"))
        return dpp::embed()
                .set_description("❌ Aucun résultat")
                .set_color(RED);

    dpp::embed emb = dpp::embed()
            .set_color(BLUE)
            .set_author("Twitch - " + (std::string) j["data"][0]["game_name"], "", "https://i.imgur.com/gArdgyC.png");

    for (auto &stream: j["data"] | std::ranges::views::filter([&filters](auto &stream) {
        return filters.empty() || std::ranges::any_of(filters, [&stream](auto &word) {
            std::string stream_title = stream["title"].dump();
            transform(word.begin(), word.end(), word.begin(), ::tolower);
            transform(stream_title.begin(), stream_title.end(), stream_title.begin(), ::tolower);
            return stream_title.find(word) != std::string::npos;
        });
    })) {
        emb.add_field(
                (std::string) stream["display_name"],
                "[" + (std::string) stream["title"] + "](https://twitch.tv/" +
                (std::string) stream["broadcaster_login"] + ")",
                true
        );
    }

    return emb;
}


void Bot::twitchHandler(const dpp::slashcommand_t &event) {
    logger(INFO) << "User " << event.command.member.user_id << " used twitch command" << std::endl;

    auto subcommand = event.command.get_command_interaction().options[0];
    std::string category = subcommand.get_value<std::string>(0);

    std::string limit = "10";
    std::vector<std::string> filters;

    if (subcommand.options.size() > 1) {
        limit = "100";
        std::istringstream iss(subcommand.get_value<std::string>(1));
        copy(std::istream_iterator<std::string>(iss), std::istream_iterator<std::string>(), back_inserter(filters));
    }

    if (!twitch_oauth.token.empty() && twitch_oauth.expires > time(nullptr))
        return Bot::reply(event, fetch_streams(env["TWITCH_CLIENT"], category, limit, filters));

    std::string data = "client_id=" + env["TWITCH_CLIENT"] +
                       "&client_secret=" + env["TWITCH_TOKEN"] +
                       "&grant_type=client_credentials";

    dpp::json j = Request("https://id.twitch.tv/oauth2/token")
            .post(data);

    if (j.contains("error"))
        return Bot::reply(event, dpp::embed()
                .set_description("❌ Une erreur est survenue")
                .set_color(RED), true
        );

    twitch_oauth.token = j["access_token"];
    twitch_oauth.expires = time(nullptr) + (time_t) j["expires_in"];

    Bot::reply(event, fetch_streams(env["TWITCH_CLIENT"], category, limit, filters));
}


void Bot::wikiHandler(const dpp::slashcommand_t &event) {
    logger(INFO) << "User " << event.command.member.user_id << " used wiki command" << std::endl;
    
    auto subcommand = event.command.get_command_interaction().options[0];
    std::string title = subcommand.get_value<std::string>(0);

    dpp::json j = Request(
            "https://fr.wikipedia.org/w/api.php?format=json&action=query&prop=extracts|pageimages&exintro&explaintext&redirects=1&titles="
                    + title
    ).get();

    if (j.empty() || j["query"]["pages"].contains("-1"))
        return Bot::reply(event, dpp::embed()
                .set_description("❌ Aucun résultat")
                .set_color(RED), true
        );

    dpp::json article = j["query"]["pages"].begin().value();
    std::string full_url = "https://fr.wikipedia.org/wiki/" + article["title"].dump();

    Bot::reply(event, dpp::embed()
            .set_color(BLUE)
            .set_author("Wikipedia - " + (std::string) article["title"], "", "https://i.imgur.com/nDTQgbf.png")
            .set_description((std::string) article["extract"] + " [En savoir plus](" + full_url + ")")
    );
}
