tellraw @s {text:"━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",color:"dark_gray"}
tellraw @s {text:"CENTRO DE AYUDA · PANZA",color:"gold",bold:true}
tellraw @s {text:"Referencia de las mecánicas y herramientas de diagnóstico del mod.",color:"white"}
tellraw @s ["",{text:"[GUÍA DEL ESTUDIANTE]",color:"yellow",underlined:true,click_event:{action:"run_command",command:"function ruletita:guia"}}]
tellraw @s ["",{text:"[AYUDA DE KUADROS]",color:"yellow",underlined:true,click_event:{action:"run_command",command:"function kuadro:ayuda"}}]
tellraw @s ["",{text:"[LISTA DE FUNCIONES PANZA]",color:"aqua",underlined:true,click_event:{action:"run_command",command:"function panza:comandos"}}]
tellraw @s ["",{text:"[EJECUTAR DIAGNÓSTICO]",color:"green",underlined:true,click_event:{action:"run_command",command:"function panza:test"}}]
tellraw @s {text:"━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",color:"dark_gray"}
