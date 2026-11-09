$execute store result score #kuadro_item kuadro.tmp run clear @s minecraft:painting[minecraft:custom_data~{kuadro:1}] 1
execute unless score #kuadro_item kuadro.tmp matches 1 run return 0
$give @s minecraft:painting[minecraft:custom_data={kuadro:1},minecraft:item_name={text:"Kuadro $(w)x$(h)",color:"gold",italic:false},minecraft:painting/variant="kuadro:k_$(w)x$(h)"] 1
$title @s actionbar {text:"Kuadro $(w)x$(h) configurado",color:"green"}
