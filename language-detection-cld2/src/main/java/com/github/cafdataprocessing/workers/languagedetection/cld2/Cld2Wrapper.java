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

import com.github.cafdataprocessing.workers.languagedetection.LanguageDetectorException;
import com.github.cafdataprocessing.workers.languagedetection.LanguageDetectorSettings;
import com.sun.jna.Library;
import com.sun.jna.Native;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for the CLD2 library
 */
public class Cld2Wrapper
{
    private static final Logger LOG = LoggerFactory.getLogger(Cld2Wrapper.class);
    
    /**
     * JNA interface access class
     */
    private Cld2Library.CppInterface cld2Library;

    /**
     * Using JNA to load the libcld2 library and use the cld2Library object as an access point
     */
    public Cld2Wrapper()
    {
        System.setProperty("jna.library.path", System.getProperty("cld2.location", System.getenv("cld2.location")));

        LOG.debug("Library location: {}", System.getProperty("jna.library.path"));

        cld2Library = Native.load(
            ("linux/libcld2.so"),
            Cld2Library.CppInterface.class,
            new HashMap<String, Object>() {{
                put(Library.OPTION_FUNCTION_MAPPER, Cld2Library.NAME_MAPPER);
            }}
        );

        LOG.debug("Loaded: {}", cld2Library);
    }

    /**
     * Using JNA, Calls into the cld2Library passing in the required fields to carry out the language detection.
     *
     * @param inputBytes - bytes of text data utf-8
     * @param settings - settings object with hints
     * @return Cld2Result - Contains the languages and is handled by the Cld2 class to produce a LanguageDetectorResult
     * @throws LanguageDetectorException - Attempt to detect the language has been unsuccessful, causes LanguageDetectorException
     */
    public Cld2Result detectLanguageSummaryWithHints(byte[] inputBytes, LanguageDetectorSettings settings)
        throws LanguageDetectorException
    {
        Cld2Result cld2Result = new Cld2Result();

        if (!settings.getHints().isEmpty()) {
            cld2Result.setTld_hint(String.join(",", settings.getHints()));
        }

        cld2Result.setEncoding_hint(Cld2Encoding.getValueFromString(settings.getEncodingHint()));

        try {
            // Validate that input is valid UTF-8 - CLD2 will crash on non-UTF-8 input
            if (!isValidUtf8(inputBytes)) {
                LOG.warn("Input is not valid UTF-8, cannot detect language");
                cld2Result.setValid(false);
                cld2Result.setLanguageCodes(new String[]{"un", "un", "un"});
                cld2Result.setLanguageNames(new String[]{"Unknown", "Unknown", "Unknown"});
                return cld2Result;
            }

            // Prepare the hints object
            final CLDHints hints = new CLDHints();
            hints.tld_hint = cld2Result.getTld_hint();
            hints.content_language_hint = null;
            hints.encoding_hint = cld2Result.getEncoding_hint();
            hints.language_hint = cld2Result.getLanguage_hint();

            byte[] isReliableBytes = new byte[1];

            int result = cld2Library.DetectLanguageSummaryWithHints(
                inputBytes,
                inputBytes.length,
                (byte) 1,
                hints,
                0,
                cld2Result.getLanguage3(),
                cld2Result.getPercent3(),
                cld2Result.getNormalizedScores3(),
                null,
                cld2Result.getTextBytes(),
                isReliableBytes);

            final boolean finalIsReliable = isReliableBytes[0] != 0;
            cld2Result.isReliable()[0] = finalIsReliable;

            if (result == Cld2Language.UNKNOWN_LANGUAGE && !finalIsReliable) {
                cld2Result.setValid(false);
            }

            cld2Result.setLanguageCodes(getLanguageCodes(cld2Result.getLanguage3()));
            cld2Result.setLanguageNames(getLanguageNames(cld2Result.getLanguage3()));
            LOG.debug("Detected language: {}", cld2Result);
            return cld2Result;
        } catch (Throwable e) {
            LOG.error("Error detecting language", e);
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

    /**
     * Validate that the input bytes are valid UTF-8.
     *
     * @param inputBytes the input byte array
     * @return true if the input is valid UTF-8, false otherwise
     */
    private boolean isValidUtf8(final byte[] inputBytes) {
        final CharsetDecoder utf8Decoder = StandardCharsets.UTF_8.newDecoder();
        try {
            utf8Decoder.decode(ByteBuffer.wrap(inputBytes));
            return true;
        } catch (final CharacterCodingException e) {
            return false;
        }
    }
}
