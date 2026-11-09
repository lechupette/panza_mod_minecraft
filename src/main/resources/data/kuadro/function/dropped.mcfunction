$execute as @n[type=item,distance=..4,nbt={Age:0s,Item:{id:"minecraft:painting"}}] at @s run function kuadro:swap_drop {size:"$(size)",label:"$(label)"}
$execute as @n[type=item,distance=..4,nbt={Age:1s,Item:{id:"minecraft:painting"}}] at @s run function kuadro:swap_drop {size:"$(size)",label:"$(label)"}
$execute as @n[type=item,distance=..4,nbt={Age:2s,Item:{id:"minecraft:painting"}}] at @s run function kuadro:swap_drop {size:"$(size)",label:"$(label)"}
$execute as @n[type=item,distance=..4,nbt={Age:3s,Item:{id:"minecraft:painting"}}] at @s run function kuadro:swap_drop {size:"$(size)",label:"$(label)"}
kill @s
