from discord import Embed, Member, Role, PermissionOverwrite, VoiceState
from discord.ext.commands import Greedy, Context
from discord.ext import commands
from discord.utils import get

from core.cls import Bot
from core.tools import vc_check
from typing import Union


class TempChannelCommands(commands.Cog, name='Vocaux', description='commands'):
    def __init__(self, bot: Bot):
        self.bot = bot

    @commands.group(
        brief='owner @Alexandre Humber',
        usage='<sous commande> <sous arguments>',
        description='Commandes liées aux channels temporaires'
    )
    @commands.guild_only()
    async def voc(self, ctx: Context):
        if ctx.invoked_subcommand is None:
            embed = Embed(color=0xe74c3c, description='❌ Sous commande inconnue : `rename` `owner`')
            await ctx.send(embed=embed)

    @voc.command(
        brief='Mdrr',
        usage='<nouveau nom>',
        description='Modifier le nom de son channel'
    )
    @vc_check()
    @commands.guild_only()
    async def rename(self, ctx: Context, *, name: str):
        entry = await ctx.bot.db.pending.find({'guild_id': ctx.guild.id, 'voc_id': ctx.author.voice.channel.id})

        voc = get(ctx.guild.voice_channels, id=entry['voc_id'])
        text = get(ctx.guild.text_channels, id=entry['txt_id'])
        await voc.edit(name=name)
        await text.edit(name=name)

        embed = Embed(color=0x2ecc71, description='✅ Nom modifié')
        await ctx.send(embed=embed)

    @voc.command(
        brief='@Noah Haenel',
        usage='<membre>',
        description='Définir le propriétaire du channel'
    )
    @vc_check()
    @commands.guild_only()
    async def owner(self, ctx: Context, member: Member):
        entry = await ctx.bot.db.pending.find({'guild_id': ctx.guild.id, 'voc_id': ctx.author.voice.channel.id})
        await self.bot.db.pending.update(entry, {'$set': {'owner': member.id}})

        embed = Embed(color=0x2ecc71, description='✅ Owner modifié')
        await ctx.send(embed=embed)

    @voc.command(
        brief='@Alexandre Humbert @Noah Haenel',
        usage='<membres et/ou rôles>',
        description='Rendre le channel privé'
    )
    @vc_check()
    @commands.guild_only()
    async def private(self, ctx: Context, entries: Greedy[Union[Role, Member]] = None):
        entry = await ctx.bot.db.pending.find({'guild_id': ctx.guild.id, 'voc_id': ctx.author.voice.channel.id})

        channel = get(ctx.guild.voice_channels, id=entry['voc_id'])
        text = get(ctx.guild.text_channels, id=entry['txt_id'])
        base = channel.members if ctx.author in channel.members else [ctx.author] + channel.members

        if entries:
            entries = base+entries if isinstance(entries, list) else list(entries)+base

        overwrites = {entry: PermissionOverwrite(view_channel=True, read_messages=True, connect=True,
                                                 send_messages=True, speak=True, embed_links=True,
                                                 use_external_emojis=True, stream=True, add_reactions=True,
                                                 attach_files=True, read_message_history=True) for entry in entries}
        overwrites |= {ctx.guild.default_role: PermissionOverwrite(view_channel=False),
                       ctx.me: PermissionOverwrite(view_channel=True, read_messages=True, connect=True,
                                                   send_messages=True, speak=True, manage_permissions=True,
                                                   manage_messages=True, manage_channels=True, embed_links=True,
                                                   use_external_emojis=True, stream=True, add_reactions=True,
                                                   attach_files=True, read_message_history=True)}

        await text.edit(overwrites=overwrites)
        await channel.edit(overwrites=overwrites)

        embed = Embed(color=0x2ecc71, description='✅ Permissions modifiées')
        await ctx.send(embed=embed)

    @commands.Cog.listener()
    async def on_voice_state_update(self, member: Member, before: VoiceState, after: VoiceState):
        entry = await self.bot.db.pending.find({'guild_id': member.guild.id, 'owner': member.id})

        if after.channel and 'Créer' in after.channel.name and not member.bot and not entry:
            if cat := after.channel.category:
                text = await member.guild.create_text_channel(name=f'Salon-de-{member.display_name}', category=cat, overwrites=after.channel.overwrites)
                channel = await member.guild.create_voice_channel(name=f'Salon de {member.display_name}', category=cat, overwrites=after.channel.overwrites)

                try:
                    await member.move_to(channel)
                    await self.bot.db.pending.insert({'guild_id': member.guild.id, 'owner': member.id, 'voc_id': channel.id, 'txt_id': text.id})
                except:
                    await channel.delete()
                    await text.delete()
            return

        if before.channel:
            entry = await self.bot.db.pending.find({'guild_id': member.guild.id, 'voc_id': before.channel.id})
            if entry and ((len(before.channel.members) <= 1 and member.guild.me in before.channel.members) or not len(before.channel.members)):
                await before.channel.delete()
                channel = get(member.guild.text_channels, id=entry['txt_id'])
                await channel.delete()
                await self.bot.db.pending.delete(entry)


def setup(bot):
    bot.add_cog(TempChannelCommands(bot))
