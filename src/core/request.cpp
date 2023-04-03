//
// Created by mrspaar on 3/24/23.
//

#include <iostream>
#include "request.h"


Request::Request(const std::string& url) {
    curl = curl_easy_init();

    curl_easy_setopt(curl, CURLOPT_WRITEDATA, &body);
    curl_easy_setopt(curl, CURLOPT_URL, url.c_str());
    curl_easy_setopt(curl, CURLOPT_DEFAULT_PROTOCOL, "https");
    curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_data);
}


Request::~Request() {
    curl_slist_free_all(headers);
    curl_easy_cleanup(curl);
}


Request &Request::add_header(const std::string& header, const std::string& value) {
    headers = curl_slist_append(headers, (header + ": " + value).c_str());
    return *this;
}


nlohmann::json Request::get() {
    curl_easy_setopt(curl, CURLOPT_HTTPGET, 1L);
    curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);

    curl_easy_perform(curl);
    return nlohmann::json::parse(body);
}


nlohmann::json Request::post(const std::string &data) {
    curl_easy_setopt(curl, CURLOPT_POST, 1L);
    curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
    curl_easy_setopt(curl, CURLOPT_POSTFIELDS, data.c_str());
    curl_easy_setopt(curl, CURLOPT_POSTFIELDSIZE, data.size());

    curl_easy_perform(curl);
    return nlohmann::json::parse(body);
}


size_t Request::write_data(void *contents, size_t size, size_t nmemb, void *userp) {
    ((std::string*) userp)->append((char*) contents, size * nmemb);
    return size * nmemb;
}
