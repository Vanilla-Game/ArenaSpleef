# ArenaSpleef

A spleef plugin using [BattleArena](https://github.com/BattlePlugins/BattleArena)

ArenaSpleef is the classic spleef plugin that dates back to the early days of Minecraft, but features multiple modes!

## Spleef Modes
- **Classic**: The classic spleef mode where players are given a shovel to break blocks and knock other players into the void.
- **Splegg**: A spleef mode where players are given an egg cannon to shoot eggs at the ground to break blocks.
- **Decay**: A spleef mode where blocks decay underneath the player. This is commonly played as "TNT Run", but any block can be used.
- **Bow Spleef**: A spleef mode where players are given a bow to shoot arrows at the ground to break blocks. 

For examples configurations of these modes, check out the [templates](https://github.com/BattlePlugins/ArenaSpleef/tree/master/templates) folder.

## Documentation
Full documentation for ArenaSpleef can be found on the [BattleDocs](https://docs.battleplugins.org/books/additional-gamemodes/chapter/spleef) website.

### Leaving the map during a match

`leave-on-boundary-exit: true` (enabled by default) checks active participants
every 20 server ticks during the `ingame` phase. A participant outside the map's
`bounds`, or in a different world, leaves through BattleArena's normal leave API.
At 20 TPS this takes up to one second after leaving. Only the position at the
time of the check matters; a player who returns before that check stays in the match.
Spectators and players in waiting, countdown, or victory are not checked.
Maps without bounds are skipped. Set the option to `false` to disable this behavior.

This works alongside `boundary-enforcer`: keep that module enabled to prevent
walking across the boundary. The periodic check handles participants who still
end up outside, for example after a teleport.
The usual `on-leave` actions still run. In particular, `restore{types=all}` also
restores the saved location, so leaving via `/home` or `/spawn` can return the
player to their pre-match location. Cancelled teleports do not cause an exit
when the player remains inside the map.

## Commands
| Command                               | Description                                    |
|---------------------------------------|------------------------------------------------|
| /spleef deathregion <map> <region>    | Sets the death region for a spleef arena.      |
| /spleef layer add <map>               | Adds a layer to a spleef arena.                |
| /spleef layer remove <map> <index>    | Removes a layer from a spleef arena.           |
| /spleef layer clear <map>             | Clears all layers from a spleef arena.         |
| /spleef layer index <map> <from> <to> | Changes the index of a layer.                  |
| /spleef layer list <map>              | Lists all layers in a spleef arena.            |

## Permissions
| Permission                              | Command              |
|-----------------------------------------|----------------------|
| battlearena.command.spleef.deathregion  | /spleef deathregion  |
| battlearena.command.spleef.layer.add    | /spleef layer add    |
| battlearena.command.spleef.layer.remove | /spleef layer remove |
| battlearena.command.spleef.layer.clear  | /spleef layer clear  |
| battlearena.command.spleef.layer.index  | /spleef layer index  |
| battlearena.command.spleef.layer.list   | /spleef layer list   |

## Links
- Website: [https://www.battleplugins.org](https://www.battleplugins.org)
- Download: [https://modrinth.com/plugin/arenaspleef](https://modrinth.com/plugin/arenaspleef)
- Discord: [BattlePlugins Discord](https://discord.com/invite/J3Hjjb8)
