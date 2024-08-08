//
// Created by MrSpaar on 07/01/2024.
//

#pragma once
#include <string>
#include <fstream>
#include <sstream>
#include <unordered_map>


class DotEnv {
public:
    explicit DotEnv(const char* envPath) {
        std::ifstream file(envPath);

        std::string line;
        std::string key, value;
        std::istringstream iss;

        while (std::getline(file, line)) {
            if (line.empty() || line[0] == '#')
                continue;

            iss = std::istringstream(line);
            std::getline(iss, key, '=');

            if (key.empty())
                continue;

            std::getline(iss, value);
            this->env[key] = value.substr(1, value.size() - 2);
        }
    }

    std::string operator[](const std::string& key) {
        return this->env[key];
    }
private:
    std::unordered_map<std::string, std::string> env;
};
