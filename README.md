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
- `smelt_item`
- `player_advancement`
- `inventory_contains`
- `death`
- `sleep`
- `totem_use`
- `bucket`
- `projectile_kill`
- `potion_effect`
- `brew_item`
- `raid_win`
- `villager_trade`
- `equip`
- `loot_container`
- `biome_stay`
- `location_range`
- `lightning_strike`
- `weather`
- `underwater_time`
- `glide_distance`
- `vehicle_distance`
- `moon_phase`
- `time_window`
- `effect_combo`
- `armorless_death`
- `villager_trade_profession`

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

### Ordered Session Chains

Multi-criteria achievements can also run as ordered chains.

Supported chain options:

- `ordered: true`
- `reset-on-death: true`
- `reset-on-world-change: true`
- `reset-on-teleport: true`

When `ordered: true` is enabled:

- only the current step in the chain can gain progress
- later steps are ignored until earlier ones are completed
- configured reset events wipe unfinished chain progress for that achievement

Example:

```yml
nether_escape_route:
  category: main
  title: "&5Nether Escape Route"
  icon: CRYING_OBSIDIAN
  requirements:
    type: all
    ordered: true
    reset-on-death: true
    reset-on-world-change: true
    reset-on-teleport: true
    criteria:
      enter_nether:
        trigger:
          type: enter_world
          environment: NETHER
          amount: 1
      use_spawn:
        trigger:
          type: execute_command
          value: /spawn
          amount: 1
```

## Translation Keys

Achievement display text can use resource-pack translation keys instead of hardcoded strings.

Supported fields:

- `key-name`
- `key-name-fallback`
- `key-lore`
- `key-lore-fallback`

If a key is set, `SopAchievements` writes the advancement display JSON as a translatable component. If no key is set, it falls back to the normal `title` and `description`.

Example:

```yml
first_join:
  title: "&aFirst Steps"
  description: "&7Join the server once."
  key-name: achivka.main.first_join.name
  key-name-fallback: First Steps
  key-lore: achivka.main.first_join.lore
  key-lore-fallback: Join the server once.
```

Categories can define the same translation keys for the root advancement tab display:

- `categories.<id>.key-name`
- `categories.<id>.key-name-fallback`
- `categories.<id>.key-lore`
- `categories.<id>.key-lore-fallback`

If a root achievement belongs to that category, category keys take priority for the root tab title and description.

## Additional Trigger Notes

`smelt_item`
- increments when a player extracts items from a furnace

`player_advancement`
- reacts to a completed vanilla or external advancement key
- example value: `minecraft:story/enter_the_nether`

`inventory_contains`
- checked by the periodic sync task
- completes when the player holds at least the configured amount of an item at once

`death`
- uses the player death cause name as its `value`
- example values: `LAVA`, `FALL`, `ENTITY_ATTACK`

`sleep`
- increments when the player successfully enters a bed

`totem_use`
- increments on `EntityResurrectEvent` for players

`bucket`
- listens to fill/empty bucket events
- use `value` for the bucket item, for example `WATER_BUCKET`
- use `%bucket_action%` in conditions if you want only `FILL` or only `EMPTY`

`projectile_kill`
- reacts to kills made by player-shot projectiles
- use `value` for the killed entity
- use `projectile` for the projectile type, for example `ARROW`

`potion_effect`
- reacts when a player gains or changes to a new potion effect
- use the potion effect name in `value`, for example `SPEED` or `HERO_OF_THE_VILLAGE`

`brew_item`
- reacts when brewing stand contents finish brewing
- best used with values like `POTION`, `SPLASH_POTION`, `LINGERING_POTION`

`raid_win`
- reacts when the player is listed among raid winners

`villager_trade`
- reacts when a player takes the result item from a villager trade
- use `value` for the traded result item, for example `EMERALD`

`equip`
- checked by the periodic sync task
- completes when the player has the target item equipped in armor or hands

`loot_container`
- reacts when a player opens an inventory
- use values like `CHEST`, `BARREL`, `SHULKER_BOX`

`biome_stay`
- measured in seconds while the player remains in the same biome
- use `value` for the biome name, for example `SWAMP`

`location_range`
- checked by the periodic sync task
- supports `world`, `x-min`, `x-max`, `y-min`, `y-max`, `z-min`, `z-max`

`lightning_strike`
- reacts when a player takes lightning damage

`weather`
- checked by the periodic sync task
- uses `CLEAR`, `RAIN`, or `THUNDER`

`underwater_time`
- checked by the periodic sync task
- increments while the player's eye position is underwater

`glide_distance`
- increments traveled distance while gliding with elytra

`vehicle_distance`
- increments traveled distance while inside a vehicle
- optional `vehicle` filter, for example `MINECART`, `BOAT`, `HORSE`

`moon_phase`
- checked by the periodic sync task
- values include `FULL_MOON`, `NEW_MOON`, `FIRST_QUARTER`, `LAST_QUARTER`, `WAXING_GIBBOUS`, `WANING_GIBBOUS`, `WAXING_CRESCENT`, `WANING_CRESCENT`

`time_window`
- checked by the periodic sync task
- supports `time-min` and `time-max`
- works with wrapped ranges too, for example night from `13000` to `23000`

`effect_combo`
- checked by the periodic sync task
- `value` is a comma-separated list of potion effect names that must all be active at once

`armorless_death`
- reacts when the player dies without armor equipped
- uses the same death-cause matching as `death`

`villager_trade_profession`
- trade trigger with extra villager filters
- supports `profession` and `level`

## Output

Final jar:

- `target/SopAchievements.jar`
