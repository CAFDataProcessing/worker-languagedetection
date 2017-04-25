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
     * @param buffer
     * @param buffer_length
     * @param is_plain_text
     * @param tld_hint
     * @param encoding_hint
     * @param language_hint
     * @param language3
     * @param percent3
     * @param text_bytes
     * @param is_reliable
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
     */
    String _ZN4CLD212LanguageNameENS_8LanguageE(int language);


    /**
     * To get language code from kLanguageToCode array in CLD2
     */
    String _ZN4CLD212LanguageCodeENS_8LanguageE(int language);


    /**
     * To get language number from kNameToLanguage array in CLD2
     */
    int _ZN4CLD219GetLanguageFromNameEPKc(String name);
}
