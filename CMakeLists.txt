cmake_minimum_required(VERSION 3.24)
project(PolyBot VERSION 1.0)
set(CMAKE_CXX_STANDARD 20)

include_directories(includes)

find_package(dpp REQUIRED)
list(APPEND LIBS dpp curl sqlite3)


add_executable(${PROJECT_NAME} src/main.cpp
        includes/database.h src/core/database.cpp includes/bot.h
        includes/request.h includes/paginator.h

        src/core/bot.cpp src/core/database.cpp src/core/paginator.cpp

        src/listeners/levels.cpp src/commands/levels.cpp
        src/commands/search.cpp src/commands/moderation.cpp src/commands/config.cpp
        src/listeners/logs.cpp src/listeners/channels.cpp src/listeners/ready.cpp
)

target_link_libraries(${PROJECT_NAME} PRIVATE ${LIBS})
target_include_directories(${PROJECT_NAME} PRIVATE ${LIBS})
