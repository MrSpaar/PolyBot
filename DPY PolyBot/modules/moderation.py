from discord import Member, Embed, Permissions, PermissionOverwrite, Guild
from discord.ext.commands import Context
from discord.ext import commands, tasks
from discord.ext.commands.core import command
from discord.utils import get

from core.tools import has_higher_perms, now
from datetime import timedelta
from core.cls import Bot


class Moderation(commands.Cog, name='Modération', description='admin'):
    def __init__(self, bot: Bot):
        self.bot = bot
        if not self.bot.debug:
            self.unmute_loop.start()

    async def fetch_settings(self, guild: Guild):
        settings = await self.bot.db.setup.find({'_id': guild.id})
        role = get(guild.roles, id=settings['mute'])
        logs = get(guild.text_channels, id=settings['logs'])

        if not role:
            role = await guild.create_role(name='Muted', color=0xa6aaab, permissions=Permissions.none())
            await self.bot.db.setup.update({'_id': guild.id}, {'$set': {'mute': role.id}})

            for channel in guild.text_channels:
                overwrite = channel.overwrites | {role:  PermissionOverwrite(add_reactions=False, send_messages=False)}
                await channel.edit(overwrites=overwrite)

        return role, logs

    @commands.command(
        brief='@Antoine Grégoire 10m mdrr',
        usage='<membre> <durée> <raison (optionnel)>',
        description='Rendre un membre muet'
    )
    @has_higher_perms()
    @commands.guild_only()
    @commands.has_permissions(manage_messages=True)
    async def mute(self, ctx: Context, member: Member, time: str = None):
        role, _ = await self.fetch_settings(ctx.guild)
        if role in member.roles:
            embed = Embed(color=0xe74c3c, description=f'❌ {member.mention} est déjà mute')
            return await ctx.send(embed=embed)

        try:
            units = {"s": [1, 'secondes'], "m": [60, 'minutes'], "h": [3600, 'heures']}
            date = now() + timedelta(seconds=(int(time[:-1])*units[time[-1]][0]))
            time = f"{time[:-1]} {units[time[-1]][1]}"
        except:
            date = now() + timedelta(days=1000)
            time = 'indéfiniment'

        try:
            await member.add_roles(role)
            await member.edit(mute=True)
            await self.bot.db.pending.insert({'guild_id': ctx.guild.id, 'id': member.id, 'end': date})

            embed = Embed(color=0x2ecc71, description=f'✅ {member.mention} a été mute {time}')
            await ctx.send(embed=embed)
        except:
            embed = Embed(color=0xe74c3c, description='❌ La cible a plus de permissions que moi')
            await ctx.send(embed=embed)

    @tasks.loop(minutes=1)
    async def unmute_loop(self):
        await self.bot.wait_until_ready()
        entries = await self.bot.db.pending.find({'end': {'$lt': now()}})
        if not entries:
            return

        entries = entries if isinstance(entries, list) else [entries]
        for entry in entries:
            guild = self.bot.get_guild(entry['guild_id'])
            settings = await self.bot.db.setup.find({'_id': entry['guild_id']})

            member = guild.get_member(entry['id'])
            role = get(guild.roles, id=settings['mute'])

            await member.remove_roles(role)
            await self.bot.db.pending.delete(entry)

    @commands.command(
        brief='@Antoine Grégoire',
        usage='<membre>',
        description='Redonner la parole à un membre'
    )
    @has_higher_perms()
    @commands.guild_only()
    @commands.has_permissions(manage_messages=True)
    async def unmute(self, ctx: Context, member: Member):
        role, _ = await self.fetch_settings(ctx.guild)
        if role not in member.roles:
            return await ctx.send(f"❌ {member.mention} n'est pas mute")

        try:
            await member.remove_roles(role)
            await member.edit(mute=False)
            await self.bot.db.pending.delete({'guild_id': ctx.guild.id, 'id': member.id})

            embed = Embed(color=0x2ecc71, description=f'✅ {member.mention} a été unmute')
            await ctx.send(embed=embed)
        except:
            embed = Embed(color=0xe74c3c, description='❌ La cible a plus de permissions que moi')
            await ctx.send(embed=embed)

    @commands.command(
        aliases=['prout'],
        brief='20', usage='<nombre de messages>',
        description='Supprimer plusieurs messages en même temps'
    )
    @commands.guild_only()
    @commands.has_permissions(manage_messages=True)
    async def clear(self, ctx: Context, x: int):
        await ctx.channel.purge(limit=x+1)

    @commands.command(
        brief='@Mee6 Obselète',
        usage='<membre> <raison (optionnel)>',
        description='Exclure un membre du serveur'
    )
    @commands.guild_only()
    @commands.has_permissions(kick_members=True)
    async def kick(self, ctx: Context, member: Member, *, reason: str = 'Pas de raison'):
        embed = Embed(color=0x2ecc71, description=f'✅ {member.mention} a été kick')

        await member.kick(reason=reason)
        await ctx.send(embed=embed)

    @commands.command(
        brief='@Mee6 Obselète',
        usage='<membre> <raison (optionnel)>',
        description='Bannir un membre du serveur'
    )
    @commands.guild_only()
    @commands.has_permissions(ban_members=True)
    async def ban(self, ctx: Context, member: Member, *, reason='Pas de raison'):
        embed = Embed(color=0x2ecc71, description=f'✅ {member.mention} a été ban')

        await member.ban(reason=reason)
        await ctx.send(embed=embed)

    @commands.command(
        brief='@Pierre Karr',
        usage='<membre> <raison (optionnel)>',
        description='Révoquer un bannissement'
    )
    @commands.guild_only()
    @commands.has_permissions(ban_members=True)
    async def unban(self, ctx: Context, user_id: int, *, reason='Pas de raison'):
        try:
            member = self.bot.get_user(user_id)
            await ctx.guild.unban(member, reason=reason)

            embed = Embed(color=0x2ecc71, description=f'✅ {member.mention} a été unban')

            await ctx.send(embed=embed)
        except:
            embed = Embed(color=0xe74c3c, description="❌ L'utilisateur n'est pas banni de ce serveur")
            await ctx.send(embed=embed)


def setup(bot):
    bot.add_cog(Moderation(bot))
