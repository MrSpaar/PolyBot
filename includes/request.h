//
// Created by mrspaar on 3/24/23.
//

#ifndef POLYBOT_REQUEST_H
#define POLYBOT_REQUEST_H

#include <list>
#include <sstream>
#include <curl/curl.h>
#include <nlohmann/json.hpp>


class Request {
public:
    explicit Request(const std::string &url);
    ~Request();

    nlohmann::json get();
    nlohmann::json post(const std::string &data);
    Request &add_header(const std::string &header, const std::string &value);
private:
    CURL *curl;
    std::string body;
    curl_slist *headers = nullptr;

    static size_t write_data(void *ptr, size_t size, size_t nmemb, void *stream);
};


#endif //POLYBOT_REQUEST_H
