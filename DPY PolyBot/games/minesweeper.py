from discord import Embed, Color, Message
from discord.ext.commands import Context

from core.cls import Bot
from random import sample


class Minesweeper:
    def __init__(self, bot, ctx):
        self.bot: Bot = bot
        self.ctx: Context = ctx
        self.message: Message = None
        self.embed: Embed = None

        self.emotes = [
            '<:0_:876396436630159401>', '<:1_:876385590055149629>', '<:2_:876385590038376448>',
            '<:3_:876385589832863764>', '<:4_:876385588872359936>', '<:5_:876385588700405761>',
            '<:6_:878585152031305798>', '<:7_:878585151838359603>', '<:8_:878582284012376075>'
        ]

        self.mine, self.flag, self.blank = [
            '<:mi:876385344491229224>', '<:fl:876385343178412102>', '<:bl:876384334133751839>'
        ]

        self.checks = (
            lambda i, m: -1 < i - 10 < 100 and (self.sol[i - 10] == self.mine or m),
            lambda i, m: -1 < i + 10 < 100 and (self.sol[i + 10] == self.mine or m),
            lambda i, m: -1 < i + 1 < 100 and (self.sol[i + 1] == self.mine or m) and (i + 1) % 10,
            lambda i, m: -1 < i - 9 < 100 and (self.sol[i - 9] == self.mine or m) and (i + 1) % 10,
            lambda i, m: -1 < i + 11 < 100 and (self.sol[i + 11] == self.mine or m) and (i + 1) % 10,
            lambda i, m: -1 < i - 1 < 100 and (self.sol[i - 1] == self.mine or m) and i % 10,
            lambda i, m: -1 < i + 9 < 100 and (self.sol[i + 9] == self.mine or m) and i % 10,
            lambda i, m: -1 < i - 11 < 100 and (self.sol[i - 11] == self.mine or m) and i % 10,
        )

        self.sol = None
        self.cur = [self.blank]*100

    async def start(self) -> None:
        self.embed = (Embed(color=Color.random())
                      .set_author(name='Partie de démineur', icon_url=self.ctx.author.avatar_url)
                      .set_footer(text='Pour voir comment jouer → !regles demineur'))

        self.message = await self.ctx.send(embed=self.embed)
        await self.show(self.cur)
        await self.loop(init=True)

    def create_grid(self) -> None:
        self.sol = sample([self.blank]*75 + [self.mine]*25, 100)

        for i, elem in enumerate(self.sol):
            if elem != self.mine:
                self.sol[i] = self.emotes[len([1 for c in self.checks if c(i, False)])]

    async def show(self, grid: list) -> None:
        emojis = [
            '<:1_:876453166424686602>', '<:2_:876453170677706772>', '<:3_:876453173131378708>',
            '<:4_:876453175140433930>', '<:5_:876453177707335710>', '<:6_:876453181620641862>',
            '<:7_:876453183470329907>', '<:8_:876453186205007893>', '<:9_:876453188927099010>',
            '<:10:876453193863815219>'
        ]

        temp = f"{self.blank}{''.join(emojis)}\n"

        for i in range(0, 100, 10):
            temp += f"{emojis[i // 10]}{''.join(grid[i:i + 10])}\n"

        self.embed.description = temp
        await self.message.edit(embed=self.embed)

    def reveal_near(self, i: int, lastest: list = []) -> None:
        for x, c in zip((-10, 10, 1, -9, 11, -1, 9, -11), self.checks):
            if c(i, True):
                self.cur[i + x] = self.sol[i + x]
                if self.sol[i+x] == self.emotes[0] and i+x not in lastest:
                    lastest.append(i + x)
                    self.reveal_near(i+x, lastest)

    async def loop(self, init: bool = False) -> None:
        message = await self.bot.wait_for('message', check=lambda m: m.author == self.ctx.author)
        content = message.content.lower()

        if content == 'quit':
            self.embed.set_author(name='Partie abandonnée', icon_url=self.ctx.author.avatar_url)
            self.embed.color = 0xe74c3c

            return await self.message.edit(embed=self.embed)
        elif content == 'repost':
            await self.message.delete()
            self.message = await self.ctx.send(embed=self.embed)

            return await self.loop()
        elif ',' not in content:
            return await self.loop()

        try:
            action, x, y = content.split(',')
            if 11 > int(x) > 0 and 11 > int(y) > 0:
                pos = (int(x) - 1)*10 + int(y) - 1
                await message.delete()
            else:
                return await self.loop(init)
        except:
            return await self.loop(init)

        if init or self.sol is None:
            self.create_grid()
            while self.sol[pos] != self.emotes[0]:
                self.create_grid()
        elif self.sol[pos] == self.mine and action == 'm':
            self.embed.set_author(name='Partie perdue !')
            self.embed.color = 0xe74c3c

            return await self.show(self.sol)

        if action == 'f':
            self.cur[pos] = self.flag
        elif action == 'm':
            self.cur[pos] = self.sol[pos]
            if self.cur[pos] == self.emotes[0]:
                self.reveal_near(pos)

        if self.blank not in self.cur and self.cur.count(self.flag) == 25:
            self.embed.set_author(name='Partie gagnée !')
            self.embed.color = 0xf1c40f

            return await self.show(self.sol)

        await self.show(self.cur)
        await self.loop()
