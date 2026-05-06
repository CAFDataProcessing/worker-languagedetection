/*
 * Copyright 2015-2026 Open Text.
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
package com.github.cafdataprocessing.workers.languagedetection.cld2;

import com.sun.jna.Library;
import com.sun.jna.FunctionMapper;
import com.sun.jna.Pointer;

/**
 * JNA Interface to access the C++ CLD2 methods
 */
public interface Cld2Library extends Library
{
    final static FunctionMapper NAME_MAPPER = (library, method) -> {
        if (method.getName().equals("ExtDetectLanguageSummary")) {
            return "_ZN4CLD224ExtDetectLanguageSummaryEPKcibPKNS_8CLDHintsEiPNS_8LanguageEPiPdPSt6vectorINS_11ResultChunkESaISA_EES7_Pb";
        }
        return method.getName();
    };

    /**
     * Calls into CLD2 to detect the language of the given text buffer, using the provided hints.
     *
     * @param buffer - Bytes of UTF-8 text to be analyzed
     * @param buffer_length - The length of the buffer in bytes
     * @param is_plain_text - True if the text is plain text, false if it contains HTML markup
     * @param hints - A CLDHints structure containing detection hints (TLD, content language, encoding, and language hints)
     * @param flags - Detection flags (use 0 for default behavior, or kCLDFlagBestEffort = 0x4000 for short text)
     * @param language3 - Output array of size 3; receives the top three detected languages as CLD2 Language enum values
     * @param percent3 - Output array of size 3; receives the confidence percentage for each of the top three detected languages
     * @param normalized_score3 - Output array of size 3; receives the normalized score for each of the top three detected languages
     * @param result_chunks - Pointer to a std::vector of ResultChunk; must be passed as null to avoid JNA/C++ interop crashes
     * @param text_bytes - Output array of size 1; receives the number of non-tag/letters-only text bytes found
     * @param is_reliable - Output array of size 1; set to non-zero if the top language is significantly more probable than the second best
     *
     * @return Integer value corresponding to the top detected language in the CLD2 Language enum (see Cld2Language)
     */
    int ExtDetectLanguageSummary(
        byte[] buffer,           // Arg 1: char*
        int buffer_length,       // Arg 2: int
        byte is_plain_text,      // Arg 3: bool (use byte to match C++ 1-byte bool)
        CLDHints hints,          // Arg 4: CLDHints const*
        int flags,               // Arg 5: int (Use 0)
        int[] language3,         // Arg 6: CLD2::Language*
        int[] percent3,          // Arg 7: int*
        double[] normalized_score3, // Arg 8: double* (Can pass null)
        Pointer result_chunks,   // Arg 9: std::vector* (MUST PASS NULL)
        int[] text_bytes,        // Arg 10: int*
        byte[] is_reliable       // Arg 11: bool*
    );

    /**
     * To get language name from kLanguageToName array in CLD2
     *
     * @param language - The language number
     * @return -The language name
     */
    String _ZN4CLD212LanguageNameENS_8LanguageE(int language);

    /**
     * To get language code from kLanguageToCode array in CLD2
     *
     * @param language - The language number
     * @return - The language code
     */
    String _ZN4CLD212LanguageCodeENS_8LanguageE(int language);

    /**
     * To get language number from kNameToLanguage array in CLD2
     *
     * @param name - The name of the language to be queried
     * @return -The language number
     */
    int _ZN4CLD219GetLanguageFromNameEPKc(String name);
}
