from discord import Role, TextChannel, Embed, Guild, Member
from discord.ext.commands import Context
from discord.ext import commands
from discord.utils import get

from typing import Union
from core.cls import Bot
from os import listdir


class SetupCommands(commands.Cog, name='Configuration', description='admin'):
    def __init__(self, bot: Bot):
        self.bot = bot

    @commands.command(
        name='set',
        brief='channel #🧙-polybot',
        usage='<mute, logs ou channel> <@role ou #channel>',
        description='Modifier les paramètres du bot'
    )
    @commands.guild_only()
    @commands.has_permissions(administrator=True)
    async def _set(self, ctx: Context, key: str, value: Union[Role, TextChannel], *, message=None):
        settings = {
            'mute': 'Rôle des muets',
            'logs': 'Channel de logs',
            'channel': 'Channel du bot',
            'role': 'Rôle des nouveaux membres',
            'welcome': 'Message de bienvenu'
        }

        if key not in settings:
            embed = Embed(color=0xe74c3c, description=f"❌ Catégorie invalide : {', '.join(settings.keys())}")
            return await ctx.send(embed=embed)

        if key == 'welcome':
            await self.bot.db.setup.update({'_id': ctx.guild.id}, {'$set': {key: {'id': value.id, 'txt': message}}})
        else:
            await self.bot.db.setup.update({'_id': ctx.guild.id}, {'$set': {key: value.id}})

        embed = Embed(color=0x2ecc71, description=f"{settings[key]} modifié ({value.mention})")
        await ctx.send(embed=embed)

    @commands.command(
        brief='',
        usage='',
        description='Afficher les paramètres du bot'
    )
    @commands.guild_only()
    @commands.has_permissions(administrator=True)
    async def settings(self, ctx: Context):
        settings = await self.bot.db.setup.find({'_id': ctx.guild.id})

        channel = getattr(get(ctx.guild.text_channels, id=settings['channel']), 'mention', 'pas défini')
        logs = getattr(get(ctx.guild.text_channels, id=settings['logs']), 'mention', 'pas défini')
        mute = getattr(get(ctx.guild.roles, id=settings['mute']), 'mention', 'pas défini')

        embed = Embed(color=0x3498db, description=f"💬 Bot : {channel}\n📟 Logs : {logs}\n🔇 Mute : {mute}")
        await ctx.send(embed=embed)

    @commands.command()
    @commands.is_owner()
    async def reload(self, ctx: Context):
        for file in listdir('modules'):
            if file != '__pycache__' and not (file in ['errors.py', 'logs.py'] and self.bot.debug):
                self.bot.reload_extension(f'modules.{file[:-3]}')

        embed = Embed(color=0x2ecc71, description='✅ Tous les modules ont été relancé')
        await ctx.send(embed=embed)

    @commands.Cog.listener()
    async def on_guild_join(self, guild):
        await self.bot.db.setup.insert({'_id': guild.id, 'mute': None, 'logs': None, 'channel': None, 'new': []})
        for member in filter(lambda m: not m.bot, guild.members):
            await self.bot.db.members.update({'_id': member.id}, {'$addToSet': {'guilds': {'id': guild.id, 'level': 0, 'xp':0}}}, True)

        await guild.owner.send("Merci beaucoup de m'avoir ajouté 👍" +
                               "\n\nPour certaines de mes commandes, quelques réglages sont nécessaires :" +
                               "\n    • `!set channel <#channel>` pour indiquer au bot ou faire les annonces de level up" +
                               "\n    • `!set logs <#channel>` pour indiquer au bot où envoyer les messages de logs" +
                               "\n\nCes **commandes sont à faire sur ton serveur**, pas ici, en privé ⚠️")

    @commands.Cog.listener()
    async def on_guild_remove(self, guild: Guild):
        await self.bot.db.setup.delete({'_id': guild.id})
        await self.bot.db.members.collection.update_many({'_id': {'$in': [member.id for member in guild.members]}}, {'$pull': {'guilds': {'id': guild.id}}})

    @commands.Cog.listener()
    async def on_member_join(self, member: Member):
        await self.bot.db.members.update({'_id': member.id}, {'$addToSet': {'guilds': {'id': member.guild.id, 'level': 0, 'xp': 0}}}, True)

    @commands.Cog.listener()
    async def on_member_remove(self, member: Member):
        await self.bot.db.members.update({'_id': member.id}, {'$pull': {'guilds': {'id': member.guild.id}}})


def setup(bot):
    bot.add_cog(SetupCommands(bot))
