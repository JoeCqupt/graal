/*
 * Copyright (c) 2018, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.truffle.espresso.jni;

import java.io.IOException;
import java.io.UTFDataFormatException;

import com.oracle.truffle.espresso.descriptors.ByteString;

/**
 * Modified UTF-8 conversions.
 */
public final class Utf8 {

    private Utf8() {
        /* no instances */
    }

    public static int UTFLength(String str) {
        int strlen = str.length();
        int utflen = 0;

        /* use charAt instead of copying String to char array */
        for (int i = 0; i < strlen; i++) {
            int c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                utflen++;
            } else if (c > 0x07FF) {
                utflen += 3;
            } else {
                utflen += 2;
            }
        }
        return utflen;
    }

    public static byte[] asUTF(String str, boolean append0) {
        return asUTF(str, 0, str.length(), append0);
    }

    public static byte[] asUTF(String str, int start, int len, boolean append0) {
        int utflen = 0;
        int c, count = 0;

        /* use charAt instead of copying String to char array */
        for (int i = 0; i < len; i++) {
            c = str.charAt(start + i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                utflen++;
            } else if (c > 0x07FF) {
                utflen += 3;
            } else {
                utflen += 2;
            }
        }

        byte[] bytearr = new byte[utflen + (append0 ? 1 : 0)]; // 0 terminated, even if empty.

        int i = 0;
        for (i = 0; i < len; i++) {
            c = str.charAt(start + i);
            if (!((c >= 0x0001) && (c <= 0x007F)))
                break;
            bytearr[count++] = (byte) c;
        }

        for (; i < len; i++) {
            c = str.charAt(start + i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                bytearr[count++] = (byte) c;

            } else if (c > 0x07FF) {
                bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                bytearr[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
                bytearr[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
            } else {
                bytearr[count++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
                bytearr[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
            }
        }

        if (append0) {
            bytearr[bytearr.length - 1] = (byte) 0;
        }

        return bytearr;
    }

    public static String toJavaString(byte[] bytearr) throws IOException {
        int utflen = bytearr.length;
        char[] chararr = new char[utflen];

        int c, char2, char3;
        int count = 0;
        int chararrCount = 0;

        while (count < utflen) {
            c = (int) bytearr[count] & 0xff;
            if (c > 127)
                break;
            count++;
            chararr[chararrCount++] = (char) c;
        }

        while (count < utflen) {
            c = (int) bytearr[count] & 0xff;
            switch (c >> 4) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    /* 0xxxxxxx */
                    count++;
                    chararr[chararrCount++] = (char) c;
                    break;
                case 12:
                case 13:
                    /* 110x xxxx 10xx xxxx */
                    count += 2;
                    if (count > utflen)
                        throw new UTFDataFormatException(
                                        "malformed input: partial character at end");
                    char2 = (int) bytearr[count - 1];
                    if ((char2 & 0xC0) != 0x80)
                        throw new UTFDataFormatException(
                                        "malformed input around byte " + count);
                    chararr[chararrCount++] = (char) (((c & 0x1F) << 6) |
                                    (char2 & 0x3F));
                    break;
                case 14:
                    /* 1110 xxxx 10xx xxxx 10xx xxxx */
                    count += 3;
                    if (count > utflen)
                        throw new UTFDataFormatException(
                                        "malformed input: partial character at end");
                    char2 = (int) bytearr[count - 2];
                    char3 = (int) bytearr[count - 1];
                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                        throw new UTFDataFormatException(
                                        "malformed input around byte " + (count - 1));
                    chararr[chararrCount++] = (char) (((c & 0x0F) << 12) |
                                    ((char2 & 0x3F) << 6) |
                                    ((char3 & 0x3F) << 0));
                    break;
                default:
                    /* 10xx xxxx, 1111 xxxx */
                    throw new UTFDataFormatException(
                                    "malformed input around byte " + count);
            }
        }
        // The number of chars produced may be less than utflen
        return new String(chararr, 0, chararrCount);
    }

    public static <T> ByteString<T> fromJavaString(String string) {
        int strlen = string.length();
        int utflen = UTFLength(string);
        int c, count = 0;

        byte[] bytearr = new byte[utflen];

        int i = 0;
        for (i = 0; i < strlen; i++) {
            c = string.charAt(i);
            if (!((c >= 0x0001) && (c <= 0x007F)))
                break;
            bytearr[count++] = (byte) c;
        }

        for (; i < strlen; i++) {
            c = string.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                bytearr[count++] = (byte) c;

            } else if (c > 0x07FF) {
                bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                bytearr[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
                bytearr[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
            } else {
                bytearr[count++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
                bytearr[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
            }
        }

        return new ByteString<>(bytearr);
    }
}
