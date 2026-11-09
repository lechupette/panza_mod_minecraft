tellraw @s {text:"REFERENCIA DE COMANDOS · KUADRO",color:"gold",bold:true}
tellraw @s {text:"/function kuadro:ayuda — abre esta ayuda.",color:"white"}
tellraw @s {text:"/function kuadro:menu — abre el selector de medidas.",color:"white"}
tellraw @s {text:"/trigger kuadro set 1 — abre el selector mediante el objetivo de activación.",color:"white"}
tellraw @s {text:"/trigger kuadro set 11 a 44 — solicita ancho y alto; el primer dígito es el ancho y el segundo la altura.",color:"white"}
tellraw @s {text:"Ejemplo: /trigger kuadro set 32 selecciona un Kuadro de 3×2.",color:"gray",italic:true}
tellraw @s ["",{text:"[Volver]",color:"yellow",underlined:true,click_event:{action:"run_command",command:"function kuadro:ayuda"}}]
