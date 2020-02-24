# -*- coding: utf-8 -*-

file = open("data_all.txt", encoding='utf-8')
f = open('test.txt', 'w', encoding='utf-8')

for line in file:
    # line2 = line.replace(",\n", ",NULL").replace("\n", "").replace(",,", ",NULL,").replace(",,", ",NULL,")
    line2 = line.replace("\n", "").replace("\"", "")
    splits = line2.split(",")
    length = splits.__len__()
    f.write("INSERT INTO jdData (clientId, deviceId, deviceCreateTime, date, imei, moveId, createTime, location, biId, itemIndex, title, subtitle, product, sku, price, originPrice, description, num, city, tab, shop, markNum, viewdNum, comment, fromWhere, goodFeedback, likeNum, salePercent, isSelfSale, hasSalePercent, jdKillRoundTime, brand, category, isOrigin, skuUrl) VALUES ")
    f.write("(")
    for i in range(0, length):
        sp = splits[i]
        if sp == "":
            sp = sp.replace(sp, "NULL")
        else:
            sp = sp.replace(sp, "\"%s\""%sp)
        f.write(sp)
        if i < length-1:
            f.write(",")
    f.write(");\n")

