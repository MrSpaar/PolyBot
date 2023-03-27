#bin/bash

while [ $# -gt 0 ]; do
  case "$1" in
    -I=*) install_path="${1#*=}" ;;
    -C=*) cores="${1#*=}" ;;
    *)
      printf "Error: Unknown option: %s" "$1"
      exit 1
  esac
  shift
done

[[ -z "$cores" ]] && cores=$(nproc)
[[ -z "$install_path" ]] && install_path="build"

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
make -C "$install_path" -j"$cores"
printf "Done!\n"