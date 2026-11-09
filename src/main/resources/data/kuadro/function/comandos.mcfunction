tellraw @s {text:"REFERENCIA DE COMANDOS · KUADRO",color:"gold",bold:true}
tellraw @s {text:"/function kuadro:ayuda — abre la ayuda general.",color:"white"}
tellraw @s {text:"/function kuadro:menu — abre el selector de medidas.",color:"white"}
tellraw @s {text:"/function kuadro:comandos — muestra esta referencia.",color:"white"}
tellraw @s {text:"/trigger kuadro set 1 — abre el selector mediante el objetivo de activación.",color:"white"}
tellraw @s {text:"/trigger kuadro set 11 a 44 — entrega un Kuadro con ancho y alto indicados.",color:"white"}
tellraw @s {text:"Ejemplo: /trigger kuadro set 32 entrega un Kuadro de 3×2.",color:"gray",italic:true}
tellraw @s {text:"La selección ya no requiere tener un cuadro en la mano: el objeto configurado aparece en el inventario.",color:"white"}
tellraw @s ["",{text:"[Volver]",color:"yellow",underlined:true,click_event:{action:"run_command",command:"function kuadro:ayuda"}}]
