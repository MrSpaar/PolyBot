//
// Created by mrspaar on 3/24/23.
//

#ifndef POLYBOT_REQUEST_H
#define POLYBOT_REQUEST_H

#include <sstream>
#include <nlohmann/json.hpp>
#include <curlpp/Easy.hpp>
#include <curlpp/cURLpp.hpp>
#include <curlpp/Options.hpp>


class Request {
public:
    explicit Request(const std::string &url);
    ~Request();

    nlohmann::json get();
    nlohmann::json post(const std::string &data);
    Request &add_header(const std::string &header, const std::string &value);
private:
    curlpp::Cleanup cleaner;
    curlpp::Easy request;

    std::ostringstream os;
    std::list<std::string> headers;
};


#endif //POLYBOT_REQUEST_H
