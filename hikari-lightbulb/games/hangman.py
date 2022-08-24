from hikari import Embed, Message, MessageCreateEvent
from lightbulb import Context

from random import choice, randint
from core.funcs import normalize_string


class Hangman:
    def __init__(self, ctx):
        self.ctx: Context = ctx
        self.message: Message = None
        self.embed: Embed = None

        self.word = choice([ligne.strip() for ligne in open('wordlist.txt', encoding='UTF-8')])
        self.normalized = normalize_string(self.word)
        self.guess = ['-']*len(self.word)
        self.lives, self.errors = 5, []

    async def start(self) -> None:
        self.embed = (Embed(title='Partie de pendu', color=f'{randint(0, 0xFFFFFF):06x}')
                      .add_field(name='Mot', value=f"```{''.join(self.guess)}```", inline=False)
                      .add_field(name='Erreurs', value='```\u200b```', inline=False)
                      .set_footer(text=f'Vies : {self.lives}'))

        self.message = await self.ctx.respond(embed=self.embed)
        await self.play()

    async def get_letter(self) -> str:
        event = await self.ctx.bot.wait_for(MessageCreateEvent, timeout=None,
                                            predicate=lambda m: m.author == self.ctx.author and len(m.content) in [1, len(self.word)])
        await event.message.delete()
        return normalize_string(event.message.content.lower())

    async def play(self) -> None:
        letter = await self.get_letter()

        if letter in self.errors or letter in self.guess:
            await self.ctx.respond('Tu as déjà envoyé cette lettre')
            return await self.play()

        if len(letter) in [1, len(self.word)] and letter not in self.normalized and letter != self.normalized:
            self.lives -= 1
            self.errors.append(letter)
            self.embed.edit_field(1, 'Erreurs', f"```{', '.join(self.errors)}```", inline=False)
            self.embed.set_footer(text=f'Vies : {self.lives}')
        elif len(letter) == 1:
            self.guess = [self.word[i] if letter == char else self.guess[i] for i, char in enumerate(self.normalized)]
            self.embed.edit_field(0, 'Mot', f"```{''.join(self.guess)}```", inline=False)

        if self.word == ''.join(self.guess) or self.normalized == letter:
            self.embed.edit_field(0, 'Mot', f"```{self.word}```", inline=False)
            await self.message.edit(embed=self.embed)
            return await self.ctx.respond(f'Bravo, tu as gagné ! :)')

        if not self.lives:
            return await self.ctx.respond(f'Perdu ! Le mot était `{self.word}`')

        await self.message.edit(embed=self.embed)
        await self.play()
