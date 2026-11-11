tellraw @s ["",{text:"▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",color:"gold"}]
tellraw @s ["",{text:" ☼ ",color:"yellow"},{text:"SISTEMA KUADRO",color:"gold",bold:true},{text:" ☼",color:"yellow"}]
tellraw @s ["",{text:" AYUDA",color:"yellow",bold:true}]
tellraw @s ["",{text:" Cuadros a la medida",color:"gray",italic:true}]
tellraw @s ["",{text:"▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",color:"gold"}]
tellraw @s ""
tellraw @s ["",{text:" ♦ ",color:"gold"},{text:"1 · ",color:"gold",bold:true},{text:"Cómo usarlo",color:"yellow",bold:true}]
tellraw @s ["",{text:"   a ► ",color:"gold",bold:true},{text:"Fabrique un Kuadro: sale de 1×1, listo para colocar.",color:"white"}]
tellraw @s ["",{text:"   b ► ",color:"gold",bold:true},{text:"Abra el selector y elija ancho y alto (hasta 4×4).",color:"white"}]
tellraw @s ["",{text:"   c ► ",color:"gold",bold:true},{text:"Coloque el Kuadro como cualquier cuadro.",color:"white"}]
tellraw @s ["",{text:"   El selector solo transforma un Kuadro fabricado.",color:"gray"}]
tellraw @s ""
tellraw @s ["",{text:" ► ",color:"gold"},{text:"Abrir selector",color:"aqua",underlined:true,click_event:{action:"run_command",command:"/trigger kuadro set 1"}},{text:"    ► ",color:"gold"},{text:"Comandos",color:"yellow",underlined:true,click_event:{action:"run_command",command:"/trigger kuadro set 3"}}]
tellraw @s ["",{text:"▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",color:"dark_gray"}]
