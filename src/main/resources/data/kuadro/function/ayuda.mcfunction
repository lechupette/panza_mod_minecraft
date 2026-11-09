tellraw @s {text:"━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",color:"dark_gray"}
tellraw @s {text:"AYUDA DEL SISTEMA KUADRO",color:"gold",bold:true}
tellraw @s {text:"1. Fabricá un Kuadro. La receta entrega por defecto un Kuadro de 1x1, listo para colocar.",color:"white"}
tellraw @s {text:"2. Si querés otra medida, abrí el selector y elegí ancho y alto. Se consume el Kuadro que tenés y se devuelve configurado.",color:"white"}
tellraw @s {text:"3. Colocá el Kuadro como cualquier cuadro. No necesitás golpear bloques ni escribir medidas en el chat.",color:"white"}
tellraw @s {text:"El selector no crea Kuadros: requiere una unidad fabricada en tu inventario.",color:"yellow"}
tellraw @s ["",{text:"[ABRIR SELECTOR]",color:"aqua",underlined:true,click_event:{action:"run_command",command:"/trigger kuadro set 1"}}]
tellraw @s ["",{text:"[COMANDOS Y FUNCIONES]",color:"aqua",underlined:true,click_event:{action:"run_command",command:"/trigger kuadro set 3"}}]
tellraw @s {text:"━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",color:"dark_gray"}
