#!/bin/bash
# This script installs the native image from GitHub releases and adds it to the path
echo "McDecentralize installer v1.1"
echo "Detecting platform..."

# Detect OS
OS_TYPE=$(uname -s)
if [[ "$OS_TYPE" == "Linux" ]]; then
    PLATFORM="linux"
elif [[ "$OS_TYPE" == "Darwin" ]]; then
    PLATFORM="macos"
else
    echo "Unsupported OS: $OS_TYPE"
    exit 1
fi

echo "Platform detected: $PLATFORM"
echo "If this script does not work, use the jar file provided in the releases"
echo "https://github.com/PanJohnny/McDecentralize/releases"

# Get the latest tag from GitHub releases
LATEST_TAG=$(curl --silent "https://api.github.com/repos/PanJohnny/McDecentralize/releases/latest" | grep -Po '"tag_name": "\K.*?(?=")')
echo "Latest tag: $LATEST_TAG"

# Download the native image from GitHub releases
echo "Downloading the native image for $PLATFORM"
curl -L "https://github.com/PanJohnny/McDecentralize/releases/download/${LATEST_TAG}/mcdec-${PLATFORM}" -o mcdec || { echo "Download failed"; exit 1; }
chmod +x mcdec

# Move the native image to the appropriate system directory
if [[ "$PLATFORM" == "linux" ]]; then
    INSTALL_PATH="/usr/bin"
elif [[ "$PLATFORM" == "macos" ]]; then
    INSTALL_PATH="/usr/local/bin"
fi

echo "Moving the native image to $INSTALL_PATH"
sudo mv mcdec $INSTALL_PATH || { echo "Move failed"; exit 1; }

echo "Installation successful"
echo "You can now run the mcdec command from anywhere"
echo "To uninstall, simply delete the mcdec file from $INSTALL_PATH"