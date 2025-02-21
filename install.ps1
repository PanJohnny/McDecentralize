# PowerShell script to install McDecentralize on Windows
Write-Host "McDecentralize installer v1.0"
Write-Host "Platform: Windows"
Write-Host "If this script does not work, use the jar file provided in the releases"
Write-Host "https://github.com/PanJohnny/McDecentralize/releases"

# Get the latest release tag from GitHub
$latestRelease = Invoke-RestMethod -Uri "https://api.github.com/repos/PanJohnny/McDecentralize/releases/latest"
$latestTag = $latestRelease.tag_name
Write-Host "Latest tag: $latestTag"

# Set download URL
$downloadUrl = "https://github.com/PanJohnny/McDecentralize/releases/download/$latestTag/mcdec-windows.exe"
$destinationPath = "$env:USERPROFILE\mcdec.exe"

# Download the native image
Write-Host "Downloading the native image from the GitHub releases"
Invoke-WebRequest -Uri $downloadUrl -OutFile $destinationPath -ErrorAction Stop

# Add the binary to the system PATH
$path = [System.Environment]::GetEnvironmentVariable("Path", [System.EnvironmentVariableTarget]::User)
if ($path -notlike "*$env:USERPROFILE*") {
    [System.Environment]::SetEnvironmentVariable("Path", "$path;$env:USERPROFILE", [System.EnvironmentVariableTarget]::User)
}

Write-Host "Installation successful"
Write-Host "You can now run the mcdec command from anywhere in a new terminal"