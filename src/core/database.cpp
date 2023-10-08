//
// Created by mrspaar on 08/10/23.
//

#include <stdexcept>
#include "database.h"


void SQLRow::insert(sqlite3_stmt *stmt, int index) {
    const char *name = sqlite3_column_name(stmt, index);
    const char *value = (const char *) sqlite3_column_text(stmt, index);
    data[name] = value;
}


SQLQuery::SQLQuery(sqlite3 *ptr, const std::string_view &sql) {
    int rc = sqlite3_prepare_v2(ptr, sql.data(), (int) sql.size(), &stmt, nullptr);

    if (rc != SQLITE_OK)
        throw std::runtime_error("Could not prepare statement: " + std::string(sqlite3_errmsg(ptr)));
}

int SQLQuery::step() {
    return sqlite3_step(stmt);
}

int SQLQuery::step(SQLRow &row) {
    int rc = sqlite3_step(stmt);

    if (rc != SQLITE_ROW)
        return rc;

    for (int i = 0; i < sqlite3_column_count(stmt); i++)
        row.insert(stmt, i);

    return rc;
}


int Database::init(const std::string_view &path) {
    return sqlite3_open_v2(path.data(), &conn, SQLITE_OPEN_READWRITE | SQLITE_OPEN_CREATE, nullptr);
}

SQLQuery Database::prepare(const std::string_view &query) {
    return SQLQuery(conn, query);
}
