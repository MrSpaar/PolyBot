import hikari as hk
import lightbulb as lb

from time import mktime

plugin = lb.Plugin("Informations")
plugin.add_checks(lb.guild_only)


@plugin.command()
@lb.command("info", "Groupes de commandes en rapport avec les informations")
@lb.implements(lb.SlashCommandGroup)
async def info(_):
    pass


@info.child
@lb.command("server", "Afficher des informations à propos du serveur")
@lb.implements(lb.SlashSubCommand)
async def serverinfo(ctx: lb.Context):
    guild = ctx.get_guild()
    channels = guild.get_channels().values()

    text = len(list(filter(lambda c: isinstance(c, hk.GuildTextChannel), channels)))
    voice = len(channels) - text
    emojis = [emoji.mention for emoji in guild.get_emojis().values()]

    creation = int(mktime(guild.created_at.timetuple()))
    owner = guild.get_member(guild.owner_id)

    members = guild.get_members().values()
    bots = [member for member in members if member.is_bot]

    description = (
        (f"{guild.description}\n\n" if guild.description else "")
        + f"🙍 {guild.member_count} membres dont {len(bots)} bots\n"
        + f"📈 Nitro niveau {guild.premium_tier.value} avec {guild.premium_subscription_count} boosts\n"
        + f"📝 {len(guild.get_roles())} roles et {len(channels)} salons ({text} textuels et {voice} vocaux)\n"
        + f"🔐 Géré par {owner.mention} et créé le <t:{creation}:D>\n"
        + f"\nEmotes du serveur : {''.join(emojis)}" if emojis else ""
    )

    embed = hk.Embed(description=description, color=0x546E7A)
    embed.set_author(name=f"{guild.name} - {guild.id}", icon=guild.icon_url)

    await ctx.respond(embed=embed)


@info.child
@lb.option("membre", "L'utilisateur dont tu veux voir les informations", hk.Member, default=None)
@lb.command("user", "Afficher des informations à propos du serveur d'un membre")
@lb.implements(lb.SlashSubCommand)
async def userinfo(ctx: lb.Context):
    member = ctx.options.membre or ctx.options.target or ctx.member

    activities = {
        0: "En train de jouer à `{0.name}`",
        1: "Est en train de [stream]({0.url})",
        2: "En train d'écouter `{0.details}` de `{0.state}`",
    }

    status = {
        "online": "En ligne",
        "offline": "Hors ligne",
        "invisible": "Invisible",
        "idle": "Absent",
        "dnd": "Ne pas déranger",
    }

    if presence := member.get_presence():
        status = status[presence.visible_status]

        activities = [
            activities[activity.type].format(activity)
            for activity in presence.activities
            if activity.type in activities
        ]
    else:
        status = "Hors ligne"
        activities = None

    flags = [flag.name.replace("_", " ").title() for flag in member.flags]

    since = int(mktime(member.joined_at.timetuple()))
    creation = int(mktime(member.created_at.timetuple()))
    boost = int(mktime(member.premium_since.timetuple())) if member.premium_since else None

    description = (
        f"⏱️ A rejoint <t:{since}:R>\n"
        + f"📝 A créé son compte <t:{creation}:R>\n"
        + f"💳 Surnom : `{member.display_name}`\n"
        + f"🏷️ Rôle principal : {member.get_top_role().mention}\n"
        + (f"🚩 Flags : {', '.join(flags)}\n" if flags else "")
        + (f"📈 A commencé à booster le serveur <t:{boost}:R>\n\n" if boost else "")
        + ("\n🏃‍♂️ Activités :\n- " + "\n- ".join(activities) if activities else "")
    )

    embed = hk.Embed(color=0x1ABC9C, description=description)
    embed.set_author(name=f"{member} - {status}", icon=member.avatar_url)

    await ctx.respond(embed=embed)


@plugin.command()
@lb.command("Info", "Afficher des informations à propos du serveur d'un membre")
@lb.implements(lb.UserCommand)
async def _userinfo(ctx: lb.Context):
    await userinfo(ctx)


@info.child
@lb.option("role", "Le rôle dont tu veux voir les informations", hk.Role)
@lb.command("role", "Afficher des informations à propos du serveur d'un rôle")
@lb.implements(lb.SlashSubCommand)
async def roleinfo(ctx: lb.Context):
    role = ctx.options.role
    since = int(mktime(role.created_at.timetuple()))
    guild = ctx.get_guild()

    perms = {
        hk.Permissions.ADMINISTRATOR: "Administrateur",
        hk.Permissions.MANAGE_GUILD: "Gérer le serveur",
        hk.Permissions.MANAGE_CHANNELS: "Gérer les salons",
        hk.Permissions.MANAGE_NICKNAMES: "Gérer les pseudos",
        hk.Permissions.MANAGE_THREADS: "Gérer les fils",
        hk.Permissions.START_EMBEDDED_ACTIVITIES: "Gérer les évènements",
        hk.Permissions.MANAGE_WEBHOOKS: "Gérer les webhooks",
        hk.Permissions.MANAGE_MESSAGES: "Gérer les messages",
        hk.Permissions.MANAGE_EMOJIS_AND_STICKERS: "Gérer les emojis et stickers",
        hk.Permissions.MANAGE_ROLES: "Gérer les rôles",
        hk.Permissions.VIEW_AUDIT_LOG: "Voir les logs",
        hk.Permissions.VIEW_GUILD_INSIGHTS: "Voir les analyses du serveur",
        hk.Permissions.USE_VOICE_ACTIVITY: "Voir les activités de voix",
        hk.Permissions.VIEW_CHANNEL: "Voir les salons",
        hk.Permissions.BAN_MEMBERS: "Bannier des membres",
        hk.Permissions.KICK_MEMBERS: "Expulser des membres",
        hk.Permissions.MODERATE_MEMBERS: "Modérer les membres",
        hk.Permissions.MUTE_MEMBERS: "Muter des membres",
        hk.Permissions.MOVE_MEMBERS: "Bouger des membres",
        hk.Permissions.DEAFEN_MEMBERS: "Rendre des membres sourds",
        hk.Permissions.ADD_REACTIONS: "Ajouter des réactions",
        hk.Permissions.CHANGE_NICKNAME: "Changer de pseudo",
        hk.Permissions.CREATE_INSTANT_INVITE: "Créer des invitations",
        hk.Permissions.MENTION_ROLES: "Mentionner everyone et les rôles",
        hk.Permissions.ATTACH_FILES: "Envoyer des fichiers",
        hk.Permissions.SEND_TTS_MESSAGES: "Envoyer des TTS",
        hk.Permissions.EMBED_LINKS: "Envoyer des intégrations",
        hk.Permissions.CREATE_PRIVATE_THREADS: "Créer des threads privés",
        hk.Permissions.CREATE_PUBLIC_THREADS: "Créer des threads publiques",
        hk.Permissions.SEND_MESSAGES_IN_THREADS: "Envoyer des messages dans un thread",
        hk.Permissions.SEND_MESSAGES: "Envoyer des messages",
        hk.Permissions.READ_MESSAGE_HISTORY: "Lire les historiques de messages",
        hk.Permissions.USE_APPLICATION_COMMANDS: "Utiliser les applications",
        hk.Permissions.USE_EXTERNAL_EMOJIS: "Utiliser les emojis externes",
        hk.Permissions.USE_EXTERNAL_STICKERS: "Utiliser les stickers externes",
        hk.Permissions.PRIORITY_SPEAKER: "Parler en prioritaire",
        hk.Permissions.REQUEST_TO_SPEAK: "Demander la parole",
        hk.Permissions.SPEAK: "Parler en vocal",
        hk.Permissions.STREAM: "Faire un stream",
        hk.Permissions.CONNECT: "Se connecter à un vocal",
    }

    perms = [perms[perm] for perm in role.permissions]

    description = (
        f"⏱️ Créé <t:{since}:R>\n"
        + f"🌈 Couleur : `{role.color.hex_code}`\n"
        + f"🔔 {'Mentionnable' if role.is_mentionable else 'Non mentionnable'}{' et affiché séparemment' if role.is_hoisted else ''}\n\n"
        + f"⛔ Permissions :"
        + ("\n- " + "\n- ".join(perms) if perms else " *pas de permissions*")
    )

    embed = hk.Embed(color=role.color, description=description)
    embed.set_author(name=f"{role.name} - {role.id}", icon=guild.icon_url)

    await ctx.respond(embed=embed)


def load(bot):
    bot.add_plugin(plugin)
