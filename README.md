# Universal Graves

A grave can turn a lost inventory into a reason to make the journey back.

**This is R3Neer's fork of [Universal Graves by Patbox](https://github.com/Patbox/UniversalGraves), not an independently authored replacement.** Upstream authorship and licensing are retained.

Universal Graves stores death loot in configurable graves. Server owners decide how those graves look, how long they remain protected and what happens when they expire.

![Default grave appearance](https://i.imgur.com/rJoRmFT.png)

## Find your way back

Use **`/graves`** to see your graves. Before relying on one, ask your server owner about its protection and expiration rules: graves are **not necessarily permanent or private forever**.

Try a recovery trip in a test world before committing valuable equipment. For server owners, experimenting with a grave's name, appearance and messages is a useful place to start.

The **[reference guide](docs/GUIDE.md)** contains commands, permissions, configuration examples and model instructions. The README leaves those details there rather than presenting a configuration file as the introduction.

## Installation and scope

This public branch targets **Minecraft 26.2 on Fabric**. Follow the branch's [mod dependency declaration](src/main/resources/fabric.mod.json) and use the Java runtime required by that Minecraft version.

The mod can operate server-side; client-side model support is optional as described by upstream. Consult [upstream](https://github.com/Patbox/UniversalGraves) for its distributions and supported versions; this fork's documentation update does not create a new release or certify other loaders.

Back up worlds and configuration before changing versions or grave-expiration settings. The guide documents configurable behavior, not a promise that every server uses the same defaults.

## Go further

- [Reference guide / wiki](docs/GUIDE.md) — administration and customization.
- [Fork issues](https://github.com/R3Neer/UniversalGraves/issues)
- [Upstream project](https://github.com/Patbox/UniversalGraves) · [Patbox's projects](https://pb4.eu) · [Upstream Discord](https://pb4.eu/discord)
- [Older 1.19.4 documentation](https://github.com/Patbox/UniversalGraves/blob/1.19.4/README.md)

[GNU Lesser General Public License v3](LICENSE). Original mod by Patbox.
