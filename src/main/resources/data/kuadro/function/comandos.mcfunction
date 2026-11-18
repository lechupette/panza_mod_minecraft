tellraw @s {text:"KUADRO · REFERENCIA",color:"gold",bold:true}
tellraw @s {text:"Uso normal: Kuadro en la mano + clic derecho al aire. No hace falta escribir comandos.",color:"white"}
tellraw @s {text:"/function kuadro:ayuda · guía rápida.",color:"gray"}
tellraw @s {text:"/trigger kuadro set 1 · abre el selector (alternativa al clic derecho).",color:"gray"}
tellraw @s {text:"/trigger kuadro set 22 · fija 2×2 directamente (ancho y alto del 1 al 4).",color:"gray"}
tellraw @s ["",{text:"[Volver a ayuda]",color:"aqua",underlined:true,click_event:{action:"run_command",command:"/trigger kuadro set 2"}}]
