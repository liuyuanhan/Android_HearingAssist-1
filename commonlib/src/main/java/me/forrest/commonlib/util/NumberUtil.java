package me.forrest.commonlib.util;

public class NumberUtil {

    /**
     * 55,55,55,55,55,55,55,55 ==> [55, 55, ...]
     * @param intString 输入的整数字符串
     * @param separator 字符串风格符
     * @return 整数数组
     */
    public static int[] StringToIntArray(String intString, String separator) {
        String[] strArray = intString.split(separator);
        int[] intArray = new int[strArray.length];
        for(int i=0; i<strArray.length; i++) {
            intArray[i] = Integer.parseInt(strArray[i]);
        }
        return intArray;
    }

    public static byte[] HexStringToByteArray(String InString) {
        int nlen;
        int retutn_len;
        int n, i;
        byte[] b;
        String temp;
        nlen = InString.length();
        if (nlen < 16) retutn_len = 16;
        retutn_len = nlen / 2;
        b = new byte[retutn_len];
        i = 0;
        for (n = 0; n < nlen; n = n + 2) {
            temp = InString.substring(n, n + 2);
            b[i] = (byte) HexToInt(temp);
            i = i + 1;
        }
        return b;
    }

    //以下用于将16进制字符串转化为无符号长整型
    public static int HexToInt(String s) {
        String[] hexch = {"0", "1", "2", "3", "4", "5", "6", "7",
                "8", "9", "A", "B", "C", "D", "E", "F"};
        int i, j;
        int r, n, k;
        String ch;

        k = 1;
        r = 0;
        for (i = s.length(); i > 0; i--) {
            ch = s.substring(i - 1, i - 1 + 1);
            n = 0;
            for (j = 0; j < 16; j++) {
                if (ch.compareToIgnoreCase(hexch[j]) == 0) {
                    n = j;
                }
            }
            r += (n * k);
            k *= 16;
        }
        return r;
    }

    public static String oneBytetoHex(byte b) {
        return ("" + "0123456789ABCDEF".charAt(0xf & b >> 4) + "0123456789ABCDEF".charAt(b & 0xf));
    }

    //将字节转成16进制的字符串,方便阅读
    public static String byteArraytoHex(byte[] bytes, int len) {
        StringBuffer strBuff = new StringBuffer();
        String str = "";
        for (int i = 0; i < len; i++) {
            str = oneBytetoHex(bytes[i]);
            strBuff.append(str);
        }
        return strBuff.toString();
    }

    //将字节数组 转成 16进制的字符串, 方便阅读, 可以添加分割符
    public static String byteArraytoHex(byte[] bytes, int len, String separator) {
        StringBuffer strBuff = new StringBuffer();
        String str = "";
        for (int i = 0; i < len; i++) {
            str = oneBytetoHex(bytes[i]);
            strBuff.append(str);
            strBuff.append(separator);
        }
        return strBuff.toString();
    }

    // 无符号byte 转 int (java 没有无符号，如需显示无符号的byte,需要转int表示)
    // 204 == byte 0xCC ==> int 204
    public static int byteToInt(byte b) {
        return b & 0xff;
    }
}
