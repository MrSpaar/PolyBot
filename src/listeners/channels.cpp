//
// Created by mrspaar on 3/23/23.
//

#include "listeners.h"


std::map<dpp::snowflake, dpp::snowflake> channel_cache;
std::map<dpp::snowflake, std::pair<dpp::snowflake, dpp::snowflake>> user_cache;


auto move_callback(const dpp::guild_member &member, const dpp::channel &voice_channel, const dpp::channel &text_channel) {
    return [member, voice_channel, text_channel](const dpp::confirmation_callback_t &callback) {
        if (callback.is_error()) {
            Env::BOT.channel_delete(voice_channel.id);
            Env::BOT.channel_delete(text_channel.id);
            return;
        }

        channel_cache[voice_channel.id] = text_channel.id;
        user_cache[member.user_id] = {voice_channel.id, voice_channel.id};
    };
}

auto text_callback(const dpp::guild_member &member, const dpp::channel &voice_channel) {
    return [member, voice_channel](const dpp::confirmation_callback_t &callback) {
        if (callback.is_error()) {
            Env::BOT.channel_delete(voice_channel.id);
            return;
        }

        auto text_channel = std::get<dpp::channel>(callback.value);
        Env::BOT.guild_member_move(
                voice_channel.id, member.guild_id, member.user_id,
                move_callback(member, voice_channel, text_channel)
        );
    };
}

auto voice_callback(const dpp::guild_member &member, const std::string &name, const dpp::snowflake &category_id) {
    return [member, name, category_id](const dpp::confirmation_callback_t &callback) {
        if (callback.is_error())
            return;

        auto voice_channel = std::get<dpp::channel>(callback.value);
        auto cb = text_callback(member, voice_channel);

        Env::BOT.channel_create(dpp::channel()
                 .set_name(name)
                 .set_parent_id(category_id)
                 .set_guild_id(member.guild_id)
                 .set_type(dpp::channel_type::CHANNEL_TEXT), cb
        );
    };
}


void channel_joined_handler(const dpp::guild_member &member, const dpp::snowflake category_id) {
    std::string effective_name = member.nickname;

    if (effective_name.empty() && member.get_user() == nullptr) {
        dpp::user_identified user = Env::BOT.user_get_sync(member.user_id);
        effective_name = user.username;
    } else if (effective_name.empty()) {
        effective_name = member.get_user()->username;
    }

    std::string name = Env::get("TEMP_CHANNEL_PREFIX") + effective_name;

    Env::BOT.channel_create(dpp::channel()
            .set_name(name)
            .set_parent_id(category_id)
            .set_guild_id(member.guild_id)
            .set_type(dpp::channel_type::CHANNEL_VOICE), voice_callback(member, name, category_id)
    );
}


void channel_left_handler(const dpp::voice_state_update_t &event) {
    if (!user_cache.contains(event.state.user_id))
        return;

    dpp::channel *voice_chan = dpp::find_channel(user_cache[event.state.user_id].first);
    if (voice_chan == nullptr || !voice_chan->get_voice_members().empty())
        return;

    Env::BOT.channel_delete(voice_chan->id);
    Env::BOT.channel_delete(channel_cache[voice_chan->id]);

    user_cache.erase(event.state.user_id);
    channel_cache.erase(voice_chan->id);
}


void Listeners::onVoiceStateUpdate(const dpp::voice_state_update_t &event) {
    dpp::channel *joined = dpp::find_channel(event.state.channel_id);
    dpp::guild_member member = dpp::find_guild_member(event.state.guild_id, event.state.user_id);

    if (joined == nullptr)
        return channel_left_handler(event);

    bool is_user_cached = user_cache.contains(event.state.user_id);
    bool is_gen_channel = joined->name.find(Env::get("GEN_CHANNEL_PREFIX"), 0) != std::string::npos;
    bool is_temp_channel = joined->name.rfind(Env::get("TEMP_CHANNEL_PREFIX"), 0) == 0;

    if (!is_temp_channel && !is_gen_channel)
        return;

    if (is_temp_channel && !is_user_cached) {
        user_cache[event.state.user_id] = {joined->id, 0};
        return;
    }

    if (is_temp_channel) {
        user_cache[event.state.user_id].first = joined->id;
        return;
    }

    if (!is_gen_channel)
        return;

    if (is_user_cached && !user_cache[event.state.user_id].second.empty())
        return Env::BOT.guild_member_move(
                user_cache[event.state.user_id].second,
                event.state.guild_id,
                event.state.user_id
        );

    if (!joined->parent_id.empty())
        return channel_joined_handler(member, joined->parent_id);
}
