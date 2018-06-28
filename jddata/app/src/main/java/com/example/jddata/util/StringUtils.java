package com.example.jddata.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;


public class StringUtils {

    private static int INDEX_NOT_FOUND = -1;

    public static String EMPTY = "";

    public static boolean equals(final CharSequence cs1, final CharSequence cs2) {
        if (cs1 == null && cs2 != null || cs1 != null && cs2 == null) {
            return false;
        }
        if (cs1 != null && cs2 != null) {
            if (cs1.length() != cs2.length()) {
                return false;
            } else {
                int len = cs1.length();
                for (int i = 0; i < len; i++) {
                    if (cs1.charAt(i) != cs2.charAt(i)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static boolean notEquals(final CharSequence cs1, final CharSequence cs2) {
        return !equals(cs1, cs2);
    }

    public static String defaultString(final String cs) {
        if (cs == null) {
            return EMPTY;
        }

        return cs;
    }

    public static String defaultString(final String cs, final String defaultString) {
        if (cs == null) {
            return defaultString;
        }

        return cs;
    }

    public static boolean equalsIgnoreCase(final CharSequence cs1, final CharSequence cs2) {
        if (cs1 == null && cs2 != null || cs1 != null && cs2 == null) {
            return false;
        }
        if (cs1 != null && cs2 != null) {
            if (cs1.length() != cs2.length()) {
                return false;
            } else {
                int len = cs1.length();
                for (int i = 0; i < len; i++) {
                    //according to jdk, need two way kinds of equal.
                    if (Character.toUpperCase(cs1.charAt(i)) != Character.toUpperCase(cs2.charAt(i))) {
                        return false;
                    }
                    if (Character.toLowerCase(cs1.charAt(i)) != Character.toLowerCase(cs2.charAt(i))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isNotEmpty(final CharSequence cs) {
        return !StringUtils.isEmpty(cs);
    }

    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(final CharSequence cs) {
        return !StringUtils.isBlank(cs);
    }

    public static String trim(final String str) {
        return str == null ? null : str.trim();
    }

    public static String trim(final String str, final String trimChars) {
        if (str == null) {
            return null;
        }

        String res = trimLeft(str, trimChars);
        return trimRight(res, trimChars);
    }

    public static String join(String[] arr, final String separator) {
        if (arr == null || arr.length == 0) {
            return EMPTY;
        }
        StringBuffer sb = new StringBuffer(arr[0]);
        for (int i = 1; i < arr.length; i++) {
            sb.append(separator).append(arr[i]);
        }

        return sb.toString();
    }

    public static <T> String join(Collection<T> col, final String separator) {
        if (col == null || col.size() == 0) {
            return EMPTY;
        }

        StringBuilder sb = new StringBuilder();
        Iterator<T> iterator = col.iterator();
        while (iterator.hasNext()) {
            sb.append(iterator.next());
            if (iterator.hasNext()) {
                sb.append(separator);
            }
        }

        return sb.toString();
    }

    public static String urandom(int length) {
        SecureRandom random = new SecureRandom();
        byte[] randomBytes = new byte[length];
        random.nextBytes(randomBytes);
        return new String(randomBytes);
    }

    public static String getWIFILocalIpAdress(Context mContext) {
        //获取wifi服务
        WifiManager wifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = formatIpAddress(ipAddress);
        return ip;
    }
    private static String formatIpAddress(int ipAdress) {

        return (ipAdress & 0xFF ) + "." +
                ((ipAdress >> 8 ) & 0xFF) + "." +
                ((ipAdress >> 16 ) & 0xFF) + "." +
                ( ipAdress >> 24 & 0xFF) ;
    }

    public static String getRandomString(int length){
        String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random=new Random();
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<length;i++){
            int number=random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    public static String getNumRandomString(int length){
        String str="0123456789";
        Random random=new Random();
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<length;i++){
            int number=random.nextInt(10);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    public static String trimLeft(final String str, final String trimChars) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        int start = 0;
        if (isEmpty(trimChars)) {
            return str;
        } else {
            while (start != strLen && trimChars.indexOf(str.charAt(start)) != INDEX_NOT_FOUND) {
                start++;
            }
        }
        return str.substring(start);
    }

    public static String trimRight(final String str, final String trimChars) {
        int end;
        if (str == null || (end = str.length()) == 0) {
            return str;
        }

        if (isEmpty(trimChars)) {
            return str;
        } else {
            while (end != 0 && trimChars.indexOf(str.charAt(end - 1)) != INDEX_NOT_FOUND) {
                end--;
            }
        }
        return str.substring(0, end);
    }

    /**
     * 必须是整数，不能是空、空格、浮点数
     * 不限制整数的长短
     *
     * @param str
     * @return
     */
    public static boolean isInteger(final String str) {
        if (isBlank(str)) {
            return false;
        }
        final int sz = str.length();
        for (int i = 0; i < sz; i++) {
            if (Character.isDigit(str.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    public static boolean isBoolean(final String str) {
        return "true".equals(str) || "false".equals(str);
    }

    public static String[] splitByInterval(String s, int interval) {
        int arrayLength = (int) Math.ceil(s.length() * 1.0f / interval);
        String[] result = new String[arrayLength];

        int j = 0;
        int lastIndex = result.length - 1;
        for (int i = 0; i < lastIndex; i++) {
            result[i] = s.substring(j, j + interval);
            j += interval;
        } //Add the last bit
        result[lastIndex] = s.substring(j);

        return result;
    }

    public static Map<String, String> split(final String str, final String kvSeparator, final String sectionSeparator) {
        Map<String, String> map = new HashMap<String, String>();

        if (StringUtils.isNotBlank(str)) {
            String[] sections = str.split(Pattern.quote(sectionSeparator));
            if (sections != null && sections.length > 0) {
                for (String section : sections) {
                    if (StringUtils.isNotBlank(section)) {
                        String[] kv = section.split(Pattern.quote(kvSeparator));
                        if (kv != null && kv.length > 0) {
                            if (kv.length > 1) {
                                map.put(kv[0], kv[1]);
                            } else {
                                map.put(kv[0], StringUtils.EMPTY);
                            }
                        }
                    }
                }
            }
        }

        return map;
    }

    public static String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {

        }

        return "";
    }

    public static String urlDecode(String str) {
        try {
            return URLDecoder.decode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {

        }

        return "";
    }

    /**
     * Base64 编码实现
     * 原理：
     * 3字节按照编码规则转变为4个字节。
     */
    public static final char[] base64Encode(byte[] aData) {
        char[] sEncodeString = new char[((aData.length + 2) / 3) * 4];

        //
        // 3 bytes encode to 4 chars.  Output is always an even
        // multiple of 4 characters.
        //
        for (int i = 0, index = 0; i < aData.length; i += 3, index += 4) {
            boolean sQuad = false; // 是否字节尾数只存在2个字节
            boolean sTrip = false; // 是否字节尾数只存在1个字节

            int sVal = (0xFF & (int) aData[i]);
            sVal <<= 8;
            if ((i + 1) < aData.length) {
                sVal |= (0xFF & (int) aData[i + 1]);
                sTrip = true;
            }
            sVal <<= 8;
            if ((i + 2) < aData.length) {
                sVal |= (0xFF & (int) aData[i + 2]);
                sQuad = true;
            }
            sEncodeString[index + 3] = (char) (sQuad ? base64Convert((byte) (sVal & 0x3F), 0) : 61);
            sVal >>= 6;
            sEncodeString[index + 2] = (char) (sTrip ? base64Convert((byte) (sVal & 0x3F), 0) : 61);
            sVal >>= 6;
            sEncodeString[index + 1] = (char) base64Convert((byte) (sVal & 0x3F), 0);
            sVal >>= 6;
            sEncodeString[index + 0] = (char) base64Convert((byte) (sVal & 0x3F), 0);
        }
        return sEncodeString;

    }

    /**
     * base64解码
     */
    public static byte[] base64Decode(byte[] aData) {
        int sLen = aData.length;
        while (sLen > 0 && aData[sLen - 1] == '=') sLen--;
        int sOutLen = (sLen * 3) / 4;
        byte[] sOut = new byte[sOutLen];
        int sInPoint = 0;
        int sOutPointp = 0;
        while (sInPoint < sLen) {
            int i0 = aData[sInPoint++];
            int i1 = aData[sInPoint++];
            int i2 = sInPoint < sLen ? aData[sInPoint++] : 'A';
            int i3 = sInPoint < sLen ? aData[sInPoint++] : 'A';
            if (i0 > 127 || i1 > 127 || i2 > 127 || i3 > 127) {
                return sOut;
            }
            int b0 = (int) base64Convert((byte) i0, 1);
            int b1 = (int) base64Convert((byte) i1, 1);
            int b2 = (int) base64Convert((byte) i2, 1);
            int b3 = (int) base64Convert((byte) i3, 1);
            if (b0 < 0 || b1 < 0 || b2 < 0 || b3 < 0) {
                return sOut;
            }
            int o0 = (b0 << 2) | (b1 >>> 4);
            int o1 = ((b1 & 0xf) << 4) | (b2 >>> 2);
            int o2 = ((b2 & 3) << 6) | b3;
            sOut[sOutPointp++] = (byte) o0;
            if (sOutPointp < sOutLen) sOut[sOutPointp++] = (byte) o1;
            if (sOutPointp < sOutLen) sOut[sOutPointp++] = (byte) o2;
        }
        return sOut;
    }

    /**
     * @param aInputByte byte
     * @param aType      int 转换的类型 0->正传，从不可见字符到可见字符,1->反转:)
     * @return byte
     */
    private static final byte base64Convert(byte aInputByte, int aType) {
        byte sOutputByte = 0;
        if (aType == 0) {
            if (aInputByte <= 25) {
                //大写字母:)
                sOutputByte = (byte) (65 + aInputByte);
            } else if (aInputByte >= 26 && aInputByte <= 51) {
                //小写字母:)
                sOutputByte = (byte) (71 + aInputByte);
            } else if (aInputByte >= 52 && aInputByte <= 61) {
                //数字:)
                sOutputByte = (byte) (aInputByte - 4);
            } else if (aInputByte == 62) {
                //'+'
                sOutputByte = 43;
            } else if (aInputByte == 63) {
                //'/'
                sOutputByte = 47;
            } else if (aInputByte == 64) {
                //'='
                sOutputByte = 61;
            }
        } else if (aType == 1) {
            if (aInputByte >= 48 && aInputByte <= 57) {
                //数字:)
                sOutputByte = (byte) (4 + aInputByte);
            } else if (aInputByte >= 65 && aInputByte <= 90) {
                //大写字母:)
                sOutputByte = (byte) (aInputByte - 65);
            } else if (aInputByte >= 97 && aInputByte <= 122) {
                //小写字母:)
                sOutputByte = (byte) (aInputByte - 71);
            } else if (aInputByte == 43) {
                //'+'
                sOutputByte = 62;
            } else if (aInputByte == 47) {
                //'/'
                sOutputByte = 63;
            } else if (aInputByte == 61) {
                //'='
                sOutputByte = 64;
            }
        }
        return sOutputByte;
    }

    public static byte[] unZipData(byte[] data) {
        if (null == data || 0 == data.length) {
            return null;
        }

        byte[] result = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            GZIPInputStream gis = new GZIPInputStream(bais);

            byte[] tmpBuf = new byte[4096];
            int readlen = 0;
            while ((readlen = gis.read(tmpBuf)) != -1) {
                baos.write(tmpBuf, 0, readlen);
            }

            gis.close();
            result = baos.toByteArray();

            bais.close();
            baos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    static public Date getDateFromFileName(String fileName) {
        if (StringUtils.isEmpty(fileName))
            return null;

        String dateStr;
        dateStr = fileName.substring(fileName.length() - 8, fileName.length());//后8位
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Date date;
        try {
            date = format.parse(dateStr);
        } catch (Exception e) {
            e.printStackTrace();
            date = null;
        }
        return date;
    }

    /**
     * 计算足够创建ICON的日期
     *
     * @param date
     * @return
     */
    static public Date calEnoughIconDate(Date date, int iconAfterSilentDay) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(calendar.DATE, iconAfterSilentDay);
        date = calendar.getTime();
        return date;
    }

    public static String getString(byte[] data) {
        if (data == null) {
            return null;
        }

        try {
            String result = new String(data, "UTF-8");
            return result;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 从协议中解析出Base64的ICON图标
     * @param base64String
     * @return
     */
    public static Bitmap parseBitmapFromBase64String(String base64String){
        if(isEmpty(base64String)){
            return null;
        }

        byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    public static byte[] xorEncode(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        int len = bytes.length;
        int key = 0x12;
        for (int i = 0; i < len; i++) {
            bytes[i] = (byte) (bytes[i] ^ key);
            key = bytes[i];
        }
        return bytes;
    }

    public static byte[] xorDecode(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        int len = bytes.length;
        int key = 0x12;
        for (int i = len - 1; i > 0; i--) {
            bytes[i] = (byte) (bytes[i] ^ bytes[i - 1]);
        }
        bytes[0] = (byte) (bytes[0] ^ key);
        return bytes;
    }
}
