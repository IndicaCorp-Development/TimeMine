# TimeMine [![Build Status](https://travis-ci.com/IndicaCorp-Development/TimeMine.svg?branch=master)](https://travis-ci.com/IndicaCorp-Development/TimeMine)

#### About
Configure mineable blocks that automatically reset to the configured block after a 
period of time. Currently requires a MySQL server/database connection. I don't have 
plans on adding another database driver, and basic file storage is not efficient 
enough for the type of queries performed by this plugin.  
  
#### Support
**Inquiries:** inquiries@indicacorp.net or DM me on Discord @Pop#0001  
**Support:** discord.indicacorp.net  
**Issues/Requests:** Submit issues and feature request on GitHub  
**DO NOT EMAIL ME WITH ISSUES OR SUPPORT REQUESTS**  
  
#### Commands
* Base command: `/timemine` or `/tm`
* Help: `/tm help`
* Add a TimeMine block: `/tm <display_block> <expires_after (number of seconds)> <drop_item> [drop_item_count (default: 1)]`
* Remove a TimeMine block `/tm remove/delete`
* Remove all TimeMine blocks: `/tm removeall/deleteall`
* List TimeMine blocks: `/tm list`
* Display a TimeMine block info: `/tm info`
* Reset all TimeMine blocks: `/tm reset`
  
***Note:** `add`, `remove`, and `info` commands all require that you look at the block in question. You must be 30 blocks or closer for it to register.*  

#### Updating  
###### v1.1.0-beta:
Create a new column in the `timemine` table - `'dropItemRange' INT DEFAULT NULL`

#### Config
```yaml
# =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
# TimeMine Config <Version>
# =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
mysql:
  username: "username"
  password: "password"
  host: "localhost"
  port: "3306"
  database: "database"

# General config options
timemine:
  # In seconds. How often the plugin should check for expired TimeMine blocks. <30 is not recommended.
  expired_block_reset_interval: 30
  prefix: "[TimeMine]"

# Mineables config section
#   Format:
#     TOOL_ENUM: (Can be any item in the game that you want to be able to the specified blocks)
#       - BLOCK_ENUM (Must be a breakable block)
#       - ANOTHER_BLOCK_ENUM (Can list as many blocks as you want which should be mineable by this block)
# Defaults:
mineables:
  DIAMOND_PICKAXE:
    - DIAMOND_ORE
    - GOLD_ORE
    - IRON_ORE
    - COAL_ORE
    - EMERALD_ORE
    - LAPIS_ORE
    - NETHER_QUARTZ_ORE
    - REDSTONE_ORE
  GOLD_PICKAXE:
    - DIAMOND_ORE
    - GOLD_ORE
    - IRON_ORE
    - COAL_ORE
    - EMERALD_ORE
    - LAPIS_ORE
    - NETHER_QUARTZ_ORE
    - REDSTONE_ORE
  IRON_PICKAXE:
    - DIAMOND_ORE
    - GOLD_ORE
    - IRON_ORE
    - COAL_ORE
    - EMERALD_ORE
    - LAPIS_ORE
    - NETHER_QUARTZ_ORE
    - REDSTONE_ORE
  STONE_PICKAXE:
    - IRON_ORE
    - COAL_ORE
    - LAPIS_ORE
    - NETHER_QUARTZ_ORE
```
