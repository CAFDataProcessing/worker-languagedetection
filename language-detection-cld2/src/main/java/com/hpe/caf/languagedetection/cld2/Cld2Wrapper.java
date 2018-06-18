/*
 * Copyright 2015-2018 Micro Focus or one of its affiliates.
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

import com.hpe.caf.languagedetection.LanguageDetectorException;
import com.hpe.caf.languagedetection.LanguageDetectorSettings;
import com.sun.jna.Native;
import com.sun.jna.Platform;

/**
 * Wrapper for the CLD2 library
 */
public class Cld2Wrapper
{
    /**
     * JNA interface access class
     */
    private Cld2Library cld2Library;

    /**
     * Using JNA to load the libcld2 library and use the cld2Library object as an access point
     */
    public Cld2Wrapper()
    {
        System.setProperty("jna.library.path", System.getProperty("cld2.location", System.getenv("cld2.location")));

        System.out.println("Library location: " + System.getProperty("jna.library.path"));

        cld2Library = (Cld2Library) Native.loadLibrary((Platform.isWindows() ? "win64/libcld2.dll" : "linux/libcld2.so"), Cld2Library.class);
//        cld2Library = (Cld2Library) Native.loadLibrary("libcld2", Cld2Library.class);
    }

    /**
     * Using JNA, Calls into the cld2Library passing in the required fields to carry out the language detection.
     *
     * @param inputBytes - bytes of text data utf-8
     * @param settings - settings object with hints
     * @return Cld2Result - Contains the languages and is handled by the Cld2 class to produce a LanguageDetectorResult
     * @throws LanguageDetectorException - Attempt to detect the language has been unsuccessful, causes LanguageDetectorException
     */
    public Cld2Result detectLanguageSummaryWithHints(byte[] inputBytes, LanguageDetectorSettings settings) throws LanguageDetectorException
    {
        Cld2Result cld2Result = new Cld2Result();

        if (!settings.getHints().isEmpty()) {
            cld2Result.setTld_hint(String.join(",", settings.getHints()));
        }

        cld2Result.setEncoding_hint(Cld2Encoding.getValueFromString(settings.getEncodingHint()));

        try {
            int result = cld2Library.DetectLanguageSummaryWithHints(inputBytes, inputBytes.length, true, cld2Result.getTld_hint(),
                                                                    cld2Result.getEncoding_hint(), cld2Result.getLanguage_hint(), cld2Result.getLanguage3(), cld2Result.getPercent3(), cld2Result.getTextBytes(), cld2Result.isReliable());

            if (result == Cld2Language.UNKNOWN_LANGUAGE && !cld2Result.isReliable()[0]) {
                cld2Result.setValid(false);
            }

            cld2Result.setLanguageCodes(getLanguageCodes(cld2Result.getLanguage3()));
            cld2Result.setLanguageNames(getLanguageNames(cld2Result.getLanguage3()));
            return cld2Result;
        } catch (Throwable e) {
            throw new LanguageDetectorException("Language detection failed.\n", e);
        }
    }

    /**
     * using jna, calls into the c++ code to retrieve the language code based on the integer language enum value. the name of the C++
     * method is mangled
     *
     * @param lang3
     * @return String[] containing the codes
     */
    private String[] getLanguageCodes(int[] lang3)
    {
        String[] codes = new String[3];
        for (int i = 0; i < 3; i++) {
            codes[i] = cld2Library._ZN4CLD212LanguageCodeENS_8LanguageE(lang3[i]);
        }
        return codes;
    }

    /**
     * using jna, calls into the c++ code to retrieve the language name based on the integer language enum value.
     *
     * @param lang3
     * @return String[] containing the language names
     */
    private String[] getLanguageNames(int[] lang3)
    {
        String[] names = new String[3];
        for (int i = 0; i < 3; i++) {
            names[i] = cld2Library._ZN4CLD212LanguageNameENS_8LanguageE(lang3[i]);
        }
        return names;
    }
}
