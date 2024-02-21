from discord import File, Embed, Message
from discord.ext.commands import Context

from asyncio import TimeoutError
from chess import Board, Move
from chess.svg import board
from cairosvg import svg2png
from core.cls import Bot
from os import remove


class Chess:
    def __init__(self, bot, ctx, opponent):
        self.bot: Bot = bot
        self.ctx: Context = ctx
        self.message: Message = None

        self.end = False
        self.board = Board()
        self.opponent = opponent
        self.cur = [ctx.author, opponent]

    async def start(self) -> None:
        await self.send_message(init=True)
        await self.message.add_reaction('✅')
        await self.message.add_reaction('❌')

        try:
            react = await self.bot.wait_for('reaction_add', timeout=300,
                                            check=lambda r, u: (str(r) == '✅' or str(r) == '❌') and u == self.opponent)
        except:
            return await self.send_message(color=0xe74c3c, text="❌ L'adversaire n'a pas accepté la partie à temps")

        if str(react[0]) == '❌':
            return await self.send_message(color=0xe74c3c, text="❌ L'adversaire à refusé la partie")

        await self.send_message(color=0xfffff)
        return await self.play()

    async def play(self) -> None:
        await self.turn()

        if self.board.is_checkmate():
            return await self.ctx.send(f'{self.cur[0].mention} a gagné la partie !')
        elif self.board.is_game_over():
            return await self.ctx.send('Égalité, personne ne gagne')
        else:
            self.cur[0], self.cur[1] = self.cur[1], self.cur[0]
            await self.play()

    async def turn(self) -> None:
        try:
            move = await self.bot.wait_for('message', timeout=120, check=lambda m: m.author == self.cur[0])

            if move.content in ['ff', 'resign', 'abandon', 'abandonner', 'quit']:
                return await self.ctx.send(f'{self.cur[0].mention} a abandonné la partie')

            try:
                m = self.board.parse_san(move.content)
                self.board.push(m)
            except:
                await self.turn()
            else:
                await move.delete()
                await self.send_message(m, 0xfffff)
        except TimeoutError:
            return await self.ctx.send(f'Temps de réflexion écoulé, {self.cur[1].mention} a gagné')

    async def send_message(self, move: Move = None, color=0x00000, text: str = '', init: bool = False) -> None:
        if self.message:
            await self.message.delete()

        b = board(self.board, lastmove=move) if move else board(self.board)
        svg2png(bytestring=b, write_to='output.png')
        footer = '2 minutes par coups' + f' • Tour de {self.cur[1].display_name}' if not init else ''

        embed = (Embed(color=color, description=text)
                 .set_footer(text=footer)
                 .set_image(url="attachment://board.png")
                 .set_author(name=f'{self.opponent.name} contre {self.ctx.author.name}',
                             icon_url=self.opponent.avatar_url))

        self.message = await self.ctx.send(embed=embed, file=File('output.png', 'board.png'))
        remove('output.png')
