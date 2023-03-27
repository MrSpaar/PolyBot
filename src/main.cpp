#include "commands.h"

// TODO :
//  - Role menu handlers
//  - Music command handlers


int main() {
    Env::load();
    Env::init(Env::get("DISCORD_TOKEN"), Env::get("DB_PATH"));
    Env::BOT.start(dpp::st_wait);
    return 0;
}
