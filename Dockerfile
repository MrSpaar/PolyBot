FROM debian:latest

RUN apt-get update && apt-get upgrade -y
RUN apt-get install curl git jq wget make cmake g++ \
        libopus-dev libsodium-dev zlib1g-dev libssl-dev libcurl4-openssl-dev libsqlite3-dev -y

RUN curl -s "https://api.github.com/repos/brainboxdotcc/DPP/releases/latest" \
            | grep "browser_download_url.*linux-x64.deb" \
            | cut -d : -f 2,3 \
            | tr -d \" \
            | wget -qi https://github.com/brainboxdotcc/DPP/releases/download/v10.0.24/libdpp-10.0.24-linux-x64.deb -O DPP.deb \
            || (printf "Error: Failed to download DPP\n" && exit) \
            && dpkg -i DPP.deb \
            && rm DPP.deb

WORKDIR /app
COPY ./includes ./includes
COPY ./src ./src
COPY ./CMakeLists.txt ./CMakeLists.txt
COPY ./.env ./.env
COPY ./data/database.db ./data/database.db

RUN mkdir build && cd build && cmake .. && make -j$(nproc)

WORKDIR /app/build
CMD ./PolyBot
