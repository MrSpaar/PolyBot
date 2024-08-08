//
// Created by mrspaar on 3/24/23.
//

#pragma once
#include <curl/curl.h>
#include <dpp/nlohmann/json.hpp>


class Request {
public:
    explicit Request(const std::string &url) {
        curl = curl_easy_init();

        curl_easy_setopt(curl, CURLOPT_WRITEDATA, &body);
        curl_easy_setopt(curl, CURLOPT_URL, url.c_str());
        curl_easy_setopt(curl, CURLOPT_DEFAULT_PROTOCOL, "https");
        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_data);
    }

    nlohmann::json get() {
        curl_easy_setopt(curl, CURLOPT_HTTPGET, 1L);
        curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);

        curl_easy_perform(curl);
        return nlohmann::json::parse(body);
    }

    nlohmann::json post(const std::string &data) {
        curl_easy_setopt(curl, CURLOPT_POST, 1L);
        curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
        curl_easy_setopt(curl, CURLOPT_POSTFIELDS, data.c_str());
        curl_easy_setopt(curl, CURLOPT_POSTFIELDSIZE, data.size());

        curl_easy_perform(curl);
        return nlohmann::json::parse(body);
    }

    Request &add_header(const std::string &header, const std::string &value) {
        headers = curl_slist_append(headers, (header + ": " + value).c_str());
        return *this;
    }

    ~Request() {
        curl_slist_free_all(headers);
        curl_easy_cleanup(curl);
    }
private:
    CURL *curl;
    std::string body;
    curl_slist *headers = nullptr;

    static size_t write_data(void *contents, size_t size, size_t nmemb, void *userp) {
        ((std::string*) userp)->append((char*) contents, size * nmemb);
        return size * nmemb;
    }
};
