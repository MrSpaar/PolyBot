from discord import Embed, Status, Member, Role, TextChannel
from discord.ext.commands import Context
from discord.ext import commands

from core.cls import Bot
from time import mktime


class Informations(commands.Cog, description='admin'):
    def __init__(self, bot: Bot):
        self.bot = bot

    @commands.command(
        brief='',
        usage='',
        description='Afficher des informations à propos du serveur'
    )
    @commands.guild_only()
    @commands.has_permissions(manage_messages=True)
    async def serverinfo(self, ctx: Context):
        guild = ctx.guild
        channels = f'{len(guild.text_channels)} textuels et {len(guild.voice_channels)} vocaux'
        creation = int(mktime(guild.created_at.timetuple()))

        bots = [member for member in guild.members if member.bot]
        online = len([1 for member in guild.members if member.status != Status.offline])

        embed = (Embed(description=guild.description if guild.description else '', color=0x546e7a)
                 .add_field(name='Membres', value=f'```{guild.member_count - len(bots)} ({online} en ligne)```')
                 .add_field(name='Bots', value=f'```{len(bots)}```')
                 .add_field(name='Création', value=f"<t:{creation}:D> (<t:{creation}:R>)", inline=False)
                 .add_field(name='Owner', value=f'```{guild.owner.display_name}```')
                 .add_field(name='Région', value=f'```{str(guild.region).title()}```')
                 .add_field(name='ID', value=f'```{guild.id}```', inline=False)
                 .add_field(name='Roles', value=f'```{len(guild.roles)}```')
                 .add_field(name='Channels', value=f'```{channels}```')
                 .set_author(name=f'Informations de serveur', icon_url=guild.icon_url))

        await ctx.send(embed=embed)

    @commands.command(
        brief='@Julien Pistre',
        usage='<membre>',
        description="Afficher des informations à propos du serveur d'un membre"
    )
    @commands.guild_only()
    @commands.has_permissions(manage_messages=True)
    async def userinfo(self, ctx: Context, member: Member = None):
        member = member or ctx.author
        activity = getattr(member.activity, 'name', 'Rien')

        since = int(mktime(member.joined_at.timetuple()))
        creation = int(mktime(member.created_at.timetuple()))

        status = {
            'online': 'En ligne',
            'offline': 'Hors ligne',
            'invisible': 'Invisible',
            'idle': 'Absent',
            'dnd': 'Ne pas déranger'
        }

        embed = (Embed(color=0x1abc9c)
                 .add_field(name='Pseudo', value=f'```{member}```')
                 .add_field(name='Surnom', value=f'```{member.display_name}```')
                 .add_field(name='Activité en cours', value=f'```{status[str(member.status)]} - {activity}```', inline=False)
                 .add_field(name='Membre depuis', value=f'<t:{since}:R>')
                 .add_field(name='Création du compte', value=f'<t:{creation}:R>')
                 .add_field(name='Role principal', value=f'```{member.top_role}```', inline=False)
                 .set_author(name="Informations de membre", icon_url=member.avatar_url))

        flags = [str(f)[10:].replace('_', ' ').title() for f in member.public_flags.all()]
        if flags:
            flags = ', '.join(flags) if flags else 'Pas de flags'
            embed.add_field(name='Flags', value=f"```{flags}```")

        if member.premium_since:
            since = member.premium_since.strftime("%d/%m/%Y")
            embed.add_field(name='📈 Booste depuis', value=f'```{since}```', inline=True)

        await ctx.send(embed=embed)

    @commands.command(
        brief='@Modo',
        usage='<role>',
        description="Afficher des informations à propos du serveur d'un rôle"
    )
    @commands.guild_only()
    @commands.has_permissions(manage_messages=True)
    async def roleinfo(self, ctx: Context, role: Role):
        since = role.created_at.strftime("%d/%m/%Y")
        perms = role.permissions
        perms = {
            'Administrateur': perms.administrator,
            'Gérer le serveur': perms.manage_guild,
            'Modifier les permissions': perms.manage_permissions,
            'Modifier les channels': perms.manage_channels,
            'Modifier les rôles': perms.manage_roles,
            'Voir les logs': perms.view_audit_log,
            'Bannir des membres': perms.ban_members,
            'Expulser des membres': perms.kick_members,
            'Gérer les messages': perms.manage_messages,
            'Gérer les emojis': perms.manage_emojis,
            'Gérer les pseudos': perms.manage_nicknames,
            'Muter les autres': perms.mute_members,
            'Bouger les autres': perms.move_members,
            'Ajouter des réactions': perms.add_reactions,
            'Changer de nom': perms.change_nickname,
            'Mentionner everyone': perms.mention_everyone,
            'Envoyer des TTS': perms.send_tts_messages,
            'Envoyer des messages': perms.send_messages,
            'Lire les historiques': perms.read_message_history,
            'Lire les messages': perms.read_messages,
            'Parler': perms.speak,
            'Streamer': perms.stream,
            'Se connecter': perms.connect,
        }

        result = ''
        for key, value in perms.items():
            result += f'✅ {key}\n' if value else f'❌ {key}\n'

        embed = (Embed(color=role.color)
                 .add_field(name='Nom', value=f'```{role}```')
                 .add_field(name='ID', value=f'```{role.id}```')
                 .add_field(name='Permissions', value=f'```{result}```', inline=False)
                 .add_field(name='Membres', value=f'```{len(role.members)}```')
                 .add_field(name='Position', value=f'```{role.position}```')
                 .add_field(name='Création', value=f'```{since}```')
                 .set_author(name=f"Informations de rôle", icon_url=ctx.guild.icon_url))
        
        await ctx.send(embed=embed)

    @commands.command(
        brief='#-general',
        usage='<channel>',
        description="Afficher des informations à propos d'un channel"
    )
    @commands.guild_only()
    @commands.has_permissions(manage_messages=True)
    async def channelinfo(self, ctx: Context, channel: TextChannel = None):
        channel = channel or ctx.channel
        category = channel.category if channel.category else "Pas de catégorie"

        bots = len([1 for member in channel.members if member.bot])
        members = len(channel.members) - bots

        embed = (Embed(color=0x3498db)
                 .add_field(name='Nom', value=f'```#{channel}```')
                 .add_field(name='ID', value=f'```{channel.id}```')
                 .add_field(name='Membres', value=f'```{members} membres et {bots} bots```', inline=False)
                 .add_field(name='Position', value=f'```{channel.position}```')
                 .add_field(name='Catégorie', value=f'```{category}```')
                 .set_author(name=f"Informations de channel", icon_url=ctx.guild.icon_url))
        
        await ctx.send(embed=embed)

    @commands.command(
        aliases=['lj'],
        brief='5',
        usage="<nombre d'entrées>",
        description='Afficher les nouveaux membres les plus récents'
    )
    @commands.guild_only()
    @commands.has_permissions(manage_messages=True)
    async def lastjoins(self, ctx: Context, x: int = 10):
        members = filter(lambda m: not m.bot, sorted(ctx.guild.members, key=lambda m: m.joined_at, reverse=True))
        members = [f'`{member.name}` : {str(member.joined_at)[:10]} ({member.id})'.replace('-', '/') for member in members]
        string = '\n'.join(members[:x])

        embed = (Embed(description=string, color=0x2ecc71)
                 .set_author(name=f'Derniers {x} nouveaux membres', icon_url=ctx.guild.icon_url))

        await ctx.send(embed=embed)


def setup(bot):
    bot.add_cog(Informations(bot))
