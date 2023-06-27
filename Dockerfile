FROM alpine:latest

RUN apk update && apk upgrade

RUN apk add sqlite-libs
RUN apk add bash curl git jq wget make cmake g++ \
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
            | jq -r ".tarball_url" \
            | wget -q -O DPP.tar.gz -i - \
            || (printf "Error: Failed to download DPP\n" && exit) \
            && tar -xzf DPP.tar.gz \
            && mv brainboxdotcc-DPP-* libs/DPP \
            && rm DPP.tar.gz

RUN mkdir build && cd build && cmake .. && make -j$(nproc)

WORKDIR /app/build
CMD ./PolyBot
