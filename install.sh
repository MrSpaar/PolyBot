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

[[ $(command -v jq) ]] || printf "Error: jq is not installed\n" || error=1
[[ $(command -v wget) ]] || printf "Error: wget is not installed\n" || error=1
[[ $(command -v git) ]] || printf "Error: git is not installed\n" || error=1
[[ $(command -v g++) ]] || printf "Error: g++ is not installed\n" || error=1
[[ $(command -v make) ]] || printf "Error: make is not installed\n" || error=1
[[ $(command -v cmake) ]] || printf "Error: cmake is not installed\n" || error=1
[[ -z "$error" ]] || exit

printf "Installing to %s with %s cores\n" "$install_path" "$cores"

[[ -d "libs" ]] || mkdir libs
[[ -d "libs/soci" ]] || git clone https://github.com/SOCI/soci.git libs/soci

if [[ ! -d "libs/DPP" ]]; then
  curl -sL "https://api.github.com/repos/brainboxdotcc/DPP/releases/latest" \
        | jq -r ".tarball_url" \
        | wget -O DPP.tar.gz -i - \
        || (printf "Error: Failed to download DPP\n" && exit)

  tar -xzf DPP.tar.gz || (printf "Error: Failed to extract DPP\n" && exit)
  mv brainboxdotcc-DPP-* libs/DPP || (printf "Error: Failed to rename extracted directory\n" && exit)
  rm DPP.tar.gz
fi

if [[ ! -d "libs/curlpp" ]]; then
  curl -sL "https://api.github.com/repos/jpbarrette/curlpp/releases/latest" \
        | jq -r ".tarball_url" \
        | wget -O curlpp.tar.gz -i - \
        || (printf "Error: Failed to download curlpp\n" && exit)

  tar -xzf curlpp.tar.gz || (printf "Error: Failed to extract curlpp\n" && exit)
  mv jpbarrette-curlpp-* libs/curlpp || (printf "Error: Failed to rename extracted directory\n" && exit)
  rm curlpp.tar.gz
fi

[[ -d "$install_path" ]] || mkdir "$install_path"
cmake -S . -B "$install_path" || (printf "Error: Failed to run cmake\n" && exit)
make -C "$install_path" -j"$cores" || (printf "Error: Failed to run make\n" && exit)
printf "Done, an executable \"PolyBot\" has been generated in %s\n" "$install_path"
