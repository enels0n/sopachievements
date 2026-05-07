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
- `block_place`
- `kill_entity`
- `craft_item`
- `fall_range`
- `pickup_item`
- `consume_item`
- `enchant_item`
- `tame_entity`
- `breed_entity`
- `enter_world`
- `chat`
- `execute_command`
- `damage_taken`
- `damage_dealt`
- `travel_distance`
- `fish_item`
- `harvest`

## Optional Conditions

Each achievement can also define an optional `conditions` block. Conditions are checked before progress is granted, so one trigger can be reused in different worlds, biomes, command flows, or PlaceholderAPI contexts.

Example:

```yml
command_sprinter:
  category: main
  title: "&aFast Travel Clerk"
  icon: CLOCK
  frame: task
  x: 14
  y: 2
  trigger:
    type: execute_command
    value: /spawn
    amount: 1
  conditions:
    type: all
    checks:
      - type: "number <="
        input: "%player_y%"
        output: "40"
```

Built-in context values currently available in conditions include:

- `%event_type%`
- `%player_name%`
- `%player_uuid%`
- `%player_world%`
- `%player_biome%`
- `%player_gamemode%`
- `%player_level%`
- `%player_food%`
- `%player_health%`
- `%player_x%`
- `%player_y%`
- `%player_z%`
- `%material%`
- `%entity_type%`
- `%command%`
- `%message%`
- `%environment%`
- `%damage_cause%`
- `%damage_amount%`
- `%distance%`
- `%amount%`
- `%fall_start_y%`
- `%fall_lowest_y%`

If PlaceholderAPI is installed, `%...%` values are also resolved through PAPI for the current player.

## Output

Final jar:

- `target/SopAchievements.jar`
