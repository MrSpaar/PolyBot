#ifndef LOGS_H
#define LOGS_H

#include <fstream>
#include <iomanip>
#include <sstream>
#include <iostream>


enum Log {
    NONE = 0,
    INFO = 1,
    WARNING = 2,
    ERROR = 3,
    CRITICAL = 4
};


class Logger {
public:
    Logger(): level(Log::INFO), output("logs.txt", std::ios::app) {}

    Logger& operator()(Log value) {
        level = value;
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
            case Log::NONE:
                break;
            case Log::INFO:
                log << "[INFO]";
                break;
            case Log::WARNING:
                log << "[WARN]";
                break;
            case Log::ERROR:
                log << "[ERR]";
                break;
            case Log::CRITICAL:
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
    Log level;
    std::ofstream output;
    std::stringstream ss;
};


#endif //LOGS_H