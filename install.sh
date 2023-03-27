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

[[ $(command -v jq) ]] || printf "Error: jq is not installed" || exit
[[ $(command -v wget) ]] || printf "Error: wget is not installed" || exit
[[ $(command -v git) ]] || printf "Error: git is not installed" || exit
[[ $(command -v g++) ]] || printf "Error: g++ is not installed" || exit
[[ $(command -v make) ]] || printf "Error: make is not installed" || exit
[[ $(command -v cmake) ]] || printf "Error: cmake is not installed" || exit

printf "Installing to %s with %s cores" "$install_path" "$cores"

[[ -d "libs" ]] || mkdir libs
[[ -d "libs/DPP" ]] || git clone https://github.com/brainboxdotcc/DPP.git libs/DPP
[[ -d "libs/soci" ]] || git clone https://github.com/SOCI/soci.git libs/soci

if [[ ! -d "libs/curlpp" ]]; then
  curl -sL "https://api.github.com/repos/jpbarrette/curlpp/releases/latest"
        /| jq -r ".tarball_url"
        /| wget -O curlpp.tar.gz -i -
        /||  printf "Error: Failed to download curlpp" || exit

  tar -xzf curlpp.tar.gz || printf "Error: Failed to extract curlpp" || exit
  mv jpbarrette-curlpp-* libs/curlpp || printf "Error: Failed to rename extracted directory" || exit
  rm curlpp.tar.gz
fi

[[ -d "$install_path" ]] || mkdir "$install_path"
cmake -S . -B "$install_path" || printf "Error: Failed to run cmake" || exit
make -C "$install_path" -j"$cores" || printf "Error: Failed to run make" || exit
printf "Done!\n"