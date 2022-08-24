from hikari import GatewayGuild

from os import environ
from random import randint
from motor.motor_asyncio import AsyncIOMotorClient


class DB:
    client = None
    cluster = None

    @staticmethod
    def connect() -> None:
        DB.client = AsyncIOMotorClient(environ["DATABASE_URL"])
        DB.cluster = DB.client["data"]

    @staticmethod
    async def fetch_settings(guild_id: int):
        data = await DB.cluster["setup"].find({"_id": guild_id}).to_list(length=None)

        return None if not data else data[0]

    @staticmethod
    async def change_setting(guild_id, key: str, value: int) -> None:
        query = {'_id': guild_id}
        update = {"$set": {key: value}}

        await DB.cluster["setup"].update_one(query, update)

    @staticmethod
    async def add_member_guild(guild_id: int, member_id: int) -> None:
        query = {"_id": member_id}
        update = {"$addToSet": {"guilds": {"id": guild_id, "level": 0, "xp": 0}}}

        await DB.cluster["members"].update_one(query, update, True)

    @staticmethod
    async def remove_member_guild(guild_id: int, member_id: int) -> None:
        query = {"_id": member_id}
        update = {"$pull": {"guilds": {"id": guild_id}}}

        await DB.cluster["members"].update_one(query, update)

    @staticmethod
    async def fetch_temp_channel(guild_id: int, voc_id: int = None, member_id: int = None) -> dict:
        if member_id:
            query = {"guildId": guild_id, "_id": member_id}
        else:
            query = {"guildId": guild_id, "vocId": voc_id}

        data = await DB.cluster["pending"].find(query).to_list(length=None)
        return None if not data else data[0]

    @staticmethod
    async def insert_temp_channel(guild_id: int, member_id: int, voc_id: int, txt_id: int) -> None:
        await DB.cluster["pending"].insert_one({
            "_id": member_id,
            "guildId": guild_id,
            "vocId": voc_id,
            "txtId": txt_id,
        })

    @staticmethod
    async def delete_temp_channel(entry: dict) -> None:
        await DB.cluster["pending"].delete_one(entry)

    @staticmethod
    async def fetch_leaderboard(guild_id: int) -> list:
        data = DB.cluster["members"].find({"guilds.id": guild_id}, {"guilds.$": 1})

        return await data.to_list(length=None)

    @staticmethod
    async def fetch_member(guild_id: int, member_id: int) -> list:
        data = await DB.cluster["members"].find(
            {"_id": member_id, "guilds.id": guild_id}, {"guilds.$": 1}
        ).to_list(length=None)

        return None if not data else data[0]

    @staticmethod
    async def update_member_xp(guild_id: int, member_id: int, xp: int, next_lvl: int) -> None:
        query = {"_id": member_id, "guilds.id": guild_id}
        update = {
            "$inc": {
                "guilds.$.xp": randint(15, 25),
                "guilds.$.level": 1 if xp >= next_lvl else 0}
        }

        await DB.cluster["members"].update_one(query, update)
