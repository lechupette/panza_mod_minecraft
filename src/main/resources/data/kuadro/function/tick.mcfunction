scoreboard players enable @a kuadro
execute as @a[scores={kuadro=1..}] run function kuadro:trigger
execute as @e[type=marker,tag=kuadro_mem] at @s unless entity @e[type=painting,distance=..0.3] run function kuadro:dropped with entity @s data
scoreboard players add #t kuadro.tmp 1
execute if score #t kuadro.tmp matches 5.. run function kuadro:scan
