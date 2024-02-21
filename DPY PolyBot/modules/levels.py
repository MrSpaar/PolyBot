from discord import Member, Embed, Reaction, Message
from discord.ext.commands import Context
from discord.ext import commands
from discord.utils import get

from core.cls import Bot
from random import randint


class Levels(commands.Cog, name='Niveaux', description='commands'):
    def __init__(self, bot: Bot):
        self.bot = bot
        self.cd = commands.CooldownMapping.from_cooldown(1, 60, commands.BucketType.user)

    @staticmethod
    def get_progress_bar(level: int, xp: int, n: int, short: bool = False):
        needed = 5 * ((level - 1) ** 2) + (50 * (level - 1)) + 100
        progress = needed - int(5/6*level * (2*level**2 + 27*level + 91) - xp) if xp else 0
        p = int((progress/needed)*n) or 1

        if short:
            progress = f'{round(progress/1000, 1)}k' if int(progress/1000) else progress
            needed = f'{round(needed/1000, 1)}k' if int(needed/1000) else needed

        return f"{'🟩'*p}{'⬛' * (n-p)} {progress} / {needed}"

    @staticmethod
    def get_page(members: list[Member], entries: dict):
        field1, field2, field3 = '', '', ''

        for id, entry in entries.items():
            member = get(members, id=id)
            level, xp = entry['level'], entry['xp']

            bar = Levels.get_progress_bar(level + 1, xp, 5, True)
            xp = f'{round(xp / 1000, 1)}k' if int(xp / 1000) else xp

            field1 += f"**{entry['pos']}.** {member.display_name}\n"
            field2 += f'{level} ({xp})\n'
            field3 += f'{bar}\n'

        return ('Noms', field1), ('Niveau', field2), ('Progrès', field3)

    @commands.command(
        brief='@Julien Pistre',
        usage='<membre (optionnel)>',
        description='Afficher sa progression'
    )
    @commands.guild_only()
    async def rank(self, ctx: Context, member: Member = None):
        member = member or ctx.author
        data = await self.bot.db.members.sort({'guilds.id':ctx.guild.id}, {'guilds.$': 1}, 'guilds.xp', -1)
        data = {entry['_id']: entry['guilds'][0] | {'pos': i+1} for i, entry in enumerate(data)}

        embed = (Embed(color=0x3498db)
                 .set_author(name=f'Progression de {member.display_name}',
                             icon_url=member.avatar_url))

        xp, lvl = data[member.id]['xp'], data[member.id]['level'] + 1
        bar = self.get_progress_bar(lvl, xp, 13)

        embed.add_field(name=f"Niveau {lvl-1} • Rang {data[member.id]['pos']}", value=bar)
        await ctx.send(embed=embed)

    @commands.command(
        brief='',
        usage='',
        description='Afficher le classement du serveur'
    )
    @commands.guild_only()
    async def levels(self, ctx: Context):
        embed = (Embed(color=0x3498db)
                 .set_author(name='Classement du serveur', icon_url=ctx.guild.icon_url)
                 .set_footer(text='Page 1'))

        data = await self.bot.db.members.sort({'guilds.id':752921557214429316}, {'guilds.$': 1}, 'guilds.xp', -1)
        data = {entry['_id']: entry['guilds'][0] | {'pos': i+1} for i, entry in enumerate(data[:10])}
        for field in self.get_page(ctx.guild.members, data):
            embed.add_field(name=field[0], value=field[1])

        message = await ctx.send(embed=embed)
        for emoji in ['◀️', '▶️']:
            await message.add_reaction(emoji)

    @commands.Cog.listener()
    async def on_reaction_add(self, reaction: Reaction, member: Member):
        if member.bot:
            return

        message, emoji = reaction.message, str(reaction.emoji)
        if not message.embeds or message.embeds[0].author.name != 'Classement du serveur':
            return

        data = await self.bot.db.members.sort({'guilds.id':752921557214429316}, {'guilds.$': 1}, 'guilds.xp', -1)

        embed, total = message.embeds[0], len(data)//10 + (len(data) % 10 > 0)
        page = (int(embed.footer.text.split()[-1]) + (-1 if emoji == '◀️' else 1)) % total or total

        a, b = (1, 10) if page == 1 else (page*10 - 9, page*10)
        data = {entry['_id']: entry['guilds'][0] | {'pos': i+a} for i, entry in enumerate(data[a-1:b])}

        for i, field in enumerate(self.get_page(member.guild.members, data)):
            embed.set_field_at(i, name=field[0], value=field[1])

        embed.set_footer(text=f'Page {page}')

        await message.edit(embed=embed)
        await reaction.remove(member)

    @commands.Cog.listener()
    async def on_message(self, message: Message):
        if not message.guild or message.channel.id in [840555556707237928, 853630887794311178] or \
        message.author.bot or message.author.id == 689154823941390507 or message.content.startswith(self.bot.command_prefix):
            return

        bucket = self.cd.get_bucket(message)
        if bucket.update_rate_limit():
            return

        member = await self.bot.db.members.find({'guilds.id': message.guild.id, '_id': message.author.id}, {'guilds.$': 1})
        if not member:
            return

        xp, lvl = member['guilds'][0]['xp'], member['guilds'][0]['level'] + 1
        next_lvl = 5 / 6 * lvl * (2 * lvl ** 2 + 27 * lvl + 91)

        await self.bot.db.members.update({'guilds.id': message.guild.id, '_id': message.author.id},
                                         {'$inc': {'guilds.$.xp': randint(15, 25), 'guilds.$.level': 1 if xp >= next_lvl else 0}})

        if xp >= next_lvl:
            settings = await self.bot.db.setup.find({'_id': message.guild.id})
            channel = get(message.guild.text_channels, id=settings['channel'])

            if not channel:
                return

            embed = Embed(description=f'🆙 {message.author.mention} vient de monter niveau **{lvl}**.', color=0xf1c40f)
            await channel.send(embed=embed)


def setup(bot):
    bot.add_cog(Levels(bot))
