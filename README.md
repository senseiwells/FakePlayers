# PuppetPlayers

Modern, simple implementation of the fake players implemented originally in Carpet Mod.

There are some fundamental differences to how these fake players are implemented
in comparison to the original implementation in Carpet Mod.

This mod can be used alongside Carpet Mod, carpet's fake player actions *will* work
on these fake players; however, it's recommended to use the provided actions instead. 

The aim of this mod is to keep the behaviour of the fake players as accurate as
possible to a real player:
- All the player ticking is done in the correct tick phase.
- The client code for player actions is simulated on the server.
- Fake players send packets to interact with the server, like a real player.

## Usage

