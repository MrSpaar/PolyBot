from discord import Role, Member, Embed
from discord_components import Button, ButtonStyle, Select, SelectOption, Interaction
from discord.ext.commands import Context
from discord.ext import commands
from discord.utils import get

from typing import Union
from core.cls import Bot


class Utility(commands.Cog, name='Utilitaire', description='admin'):
    def __init__(self, bot: Bot):
        self.bot = bot

    @commands.command(hidden=True)
    @commands.has_permissions(administrator=True)
    async def verify(self, ctx: Context, member: Member, *, nick):
        if ctx.guild.id == 752921557214429316:
            raise commands.CommandNotFound('Commande inexistante')

        verif = get(ctx.guild.roles, id=878256370363805707)
        role = get(ctx.guild.roles, id=877952434952081419)

        await member.edit(nick=nick)
        await member.remove_roles(verif)
        await member.add_roles(role)

        channel = get(ctx.guild.text_channels, id=879041859765272706)
        await channel.send(f'Bienvenue à {member.mention} ! <:pepeOK:819556642487926784>')

    @commands.command(hidden=True)
    @commands.has_permissions(administrator=True)
    async def edit(self, ctx: Context, *, content):
        if not ctx.message.reference:
            embed = Embed(color=0xe74c3c, description="❌ Tu n'as répondu à aucun message")
            return await ctx.send(embed=embed)

        message = await ctx.channel.fetch_message(ctx.message.reference.message_id)

        if message.author != ctx.me:
            embed = Embed(color=0xe74c3c, description="❌ Je ne peux pas éditer un message que je n'ai pas écrit")
            return await ctx.send(embed=embed)

        await message.edit(content=content)
        await ctx.message.delete()

    @commands.group(
        brief='boutons @CM 1 @CM 2 Groupes de CM',
        usage='<sous commande> <sous arguments>',
        description='Commandes liées aux menus de rôles'
    )
    @commands.guild_only()
    @commands.has_permissions(manage_roles=True)
    async def menu(self, ctx: Context):
        await ctx.message.delete()
        if ctx.invoked_subcommand is None:
            embed = Embed(color=0xe74c3c, description='❌ Sous commande inconnue : `boutons` `liste`')
            await ctx.send(embed=embed)

    @menu.command(
        name='boutons',
        brief='@CM 1 @CM 2 Groupes de CM',
        usage='<rôles> <titre>',
        description='Faire un menu de rôles avec des boutons'
    )
    @commands.guild_only()
    @commands.has_permissions(manage_roles=True)
    async def buttons(self, ctx: Context, roles: commands.Greedy[Role], *, title: str):
        groups = [[role for role in roles[i:i+5]] for i in range(0, len(roles), 5)]
        buttons = [[Button(label=role.name, style=ButtonStyle.green, custom_id=role.id) for role in group] for group in groups]

        await ctx.send(f'Menu de rôles - {title}', components=buttons)

    @menu.command(
        brief='🥫 @Kouizinier 🎮 @Soirées jeux',
        usage='<emojis et rôles> <titre>',
        description='Faire un menu de rôles avec des boutons incluant des emojis'
    )
    @commands.guild_only()
    @commands.has_permissions(manage_roles=True)
    async def emoji(self, ctx: Context, entries: commands.Greedy[Union[Role, str]]):
        groups = [[(emoji, role) for emoji, role in zip(entries[i:i+10:2], entries[i+1:i+10:2])] for i in range(0, len(entries), 10)]
        buttons = [[Button(label=entry[1].name, style=ButtonStyle.green, custom_id=entry[1].id, emoji=entry[0]) for entry in group] for group in groups]

        await ctx.send(f'Menu de rôles', components=buttons)

    @menu.command(
        name='liste',
        brief='@CM 1 @CM 2 Choisis ton CM',
        usage='<rôles> <titre>',
        description='Faire un menu de rôles avec une liste déroulante'
    )
    @commands.guild_only()
    @commands.has_permissions(manage_roles=True)
    async def dropdown(self, ctx: Context, roles: commands.Greedy[Role], *, title: str):
        select = [Select(placeholder=title, 
                        options=[
                            SelectOption(label=role.name, value=role.id) for role in roles
                        ])]
        await ctx.send('Menu de rôles', components=select)

    @commands.Cog.listener()
    async def on_button_click(self, interaction: Interaction):
        if 'Menu de rôles' not in interaction.message.content:
            return

        role = get(interaction.guild.roles, id=int(interaction.component.custom_id))
        await interaction.user.add_roles(role)
        await interaction.respond(content=f'✅ Rôle {role.mention} ajouté')

    @commands.Cog.listener()
    async def on_select_option(self, interaction: Interaction):
        if 'Menu de rôles' not in interaction.message.content:
            return

        roles = [get(interaction.guild.roles, id=int(option.value)) for option in interaction.component.options]
        if common := [role for role in roles if role in interaction.user.roles]:
            return await interaction.respond(content=f'❌ Tu as déjà un des rôles ({common[0].mention})')

        role = get(interaction.guild.roles, id=int(interaction.values[0]))
        await interaction.user.add_roles(role)
        await interaction.respond(content=f'✅ Rôle {role.mention} ajouté')


def setup(bot):
    bot.add_cog(Utility(bot))
