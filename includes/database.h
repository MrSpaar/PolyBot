//
// Created by mrspaar on 6/28/23.
//

#ifndef POLYBOT_SQLITE_H
#define POLYBOT_SQLITE_H

#include <map>
#include <vector>
#include <string>
#include <sqlite3.h>


class SQLRow {
public:
    void insert(sqlite3_stmt *stmt, int index);
    template<typename T> T get(const char *key);
private:
    std::map<std::string, std::string> data;
};


class SQLQuery {
public:
    explicit SQLQuery(sqlite3 *ptr, const std::string_view &sql);
    ~SQLQuery() { sqlite3_finalize(stmt); };

    int step();
    int step(SQLRow &row);
    template<typename T> SQLQuery& bind(const T &value);
private:
    int index = 1;
    sqlite3_stmt *stmt = nullptr;
};


class Database {
public:
    Database() = default;
    ~Database() { sqlite3_close(conn); };

    int init(const std::string_view &path);
    SQLQuery prepare(const std::string_view &query);
private:
    sqlite3 *conn = nullptr;
};


template<typename T>
SQLQuery& SQLQuery::bind(const T &value) {
    if constexpr (std::is_convertible_v<T, float>)
        sqlite3_bind_double(stmt, index++, value);
    else if constexpr (std::is_convertible_v<T, int>)
        sqlite3_bind_int(stmt, index++, value);
    else if constexpr (std::is_convertible_v<T, const char*>)
        sqlite3_bind_text(stmt, index++, value, -1, SQLITE_TRANSIENT);
    else if constexpr (std::is_convertible_v<T, std::string>)
        sqlite3_bind_text(stmt, index++, value.data(), (int) value.size(), SQLITE_TRANSIENT);
    else
        static_assert(false, "Unsupported type");

    return *this;
}

template<typename T>
T SQLRow::get(const char *key) {
    if constexpr (std::is_convertible_v<T, float>)
        return std::stof(data[key]);
    else if constexpr (std::is_convertible_v<T, int>)
        return std::stoi(data[key]);
    else if constexpr (std::is_convertible_v<T, std::string>)
        return data[key];
    else
        static_assert(false, "Unsupported type");
}


#endif //POLYBOT_SQLITE_H
