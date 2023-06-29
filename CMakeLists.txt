cmake_minimum_required(VERSION 3.24)
project(PolyBot VERSION 1.0)
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

include_directories(includes)

find_package(dpp REQUIRED)
list(APPEND LIBS dpp curl sqlite3)


add_executable(${PROJECT_NAME} src/main.cpp
    includes/sqlite.h includes/env.h
    includes/listeners.h includes/commands.h
    includes/request.h includes/pages.h

    src/commands/search.cpp src/commands/moderation.cpp
    src/commands/config.cpp src/commands/menu.cpp src/commands/levels.cpp
    src/listeners/menu.cpp src/listeners/levels.cpp
    src/listeners/logs.cpp src/listeners/channels.cpp src/listeners/ready.cpp
)

target_link_libraries(${PROJECT_NAME} PRIVATE ${LIBS})
target_include_directories(${PROJECT_NAME} PRIVATE ${LIBS})

set_target_properties(${PROJECT_NAME} PROPERTIES
    CXX_STANDARD 20
    CXX_STANDARD_REQUIRED ON
)
