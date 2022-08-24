from discord import Embed, Color, Message
from discord.ext.commands import Context

from core.cls import Bot
from random import choice
from core.tools import normalize_string


class Hangman:
    def __init__(self, bot, ctx):
        self.bot: Bot = bot
        self.ctx: Context = ctx
        self.message: Message = None
        self.embed: Embed = None

        self.word = choice([ligne.strip() for ligne in open('wordlist.txt', encoding='UTF-8')])
        self.normalized = normalize_string(self.word)
        self.guess = ['-']*len(self.word)
        self.lives, self.errors = 5, []

    async def start(self) -> None:
        self.embed = (Embed(title='Partie de pendu', color=Color.random())
                      .add_field(name='Mot', value=f"```{''.join(self.guess)}```", inline=False)
                      .add_field(name='Erreurs', value='```\u200b```', inline=False)
                      .set_footer(text=f'Vies : {self.lives}'))

        self.message = await self.ctx.send(embed=self.embed)
        await self.play()

    async def get_letter(self) -> str:
        message = await self.bot.wait_for('message', check=lambda m: m.author == self.ctx.author and len(m.content) in [1, len(self.word)])
        await message.delete()
        return normalize_string(message.content.lower())

    async def play(self) -> None:
        letter = await self.get_letter()

        if letter in self.errors or letter in self.guess:
            await self.ctx.send('Tu as déjà envoyé cette lettre')
            return await self.play()

        if len(letter) in [1, len(self.word)] and letter not in self.normalized and letter != self.normalized:
            self.lives -= 1
            self.errors.append(letter)
            self.embed.set_field_at(1, name='Erreurs', value=f"```{', '.join(self.errors)}```", inline=False)
            self.embed.set_footer(text=f'Vies : {self.lives}')
        elif len(letter) == 1:
            self.guess = [self.word[i] if letter == char else self.guess[i] for i, char in enumerate(self.normalized)]
            self.embed.set_field_at(0, name='Mot', value=f"```{''.join(self.guess)}```", inline=False)

        if self.word == ''.join(self.guess) or self.normalized == letter:
            self.embed.set_field_at(0, name='Mot', value=f"```{self.word}```", inline=False)
            await self.message.edit(embed=self.embed)
            return await self.ctx.send(f'Bravo, tu as gagné ! :)')

        if not self.lives:
            return await self.ctx.send(f'Perdu ! Le mot était `{self.word}`')

        await self.message.edit(embed=self.embed)
        await self.play()
