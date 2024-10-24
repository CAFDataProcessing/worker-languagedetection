/*
 * Copyright 2015-2024 Open Text.
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
package com.hpe.caf.languagedetection.cld2.testing;

import com.hpe.caf.languagedetection.*;
import com.hpe.caf.languagedetection.cld2.Cld2DetectorProvider;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

/**
 * CLD2 Language detection integration test
 */
public class LanguageDetectionCld2IT
{
    private String filename;
    private LanguageDetectorProvider provider;
    private LanguageDetector detector;
    private LanguageDetectorResult result;
    private LanguageDetectorSettings settings;
    private boolean multiLang;

    public LanguageDetectionCld2IT()
    {
    }

    /**
     * Set up the provider and detector
     *
     * @throws LanguageDetectorException
     */
    @BeforeEach
    public void setup() throws LanguageDetectorException
    {
        provider = new Cld2DetectorProvider();
        detector = provider.getLanguageDetector();
    }

    /**
     * Test text with single language detection. Assert expected results
     *
     * @throws LanguageDetectorException
     * @throws IOException
     */
    @Test
    public void testSingleLanguage() throws LanguageDetectorException, IOException
    {
        multiLang = false;

        filename = "emailGerman.txt";

        byte[] bytes = getAllData(filename);

        settings = new LanguageDetectorSettings("utf-8", multiLang, "de");

        result = detector.detectLanguage(bytes, settings);

        DetectedLanguage[] arr = result.getLanguages().toArray(new DetectedLanguage[1]);

        assertEquals(LanguageDetectorStatus.COMPLETED, result.getLanguageDetectorStatus());
        assertTrue(result.isReliable());//German is the main language here with a much higher percentage therefore result should be reliable
        assertEquals(1, result.getLanguages().size());

        assertEquals("de", arr[0].getLanguageCode());
        assertEquals("GERMAN", arr[0].getLanguageName());
    }

    /**
     * Test text file with multiple languages (3) and assert expected values
     *
     * @throws LanguageDetectorException
     * @throws IOException
     */
    @Test
    public void testMultiLanguage() throws LanguageDetectorException, IOException
    {
        multiLang = true;
        String[] testCodes = {"de", "es", "en"};
        String[] testNames = {"GERMAN", "SPANISH", "ENGLISH"};

        filename = "extractEnEsDe.txt";

        byte[] bytes = getAllData(filename);

        settings = new LanguageDetectorSettings(multiLang);

        result = detector.detectLanguage(bytes, settings);

        DetectedLanguage[] arr = result.getLanguages().toArray(new DetectedLanguage[3]);

        assertEquals(LanguageDetectorStatus.COMPLETED, result.getLanguageDetectorStatus());
        assertFalse(result.isReliable());//spanish and german have similar language percentages therefore the result is not reliable
        assertEquals(3, result.getLanguages().size());

        for (int i = 0; i < 3; i++) {
            assertEquals(testCodes[i], arr[i].getLanguageCode());
            assertEquals(testNames[i], arr[i].getLanguageName());
        }
    }

    /**
     * Test result obtained from short text with one language. Assert expected values.
     *
     * @throws LanguageDetectorException
     * @throws IOException
     */
    @Test
    public void testSingleLanguageShortText() throws LanguageDetectorException, IOException
    {
        multiLang = false;

        filename = "extractNLShort.txt";

        byte[] bytes = getAllData(filename);

        settings = new LanguageDetectorSettings(multiLang, "nl");

        result = detector.detectLanguage(bytes, settings);

        DetectedLanguage[] arr = result.getLanguages().toArray(new DetectedLanguage[1]);

        assertEquals(LanguageDetectorStatus.COMPLETED, result.getLanguageDetectorStatus());
//        Assert.assertFalse(result.isReliable());//spanish and german have similar language percentages therefore the result is not reliable
        assertEquals(1, result.getLanguages().size());

        assertEquals("nl", arr[0].getLanguageCode());
        assertEquals("DUTCH", arr[0].getLanguageName());
    }

    /**
     * Fail test on gibberish text of no language. Assert the results indicate unknown language
     *
     * @throws LanguageDetectorException
     * @throws IOException
     */
    @Test
    public void testMultipleLanguageGibberish() throws LanguageDetectorException, IOException
    {
        multiLang = true;

        filename = "extractGibberish.txt";

        byte[] bytes = getAllData(filename);

        settings = new LanguageDetectorSettings(multiLang);

        result = detector.detectLanguage(bytes, settings);

        DetectedLanguage[] arr = result.getLanguages().toArray(new DetectedLanguage[3]);

        assertEquals(LanguageDetectorStatus.FAILED, result.getLanguageDetectorStatus());
        assertFalse(result.isReliable());//spanish and german have similar language percentages therefore the result is not reliable
        assertEquals(3, result.getLanguages().size());

        for (int i = 0; i < 3; i++) {
            assertEquals("un", arr[i].getLanguageCode());
            assertEquals("Unknown", arr[i].getLanguageName());
        }
    }

    /**
     * test a text file with UCS-2 LE BOM encoded text which is not supported. According to CLD2 all text should be utf-8
     *
     * @throws LanguageDetectorException
     * @throws IOException
     */
    @Test
    public void testLanguageUCS2() throws LanguageDetectorException, IOException
    {
        multiLang = false;

        filename = "greekUCS2.txt";

        byte[] bytes = getAllData(filename);

        settings = new LanguageDetectorSettings(multiLang);

        result = detector.detectLanguage(bytes, settings);

        DetectedLanguage[] arr = result.getLanguages().toArray(new DetectedLanguage[1]);

        assertEquals(LanguageDetectorStatus.FAILED, result.getLanguageDetectorStatus());
    }

    /**
     * Read in an entire file into a byte array
     *
     * @param name
     * @return
     * @throws IOException
     */
    private byte[] getAllData(final String name) throws IOException, LanguageDetectorException
    {
        try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream(name)) {
            return stream.readAllBytes();
        } catch (IOException | NullPointerException e) {
            throw new LanguageDetectorException("File could not be converted to byte array. Invalid filename: " + name);
        }
    }
}
