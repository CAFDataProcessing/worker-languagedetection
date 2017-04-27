/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development LP.
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

import com.sun.jna.Library;

/**
 * JNA Interface to access the C++ CLD2 methods
 */
public interface Cld2Library extends Library {
    /**
     * this is the method used in CLD2 to detect the language, passing in the hints and
     * references (as java arrays) of the text buffer, language3, percent3, text_bytes and is_reliable fields
     *
     * @param buffer - Bytes of text to be queried
     * @param buffer_length - The length of buffer
     * @param is_plain_text - States whether the text passed in is plain-text or not
     * @param tld_hint - Encoding hint, it is from an encoding detector applied to an input
     * @param encoding_hint - Detector hint, such as "en" "en,it", ENGLISH
     * @param language_hint - The language code specifying a possible language that may be in the text.
     * @param language3 - Contains the top three languages found
     * @param percent3 - The confidence percentage for each of the top three languages found
     * @param text_bytes - The amount of non-tag/letters-only text found
     * @param is_reliable - Determines the confidence in the findings - True if the returned language is some amount more probable than
     *                      the second best.
     *
     * @return integer value for language corresponding to value in Cld2Language.
     *
     * language3 array contains the top three languages found
     * percent3 array contains the confidence for each of the top three languages found, that they are correct
     * text_bytes array contains the amount of non-tag/letters-only text found
     * is_reliable, which is set true if the returned language is some amount more probable than the second best
     * language. Calculation is a complex function of the length of the text and the different-script runs of text.
     */
    int DetectLanguageSummaryWithHints(
            byte[] buffer,
            int buffer_length,
            boolean is_plain_text,
            String tld_hint,
            int encoding_hint,
            int language_hint,
            int[] language3,
            int[] percent3,
            int[] text_bytes,
            boolean[] is_reliable
    );


    /**
     * To get language name from kLanguageToName array in CLD2
     * @param language - The language number
     * @return -The language name
     */
    String _ZN4CLD212LanguageNameENS_8LanguageE(int language);


    /**
     * To get language code from kLanguageToCode array in CLD2
     * @param language - The language number
     * @return - The language code
     */
    String _ZN4CLD212LanguageCodeENS_8LanguageE(int language);


    /**
     * To get language number from kNameToLanguage array in CLD2
     * @param name - The name of the  language to be queried
     * @return -The language number
     */
    int _ZN4CLD219GetLanguageFromNameEPKc(String name);
}
