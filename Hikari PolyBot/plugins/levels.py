import hikari as hk
import lightbulb as lb

from core.db import DB
from core.cd import Cooldown

cd = Cooldown(1, 60)
plugin = lb.Plugin("Niveaux")
plugin.add_checks(lb.guild_only)


def get_progress_bar(level: int, xp: int, n: int, short: bool = False):
    needed = 5 * ((level - 1) ** 2) + (50 * (level - 1)) + 100
    progress = (
        needed - int(5 / 6 * level * (2 * level ** 2 + 27 * level + 91) - xp)
        if xp
        else 0
    )
    p = int((progress / needed) * n) or 1

    if short:
        progress = f"{round(progress/1000, 1)}k" if int(progress / 1000) else progress
        needed = f"{round(needed/1000, 1)}k" if int(needed / 1000) else needed

    return f"{'🟩'*p}{'⬛' * (n-p)} {progress} / {needed}"


def get_page(guild: hk.Guild, entries: dict):
    field1, field2, field3 = "", "", ""

    for user_id, entry in entries.items():
        member = guild.get_member(user_id)
        if not member:
            continue

        level, xp = entry["level"], entry["xp"]

        bar = get_progress_bar(level + 1, xp, 5, True)
        xp = f"{round(xp / 1000, 1)}k" if int(xp / 1000) else xp

        field1 += f"**{entry['pos']}.** {member.display_name}\n"
        field2 += f"{level} ({xp})\n"
        field3 += f"{bar}\n"

    return ("Noms", field1), ("Niveau", field2), ("Progrès", field3)


@plugin.command()
@lb.option("membre", "Le membre dont tu veux voir le rang", hk.Member, default=None)
@lb.command("rank", "Afficher le niveau d'un membre")
@lb.implements(lb.SlashCommand)
async def rank(ctx: lb.Context):
    guild = ctx.get_guild()
    member = ctx.options.membre or ctx.options.target or ctx.member

    data = await DB.fetch_leaderboard(guild.id)
    data = {
        entry["_id"]: entry["guilds"][0] | {"pos": i + 1}
        for i, entry in enumerate(sorted(data, reverse=True, key=lambda e: e['guilds'][0]["xp"]))
    }

    embed = hk.Embed(color=0x3498DB)
    embed.set_author(name=f"Progression de {member.display_name}", icon=member.avatar_url)

    xp, lvl = data[member.id]["xp"], data[member.id]["level"] + 1
    bar = get_progress_bar(lvl, xp, 13)

    embed.add_field(name=f"Niveau {lvl-1} • Rang {data[member.id]['pos']}", value=bar)
    await ctx.respond(embed=embed)


@plugin.command()
@lb.command("Rang", "Afficher le niveau d'un membre")
@lb.implements(lb.UserCommand)
async def _rank(ctx: lb.Context):
    await rank(ctx)


@plugin.command()
@lb.command("levels", description="Afficher le classement du serveur")
@lb.implements(lb.SlashCommand)
async def levels(ctx: lb.Context):
    guild = ctx.get_guild()
    embed = (
        hk.Embed(color=0x3498DB)
        .set_author(name="Classement du serveur", icon=guild.icon_url)
        .set_footer(text="Page 1")
    )

    data = await DB.fetch_leaderboard(guild.id)
    data = list(sorted(data, reverse=True, key=lambda e: e['guilds'][0]['xp']))
    data = {
        entry["_id"]: entry["guilds"][0] | {"pos": i + 1}
        for i, entry in enumerate(data[:10])
    }

    for field in get_page(guild, data):
        embed.add_field(name=field[0], value=field[1], inline=True)

    resp = await ctx.respond(embed=embed)
    message = await resp.message()

    for emoji in ["◀️", "▶️"]:
        await message.add_reaction(emoji)


@plugin.listener(hk.GuildReactionAddEvent)
async def on_reaction_add(event):
    guild, member = plugin.bot.cache.get_guild(event.guild_id), event.member
    if member.is_bot:
        return

    message = plugin.bot.cache.get_message(event.message_id)
    if (
        not message
        or not message.embeds
        or not message.embeds[0].author
        or message.embeds[0].author.name != "Classement du serveur"
    ):
        return

    data = await DB.fetch_leaderboard(guild.id)
    data = list(sorted(data, reverse=True, key=lambda e: e['guilds'][0]['xp']))

    embed, total = message.embeds[0], len(data) // 10 + (len(data) % 10 > 0)
    inc = -1 if event.emoji_name == "◀️" else 1
    page = (int(embed.footer.text.split()[-1]) + inc) % total or total

    a, b = (1, 10) if page == 1 else (page * 10 - 9, page * 10)
    data = {
        entry["_id"]: entry["guilds"][0] | {"pos": i + a}
        for i, entry in enumerate(data[a-1:b])
    }

    for i, field in enumerate(get_page(guild, data)):
        embed.edit_field(i, field[0], field[1])

    embed.set_footer(text=f"Page {page}")

    await message.edit(embed=embed)
    await message.remove_reaction(event.emoji_name, user=member.id)


@plugin.listener(hk.GuildMessageCreateEvent)
async def on_message(event):
    guild, member = plugin.bot.cache.get_guild(event.guild_id), event.member

    if not guild or not member or member.is_bot or member.id == 689154823941390507:
        return

    if not cd.update_cooldown(member, guild):
        return

    entry = await DB.fetch_member(guild.id, member.id)
    if not entry:
        return

    xp, lvl = entry["guilds"][0]["xp"], entry["guilds"][0]["level"] + 1
    next_lvl = 5 / 6 * lvl * (2 * lvl ** 2 + 27 * lvl + 91)

    await DB.update_member_xp(guild.id, member.id, xp, next_lvl)

    if next_lvl > xp:
        return

    settings = await DB.fetch_settings(guild.id)
    if settings["announceChannelId"] is None:
        return

    channel = guild.get_channel(settings["announceChannelId"])
    if channel:
        embed = hk.Embed(color=0xF1C40F, description=f"🆙 {event.message.author.mention} vient de monter niveau **{lvl}**.")
        await channel.send(embed=embed)


def load(bot):
    bot.add_plugin(plugin)
