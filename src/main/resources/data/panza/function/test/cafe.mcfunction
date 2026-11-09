scoreboard objectives add panza.test dummy
execute store result score #berries panza.test run clear @s ruletita:cerezas_cafe 0
execute store result score #roast panza.test run clear @s ruletita:cafe_tostado 0
execute store result score #roast2 panza.test run clear @s ruletita:cafe_tostado2 0
execute store result score #ground panza.test run clear @s ruletita:cafe_molido 0
execute store result score #cups panza.test run clear @s ruletita:taza 0
execute store result score #coffee panza.test run clear @s ruletita:taza_cafe 0
tellraw @s {text:"[PANZA / CAFÉ] Inventario de preparación (conteo sin consumo):",color:"yellow"}
tellraw @s ["",{text:"Cerezas: "},{score:{name:"#berries",objective:"panza.test"}},{text:" · Café tostado: "},{score:{name:"#roast",objective:"panza.test"}},{text:" · Tostado alternativo: "},{score:{name:"#roast2",objective:"panza.test"}}]
tellraw @s ["",{text:"Molido: "},{score:{name:"#ground",objective:"panza.test"}},{text:" · Tazas vacías: "},{score:{name:"#cups",objective:"panza.test"}},{text:" · Tazas preparadas: "},{score:{name:"#coffee",objective:"panza.test"}}]
execute if entity @s[nbt={ActiveEffects:[{id:"minecraft:speed"}]}] run tellraw @s {text:"[PANZA / CAFÉ] Efecto de velocidad detectado.",color:"green"}
execute unless entity @s[nbt={ActiveEffects:[{id:"minecraft:speed"}]}] run tellraw @s {text:"[PANZA / CAFÉ] No se detecta velocidad activa.",color:"gray"}
execute if entity @s[nbt={ActiveEffects:[{id:"minecraft:jump_boost"}]}] run tellraw @s {text:"[PANZA / CAFÉ] Efecto de salto detectado.",color:"green"}
execute if entity @s[nbt={ActiveEffects:[{id:"minecraft:haste"}]}] run tellraw @s {text:"[PANZA / CAFÉ] Efecto de prisa minera detectado.",color:"green"}
