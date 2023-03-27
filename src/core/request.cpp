//
// Created by mrspaar on 3/24/23.
//

#include "request.h"


Request::Request(const std::string &url) {
    request.setOpt<curlpp::options::Url>(url);
    request.setOpt<curlpp::options::WriteStream>(&os);
}

Request::~Request() {
    os.clear();
    headers.clear();
}

Request &Request::add_header(const std::string &header, const std::string &value) {
    headers.push_back(header+": "+value);
    return *this;
}

nlohmann::json Request::get() {
    request.setOpt<curlpp::options::HttpGet>(true);
    request.setOpt<curlpp::options::HttpHeader>(headers);

    request.perform();
    return nlohmann::json::parse(os.str());
}

nlohmann::json Request::post(const std::string &data) {
    request.setOpt<curlpp::options::Post>(true);
    request.setOpt<curlpp::options::PostFields>(data);
    request.setOpt<curlpp::options::HttpHeader>(headers);

    request.perform();
    return nlohmann::json::parse(os.str());
}
