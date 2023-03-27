//
// Created by mrspaar on 3/23/23.
//

#include "commands.h"


std::map<dpp::snowflake, dpp::snowflake> voice_cache;

void channel_joined_handler(dpp::guild_member &member, dpp::channel *category) {
    std::string name = "Salon de " + (member.nickname.empty() ? member.get_user()->username : member.nickname);

    auto voice_chan = dpp::channel()
            .set_name(name)
            .set_parent_id(category->id)
            .set_type(dpp::channel_type::CHANNEL_VOICE);

    Env::BOT.channel_create(voice_chan, [&](const auto &callback) {
        if (callback.is_error())
            return;

        voice_chan = std::get<dpp::channel>(callback.value);
        voice_cache[member.user_id] = voice_chan.id;
    });

    if (voice_chan.id.empty())
        return;

    auto text_chan = dpp::channel()
            .set_name(name)
            .set_parent_id(category->id)
            .set_type(dpp::channel_type::CHANNEL_TEXT);

    Env::BOT.channel_create(text_chan, [&](const auto &callback) {
        if (callback.is_error())
            return;

        text_chan = std::get<dpp::channel>(callback.value);
    });

    if (text_chan.id.empty())
        return;

    Env::BOT.guild_member_move(voice_chan.id, member.guild_id, member.user_id, [&](const auto &callback) {
        if (callback.is_error()) {
            Env::BOT.channel_delete(voice_chan.id);
            Env::BOT.channel_delete(text_chan.id);
            return;
        }

        Env::SQL << "INSERT INTO temp_channels (guild_id, user_id, voice_channel_id, text_channel_id) VALUES (?, ?, ?, ?)",
                soci::use(uint64_t(member.guild_id)), soci::use(uint64_t(member.user_id)),
                soci::use(uint64_t(voice_chan.id)), soci:: use(uint64_t(text_chan.id));
    });
}


void channel_left_handler(const dpp::voice_state_update_t &event) {
    if (!voice_cache.contains(event.state.user_id))
        return;

    dpp::channel *voice_chan = dpp::find_channel(voice_cache[event.state.user_id]);
    if (voice_chan == nullptr)
        return;

    voice_cache.erase(event.state.user_id);
    if (!voice_chan->get_voice_members().empty())
        return;

    Env::BOT.channel_delete(voice_chan->id, [&](const auto &callback) {
        if (callback.is_error())
            return;

        uint64_t text_chan_id;
        Env::SQL << "SELECT text_channel_id FROM temp_channels WHERE guild_id = ? AND voice_channel_id = ?",
                soci::use(uint64_t(event.state.guild_id)), soci::use(uint64_t(voice_chan->id)), soci::into(text_chan_id);

        Env::SQL << "DELETE FROM temp_channels WHERE guild_id = ? AND voice_channel_id = ?",
                soci::use(uint64_t(event.state.guild_id)), soci::use(uint64_t(voice_chan->id));

        Env::BOT.channel_delete(text_chan_id);
    });
}


Listener<dpp::voice_state_update_t> vsuh(&Env::BOT.on_voice_state_update, [](const auto &event) {
    dpp::channel *joined = dpp::find_channel(event.state.channel_id);
    dpp::guild_member member = dpp::find_guild_member(event.state.guild_id, event.state.user_id);

    if (joined == nullptr)
        return channel_left_handler(event);

    for (auto &chan : voice_cache)
        if (chan.second == joined->id) {
            voice_cache[event.state.user_id] = chan.second;
            return;
        }

    if (joined->name.find("Créer") == std::string::npos)
        return;

    if (voice_cache.contains(event.state.user_id)) {
        Env::BOT.guild_member_move(voice_cache[event.state.user_id], event.state.guild_id, event.state.user_id);
        return;
    }

    dpp::channel *category = dpp::find_channel(joined->parent_id);
    if (category == nullptr)
        return;

    channel_joined_handler(member, category);
});
