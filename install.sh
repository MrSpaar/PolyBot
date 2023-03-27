#bin/bash

[[ -z "$1" ]] && core_count=$(($(nproc) - 1)) || core_count=$1
[[ -z "$2" ]] && install_path="cmake-build-run" || install_path=$2

[[ $(command -v git) ]] || printf "Error: git is not installed" || exit
[[ $(command -v g++) ]] || printf "Error: g++ is not installed" || exit
[[ $(command -v make) ]] || printf "Error: make is not installed" || exit
[[ $(command -v cmake) ]] || printf "Error: cmake is not installed" || exit

[[ -d "libs" ]] || mkdir libs
[[ -d "libs/DPP" ]] || git clone https://github.com/brainboxdotcc/DPP.git libs/DPP
[[ -d "libs/soci" ]] || git clone https://github.com/SOCI/soci.git libs/soci

if [[ ! -d "libs/curlpp" ]]; then
  curl -sL "https://api.github.com/repos/jpbarrette/curlpp/releases/latest" | jq -r ".tarball_url" | wget -O a.tar.gz -i -

  if [[ ! -f "a.tar.gz" ]]; then
    printf "Error: Failed to download curlpp due to rate limits\n"
    exit
  fi

  tar -xzf curlpp.tar.gz
  mv jpbarrette-curlpp-* libs/curlpp
  rm curlpp.tar.gz
fi

[[ -d "$install_path" ]] || mkdir "$install_path"
cmake -S . -B "$install_path"
make -C "$install_path" -j"$core_count"
printf "Done!\n"