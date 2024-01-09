//
// Created by mrspaar on 6/28/23.
//

#pragma once
#include <map>
#include <string>
#include <sqlite3.h>
#include <stdexcept>
#include <type_traits>


template<typename T>
struct always_false: std::false_type {};

struct SQLite {
    sqlite3 *ptr = nullptr;

    SQLite(std::string_view path) {
        if (sqlite3_open_v2(path.data(), &ptr, SQLITE_OPEN_READWRITE | SQLITE_OPEN_CREATE, nullptr) != SQLITE_OK)
            throw std::runtime_error("Could not open database: " + std::string(sqlite3_errmsg(ptr)));
    }

    ~SQLite() { sqlite3_close(ptr); };
};

class SQLRow {
public:
    void insert(sqlite3_stmt *stmt, int index) {
        const char *name = sqlite3_column_name(stmt, index);
        const char *value = (const char *) sqlite3_column_text(stmt, index);
        data[name] = value;
    }

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
    explicit SQLQuery(SQLite &conn, const std::string_view &sql) {
        int rc = sqlite3_prepare_v2(conn.ptr, sql.data(), (int) sql.size(), &stmt, nullptr);

        if (rc != SQLITE_OK)
            throw std::runtime_error("Could not prepare statement: " + std::string(sqlite3_errmsg(conn.ptr)));
    };

    ~SQLQuery() {
        sqlite3_finalize(stmt);
    };

    int step() {
        return sqlite3_step(stmt);
    }

    int step(SQLRow &row) {
        int rc = sqlite3_step(stmt);

        if (rc != SQLITE_ROW)
            return rc;

        for (int i = 0; i < sqlite3_column_count(stmt); i++)
            row.insert(stmt, i);

        return rc;
    }

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
