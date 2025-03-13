# McDecentralize
McDecentralize is a project that aims to decentralize the Minecraft hosting space. 
It is useful for people who don't have the capacity to host a Minecraft server 24/7, but still want to play with their friends.
This project can help you easily synchronize your whole server across multiple devices, so your friend group can take turns in hosting the server.

## Use case example
Player A starts a server with player B. Player A can occasionally play only in the late evenings, while player B has more free time. Player B can host the server during the day, and player A can host the server during the night. The server files are synchronized across both devices, so the players can continue playing where they left off.

## How it works
McDecentralize uses a synchronization provider in order to synchronize the server files across multiple devices. Currently, the only supported provider is rclone.

## Prerequisites
- rclone installed and configured: https://rclone.org
- java for running the Minecraft server

> [!NOTE]
> This project has been tested on Linux and Windows. MacOS is supported but the native build was not tested.

## Setup
### On Linux/MacOS
Run the following command in the terminal to install the project:
```bash
curl -sSL https://raw.githubusercontent.com/PanJohnny/McDecentralize/refs/heads/master/install.sh | bash
```

### On Windows
Run the following command in powershell to install the project:
```powershell
iwr https://raw.githubusercontent.com/PanJohnny/McDecentralize/refs/heads/master/install.ps1 -useb | iex
```

If there is a error with the execution policy, you can run the following command to bypass it:
```powershell
Set-ExecutionPolicy Bypass -Scope Process -Force
```
And then run the installation command again.

### Jar file
You can also download the jar file from the [releases page](https://raw.githubusercontent.com/PanJohnny/McDecentralize/releases)

### Portable executable
Portable executable are available on the [releases page](https://raw.githubusercontent.com/PanJohnny/McDecentralize/releases)

### Building from source
You can also build the project from source. To do that, you need to have maven and graalvm (jdk 23) installed. Then, you can run the following command:
```bash
mvn clean package
```

## Usage
> [!NOTE]
> This project currently only supports fabric servers.
### Initializing McDecentralize in a directory
If you want to use your existing server files, rename your server jar to `server.jar`. In a directory where the server should be located (does not have to be there, everything will be downloaded if it is not), run the following command:
```bash
mcdec init
```
Go through the setup process and configure the synchronization provider.
> [!NOTE]
> Only rclone is supported at the moment.

### Synchronizing the server
To synchronize the server, run the following command:
```bash
mcdec sync [up|down]
```
Up is for uploading the server files to the remote storage, and down is for downloading the server files from the remote storage. Before hosting the server on another machine you need to run up on your machine and down on the other machine. This will synchronize the server files across all devices.
> [!IMPORTANT]
> Synchronization can delete your local changes, make sure that you are not ahead of the remote storage (sync all the time).

### Running the server
To run the server, run the following command:
```bash
mcdec run
```
This just executes your local server jar file. You can also run the server manually.

### Sharing your configuration with others
If you want to share your configuration with others, you can run the following command:
```bash
mcdec share
```
This commands will pack mods.txt into the new configuration file for better sharing, and it resets some system specific configurations.

### Get help
To get help, run mcdec with the `-h` flag:
```bash
mcdec -h
```
Alternatively, this info will also be displayed if you run the command without any arguments.

Have fun playing with your friends!

## Contributing
If you want to contribute to the project, feel free to open a pull request.

### Adding synchronization providers
Create a class extending `SyncProvider` and implement the methods. Then, add the provider to the `SyncProviders` factory registry. For an example, see the `RCloneSyncProvider` class.

### Adding server types
Create a class extending `ServerList` and implement the methods. Then, add the server type to the `ServerManager`s SERVER_LISTS list. For an example, see the `FabricServerList` class.

## Notes
This project is a weekend project, so do not expect it to be perfect. If you find any bugs, feel free to open an issue.