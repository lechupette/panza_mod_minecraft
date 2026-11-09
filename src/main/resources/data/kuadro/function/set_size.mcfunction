scoreboard players operation #w kuadro.tmp = @s kuadro
scoreboard players operation #h kuadro.tmp = @s kuadro
scoreboard players set #10 kuadro.tmp 10
scoreboard players operation #w kuadro.tmp /= #10 kuadro.tmp
scoreboard players operation #h kuadro.tmp %= #10 kuadro.tmp
execute unless score #w kuadro.tmp matches 1..4 run return 0
execute unless score #h kuadro.tmp matches 1..4 run return 0
execute store result storage kuadro:tmp w int 1 run scoreboard players get #w kuadro.tmp
execute store result storage kuadro:tmp h int 1 run scoreboard players get #h kuadro.tmp
function kuadro:apply with storage kuadro:tmp
