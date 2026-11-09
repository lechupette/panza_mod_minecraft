tellraw @s {text:"REFERENCIA DE FUNCIONES · KUADRO",color:"gold",bold:true}
tellraw @s {text:"/function kuadro:ayuda — explica fabricación, selección y colocación.",color:"white"}
tellraw @s {text:"/trigger kuadro set 1 — abre el selector; no entrega objetos por sí solo.",color:"white"}
tellraw @s {text:"/function kuadro:comandos — muestra esta referencia.",color:"white"}
tellraw @s {text:"La receta entrega un Kuadro 1x1 listo para colocar.",color:"white"}
tellraw @s {text:"Los botones del selector usan /trigger kuadro set 11 hasta 44 (ancho y alto), sin necesitar permisos de operador.",color:"white"}
tellraw @s {text:"La selección consume un Kuadro fabricado y devuelve uno de la medida elegida. Sin ese objeto, no hay entrega.",color:"yellow"}
tellraw @s ["",{text:"[Volver a ayuda]",color:"aqua",underlined:true,click_event:{action:"run_command",command:"/trigger kuadro set 2"}}]
