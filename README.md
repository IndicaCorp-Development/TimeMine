# TimeMine [![Build Status](https://travis-ci.com/popatop15/TimeMine.svg?branch=master)](https://travis-ci.com/popatop15/TimeMine)

#### About
Configure mineable blocks that automatically reset to the configured ore after a 
period of time. Currently requires a MySQL server/databaseHelper connection. I don't have 
plans on adding another databaseHelper driver, and basic file storage is not efficient 
enough for the type of queries performed by this plugin. Feel free to make another
databaseHelper driver in a PR :)

#### Commands
* Base Command: `/timemine` or `/tm`
* Help: `/tm help`
* Add TimeMine Block: `/tm <display_block> <reset_interval> <drop_item> [drop_item_count | default: 1]`
* Remove TimeMine Block `/tm remove/delete`
* Remove All TimeMine Blocks: `/tm removeall/deleteall`
* List TimeMine Blocks: `/tm list`
* TimeMine Block Info: `/tm info`

***Note:** `add`, `remove`, and `info` commands all require that you look at the block in question. You must be 30 blocks or closer for it to register.*

#### Planned
* Add options to configure which tools can mine which items