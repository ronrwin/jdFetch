# -*- coding: utf-8 -*-
import sqlite3
import os
import datetime

def run(typeStr):
    cx = sqlite3.connect("target.db")
    src = open("%s_id.txt"%typeStr, encoding='utf-8')
    file = open("for_%s.txt"%typeStr, encoding='utf-8')
    # f = open('update_%s.txt'%typeStr, 'w', encoding='utf-8')

    starttime = datetime.datetime.now()
    d={}
    for line in file:
        parts = line.replace("\n", "").replace("\r", "").replace("\"", "").split("--------")
        d[parts[0]]=parts[1]

    for line in src:
        parts = line.replace("\n", "").replace("\r", "").replace("\"", "").split(",")
        keyword = parts[0]
        iid = parts[1]
        itemindex = parts[2]
        sp = itemindex.split("---")
        index = sp[sp.__len__()-1]

        key = "%s->1"%(keyword)
        if typeStr!="sku":
            key = "%s->%s"%(keyword, index)
        if (key in d.keys()):
            value = d[key]
            items = value.split(",")
            product = items[0]
            price = items[1]
            sku = items[2]
            url = "https://item.jd.com/%s.html"%(sku)
            cx.execute("UPDATE jdData SET product=\"%s\", sku=\"%s\", skuUrl=\"%s\", price=\"%s\" WHERE id=%s;\n"%(product, sku, url, price, iid))
            # f.write("UPDATE jdData SET product=\"%s\", sku=\"%s\", skuUrl=\"%s\", price=\"%s\" WHERE id=%s;\n"%(product, sku, url, price, iid))
    cx.commit()
    endtime = datetime.datetime.now()
    print (endtime - starttime)
    cx.close()


if __name__=="__main__":
    run("sku")
