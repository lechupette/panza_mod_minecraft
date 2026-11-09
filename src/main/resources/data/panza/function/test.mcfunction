scoreboard objectives add panza.test dummy
tellraw @s {text:"━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",color:"dark_gray"}
tellraw @s {text:"DIAGNÓSTICO DEL MOD PANZA",color:"gold",bold:true}
tellraw @s {text:"Comprobaciones no destructivas del estado actual del jugador.",color:"gray"}
function panza:test/grasa
function panza:test/hambre
function panza:test/cafe
function panza:test/frascos
tellraw @s {text:"Diagnóstico terminado. Podés repetir cada prueba por separado con /function panza:test/...",color:"green"}
tellraw @s {text:"━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",color:"dark_gray"}
