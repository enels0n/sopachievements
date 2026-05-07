# SopAchievements

`SopAchievements` is a custom achievements plugin that exposes achievements in the vanilla `L` advancements menu while validating completion in plugin code instead of datapack logic.

## Goals

- show custom achievement tabs and entries in the vanilla advancements screen
- keep achievement logic in plugin code
- support simple and complex checks without tick-heavy brute force validation
- stay friendly to the main target versions:
  - `1.16.5`
  - `1.21.1`
  - `1.21.11`

## Current Foundation

The first working layer includes:

- achievement loading from `achievements.yml`
- registration as in-game advancements
- plugin-side completion checks
- progress storage in player `PersistentDataContainer`
- reload command

## Implemented Trigger Types

- `join`
- `break_block`
- `kill_entity`
- `craft_item`
- `fall_range`

## Output

Final jar:

- `target/SopAchievements.jar`
