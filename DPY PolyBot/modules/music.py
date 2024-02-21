from discord import Embed, FFmpegPCMAudio, VoiceChannel
from discord.ext.commands import Context
from discord.ext import commands
from discord.utils import get

from asyncio import run_coroutine_threadsafe
from yt_dlp import YoutubeDL
from typing import Optional
from core.cls import Bot
from re import findall


class Music(commands.Cog, name='Musique', description='commands'):
    def __init__(self, bot: Bot):
        self.bot = bot
        self.song_queue = {}
        self.message = {}
        self.FFMPEG_OPTIONS = {
            'before_options': '-reconnect 1 -reconnect_streamed 1 -reconnect_delay_max 5',
            'options': '-vn'
        }

    async def edit_message(self, ctx):
        embed = self.song_queue[ctx.guild.id][0]['embed']
        content = "\n".join([f"({self.song_queue[ctx.guild.id].index(i)}) {i['title']}" for i in self.song_queue[ctx.guild.id][1:]])
        embed.set_field_at(index=3, name="File d'attente:", value=content or "Pas de vidéos en attente", inline=False)
        await self.message[ctx.guild.id].edit(embed=embed)

    def play_next(self, ctx):
        voice = get(self.bot.voice_clients, guild=ctx.guild)
        if voice is None:
            del self.song_queue[ctx.guild.id]
            run_coroutine_threadsafe(self.message[ctx.guild.id].delete(), self.bot.loop)
        elif len(self.song_queue[ctx.guild.id]) > 1:
            del self.song_queue[ctx.guild.id][0]
            run_coroutine_threadsafe(self.edit_message(ctx), self.bot.loop)
            voice.play(FFmpegPCMAudio(self.song_queue[ctx.guild.id][0]['source'], **self.FFMPEG_OPTIONS), after=lambda e: self.play_next(ctx))
            voice.is_playing()
        else:
            run_coroutine_threadsafe(voice.disconnect(), self.bot.loop)
            run_coroutine_threadsafe(self.message[ctx.guild.id].delete(), self.bot.loop)

    @commands.command(
        aliases=['p'],
        brief='goat polyphia',
        usage='<recherche ou lien>',
        description='Jouer une vidéo dans un channel vocal'
    )
    @commands.guild_only()
    @commands.cooldown(1, 5, commands.BucketType.channel)
    async def play(self, ctx: Context, channel: Optional[VoiceChannel], *, query: str):
        channel = channel or ctx.author.voice.channel
        voice = get(self.bot.voice_clients, guild=ctx.guild)

        if not voice:
            voice = await channel.connect()
        elif ctx.author not in voice.channel.members:
            return await ctx.send("❌ Tu n'es pas dans le même channel que moi")

        regex = r"http(?:s?):\/\/(?:www\.)?youtu(?:be\.com\/watch\?v=|\.be\/)([\w\-\_]*)(&(amp;)?‌​[\w\?‌​=]*)?"
        video_id = findall(regex, query)
        query = video_id[0][0] if video_id else f"ytsearch:{query}"

        with YoutubeDL({'format': 'bestaudio/best', 'noplaylist': 'True', 'quiet': 'True'}) as ydl:
            video = ydl.extract_info(query, download=False)
            video = video if video_id else video['entries'][0]
        
        m, s = divmod(int(video['duration']), 60)
        h, m = divmod(m, 60)
        duration = f'{h:02d}:{m:02d}:{s:02d}'
        
        embed = (Embed(title='🎵 Vidéo en cours:', description=f"[{video['title']}]({video['webpage_url']})", color=0x3498db)
                 .add_field(name='Durée', value=duration)
                 .add_field(name='Demandée par', value=ctx.author)
                 .add_field(name='Chaine', value=f"[{video['uploader']}]({video['uploader_url']})")
                 .add_field(name="File d'attente", value=f"Pas de vidéos en attente")
                 .set_thumbnail(url=video['thumbnail']))

        song = {'embed': embed, 'source': video['formats'][0]['url'], 'title': video['title']}

        await ctx.message.delete()
        if not voice.is_playing():
            self.song_queue[ctx.guild.id] = [song]
            self.message[ctx.guild.id] = await ctx.send(embed=song['embed'])
            voice.play(FFmpegPCMAudio(song['source'], **self.FFMPEG_OPTIONS), after=lambda e: self.play_next(ctx))
            voice.is_playing()
        else:
            self.song_queue[ctx.guild.id].append(song)
            await self.edit_message(ctx)

    async def different_channel(self, ctx):
        embed = Embed(color=0xe74c3c, description="❌ Tu n'es pas dans le même channel que moi")
        await ctx.send(embed=embed)

    @commands.command(
        brief='',
        usage='',
        description='Mettre la vidéo en cours de lecture en pause'
    )
    @commands.guild_only()
    async def pause(self, ctx: Context):
        voice = get(self.bot.voice_clients, guild=ctx.guild)

        if ctx.author not in voice.channel.members:
            return await self.different_channel(ctx)
        if voice.is_connected():
            await ctx.message.delete()
            if voice.is_playing():
                voice.pause()
            else:
                voice.resume()

    @commands.command(
        brief='',
        usage='',
        description='Passer la vidéo en cours de lecture'
    )
    @commands.guild_only()
    async def skip(self, ctx: Context):
        voice = get(self.bot.voice_clients, guild=ctx.guild)

        if ctx.author not in voice.channel.members:
            return await self.different_channel(ctx)
        if voice.is_playing():
            await ctx.message.delete()
            voice.stop()

    @commands.command(
        brief='1',
        usage='<position dans la file>', 
        description='Enlever une vidéo de la file'
    )
    @commands.guild_only()
    async def remove(self, ctx: Context, num: int):
        voice = get(self.bot.voice_clients, guild=ctx.guild)

        if ctx.author not in voice.channel.members:
            return await self.different_channel(ctx)
        if voice.is_playing():
            del self.song_queue[ctx.guild.id][num]
            await ctx.message.delete()
            await self.edit_message()

    @commands.command(
        aliases=['stop'],
        brief='',
        usage='',
        description='Arrêter le bot de jouer des vidéos'
    )
    @commands.guild_only()
    async def leave(self, ctx: Context):
        voice = get(self.bot.voice_clients, guild=ctx.guild)
        if ctx.author not in voice.channel.members:
            return await self.different_channel(ctx)

        self.song_queue[ctx.guild.id] = []
        voice.stop()
        await ctx.message.delete()


def setup(bot):
    bot.add_cog(Music(bot))
