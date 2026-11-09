execute if entity @s[nbt={ActiveEffects:[{id:"minecraft:speed"}]}] run tellraw @s {text:"[PANZA / CAFÉ] Efecto de velocidad detectado.",color:"green"}
execute unless entity @s[nbt={ActiveEffects:[{id:"minecraft:speed"}]}] run tellraw @s {text:"[PANZA / CAFÉ] No se detecta velocidad activa. Tomá una taza de café para probar sus efectos.",color:"gray"}
execute if entity @s[nbt={ActiveEffects:[{id:"minecraft:jump_boost"}]}] run tellraw @s {text:"[PANZA / CAFÉ] Efecto de salto detectado.",color:"green"}
execute if entity @s[nbt={ActiveEffects:[{id:"minecraft:haste"}]}] run tellraw @s {text:"[PANZA / CAFÉ] Efecto de prisa minera detectado.",color:"green"}
