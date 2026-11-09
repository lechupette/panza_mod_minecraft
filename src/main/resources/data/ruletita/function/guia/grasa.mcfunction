tellraw @s {text:"━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",color:"dark_gray"}
tellraw @s {text:"LÁMINA 01 · ALIMENTACIÓN Y RESERVAS CORPORALES",color:"gold",bold:true}
tellraw @s {text:"FIG. 01 / ESCALA DE VOLUMEN CORPORAL",color:"gray",bold:true}
tellraw @s ["",{text:"ALIMENTACIÓN  ",color:"white"},{text:"□□□□□□□□□□",color:"gray"},{text:"  RESERVA  ",color:"yellow"},{text:"□□□□□",color:"gold"}]
tellraw @s {text:"10 niveles proceden de la barra de hambre. 5 niveles adicionales representan la reserva por exceso.",color:"white"}
tellraw @s {text:"La reserva aumenta al consumir alimentos cuando ya estás completamente saciado. Los alimentos del mod pueden consumirse incluso con la barra llena para activar este sistema.",color:"white"}
tellraw @s {text:"La reserva disminuye gradualmente con el tiempo. Cada nivel puede modificar levemente la velocidad de movimiento.",color:"white"}
tellraw @s {text:"FIG. 02 / DISTRIBUCIÓN DEL VOLUMEN",color:"gray",bold:true}
tellraw @s {text:"        [ TORSO ]  →  ABDOMEN",color:"yellow"}
tellraw @s {text:"        [ CINTURA ] → ZONA POSTERIOR",color:"yellow"}
tellraw @s {text:"La geometría utiliza la textura corporal de la skin. El volumen se distribuye entre abdomen y nalgas; brazos, piernas y cabeza permanecen sin cambios.",color:"white"}
tellraw @s {text:"NOTA: la escala es progresiva y suaviza las transiciones entre niveles.",color:"gray",italic:true}
tellraw @s ["",{text:"[ÍNDICE DE LÁMINAS]",color:"yellow",underlined:true,click_event:{action:"run_command",command:"function ruletita:guia"}}]
tellraw @s {text:"━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",color:"dark_gray"}
