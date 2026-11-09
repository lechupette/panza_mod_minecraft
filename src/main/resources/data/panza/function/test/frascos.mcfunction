scoreboard objectives add panza.test dummy
execute store result score #empty panza.test run clear @s ruletita:frasco_vacio 0
execute store result score #formol panza.test run clear @s ruletita:frasco_formol 0
execute store result score #cod panza.test run clear @s ruletita:frasco_pez_cod 0
execute store result score #salmon panza.test run clear @s ruletita:frasco_pez_salmon 0
execute store result score #tropical panza.test run clear @s ruletita:frasco_pez_tropical 0
execute store result score #globo panza.test run clear @s ruletita:frasco_pez_globo 0
tellraw @s {text:"[PANZA / FRASCOS] Conteo de colección en el inventario (sin consumo):",color:"yellow"}
tellraw @s ["",{text:"Vacíos: "},{score:{name:"#empty",objective:"panza.test"}},{text:" · Formol: "},{score:{name:"#formol",objective:"panza.test"}}]
tellraw @s ["",{text:"Bacalao: "},{score:{name:"#cod",objective:"panza.test"}},{text:" · Salmón: "},{score:{name:"#salmon",objective:"panza.test"}}]
tellraw @s ["",{text:"Tropical: "},{score:{name:"#tropical",objective:"panza.test"}},{text:" · Globo: "},{score:{name:"#globo",objective:"panza.test"}}]
tellraw @s {text:"Los frascos son bloques decorativos: colocá uno para exhibirlo y rompelo para recuperarlo.",color:"gray"}
