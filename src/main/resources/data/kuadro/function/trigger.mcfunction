execute if score @s kuadro matches 1 run function kuadro:menu
execute if score @s kuadro matches 2 run function kuadro:ayuda
execute if score @s kuadro matches 3 run function kuadro:comandos
execute if score @s kuadro matches 11..44 run function kuadro:set_size
scoreboard players set @s kuadro 0
scoreboard players enable @s kuadro
