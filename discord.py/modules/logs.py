from discord import Embed, Member, Guild, User, Message, Invite
from discord.ext.commands import Context
from discord.ext import commands
from discord.utils import get

from core.cls import Bot
from core.tools import now
from datetime import timedelta
from time import mktime


class Logs(commands.Cog):
    def __init__(self, bot: Bot):
        self.bot = bot

    async def send_log(self, guild, embed):
        settings = await self.bot.db.setup.find({'_id': guild.id})
        logs = get(guild.text_channels, id=settings['logs'])

        if logs:
            await logs.send(embed=embed)
        return settings

    @commands.Cog.listener()
    async def on_member_join(self, member: Member):
        embed = Embed(color=0x2ecc71, description=f':inbox_tray: {member.mention} a rejoint le serveur !')
        settings = await self.send_log(member.guild, embed)

        if settings['welcome']:
            channel = get(member.guild.text_channels, id=settings['welcome']['id'])
            await channel.send(settings['welcome']['txt'].replace('mention', member.mention))

        if settings['new']:
            role = get(member.guild.roles, id=settings['new'])
            await member.add_roles(role)    

    @commands.Cog.listener()
    async def on_member_remove(self, member: Member):
        embed = Embed(color=0xe74c3c, description=f':outbox_tray: {member.display_name} ({member}) a quitté le serveur')
        await self.send_log(member.guild, embed)

    @commands.Cog.listener()
    async def on_member_unban(self, guild: Guild, user: User):
        entry = await guild.audit_logs(limit=1).flatten()
        embed = Embed(color=0xc27c0e, description=f"👨‍⚖️ {entry[0].user} a unban {user}\n❔ Raison : {entry[0].reason or 'Pas de raison'}")

        await self.send_log(guild, embed)

    @commands.Cog.listener()
    async def on_member_update(self, before: Member, after: Member):
        entry = (await after.guild.audit_logs(limit=1).flatten())[0]
        member = get(before.guild.members, id=entry.user.id)
        embed = Embed(color=0x3498db)

        if before.display_name != after.display_name:
            if after == member:
                embed.description = f"📝 {member.mention} a changé son surnom (`{before.display_name}` → `{after.display_name}`)"
            else:
                embed.description = f"📝 {member.mention} a changé de surnom de {before.mention} (`{before.display_name}` → `{after.display_name}`)"
        elif before.roles != after.roles:
            new = list(filter(lambda r: r not in before.roles, after.roles))
            removed = list(filter(lambda r: r not in after.roles, before.roles))
            role, = new if new else removed

            if after == member:
                embed.description = f"📝 {member.mention} s'est {'ajouté' if new else 'retiré'} {role.mention}"
            else:
                embed.description = f"📝 {member.mention} à {'ajouté' if new else 'retiré'} {role.mention} à {before.mention}"
        else:
            return

        await self.send_log(before.guild, embed)

    @commands.Cog.listener()
    async def on_message_delete(self, message: Message):
        if message.embeds or message.author.bot or 'test' in message.channel.name or len(message.content) == 1 or \
           (len(message.content) in [5, 6, 7] and message.content.count(',') == 2) or not message.guild:
            return

        flags = [
            (now(utc=True)-message.created_at).total_seconds() <= 20 and message.mentions and message.content,
            message.content and not message.attachments,
            message.content or message.attachments
        ]

        infos = [
            {'emoji': '<:ping:768097026402942976>', 'color': 0xe74c3c},
            {'emoji': '🗑️', 'color': 0x979c9f},
            {'emoji': '🗑️', 'color': 0xf1c40f}
        ]

        entry = [infos[i] for i, flag in enumerate(flags) if flag][0]

        embed = Embed(color=entry['color'], description=f'{entry["emoji"]} Message de {message.author.mention} supprimé dans {message.channel.mention}:')

        if message.content:
            embed.description += f'\n\n> {message.content}'
        if message.attachments:
            embed.set_image(url=message.attachments[0].url)

        await self.send_log(message.guild, embed)

    @commands.Cog.listener()
    async def on_guild_join(self, guild: Guild):
        owner = self.bot.get_user(self.bot.owner_id)
        embed = (Embed(description=f"Owner : {guild.owner.mention}\nNom : {guild.name}\nID : `{guild.id}`", color=0x2ecc71)
                 .set_author(name="J'ai rejoint un serveur", icon_url=guild.icon_url))
        await owner.send(embed=embed)

    @commands.Cog.listener()
    async def on_guild_remove(self, guild: Guild):
        owner = self.bot.get_user(self.bot.owner_id)
        embed = (Embed(description=f"Owner : {guild.owner.mention}\nNom : {guild.name}\nID : `{guild.id}`", color=0xe74c3c)
                 .set_author(name="J'ai quitté un serveur", icon_url=guild.icon_url))
        await owner.send(embed=embed)

    @commands.Cog.listener()
    async def on_invite_create(self, invite: Invite):
        uses = f'{invite.max_uses} fois' if invite.max_uses else "à l'infini"
        expire = f'<t:{int(mktime((now() + timedelta(seconds=invite.max_age)).timetuple()))}:R>' if invite.max_age else 'jamais'

        embed = Embed(color=0x3498db, description=f'✉️ {invite.inviter.mention} a créé une invitation qui expire {expire}, utilisable {uses} : {invite.url}')
        await self.send_log(invite.guild, embed)

    @commands.Cog.listener()
    async def on_command_completion(self, ctx: Context):
        print(f'[INFO] {now().strftime("%d/%m/%Y %H:%M:%S")} Commande exécutée sur {ctx.guild.name}')
        print(f'Par {ctx.author} : {ctx.message.clean_content}')


def setup(bot):
    bot.add_cog(Logs(bot))
