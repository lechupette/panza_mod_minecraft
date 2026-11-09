scoreboard objectives add panza.test dummy
execute store result score #food panza.test run data get entity @s foodLevel 1
tellraw @s ["",{text:"[PANZA / HAMBRE] Nivel actual: ",color:"yellow"},{score:{name:"#food",objective:"panza.test"},color:"white"},{text:" / 20",color:"gray"}]
