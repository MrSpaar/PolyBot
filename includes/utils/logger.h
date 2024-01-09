//
// Created by MrSpaar on 08/01/2024.
//

#pragma once
#include <fstream>
#include <iomanip>
#include <sstream>
#include <iostream>


#define GREEN 0x2ECC71
#define ORANGE 0xC27C0E
#define RED 0xE74C3C
#define BLUE 0x3498DB
#define GOLD 0xF1C40F


enum LOG_LEVEL {
    NONE = 0,
    INFO = 1,
    WARNING = 2,
    ERROR = 3,
    CRITICAL = 4
};

class Logger {
public:
    Logger(): level(LOG_LEVEL::INFO), output("logs.txt", std::ios::app) {}

    Logger& operator()(LOG_LEVEL level) {
        this->level = level;
        return *this;
    }

    template<typename T>
    Logger& operator<<(const T& t) {
        ss << t;
        return *this;
    }

    Logger& operator<<(std::ostream& (*f)(std::ostream&)) {
        f(ss);
        std::stringstream log;

        time_t now = time(nullptr);
        tm *ltm = localtime(&now);

        switch (level) {
            case LOG_LEVEL::NONE:
                break;
            case LOG_LEVEL::INFO:
                log << "[INFO]";
                break;
            case LOG_LEVEL::WARNING:
                log << "[WARN]";
                break;
            case LOG_LEVEL::ERROR:
                log << "[ERR]";
                break;
            case LOG_LEVEL::CRITICAL:
                log << "[CRIT]";
                break;
        }

        log << "["
            << std::setfill('0') << std::setw(2) << ltm->tm_mday << "/"
            << std::setfill('0') << std::setw(2) << ltm->tm_mon+1 << "/"
            << std::setfill('0') << std::setw(2) << ltm->tm_year-100 << " "
            << std::setfill('0') << std::setw(2) << ltm->tm_hour << ":"
            << std::setfill('0') << std::setw(2) << ltm->tm_min << ":"
            << std::setfill('0') << std::setw(2) << ltm->tm_sec
            << "] " << ss.str();

        std::cout << log.str();
        output << log.str();

        ss.str("");
        output.flush();

        return *this;
    }
private:
    LOG_LEVEL level;
    std::ofstream output;
    std::stringstream ss;
};
