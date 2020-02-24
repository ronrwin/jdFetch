# -*- coding: utf-8 -*-
import sqlite3

def run(typeStr):
    file = open("%s.txt"%typeStr, encoding='utf-8')
    f = open('update_%s.txt'%typeStr, 'w', encoding='utf-8')

    d={}
    for line in file:
        line2 = line.replace("\n", "").replace("\r", "").replace("\"", "")
        parts = line2.split(",")
        product = parts[0]
        f.write("%s->1--------%s\n"%(parts[0], line2))



if __name__=="__main__":
    run("product")
