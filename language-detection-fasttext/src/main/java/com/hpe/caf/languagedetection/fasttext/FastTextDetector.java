/*
 * Copyright 2015-2019 Micro Focus or one of its affiliates.
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
package com.hpe.caf.languagedetection.fasttext;

import com.hpe.caf.languagedetection.*;
import java.io.ByteArrayInputStream;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FastText implementation of LanguageDetector
 */
public class FastTextDetector implements LanguageDetector
{
    private static final Logger LOGGER = LoggerFactory.getLogger(FastTextDetector.class);
    private final static int MAX_LANG_COUNT = 3;
    static final double MIN_RELIABILITY = 0.3;
    
    /**
     * Calls the overload detectLanguage method with a default settings object with detectMultipleLanguages set to true.
     *
     * @param textBytes - bytes making up the text
     * @return LanguageDetectorResult
     */
    @Override
    public LanguageDetectorResult detectLanguage(final byte[] textBytes) {
        return detectLanguage(textBytes, new LanguageDetectorSettings(true));
    }
    
    /**
     * Calls the overload detectLanguage method.
     *
     * @param textBytes - byte array containing bytes of the text
     * @param settings - used by implementation to produce result
     * @return LanguageDetectorResult - the result returned to the consumer
     */
    @Override
    public LanguageDetectorResult detectLanguage(final byte[] textBytes, final LanguageDetectorSettings settings)
    {
        Objects.requireNonNull(textBytes);
        Objects.requireNonNull(settings);
        return detectLanguage(new ByteArrayInputStream(textBytes), settings);
    }

    /**
     * Calls into the overloaded detectLanguage method passing in a default settings object with detectMultipleLanguages set to true.
     *
     * @param textStream - an InputSteam object containing the text for detection
     * @return LanguageDetectorResult
     */
    @Override
    public LanguageDetectorResult detectLanguage(final InputStream textStream) {
        return detectLanguage(textStream, new LanguageDetectorSettings(true));
    }

    /**
     * If conversion of the stream to UTF-8 string fails then it returns a LanguageDetectorResult.
     * with a FAILED status and isReliable set to false 
     *
     * @param textStream - an InputSteam object containing text for detection
     * @param settings - used by implementation to produce result
     * @return LanguageDetectorResult
     */
    @Override
    public LanguageDetectorResult detectLanguage(final InputStream textStream, final LanguageDetectorSettings settings) {
        Objects.requireNonNull(textStream);
        Objects.requireNonNull(settings);

        try {
            final String document = IOUtils.toString(textStream, StandardCharsets.UTF_8);
            return processDetectionResults(new FastTextWrapper().detect(document), settings);            
        } catch (final IOException ex) {
            LOGGER.error("Detection failed.", ex);
            return new LanguageDetectorResult(LanguageDetectorStatus.FAILED, false);
        }
    }

    private LanguageDetectorResult processDetectionResults(final LDResult result, final LanguageDetectorSettings settings) {        
        final LanguageDetectorResult languageDetectorResult = new LanguageDetectorResult();
        
        if (result != null) {
            final int detectedLangCount = result.getLanguages().size();
            if (detectedLangCount > 0) {
                final int itemCountToReturn = settings.isDetectMultipleLanguages() ? 
                    (detectedLangCount > MAX_LANG_COUNT ? MAX_LANG_COUNT : detectedLangCount) : 1;
                languageDetectorResult.setLanguages(result.getLanguages().subList(0, itemCountToReturn));
                LOGGER.info("Accuracy percentage: {}", result.getOverallAccuracy());
                languageDetectorResult.setReliable((result.getOverallAccuracy() > MIN_RELIABILITY));
                languageDetectorResult.setLanguageDetectorStatus(LanguageDetectorStatus.COMPLETED);
            }
        } else {
            languageDetectorResult.setLanguageDetectorStatus(LanguageDetectorStatus.FAILED);
        }
        return languageDetectorResult;
    }
}
