tellraw @s {text:"━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",color:"dark_gray"}
tellraw @s {text:"CENTRO DE AYUDA · PANZA",color:"gold",bold:true}
tellraw @s {text:"Manuales de estudio, colección y operaciones del programa.",color:"white"}
tellraw @s ["",{text:"[ABRIR GUÍA DEL ESTUDIANTE]",color:"yellow",underlined:true,click_event:{action:"run_command",command:"/trigger panza set 10"}}]
tellraw @s {text:"La guía impresa se obtiene con un libro y una hoja de papel. Usala para abrir el índice de consulta.",color:"white"}
tellraw @s ["",{text:"[AYUDA DE KUADROS]",color:"yellow",underlined:true,click_event:{action:"run_command",command:"/trigger kuadro set 2"}}]
tellraw @s ["",{text:"[LISTA DE FUNCIONES PANZA]",color:"aqua",underlined:true,click_event:{action:"run_command",command:"/trigger panza set 2"}}]
tellraw @s ["",{text:"[EJECUTAR DIAGNÓSTICO]",color:"green",underlined:true,click_event:{action:"run_command",command:"/trigger panza set 3"}}]
tellraw @s {text:"━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",color:"dark_gray"}
