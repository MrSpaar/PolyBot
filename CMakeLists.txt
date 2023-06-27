cmake_minimum_required(VERSION 3.24)
project(PolyBot VERSION 1.0)
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

include_directories(includes)

set(WITH_SQLITE3 ON)
set(WITH_BOOST OFF)
set(WITH_MYSQL OFF)
set(WITH_ORACLE OFF)
set(WITH_POSTGRESQL OFF)
set(WITH_ODBC OFF)
set(WITH_DB2 OFF)
set(WITH_FIREBIRD OFF)

add_subdirectory(libs/soci)
find_package(dpp REQUIRED)

list(APPEND LIBS soci_core soci_sqlite3 dpp curl)


add_executable(${PROJECT_NAME}
        src/main.cpp includes/listeners.h
        includes/env.h src/core/env.cpp
        includes/commands.h
        includes/request.h src/core/request.cpp
        includes/pages.h src/core/pages.cpp

        src/commands/search.cpp
        src/commands/moderation.cpp src/commands/config.cpp
        src/commands/menu.cpp src/listeners/menu.cpp
        src/commands/levels.cpp src/listeners/levels.cpp
        src/listeners/logs.cpp src/listeners/channels.cpp
        src/listeners/ready.cpp)

target_link_libraries(${PROJECT_NAME} PRIVATE ${LIBS})
target_include_directories(${PROJECT_NAME} PRIVATE ${LIBS})

set_target_properties(${PROJECT_NAME} PROPERTIES
    CXX_STANDARD 20
    CXX_STANDARD_REQUIRED ON
)
