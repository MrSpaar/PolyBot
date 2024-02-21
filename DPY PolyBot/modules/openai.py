from discord import Message
from discord.ext import commands

from aiohttp import ClientSession
from core.tools import normalize_string
from core.cls import Bot
from os import environ


class OpenAI(commands.Cog):
    def __init__(self, bot: Bot):
        self.bot = bot

    @commands.Cog.listener()
    async def on_message(self, message: Message):
        if not message.content.startswith(self.bot.mention) and not message.content.startswith(self.bot.user.mention):
            return

        question = normalize_string(message.content.strip(self.bot.mention).strip())
        query = f"Ce qui suit est une conversation avec un assistant IA. L'assistant est serviable, creatif, intelligent et tres sympathique.\\n\\n {question}"
        data = '{"prompt": "%s", "max_tokens": 100, "temperature": 0.1}' % query

        headers = {
            'Content-Type': 'application/json',
            'Authorization': f'Bearer {environ["OPENAI_TOKEN"]}',
        }

        async with message.channel.typing():
            async with ClientSession() as s:
                async with s.post('https://api.openai.com/v1/engines/davinci/completions', headers=headers, data=data) as resp:
                    data = await resp.json()
                    try:
                        msg = data['choices'][0]['text'].split('\n\n')[1].strip('—-')
                        if "Ce qui suit est une conversation avec un assistant IA. L'assistant est serviable, creatif, intelligent et tres sympathique".lower() not in msg.lower():
                            await message.reply(msg)
                        else:
                            await message.reply("J'ai pas compris :(")
                    except:
                        await message.reply("J'ai pas compris :(")


def setup(bot):
    bot.add_cog(OpenAI(bot))
