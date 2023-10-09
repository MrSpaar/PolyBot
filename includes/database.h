//
// Created by mrspaar on 6/28/23.
//

#ifndef POLYBOT_SQLITE_H
#define POLYBOT_SQLITE_H

#include <string>
#include <sqlite3.h>


struct Database {
    sqlite3 *conn = nullptr;
    ~Database() { sqlite3_close(conn); };
    int init(const std::string_view &path);
};


template<typename>
struct always_false : std::false_type {};


class SQLRow {
public:
    void insert(sqlite3_stmt *stmt, int index);

    template<typename T> T get(const char *key) {
        if constexpr (std::is_convertible_v<T, float>)
            return std::stof(data[key]);
        else if constexpr (std::is_convertible_v<T, int>)
            return std::stoi(data[key]);
        else if constexpr (std::is_convertible_v<T, std::string>)
            return data[key];
        else
            static_assert(always_false<T>::value, "Unsupported type");
    }
private:
    std::map<std::string, std::string> data;
};


class SQLQuery {
public:
    explicit SQLQuery(Database &db, const std::string_view &sql);
    ~SQLQuery() { sqlite3_finalize(stmt); };

    int step();
    int step(SQLRow &row);

    template<typename T> SQLQuery& bind(const T &value) {
        int rc;

        if constexpr (std::is_same_v<T, float> || std::is_same_v<T, double>)
            rc = sqlite3_bind_double(stmt, index++, value);
        else if constexpr (std::is_convertible_v<T, int>)
            rc = sqlite3_bind_int(stmt, index++, value);
        else if constexpr (std::is_convertible_v<T, std::string>)
            rc = sqlite3_bind_text(stmt, index++, value.data(), (int) value.size(), SQLITE_TRANSIENT);
        else
            static_assert(always_false<T>::value, "Unsupported type");

        if (rc != SQLITE_OK)
            throw std::runtime_error("Could not bind value: " + std::string(sqlite3_errmsg(sqlite3_db_handle(stmt))));

        return *this;
    }
private:
    int index = 1;
    sqlite3_stmt *stmt = nullptr;
};


#endif //POLYBOT_SQLITE_H
