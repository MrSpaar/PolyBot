//
// Created by mrspaar on 3/3/23.
//

#include <ranges>

#include "request.h"
#include "commands.h"


struct {
    std::string token;
    std::time_t expires{};
} twitch_oauth;


dpp::embed fetch_streams(const std::string &category, const std::string &limit, std::vector<std::string> &filters) {
    json j = Request("https://api.twitch.tv/helix/search/channels?live_only=true&query=" + category + "&first=" + limit)
            .add_header("Client-ID", Env::get("TWITCH_CLIENT"))
            .add_header("Authorization", "Bearer " + twitch_oauth.token)
            .get();

    if (j.contains("error"))
        return dpp::embed()
                .set_description("❌ Aucun résultat")
                .set_color(colors::RED);

    dpp::embed emb = dpp::embed()
            .set_color(colors::BLUE)
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


void twitch_handler(const dpp::slashcommand_t &event) {
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
        return Command::reply(event, fetch_streams(category, limit, filters));

    std::string data = "client_id=" + Env::get("TWITCH_CLIENT") +
                       "&client_secret=" + Env::get("TWITCH_TOKEN") +
                       "&grant_type=client_credentials";

    json j = Request("https://id.twitch.tv/oauth2/token")
            .post(data);

    if (j.contains("error"))
        return Command::reply(event, dpp::embed()
                .set_description("❌ Une erreur est survenue")
                .set_color(colors::RED), true
        );

    twitch_oauth.token = j["access_token"];
    twitch_oauth.expires = time(nullptr) + (time_t) j["expires_in"];

    Command::reply(event, fetch_streams(category, limit, filters));
}


void wiki_handler(const dpp::slashcommand_t &event) {
    auto subcommand = event.command.get_command_interaction().options[0];
    std::string title = subcommand.get_value<std::string>(0);

    json j = Request(
            "https://fr.wikipedia.org/w/api.php?format=json&action=query&prop=extracts|pageimages&exintro&explaintext&redirects=1&titles="
                    + title
    ).get();

    if (j.empty() || j["query"]["pages"].contains("-1"))
        return Command::reply(event, dpp::embed()
                .set_description("❌ Aucun résultat")
                .set_color(colors::RED), true
        );

    json article = j["query"]["pages"].begin().value();
    std::string full_url = "https://fr.wikipedia.org/wiki/" + article["title"].dump();

    Command::reply(event, dpp::embed()
            .set_color(colors::BLUE)
            .set_author("Wikipedia - " + (std::string) article["title"], "", "https://i.imgur.com/nDTQgbf.png")
            .set_description((std::string) article["extract"] + " [En savoir plus](" + full_url + ")")
    );
}


Command search = Command("recherche", "Base des commandes de recherche")
        .add_subcommand(
                Subcommand("twitch", "Rechercher des streams", twitch_handler)
                .add_option(dpp::co_string, "categorie", "La catégorie des streams à rechercher", true)
                .add_option(dpp::co_string, "filtres", "Mots-clés pour filtrer les résultats")
        )
        .add_subcommand(
                Subcommand("wiki", "Rechercher un article Wikipedia", wiki_handler)
                .add_option(dpp::co_string, "titre", "Le nom de l'article à rechercher", true)
        );
