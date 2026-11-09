tellraw @s {text:"━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",color:"dark_gray"}
tellraw @s {text:"MANUAL DE MONTAJE · SISTEMA KUADRO",color:"gold",bold:true}
tellraw @s {text:"Los Kuadros son cuadros decorativos con variantes de tamaño. Elegí una medida y aplicala al Kuadro que tengas seleccionado.",color:"white"}
tellraw @s ["",{text:"[ABRIR SELECTOR DE MEDIDAS]",color:"yellow",bold:true,underlined:true,click_event:{action:"run_command",command:"function kuadro:menu"}}]
tellraw @s ["",{text:"[VER COMANDOS Y FUNCIONES]",color:"aqua",underlined:true,click_event:{action:"run_command",command:"function kuadro:comandos"}}]
tellraw @s {text:"Medidas disponibles: de 1×1 a 4×4. El selector aplica la variante al objeto; después podés colocarlo normalmente.",color:"gray"}
tellraw @s {text:"━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",color:"dark_gray"}
