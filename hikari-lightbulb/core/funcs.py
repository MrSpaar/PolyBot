from hikari import Permissions
from lightbulb import Context, Check, errors

from datetime import datetime, timedelta
from unicodedata import normalize
from aiohttp import ClientSession
from typing import Union
from os import environ


async def api_call(link: str, headers: dict = None, post: bool = False, json: bool = True) -> Union[dict, str]:
    async with ClientSession() as s:
        if post:
            async with s.post(link, headers=headers) as resp:
                return await resp.json() if json else await resp.text()

        async with s.get(link, headers=headers) as resp:
            return await resp.json() if json else await resp.text()


async def get_oauth():
    client, secret = environ["TWITCH_CLIENT"], environ["TWITCH_TOKEN"]
    data = await api_call(f"https://id.twitch.tv/oauth2/token?client_id={client}&client_secret={secret}&grant_type=client_credentials", post=True)

    return {
        "token": data["access_token"],
        "expire": datetime.now() + timedelta(seconds=data["expires_in"])
    }


def normalize_string(s: str) -> str:
    return normalize(u"NFKD", s).encode("ascii", "ignore").decode("utf8")


def now(utc: bool = False) -> datetime:
    if utc:
        return datetime.utcnow()
    return datetime.utcnow() + timedelta(hours=2)


def _is_higher(ctx: Context) -> Union[bool, Exception]:
    args = ctx.message.content.split()
    guild = ctx.get_guild()
    member = guild.get_member(int(args[1].strip("<@!>")))

    if not member:
        raise errors.ConverterFailure("member")

    author_top = ctx.member.get_top_role()
    member_top = member.get_top_role()

    if author_top.position > member_top.position:
        return True
    raise errors.MissingRequiredPermission(Permissions.ADMINISTRATOR)


is_higher = Check(_is_higher)
