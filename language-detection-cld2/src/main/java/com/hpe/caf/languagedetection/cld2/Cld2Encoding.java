/*
 * Copyright 2015-2017 EntIT Software LLC, a Micro Focus company.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hpe.caf.languagedetection.cld2;

import java.util.HashSet;

/**
 * Interface mapped to C++ Encoding Enum containing encodings and associated ordinals
 */
public enum Cld2Encoding
{
    ISO_8859_1(0),
    ISO_8859_2(1),
    ISO_8859_3(2),
    ISO_8859_4(3),
    ISO_8859_5(4),
    ISO_8859_6(5),
    ISO_8859_7(6),
    ISO_8859_8(7),
    ISO_8859_9(8),
    ISO_8859_10(9),
    JAPANESE_EUC_JP(10),
    JAPANESE_SHIFT_JIS(11),
    JAPANESE_JIS(12),
    CHINESE_BIG5(13),
    CHINESE_GB(14),
    CHINESE_EUC_CN(15),
    KOREAN_EUC_KR(16),
    UNICODE_UNUSED(17),
    CHINESE_EUC_DEC(18),
    CHINESE_CNS(19),
    CHINESE_BIG5_CP950(20),
    JAPANESE_CP932(21),
    UTF8(22),
    UNKNOWN_ENCODING(23),
    ASCII_7BIT(24),
    RUSSIAN_KOI8_R(25),
    RUSSIAN_CP1251(26),
    MSFT_CP1252(27),
    RUSSIAN_KOI8_RU(28),
    MSFT_CP1250(29),
    ISO_8859_15(30),
    MSFT_CP1254(31),
    MSFT_CP1257(32),
    ISO_8859_11(33),
    MSFT_CP874(34),
    MSFT_CP1256(35),
    MSFT_CP1255(36),
    ISO_8859_8_I(37),
    HEBREW_VISUAL(38),
    CZECH_CP852(39),
    CZECH_CSN_369103(40),
    MSFT_CP1253(41),
    RUSSIAN_CP866(42),
    ISO_8859_13(43),
    ISO_2022_KR(44),
    GBK(45),
    GB18030(46),
    BIG5_HKSCS(47),
    ISO_2022_CN(48),
    TSCII(49),
    TAMIL_MONO(50),
    TAMIL_BI(51),
    JAGRAN(52),
    MACOSH_ROMAN(53),
    UTF7(54),
    BHASKAR(55),
    HTCHANAKYA(56),
    UTF16BE(57),
    UTF16LE(58),
    UTF32BE(59),
    UTF32LE(60),
    BINARYENC(61),
    HZ_GB_2312(62),
    UTF8UTF8(63),
    TAM_ELANGO(64),
    TAM_LTTMBARANI(65),
    TAM_SHREE(66),
    TAM_TBOOMIS(67),
    TAM_TMNEWS(68),
    TAM_WEBTAMIL(69),
    KDDI_SHIFT_JIS(70),
    DOCOMO_SHIFT_JIS(71),
    SOFTBANK_SHIFT_JIS(72),
    KDDI_ISO_2022_JP(73),
    SOFTBANK_ISO_2022_JP(74),
    NUM_ENCODINGS(75);

    /**
     * integer to hold each enum's value
     */
    private final int value;

    /**
     * constructor allowing each enum declared above to pass in an int value and save into the enum's value field
     *
     * @param newValue
     */
    Cld2Encoding(final int newValue)
    {
        value = newValue;
    }

    public int getValue()
    {
        return value;
    }

    /**
     * Converts code passed in to Cld2Encoding enum and returns the value. If the code passed in is not the exact enum name, it will
     * compare the code with text name variations of popular encodings and return that encoding's value.
     *
     * @param code - The code to be queried
     * @return - Returns the enum value.
     */
    public static int getValueFromString(String code)
    {
        if (contains(code)) {
            return Cld2Encoding.valueOf(code).getValue();
        }

        switch (code) {
            case "utf8":
                return Cld2Encoding.UTF8.getValue();
            case "ascii_7bit":
                return Cld2Encoding.ASCII_7BIT.getValue();
            case "iso_8859_1":
                return Cld2Encoding.ISO_8859_1.getValue();
            case "iso_8859_2":
                return Cld2Encoding.ISO_8859_2.getValue();
            case "iso_8859_3":
                return Cld2Encoding.ISO_8859_3.getValue();
            case "hz_gb_2312":
                return Cld2Encoding.HZ_GB_2312.getValue();
            case "msft_cp1252":
                return Cld2Encoding.MSFT_CP1252.getValue();
            case "msft_cp1250":
                return Cld2Encoding.MSFT_CP1250.getValue();
            case "msft_cp1256":
                return Cld2Encoding.MSFT_CP1256.getValue();
            case "japanese_shift_jis":
                return Cld2Encoding.JAPANESE_SHIFT_JIS.getValue();
            case "korean_euc_kr":
                return Cld2Encoding.KOREAN_EUC_KR.getValue();
            default:
                return Cld2Encoding.UNKNOWN_ENCODING.getValue();
        }
    }

    public static boolean contains(String text)
    {
        for (Cld2Encoding c : Cld2Encoding.values()) {
            if (c.name().equals(text)) {
                return true;
            }
        }
        return false;
    }

    public static HashSet<String> getEnums()
    {
        HashSet<String> values = new HashSet<String>();

        for (Cld2Encoding c : Cld2Encoding.values()) {
            values.add(c.name());
        }
        return values;
    }
}
