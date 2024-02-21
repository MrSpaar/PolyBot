import hikari as hk
import lightbulb as lb

from time import mktime
from core.funcs import now
from core.db import DB


plugin = lb.Plugin("Logs")


async def get_audit_log(guild, event):
    entries = await plugin.bot.rest.fetch_audit_log(guild, event_type=event)
    log_id = list(entries[0].entries.keys())[0]
    return entries[0].entries[log_id]


async def send_log(guild: hk.Guild, embeds: list[hk.Embed], attachments: list[hk.Attachment] = ()) -> dict:
    settings = await DB.fetch_settings(guild.id)
    channel = guild.get_channel(settings["logsChannelId"])

    if channel:
        await channel.send(embeds=embeds, attachments=attachments)

    return settings


@plugin.listener(hk.events.MemberCreateEvent)
async def on_member_join(event):
    guild, member = event.get_guild(), event.member

    embed = hk.Embed(color=0x2ECC71, description=f":inbox_tray: {member.mention} a rejoint le serveur !")
    settings = await send_log(guild, [embed])

    if settings["welcomeChannelId"]:
        channel = guild.get_channel(settings["welcomeChannelId"])
        message = settings["welcomeText"].replace("<mention>", member.mention)

        await channel.send(message)

    if settings["newcomerRoleId"]:
        role = guild.get_role(settings["newcomerRoleId"])
        await member.add_role(role)


@plugin.listener(hk.events.MemberDeleteEvent)
async def on_member_remove(event):
    if not event.old_member:
        return

    guild, member = event.get_guild(), event.old_member
    name = f"{member.display_name} ({member})" if member.display_name else str(member)

    embed = hk.Embed(color=0xE74C3C, description=f":outbox_tray: {name} a quitté le serveur")
    await send_log(guild, [embed])


@plugin.listener(hk.events.BanCreateEvent)
async def on_member_ban(event):
    guild, target = event.get_guild(), event.user

    try:
        entry = await get_audit_log(guild, hk.AuditLogEventType.MEMBER_BAN_ADD)
    except Exception:
        return

    reason, user = entry.reason or 'Pas de raison', guild.get_member(entry.user_id)

    embed = hk.Embed(color=0xE74C3C, description=f"👨‍⚖️ {user.mention} a ban {target.mention}\n❔ Raison : {reason}")
    await send_log(guild, [embed])


@plugin.listener(hk.events.BanDeleteEvent)
async def on_member_unban(event):
    guild, target = event.get_guild(), event.user

    try:
        entry = await get_audit_log(guild, hk.AuditLogEventType.MEMBER_BAN_REMOVE)
    except Exception:
        return

    reason, user = entry.reason or 'Pas de raison', guild.get_member(entry.user_id)

    embed = hk.Embed(color=0xC27C0E, description=f"👨‍⚖️ {user.mention} a unban {target.mention}\n❔ Raison : {reason}")
    await send_log(guild, [embed])


@plugin.listener(hk.events.MemberUpdateEvent)
async def on_nickname_update(event):
    guild = event.get_guild()
    before, after = event.old_member, event.member

    if not before or not after or before.display_name == after.display_name:
        return

    try:
        entry = await get_audit_log(guild, hk.AuditLogEventType.MEMBER_UPDATE)
    except Exception:
        return

    embed = hk.Embed(color=0x3498DB)
    member = guild.get_member(entry.user_id)
    summary = f"(`{before.display_name}` → `{after.display_name}`)"

    if after == member:
        embed.description = f"📝 {member.mention} a changé son surnom {summary}"
    else:
        embed.description = f"📝 {member.mention} a changé le surnom de {before.mention} {summary})"

    await send_log(guild, [embed])


@plugin.listener(hk.events.MemberUpdateEvent)
async def on_role_update(event):
    guild = event.get_guild()
    before, after = event.old_member, event.member

    if not before or not after:
        return

    aroles = after.get_roles()
    broles = before.get_roles()

    if aroles == broles:
        return

    try:
        entry = await get_audit_log(guild, hk.AuditLogEventType.MEMBER_ROLE_UPDATE)
    except Exception:
        return

    embed = hk.Embed(color=0x3498DB)

    member = guild.get_member(entry.user_id)
    role, = set(broles) ^ set(aroles)
    word = 'ajouté' if role in aroles else 'retiré'

    if after == member:
        embed.description = f"📝 {member.mention} s'est {word} {role.mention}"
    else:
        embed.description = f"📝 {member.mention} à {word} {role.mention} à {before.mention}"

    await send_log(guild, [embed])


@plugin.listener(hk.events.GuildMessageDeleteEvent)
async def on_message_delete(event):
    if not event.old_message:
        return

    guild = event.get_guild()
    if not guild:
        return

    channel = event.get_channel()
    message = event.old_message

    if (
        message.author.is_bot
        or "test" in channel.name
        or (message.content and len(message.content) == 1)
    ):
        return

    date = message.timestamp.replace(tzinfo=None)
    mentions = tuple(message.mentions.users.keys()) + message.mentions.role_ids

    attachments = {"images": [], "other": []}
    for attachment in message.attachments:
        (attachments["other"], attachments["images"])[
            attachment.media_type.split("/")[0] == "image"
        ].append(attachment)

    if (now(utc=True) - date).total_seconds() <= 20 and mentions and message.content:
        emoji, color = "<:ping:768097026402942976>", 0xE74C3C
    elif message.content and not message.attachments:
        emoji, color = "🗑️", 0x979C9F
    else:
        emoji, color = "🗑️", 0xF1C40F

    embeds = [
        hk.Embed(color=color, description=f"{emoji} Message de {message.author.mention} supprimé dans {channel.mention}")
    ]

    if message.content:
        embeds[0].description += f"\n\n> {message.content}"

    if attachments["images"]:
        embeds[0].set_image(attachments["images"][0])
        embeds.extend([
            (hk.Embed(color=0xF1C40F).set_image(image))
            for image in attachments["images"][1:]
        ])

    await send_log(guild, embeds, attachments["other"])


@plugin.listener(hk.events.InviteCreateEvent)
async def on_invite_create(event):
    invite, guild = event.invite, event.get_guild()

    if not invite.inviter:
        return

    url = f"https://discord.gg/{invite.code}"
    uses = "utilisable " + (f"{invite.max_uses} fois" if invite.max_uses else "à l'infini")
    inviter = invite.inviter.mention
    expire = (
        f"expire <t:{int(mktime((now() + invite.max_age).timetuple()))}:R>"
        if invite.max_age else "n'expire jamais"
    )

    embed = hk.Embed(color=0x3498DB, description=f"✉️ {inviter} a créé une [invitation]({url}) qui {expire}, {uses}")
    await send_log(guild, [embed])


def load(bot):
    bot.add_plugin(plugin)
