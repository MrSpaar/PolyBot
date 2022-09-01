import io.github.cdimascio.dotenv.dotenv

class Vars {
    companion object {
        private val dotenv = dotenv()
        val DISCORD_TOKEN = dotenv["DISCORD_TOKEN"]!!
        val DB_URI = dotenv["DB_URI"]!!
    }
}