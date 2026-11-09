tellraw @s {text:"━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",color:"dark_gray"}
tellraw @s {text:"AYUDA DEL SISTEMA KUADRO",color:"gold",bold:true}
tellraw @s {text:"1. Fabricá un Kuadro con su receta. La fabricación entrega una unidad sin configurar.",color:"white"}
tellraw @s {text:"2. Se abrirá el selector. Elegí ancho y alto; se consume ese Kuadro y se devuelve la misma unidad con el tamaño elegido.",color:"white"}
tellraw @s {text:"3. Colocá el Kuadro configurado como cualquier cuadro. No necesitás sostenerlo para elegir la medida.",color:"white"}
tellraw @s {text:"Importante: abrir el selector o ejecutar comandos no crea Kuadros gratis. Hace falta tener uno fabricado.",color:"yellow"}
tellraw @s ["",{text:"[ABRIR SELECTOR]",color:"aqua",underlined:true,click_event:{action:"run_command",command:"function kuadro:menu"}}]
tellraw @s ["",{text:"[COMANDOS Y FUNCIONES]",color:"aqua",underlined:true,click_event:{action:"run_command",command:"function kuadro:comandos"}}]
tellraw @s {text:"━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",color:"dark_gray"}
