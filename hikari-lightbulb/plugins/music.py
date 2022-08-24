import hikari as hk
import lightbulb as lb
import lavasnek_rs as lrs


class EventHandler:
    async def track_finish(self, lavalink: lrs.Lavalink, event):
        await update_queue(lavalink, event.guild_id)


async def create_message(ctx: lb.Context, track) -> hk.Message:
    embed1 = hk.Embed(color=0x3498DB, description=f"🎵 [`{track.info.title}`]({track.info.uri}) de `{track.info.author}`\n🙍 Demandé par {ctx.author.mention}")
    embed2 = hk.Embed(color=0x99AAB5, description="*Pas de vidéos en attente*")

    resp = await ctx.respond(embeds=[embed1, embed2])
    message = await resp.message()

    for emoji in ("⏹️", "⏯️", "⏭️"):
        await message.add_reaction(emoji)

    return message


async def update_queue(lavalink: lrs.Lavalink, guild_id: int, track: lrs.Track = None):
    node = await lavalink.get_guild_node(guild_id)

    if node and not node.now_playing and not node.queue:
        return await stop(lavalink, guild_id)

    message = node.get_data()
    embeds = message.embeds
    np = node.queue[0]

    tracks = [t.track.info for t in node.queue[1:]] + [track.info] if track else []
    queue = [
        f"{i+1}) [`{info.title}`]({info.uri}) de `{info.author}`"
        for i, info in enumerate(tracks)
    ]

    embeds[0].description = f"🎵 [`{np.track.info.title}`]({np.track.info.uri}) de `{np.track.info.author}`\n🙍 Demandé par <@{np.requester}>"
    embeds[1].description = "\n".join(queue) or "*Pas de vidéos en attente*"

    await message.edit(embeds=embeds)


async def stop(lavalink, guild_id):
    node = await lavalink.get_guild_node(guild_id)
    try:
        await node.get_data().delete()
    except Exception:
        pass

    await lavalink.destroy(guild_id)
    await lavalink.leave(guild_id)
    await lavalink.remove_guild_node(guild_id)
    await lavalink.remove_guild_from_loops(guild_id)


async def join(ctx):
    states = plugin.bot.cache.get_voice_states_view_for_guild(ctx.get_guild())
    voice_state = list(filter(lambda i: i.user_id == ctx.author.id, states.iterator()))

    if not voice_state:
        embed = hk.Embed(color=0xE74C3C, description="❌ Tu n'es connecté à aucun salon vocal")
        return await ctx.respond(embed=embed, flags=hk.MessageFlag.EPHEMERAL)

    try:
        connection_info = await plugin.bot.d.lavalink.join(ctx.guild_id, voice_state[0].channel_id)
    except TimeoutError:
        embed = hk.Embed(color=0xE74C3C, description="❌ Je n'ai pas pu rejoindre le salon")
        return await ctx.respond(embed=embed, flags=hk.MessageFlag.EPHEMERAL)

    await plugin.bot.d.lavalink.create_session(connection_info)


plugin = lb.Plugin("Musique")
plugin.add_checks(lb.guild_only)


@plugin.listener(hk.ShardReadyEvent)
async def create_client(_):
    builder = (
        lrs.LavalinkBuilder(plugin.bot.get_me().id, plugin.bot._token)
        .set_host("127.0.0.1")
        .set_password("")
    )

    plugin.bot.d.lavalink = await builder.build(EventHandler())


@plugin.command()
@lb.option("lien", "Lien vers la vidéo a écouter", modifier=lb.OptionModifier.CONSUME_REST)
@lb.command("play", "Écouter une vidéo dans le channel où vous êtes connecté")
@lb.implements(lb.SlashCommand)
async def play(ctx: lb.Context):
    if not plugin.bot.d.lavalink.get_guild_gateway_connection_info(ctx.guild_id):
        if await join(ctx):
            return

    query = await plugin.bot.d.lavalink.auto_search_tracks(ctx.options.lien)

    if not query.tracks:
        embed = hk.Embed(color=0xE74C3C, description="❌ Aucun résultat correspondant à ta recherche")
        return await ctx.respond(embed=embed, flags=hk.MessageFlag.EPHEMERAL)

    track = query.tracks[0]
    node = await plugin.bot.d.lavalink.get_guild_node(ctx.guild_id)

    if node.now_playing:
        await update_queue(plugin.bot.d.lavalink, ctx.guild_id, track)
        await ctx.respond("Musique ajoutée", flags=hk.MessageFlag.EPHEMERAL)
    else:
        message = await create_message(ctx, track)
        node.set_data(message)

    await plugin.bot.d.lavalink.play(ctx.guild_id, track).requester(ctx.author.id).queue()


@plugin.listener(hk.GuildReactionAddEvent)
async def reaction_pressed(event):
    if event.member.is_bot:
        return

    node = await plugin.bot.d.lavalink.get_guild_node(event.guild_id)
    if not node:
        return

    message = node.get_data()
    await message.remove_reaction(event.emoji_name, user=event.member.id)

    if not message:
        return

    if event.emoji_name == "⏹️":
        await stop(plugin.bot.d.lavalink, event.guild_id)
    elif event.emoji_name == "⏭️":
        await plugin.bot.d.lavalink.skip(event.guild_id)
    elif event.emoji_name == "⏭️" and not node.queue and not node.now_playing:
        await stop(plugin.bot.d.lavalink, event.guild_id)
    elif event.emoji_name == "⏯️" and not node.is_paused:
        await plugin.bot.d.lavalink.pause(event.guild_id)
    else:
        await plugin.bot.d.lavalink.resume(event.guild_id)


def load(bot):
    bot.add_plugin(plugin)
