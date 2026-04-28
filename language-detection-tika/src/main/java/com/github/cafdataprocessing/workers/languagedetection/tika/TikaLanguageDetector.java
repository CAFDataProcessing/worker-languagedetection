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
package com.github.cafdataprocessing.workers.languagedetection.tika;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.io.IOUtils;
import org.apache.tika.detect.EncodingDetector;
import org.apache.tika.language.detect.LanguageResult;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.txt.UniversalEncodingDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.cafdataprocessing.workers.languagedetection.DetectedLanguage;
import com.github.cafdataprocessing.workers.languagedetection.LanguageDetector;
import com.github.cafdataprocessing.workers.languagedetection.LanguageDetectorException;
import com.github.cafdataprocessing.workers.languagedetection.LanguageDetectorResult;
import com.github.cafdataprocessing.workers.languagedetection.LanguageDetectorSettings;
import com.github.cafdataprocessing.workers.languagedetection.LanguageDetectorStatus;
import com.github.cafdataprocessing.workers.languagedetection.tika.gibberishdetection.GibberishDetector;
import com.github.cafdataprocessing.workers.languagedetection.tika.gibberishdetection.GibberishDetectorFactory;

/**
 * Tika implementation of LanguageDetector
 */
public final class TikaLanguageDetector implements LanguageDetector
{

    private static final Logger LOGGER = LoggerFactory.getLogger(TikaLanguageDetector.class);
    private final GibberishDetector gibberishDetector;

    public TikaLanguageDetector()
    {
        gibberishDetector = GibberishDetectorFactory.createGibberishDetectorFromLocalFile(
            "bigEnglish.txt",
            "goodEnglish.txt",
            "badEnglish.txt",
            "abcdefghijklmnopqrstuvwxyz "
        );
    }

    /**
     * Tika implementation of detectLanguage.
     *
     * @param textBytes - byte array containing bytes of the text
     * @param settings  - used by implementation to produce result
     * @return LanguageDetectorResult - the result returned to the consumer
     * @throws LanguageDetectorException - Attempt to detect the language has been unsuccessful, causes LanguageDetectorException
     */
    @Override
    public LanguageDetectorResult detectLanguage(final byte[] textBytes, final LanguageDetectorSettings settings)
        throws LanguageDetectorException
    {
        final long startTime = System.nanoTime();
        Objects.requireNonNull(textBytes);
        Objects.requireNonNull(settings);

        final String text = getText(textBytes, settings.getEncodingHint());
        LOGGER.info("Detect language in: {}", text);

        final LanguageDetectorResult languageDetectorResult = new LanguageDetectorResult();
        final ArrayList<DetectedLanguage> languages = new ArrayList<DetectedLanguage>();

        /*
        final TikaGibberishDetector.GibberishResult gibberish = TikaGibberishDetector.diagnose(text);
        if (gibberish.isGibberish) {
            LOGGER.info("Text appears to be gibberish: {}", gibberish);
            languageDetectorResult.setReliable(false);
            languageDetectorResult.setLanguageDetectorStatus(LanguageDetectorStatus.FAILED);
            languages.add(createUnknownLanguage());
            languageDetectorResult.setLanguages(languages);
            return languageDetectorResult;
        }
        */
        if (gibberishDetector.isGibberish(text)) {
            LOGGER.info("Text appears to be gibberish: {}", text);
            languageDetectorResult.setReliable(false);
            languageDetectorResult.setLanguageDetectorStatus(LanguageDetectorStatus.FAILED);
            languages.add(createUnknownLanguage());
            languageDetectorResult.setLanguages(languages);
            LOGGER.info("Language detection completed in {} ms. Result: {} for text: {}",
                (System.nanoTime() - startTime) / 1_000_000, languageDetectorResult, textBytes);
            return languageDetectorResult;
        }

        try {
            final org.apache.tika.language.detect.LanguageDetector detector = org.apache.tika.language.detect.LanguageDetector
                .getDefaultLanguageDetector().loadModels();

            final int numLangs = (settings.isDetectMultipleLanguages()) ? 3 : 1;

            final Collection<String> hints = settings.getHints().stream().filter(e -> !e.trim().isEmpty()).toList();
            LOGGER.info("Configured (non blank) Hints: {}", hints);
            if (hints.size() > 0) {
                final Map<String, Float> priors = new HashMap<>();
                float probability = 0.8f;
                hints.stream().limit(numLangs).forEach(e -> priors.put(e, probability - 0.1f));
                if (!priors.isEmpty()) {
                    LOGGER.info("Prioritize these langauges: {}", priors);
                    detector.setPriors(priors);
                }
            }

            final List<LanguageResult> results = detector.detectAll(text);

            results.stream()
                .limit(numLangs)
                .forEach(e -> languages.add(createDetectedLanguage(e)));

            if (results.isEmpty()) {
                languageDetectorResult.setReliable(false);
                languageDetectorResult.setLanguageDetectorStatus(LanguageDetectorStatus.FAILED);
            }

            final LanguageResult best = results.get(0);

            //if (best.isUnknown() || !best.isReasonablyCertain()) {
            if (!isReliable(results)) {
                // Unknown language or unreliable result
                LOGGER.info("Low confidence: {} (score={}, confidence={})",
                    best.getLanguage(), best.getRawScore(), best.getConfidence());
                languageDetectorResult.setReliable(false);
                languageDetectorResult.setLanguageDetectorStatus(LanguageDetectorStatus.FAILED);
            } else {
                languageDetectorResult.setReliable(true);
                LOGGER.info("Detected reliable: {} (score={})",
                    best.getLanguage(), best.getRawScore());
                languageDetectorResult.setLanguageDetectorStatus(LanguageDetectorStatus.COMPLETED);
            }

            languageDetectorResult.setLanguages(languages);

        } catch (final Exception e) {
            languageDetectorResult.setLanguageDetectorStatus(LanguageDetectorStatus.FAILED);
        }
        LOGGER.info("Language detection completed in {} ms. Result: {} for text: {}",
            (System.nanoTime() - startTime) / 1_000_000, languageDetectorResult, textBytes);
        return languageDetectorResult;
    }

    /**
     * Calls the overload detectLanguage method with a default settings object with detectMultipleLanguages set to true
     *
     * @param textBytes - bytes making up the text
     * @return LanguageDetectorResult
     * @throws LanguageDetectorException - Attempt to detect the language has been unsuccessful, causes LanguageDetectorException
     */
    @Override
    public LanguageDetectorResult detectLanguage(final byte[] textBytes) throws LanguageDetectorException
    {
        return detectLanguage(textBytes, new LanguageDetectorSettings(true));
    }

    /**
     * Inputstream is converted to a byte array for CLD2 detection as it does not support InputStream. Returns a LanguageDetectorResult
     * with a FAILED status and isReliable set to false if it fails to convert the stream to bytes
     *
     * @param textStream - AN InputSteam object containing the text for detection
     * @param settings   - used by implementation to produce result
     * @return LanguageDetectorResult
     * @throws LanguageDetectorException - Attempt to detect the language has been unsuccessful, causes LanguageDetectorException
     */
    @Override
    public LanguageDetectorResult detectLanguage(final InputStream textStream, final LanguageDetectorSettings settings)
        throws LanguageDetectorException
    {
        Objects.requireNonNull(textStream);

        byte[] bytes;
        try {
            bytes = IOUtils.toByteArray(textStream);
        } catch (final IOException e) {
            return new LanguageDetectorResult(LanguageDetectorStatus.FAILED, false);
        }
        return detectLanguage(bytes, settings);
    }

    /**
     * Calls into the overloaded detectLanguage method passing in a default settings object with detectMultipleLanguages set to true.
     *
     * @param textStream - AN InputSteam object containing the text for detection
     * @return LanguageDetectorResult
     * @throws LanguageDetectorException - Attempt to detect the language has been unsuccessful, causes LanguageDetectorException
     */
    @Override
    public LanguageDetectorResult detectLanguage(final InputStream textStream) throws LanguageDetectorException
    {
        return this.detectLanguage(textStream, new LanguageDetectorSettings(true));
    }

    private static boolean isReliable(final List<LanguageResult> results) {
        final LanguageResult best = results.get(0);
        final LanguageResult second = results.size() > 1 ? results.get(1) : null;

        // Mirrors CLD2's Rd check (score gap between 1st and 2nd)
        final float gap = second != null ? best.getRawScore() - second.getRawScore() : 1.0f;

        // Mirrors CLD2's overall reliable flag
        final boolean reliable = best.isReasonablyCertain()   // HIGH confidence (absolute score ok)
            && gap > 0.2f                   // clear margin over 2nd best
            && !best.isUnknown();           // not "und"

        LOGGER.info("Language: {} | Gap: {} | Reliable: {}",
            best.getLanguage(), gap, reliable);
        return reliable;
    }

    private static String getText(final byte[] rawBytes, final String encodingHint) throws LanguageDetectorException {
        Charset charset;

        if (encodingHint == null) {
            // Detect encoding
            final EncodingDetector encodingDetector = new UniversalEncodingDetector();
            try {
                charset = encodingDetector.detect(new ByteArrayInputStream(rawBytes), new Metadata());
                LOGGER.info("Encoding detected charset: {}", charset);
            } catch (final IOException e) {
                throw new LanguageDetectorException("Text input stream could not be read");
            }
        } else {
            try {
                // trust the hint, no auto-detect
                charset = Charset.forName(encodingHint);
                LOGGER.info("Specified encoding hint maps to charset: {}", charset);
            } catch (final IllegalArgumentException e) {
                LOGGER.error("Specfied encoding hint not found, using UTF_8 charset");
                charset = StandardCharsets.UTF_8;
            }
        }
        LOGGER.info("Encoding hint specified: {}, using charset: {}", encodingHint, charset);
        return new String(rawBytes, charset);
    }

    private static DetectedLanguage createDetectedLanguage(final LanguageResult languageResult)
    {
        final DetectedLanguage detectedLanguage = new DetectedLanguage();
        final String langCode = languageResult.getLanguage();
        detectedLanguage.setLanguageName(Locale.forLanguageTag(langCode).getDisplayLanguage(Locale.US).toUpperCase(Locale.US));
        detectedLanguage.setLanguageCode(langCode);
        detectedLanguage.setConfidencePercentage((int)(languageResult.getRawScore() * 100));
        LOGGER.info("Detected language: {}", detectedLanguage);
        return detectedLanguage;
    }

    private static DetectedLanguage createUnknownLanguage()
    {
        final DetectedLanguage detectedLanguage = new DetectedLanguage();
        detectedLanguage.setLanguageName("Unknown");
        detectedLanguage.setLanguageCode("un");
        detectedLanguage.setConfidencePercentage(0);
        LOGGER.info("Detected language: {}", detectedLanguage);
        return detectedLanguage;
    }
}