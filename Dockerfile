FROM debian:latest

RUN apt update && apk upgrade

RUN apt install sqlite-libs
RUN apt install add bash curl git jq wget make cmake g++ \
        opus-dev libsodium-dev zlib-dev openssl-dev curl-dev sqlite-dev

WORKDIR /app
COPY ./includes ./includes
COPY ./src ./src
COPY ./CMakeLists.txt ./CMakeLists.txt
COPY ./.env ./.env
COPY data/database.db ./database.db

RUN mkdir libs
RUN git clone https://github.com/SOCI/soci.git libs/soci

RUN curl -sL "https://api.github.com/repos/brainboxdotcc/DPP/releases/latest" \
            | jq -r "linux-x64.deb" \
            | wget -q -O DPP.deb -i - \
            || (printf "Error: Failed to download DPP\n" && exit) \
            && dpkg -i DPP.deb \
            && rm DPP.deb

RUN mkdir build && cd build && cmake .. && make -j$(nproc)

WORKDIR /app/build
CMD ./PolyBot
