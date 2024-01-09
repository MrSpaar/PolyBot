//
// Created by mrspaar on 3/23/23.
//

#include "framework/listener.h"


std::map<dpp::snowflake, dpp::snowflake> channel_cache;
std::map<dpp::snowflake, std::pair<dpp::snowflake, dpp::snowflake>> user_cache;


void move_callback(
    Logger &logger, dpp::cluster &cluster,
    const dpp::confirmation_callback_t &callback, const dpp::guild_member &member,
    const dpp::channel &voice_channel, const std::string &name, const dpp::snowflake &category_id
) {
    if (callback.is_error()) {
        logger(WARNING) << "Error while moving user " << member.user_id << std::endl;
        cluster.channel_delete(voice_channel.id);
        return;
    }

    logger(INFO) << "Moved user " << member.user_id << std::endl;

    user_cache[member.user_id] = {voice_channel.id, voice_channel.id};
    cluster.channel_create(dpp::channel()
            .set_name(name)
            .set_parent_id(category_id)
            .set_guild_id(member.guild_id)
            .set_type(dpp::channel_type::CHANNEL_TEXT),

            [&](const dpp::confirmation_callback_t &callback) {
                auto text_channel = std::get<dpp::channel>(callback.value);
                channel_cache[voice_channel.id] = text_channel.id;
            }
    );
}

void channel_joined_handler(
    DotEnv &env, Logger &logger, dpp::cluster &cluster,
    const dpp::guild_member &member, const dpp::snowflake &category_id
) {
    std::string effective_name = member.get_nickname();

    if (effective_name.empty() && member.get_user() == nullptr) {
        dpp::user_identified user = cluster.user_get_sync(member.user_id);
        effective_name = user.username;
    } else if (effective_name.empty()) {
        effective_name = member.get_user()->username;
    }

    std::string name = env["TEMP_CHANNEL_PREFIX"] + effective_name;
    logger(INFO) << "Creating channel " << name << std::endl;

    cluster.channel_create(dpp::channel()
            .set_name(name)
            .set_parent_id(category_id)
            .set_guild_id(member.guild_id)
            .set_type(dpp::channel_type::CHANNEL_VOICE),

            [&, member, category_id, name](const dpp::confirmation_callback_t &callback) {
                if (callback.is_error())
                    return;

                auto voice_channel = std::get<dpp::channel>(callback.value);
                cluster.guild_member_move(
                        voice_channel.id, member.guild_id, member.user_id,
                        [&](const dpp::confirmation_callback_t &callback) {
                            move_callback(logger, cluster, callback, member, voice_channel, name, category_id);
                        }
                );
            }
    );
}

void channel_left_handler(Logger &logger, dpp::cluster &cluster, const dpp::voice_state_update_t &event) {
    if (!user_cache.contains(event.state.user_id))
        return;

    dpp::channel *voice_chan = dpp::find_channel(user_cache[event.state.user_id].first);
    if (voice_chan == nullptr || !voice_chan->get_voice_members().empty())
        return;

    logger(INFO) << "Deleting channel " << voice_chan->id << std::endl;

    cluster.channel_delete(voice_chan->id);
    cluster.channel_delete(channel_cache[voice_chan->id]);

    user_cache.erase(event.state.user_id);
    channel_cache.erase(voice_chan->id);
}

DECLARE_LISTENER(Voice) {
    cluster.on_voice_state_update(WRAP_LTNR(voiceHandler));
}

EVENT_HANDLER(voiceHandler, dpp::voice_state_update_t) {
    dpp::channel *joined = dpp::find_channel(event.state.channel_id);
    dpp::guild_member member = dpp::find_guild_member(event.state.guild_id, event.state.user_id);

    if (joined == nullptr)
        return channel_left_handler(logger, cluster, event);

    bool is_user_cached = user_cache.contains(event.state.user_id);
    bool is_gen_channel = joined->name.find(env["GEN_CHANNEL_PREFIX"], 0) != std::string::npos;
    bool is_temp_channel = joined->name.rfind(env["TEMP_CHANNEL_PREFIX"], 0) == 0;

    if (!is_temp_channel && !is_gen_channel)
        return channel_left_handler(logger, cluster, event);

    if (is_temp_channel && !is_user_cached) {
        user_cache[event.state.user_id] = {joined->id, 0};
        return;
    }

    if (is_temp_channel) {
        user_cache[event.state.user_id].first = joined->id;
        return;
    }

    if (!is_gen_channel)
        return channel_left_handler(logger, cluster, event);

    if (is_user_cached && !user_cache[event.state.user_id].second.empty())
        return cluster.guild_member_move(
                user_cache[event.state.user_id].second,
                event.state.guild_id,
                event.state.user_id
        );

    if (!joined->parent_id.empty())
        return channel_joined_handler(env, logger, cluster, member, joined->parent_id);
}
