# -*- coding: utf-8 -*-
import sqlite3

cx = sqlite3.connect("target.db")
file = open("test.txt", encoding='utf-8')
linenum = 0
for line in file:
    # print(linenum)
    # linenum = linenum+1
    cx.execute(line)
cx.commit()
