# -*- coding: utf-8 -*-
import sqlite3
import os
import datetime

def fun1(prefix, low, high):
    starttime = datetime.datetime.now()
    cx = sqlite3.connect("target.db")
    # prefix = "update_worth"
    for i in range(low, high):
        path = "%s%s.txt"%(prefix, i)
        file = open(path, encoding='utf-8')
        for line in file:
           cx.execute(line)
        cx.commit()
        print(path + " end")
    cx.close()
    endtime = datetime.datetime.now()
    print (endtime - starttime)

def brandkill():
    file = open("brandkill.txt", encoding='utf-8')
    f = open("for_brand.txt", 'w', encoding='utf-8')

    d = {}
    for line in file:
        parts = line.replace("\n", "").replace("\r", "").replace("\"", "").split(",")
        title = parts[0]
        product = parts[1]
        price = parts[2]
        sku = parts[3]
        skuUrl = parts[4]
        itemindex = parts[5]
        sp = itemindex.split("---")
        index = sp[sp.__len__()-1]

        key = "%s->%s"%(title, index)
        d[key]="%s,%s,%s"%(product,price,sku)

    for items in d.items():
        f.write("%s--------%s\n"%(items[0], items[1]))

def typekill():
    file = open("typekill.txt", encoding='utf-8')
    f = open("for_type.txt", 'w', encoding='utf-8')

    d = {}
    for line in file:
        parts = line.replace("\n", "").replace("\r", "").replace("\"", "").split(",")
        title = parts[0]
        product = parts[1]
        price = parts[2]
        sku = parts[3]
        skuUrl = parts[4]
        itemindex = parts[5]
        sp = itemindex.split("---")
        index = sp[sp.__len__()-1]

        key = "%s->%s"%(title, index)
        d[key]="%s,%s,%s"%(product,price,sku)

    for items in d.items():
        f.write("%s--------%s\n"%(items[0], items[1]))


def checkLack(dateStr, biId):
    cx = sqlite3.connect("target.db")
    f = open("%s_%s_lack.txt"%(biId,dateStr), 'w', encoding='utf-8')
    rows = cx.execute("SELECT clientId from jdData where biId='%s' and date='%s' group by clientId;"%(biId,dateStr))

    d = {}
    for i in range(1, 461):
        d[str(i)] = False

    for row in rows:
        num = row[0]
        d[row[0]] = True

    for items in d.items():
        if items[1] ==False:
            f.write("%s\n"%items[0])
    cx.close()

def checkLackJdKill(dateStr, timeRange):
    cx = sqlite3.connect("target.db")
    f = open("%s_%s_lack.txt"%(timeRange,dateStr), 'w', encoding='utf-8')
    rows = cx.execute("SELECT clientId from jdData where biId='京东秒杀' and date='%s' and jdKillRoundTime='%s' group by clientId;"%(dateStr,timeRange))

    d = {}
    for i in range(1, 461):
        d[str(i)] = False

    for row in rows:
        num = row[0]
        d[row[0]] = True

    for items in d.items():
        if items[1] ==False:
            f.write("%s\n"%items[0])
    cx.close()

def release():
     cx = sqlite3.connect("target.db")
     cx.close()

def distinct(filename):
    file = open("%s.txt"%(filename), encoding='utf-8')
    f = open("distinct.txt", 'w', encoding='utf-8')
    d={}
    for line in file:
        parts = line.replace("\n", "").replace("\r", "").replace("\"", "").split("--------")
        d[parts[0]] = parts[1]
    for items in d.items():
        f.write("%s--------%s\n"%(items[0], items[1]))


def search():
    f = open("sql.txt", 'w', encoding='utf-8')
    cx = sqlite3.connect("target.db")
    rows = cx.execute("select tab, title, subTitle from jdData where biId='品牌秒杀' and date='05-20';")
    for row in rows:
        f.write("%s,%s,%s\n"%(row[0],row[1],row[2]))
    cx.close()


def export():
    f = open("export.txt", 'w', encoding='utf-8')
    cx = sqlite3.connect("target.db")
    rows = cx.execute("SELECT product,id,itemIndex from jdData where sku is null and product is not null;")
    # rows = cx.execute("SELECT shop,id,ItemIndex from jdData where biId='逛好店' and sku is null;")
    # rows = cx.execute("SELECT title,id,itemIndex from jdData where sku is null and title is not null and biId!='DMP点位';")
    for row in rows:
        line = row[0].replace("\n", "").replace("\r", "").replace("\"", "").strip()
        f.write("%s,%s,%s\n"%(line,row[1],row[2]))
    cx.close()

def getFromSql():
    f = open("fromsql.txt", 'w', encoding='utf-8')
    cx = sqlite3.connect("target.db")
    rows = cx.execute("SELECT distinct product from jdData where sku is null and product is not null;")
    # rows = cx.execute("SELECT distinct shop FROM jdData WHERE biId='逛好店' and sku is null;")
    # rows = cx.execute("SELECT distinct title from jdData where sku is null and title is not null and biId!='DMP点位';")

    for row in rows:
        line = row[0].replace("\n", "").replace("\r", "").replace("\"", "").strip()
        line = row[0].replace("\n", "").replace("\r", "").replace("\"", "").strip()
        f.write("%s\n"%line)
    cx.close()

def test():
    f = open("day9.txt", 'w', encoding='utf-8')
    for i in range(1,34):
        f.write("%s,5\n"%i)

    for i in range(34,67):
        f.write("%s,6\n"%i)

    for i in range(67,100):
        f.write("%s,7\n"%i)

if __name__=="__main__":
    # brandkill()
    # checkLack("09-04", "我的")
    # search()
    # # release()
    export()
    getFromSql()
    # checkLackJdKill("09-04", "10")
    # checkLackJdKill("09-04", "20")
    # fun1("update_worth", 0,1)
    # distinct("for_sku_old")
    # brandkill()
    # test()