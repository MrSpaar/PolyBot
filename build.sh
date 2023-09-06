sudo apt update && sudo apt upgrade -y
sudo apt install curl git jq wget make cmake g++ libopus-dev libsodium-dev \
                 zlib1g-dev libssl-dev libcurl4-openssl-dev libsqlite3-dev -y

curl -s "https://api.github.com/repos/brainboxdotcc/DPP/releases/latest" \
            | grep "browser_download_url.*linux-x64.deb" \
            | cut -d : -f 2,3 \
            | tr -d \" \
            | wget -qi - -O DPP.deb \
            || (printf "Error: Failed to download DPP\n" && exit) \
            && dpkg -i DPP.deb \
            && rm DPP.deb

mkdir -p cmake-build-release && cd cmake-build-release
cmake -DCMAKE_BUILD_TYPE=Release .. && make -j$(nproc)