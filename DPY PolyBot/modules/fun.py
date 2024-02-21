from discord_components import Button, ButtonStyle, Interaction
from discord.ext.commands import Context
from discord import Member, Embed
from discord.ext import commands

from games.minesweeper import Minesweeper
from games.hangman import Hangman
from games.dchess import Chess
from random import randint, choice
from datetime import datetime
from asyncio import sleep
from core.cls import Bot


class Fun(commands.Cog, description='commands'):
    def __init__(self, bot: Bot):
        self.bot = bot

    @commands.command(
        aliases=['chess'],
        brief='@Noah Conrard', usage='<membre>',
        description="Jouer aux échecs contre quelqu'un (!regles echecs)"
    )
    @commands.guild_only()
    @commands.max_concurrency(1, commands.BucketType.channel)
    async def echecs(self, ctx: Context, opponent: Member):
        if opponent.bot or opponent == ctx.author:
            return await ctx.send('Tu ne peux pas jouer contre un bot ou contre toi-même')
        
        await Chess(self.bot, ctx, opponent).start()

    @commands.command(
        aliases=['hangman'],
        brief='',
        usage='',
        description='Jouer au pendu'
    )
    @commands.max_concurrency(1, commands.BucketType.user)
    async def pendu(self, ctx: Context):
        await Hangman(self.bot, ctx).start()

    @commands.command(
        aliases=['minesweeper'],
        brief='',
        usage='',
        description='Jouer au démineur (!regles demineur)'
    )
    @commands.max_concurrency(1, commands.BucketType.channel)
    async def demineur(self, ctx: Context):
        await Minesweeper(self.bot, ctx).start()

    @commands.command(
        brief='echecs',
        usage='<echecs ou demineur>',
        description='Afficher une aide pour jouer aux échecs ou au démineur'
    )
    async def regles(self, ctx: Context, game: str):
        if game.lower() in ['démineur', 'demineur']:
            embed = (Embed(color=0x3498db)
                     .add_field(name='Pour jouer', value='Envois un message sous la forme `action,ligne,colonne` :\n' +
                                                         '    • `f` pour mettre un drapeau\n' +
                                                         '    •  `m` pour révéler une case\n\n' +
                                                         'Par exemple : `m,2,1` pour révéler la case à la deuxième ligne, première colonne.')
                     .add_field(inline=False, name='Autres fonctionnalités', value='Envoie `quit` pour abandonner la partie.\n' +
                                                                                   'Envoie `repost` pour renvoyer une grille (garde la progression)'))
        else:
            embed = (Embed(color=0x3498db)
                     .add_field(inline=False, name='Pour jouer',
                                value='Envoie un message sous une des deux formes supportées :\n' +
                                      '    • SAN : représentation usuelle (`a4`, `Nf5`, ...)\n' +
                                      '    • UCI : représentation universelle `départarrivée` (`a2a4`, `b1c3`, ...)\n' +
                                      '\n Les promotions sous le format UCI se font en ajoutant un `q` à la fin (`a2a1q` par exemple)')
                     .add_field(inline=False, name='Autres fonctionnalités', value='Envoie `quit` pour abandonner la partie.\n'))

        embed.set_footer(text='⚠️ Ne pas mettre ! dans vos messages')
        await ctx.send(embed=embed)

    @commands.command(
        aliases=['pof', 'hot'],
        brief='pile',
        usage='<pile ou face>',
        description='Jouer au pile ou face contre le bot'
    )
    async def toss(self, ctx: Context, arg: str):
        result = choice(['Pile', 'Face'])

        if arg.title() not in ['Pile', 'Face']:
            color = 0xe74c3c
            desc = '❌ Tu dois entrer `pile` ou `face` !'
        elif arg.title() in result:
            color = 0xf1c40f
            desc = f'🪙 {result} ! Tu as gagné.'
        else:
            color = 0xe74c3c
            desc = f'🪙 {result} ! Tu as perdu.'

        embed = Embed(color=color, description=desc)
        await ctx.send(embed=embed)

    @commands.command(
        brief='2d6+5d20+20',
        usage='<texte>',
        description='Faire une lancer de dés'
    )
    async def roll(self, ctx: Context, dices: str):
        content = dices.split('+')
        rolls = [int(content.pop(i))
                 for i in range(len(content)) if content[i].isdigit()]

        for elem in content:
            n, faces = elem.split('d') if elem.split('d')[0] != '' else (1, elem[1:])
            rolls += [randint(1, int(faces)) for _ in range(int(n))]

        rolls_str = ' + '.join([str(n) for n in rolls])

        embed = Embed(color=0xf1c40f, description=f"**🎲 Résultat du lancé :** {rolls_str} = **{sum(rolls)}**")
        await ctx.send(embed=embed)

    @commands.command(
        brief='',
        usage='',
        description='Tester son temps de réaction'
    )
    @commands.max_concurrency(1, commands.BucketType.user)
    async def reaction(self, ctx: str):
        await ctx.send('\u200b', components=[Button(label='Appuie quand tu es prêt', custom_id=ctx.author.id)])

        interaction = await self.bot.wait_for('button_click', check=lambda i: i.user==ctx.author)
        await interaction.edit_origin(components=[Button(label='Appuie dès que je change de couleur', style=ButtonStyle.red, custom_id=interaction.user.id)])
        await sleep(randint(2,10))

        message = await ctx.channel.fetch_message(interaction.message.id)
        if not message.components:
            return

        await interaction.message.edit(components=[Button(label='Maintenant !', style=ButtonStyle.green, custom_id=ctx.author.id)])
        start = datetime.now()

        interaction = await self.bot.wait_for('button_click', check=lambda i: i.user==ctx.author)
        td = datetime.now() - start
        td = round(td.seconds+td.microseconds/1000000-self.bot.latency, 3)

        embed = Embed(color=0x3498db, description=f'⏱️ Ton temps de réaction : `{td}` secondes')
        await interaction.edit_origin(content=None, embed=embed, components=[])

    @commands.Cog.listener()
    async def on_button_click(self, interaction: Interaction):
        if interaction.component.label == 'Appuie dès que je change de couleur' and interaction.component.style == ButtonStyle.red:
            embed = Embed(color=0xe74c3c, description='❌ Tu as appuyé trop tôt')
            return await interaction.edit_origin(content=None, embed=embed, components=[])


def setup(bot):
    bot.add_cog(Fun(bot))
