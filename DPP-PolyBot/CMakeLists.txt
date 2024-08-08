cmake_minimum_required(VERSION 3.24)
project(PolyBot VERSION 1.0)
set(CMAKE_CXX_STANDARD 20)

find_package(dpp REQUIRED)
list(APPEND LIBS dpp curl sqlite3)
include_directories(includes)

add_executable(${PROJECT_NAME} src/main.cpp
    src/commands/config.cpp
    src/commands/levels.cpp
    src/commands/moderation.cpp
    src/commands/search.cpp
    
    src/listeners/channels.cpp
    src/listeners/levels.cpp
    src/listeners/logs.cpp
)

target_link_libraries(${PROJECT_NAME} PRIVATE ${LIBS})
