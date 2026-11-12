scoreboard players enable @a kuadro
execute as @a[scores={kuadro=1..}] run function kuadro:trigger
scoreboard players add #t kuadro.tmp 1
execute if score #t kuadro.tmp matches 5.. run function kuadro:scan
