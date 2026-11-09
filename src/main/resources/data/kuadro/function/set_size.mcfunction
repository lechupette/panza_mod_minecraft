scoreboard players operation #w kuadro.tmp = @s kuadro
scoreboard players operation #h kuadro.tmp = @s kuadro
scoreboard players set #10 kuadro.tmp 10
scoreboard players operation #w kuadro.tmp /= #10 kuadro.tmp
scoreboard players operation #h kuadro.tmp %= #10 kuadro.tmp
execute unless score #w kuadro.tmp matches 1..4 run return 0
execute unless score #h kuadro.tmp matches 1..4 run return 0
execute unless items entity @s weapon.mainhand minecraft:painting[minecraft:custom_data~{kuadro:1b}] run return run tellraw @s {text:"Sostén un Kuadro en la mano principal.",color:"red"}
execute store result storage kuadro:tmp w int 1 run scoreboard players get #w kuadro.tmp
execute store result storage kuadro:tmp h int 1 run scoreboard players get #h kuadro.tmp
execute store result storage kuadro:tmp count int 1 run data get entity @s SelectedItem.count
function kuadro:apply with storage kuadro:tmp
