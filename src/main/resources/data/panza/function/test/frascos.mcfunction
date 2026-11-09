scoreboard objectives add panza.test dummy
execute store result score #cod panza.test run clear @s ruletita:frasco_pez_cod 0
execute store result score #salmon panza.test run clear @s ruletita:frasco_pez_salmon 0
execute store result score #tropical panza.test run clear @s ruletita:frasco_pez_tropical 0
execute store result score #globo panza.test run clear @s ruletita:frasco_pez_globo 0
tellraw @s ["",{text:"[PANZA / FRASCOS] Bacalao: ",color:"yellow"},{score:{name:"#cod",objective:"panza.test"}},{text:" · Salmón: "},{score:{name:"#salmon",objective:"panza.test"}},{text:" · Tropical: "},{score:{name:"#tropical",objective:"panza.test"}},{text:" · Globo: "},{score:{name:"#globo",objective:"panza.test"}}]
tellraw @s {text:"La prueba cuenta unidades del inventario sin consumirlas.",color:"gray"}
