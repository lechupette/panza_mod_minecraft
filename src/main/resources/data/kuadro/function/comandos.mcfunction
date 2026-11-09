tellraw @s {text:"REFERENCIA DE FUNCIONES · KUADRO",color:"gold",bold:true}
tellraw @s {text:"/function kuadro:ayuda — explica fabricación, selección y colocación.",color:"white"}
tellraw @s {text:"/function kuadro:menu — abre el selector; no entrega objetos por sí solo.",color:"white"}
tellraw @s {text:"/function kuadro:comandos — muestra esta referencia.",color:"white"}
tellraw @s {text:"/trigger kuadro set 1 — abre el selector.",color:"white"}
tellraw @s {text:"/trigger kuadro set 11, 12 ... 44 — configura un Kuadro fabricado.",color:"white"}
tellraw @s {text:"El comando consume un Kuadro sin configurar y devuelve uno del tamaño elegido. Sin Kuadro en el inventario, no entrega nada.",color:"yellow"}
tellraw @s ["",{text:"[Volver a ayuda]",color:"aqua",underlined:true,click_event:{action:"run_command",command:"function kuadro:ayuda"}}]
