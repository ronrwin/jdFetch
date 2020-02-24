# -*- coding: utf-8 -*-
import sqlite3

rangeNum=774
currentPart = 1
count=0
src="sku"

file = open("%s.txt"%(src), encoding='utf-8')
f = open("%s%s.txt"%(src, currentPart), 'w', encoding='utf-8')

for line in file:
    count=count+1
    if (count%rangeNum==0):
        currentPart=currentPart+1
        f = open("%s%s.txt"%(src, currentPart), 'w', encoding='utf-8')
    f.write(line)

