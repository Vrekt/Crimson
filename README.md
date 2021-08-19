# What is Crimson?
Crimson is a work-in-progress server for Minecraft: Bedrock Edition.

**Crimson is built from scratch, with the help of a few resources, these include:**
    
    - Cloudburst Networking components
        - https://github.com/CloudburstMC/Network
    - Cloudburt Nukkit
        - https://github.com/CloudburstMC/Nukkit
    - The bedrock protocol on wiki.vg
        - https://wiki.vg/Raknet_Protoco
        - https://wiki.vg/Bedrock_Protocol


**Some goals of Crimson include:**
    
    - Easy to work with, fluent and fully featured API.
    - Great networking and server performance.
    - Extensive configuration options
    - Extensive documentation

**What is currently implemented?**
    
    - Partial RakNet protocol
        - C→S: Open Connection Request 1
        - S→C: Open Connection Reply 1
        - C→S: Open Connection Request 2
        - S→C: Open Connection Reply 2
        - C→S: Connection Request
        - MOTD Pings.
    - An imcomplete basic API
        - BanLists
        - Basic permission system
        - Console input
        - server.properties
        - Fluent system for building MOTD pings.
