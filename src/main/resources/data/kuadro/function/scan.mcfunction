scoreboard players set #t kuadro.tmp 0
execute as @e[type=marker,tag=kuadro_mem] at @s unless entity @e[type=painting,distance=..0.3] run function kuadro:dropped with entity @s data
execute as @a at @s as @e[type=painting,tag=!kuadro_seen,distance=..16] at @s run function kuadro:new_painting
