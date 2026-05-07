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
- `statistic`

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

## Rewards

Achievements can also execute reward actions on first completion only.

Example:

```yml
sky_to_bottom:
  trigger:
    type: fall_range
    start-y-min: 250
    end-y-max: 5
  rewards:
    message: "&6Challenge complete: %achievement_title%"
    commands:
      - "[console] give %player_name% diamond 3"
      - "[player] me just survived the abyss"
```

Supported reward placeholders currently include:

- `%player_name%`
- `%player_uuid%`
- `%achievement_id%`
- `%achievement_title%`
- `%achievement_description%`
- `%achievement_category%`

Reward commands support:

- `[console] ...`
- `[player] ...`
- or plain commands, which default to console execution

## Statistic Trigger

`statistic` achievements read vanilla Bukkit statistics on a configurable interval instead of listening to a dedicated event.

Example:

```yml
jumper:
  category: main
  title: "&eJumping Bean"
  icon: RABBIT_FOOT
  trigger:
    type: statistic
    statistic: JUMP
    amount: 250
```

Typed statistics are also supported with extra keys when Bukkit requires them:

```yml
zombie_hunter:
  trigger:
    type: statistic
    statistic: KILL_ENTITY
    entity-type: ZOMBIE
    amount: 25
```

```yml
diamond_digger:
  trigger:
    type: statistic
    statistic: MINE_BLOCK
    material: DIAMOND_ORE
    amount: 12
```

## Multi-Criteria Requirements

Achievements can also define multiple criteria under `requirements.criteria`.

`type: all`
- every criterion must be completed

`type: any`
- any one criterion is enough

Example:

```yml
hunter_training:
  category: main
  title: "&6Hunter Training"
  icon: CROSSBOW
  requirements:
    type: all
    criteria:
      zombies:
        trigger:
          type: kill_entity
          value: ZOMBIE
          amount: 10
      skeletons:
        trigger:
          type: kill_entity
          value: SKELETON
          amount: 5
```

If `requirements.criteria` is omitted, the old single `trigger` format still works unchanged.

## Output

Final jar:

- `target/SopAchievements.jar`
