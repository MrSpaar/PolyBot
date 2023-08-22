//
// Created by mrspaar on 6/28/23.
//

#ifndef POLYBOT_SQLITE_H
#define POLYBOT_SQLITE_H

#include <map>
#include <vector>
#include <string>
#include <memory>
#include <iostream>
#include <sqlite3.h>


namespace sqlite {
    static struct run_t{} run;
}


class SQLRow {
public:
    void insert(const std::string &key, const std::string &value) {
        data[key] = value;
    }

    template<typename T>
    T get(const std::string &key) {
        if (data.find(key) == data.end()) {
            std::cerr << "Key not found: " << key << std::endl;
            return T();
        }

        try {
            if constexpr (std::is_same_v<T, int>)
                return std::stoi(data[key]);
            else if constexpr (std::is_same_v<T, double>)
                return std::stod(data[key]);
            else if constexpr (std::is_same_v<T, std::string>)
                return data[key];

            std::cerr << "Invalid type" << std::endl;
            return T();
        } catch (std::exception &e) {
            std::cerr << "Could not cast value \"" << data[key] << "\" to type \"" << typeid(T).name() << "\"" << std::endl;
            return T();
        }
    }
private:
    std::map<std::string, std::string> data;
};


class SQLResult {
public:
    static int fetch(void* res, int argc, char **argv, char **col_name) {
        auto *result = (SQLResult*) res;
        result->data.emplace_back();

        for (int i = 0; i < argc; i++)
            result->data.back().insert(col_name[i], argv[i] ? argv[i] : "NULL");

        return 0;
    }

    SQLRow first() {
        if (data.empty()) {
            std::cerr << "No data" << std::endl;
            return {};
        }

        return data[0];
    }

    void clear() { data.clear(); }
    [[nodiscard]] auto begin() { return data.begin(); }
    [[nodiscard]] auto end() { return data.end(); }
    [[nodiscard]] bool empty() const { return data.empty(); }
private:
    std::vector<SQLRow> data;
};


class SQLite {
public:
    void init(const std::string &path) {
        int status = sqlite3_open_v2(path.c_str(), &db, SQLITE_OPEN_READWRITE | SQLITE_OPEN_CREATE, nullptr);

        if (status != SQLITE_OK)
            throw std::runtime_error("Could not open database: " + std::string(sqlite3_errmsg(db)));
    }

    template<typename T>
    SQLite &operator<<(const T &value) {
        query.append(value);
        return *this;
    }

    template<typename T>
    SQLite& operator,(const T &value) {
        size_t pos = query.find('?');
        query.erase(pos, 1);

        if constexpr (std::is_convertible_v<T, std::string>)
            query.insert(pos, "'" + value + "'");
        else
            query.insert(pos, std::to_string(value));

        return *this;
    }

    SQLite &operator,(sqlite::run_t) {
        result.clear();
        char *err_msg = nullptr;
        sqlite3_exec(db, query.c_str(), SQLResult::fetch, (void*) &result, &err_msg);

        if (err_msg != nullptr) {
            std::cerr << "SQLite error: " << err_msg << std::endl;
            sqlite3_free(err_msg);
        }

        query.clear();
        return *this;
    }

    template<typename T>
    T get(const std::string &key) { return result.first().get<T>(key); }

    [[nodiscard]] auto begin() { return result.begin(); }
    [[nodiscard]] auto end() { return result.end(); }
    [[nodiscard]] bool empty() const { return result.empty(); }

    ~SQLite() { sqlite3_close(db); }
private:
    sqlite3 *db = nullptr;
    std::string query;
    SQLResult result;
};

#endif //POLYBOT_SQLITE_H
