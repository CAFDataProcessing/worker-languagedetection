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

import com.hpe.caf.languagedetection.DetectedLanguage;
import static com.hpe.caf.languagedetection.fasttext.FastTextDetector.MIN_RELIABILITY;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jep.JepException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FastTextWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(FastTextWrapper.class);
    private static final double MIN_LINE_ACCURACY = 0.95;
    private static final int WORDS_PER_SENTENCE = 3;
    private final String langPrefix = "__label__";
    
    public LDResult detect(final String document) {
        final List<String> lines = new ArrayList<>();
        for (final String line : document.split("\n")) {
            lines.add(line.trim());
        }
        return lines.size() > 0 ? detect(lines) : new LDResult(new ArrayList(), 0);
    }
    
    private List<String> sptlitTextIntoSentences(final String text) {
        final List<String> sentences = new ArrayList();
        final String[] words = text.split(" ");
        for (int i = 0; i < words.length; i+=WORDS_PER_SENTENCE) {
            String sentence = "";
            for (int j = 0; j < WORDS_PER_SENTENCE; j++) {
                final int index = i+j;
                if (index < words.length) {
                    final String word = words[index].trim();
                    if (!word.isEmpty()) {
                        sentence = sentence + (j == 0 ? "" : " ") + word;
                    }
                }
            }
            if(!sentence.isEmpty()) {
                sentences.add(sentence);
            }
        }
        return sentences;
    }
    
    private LDResult detect(final List<String> lines) {
        // accuracy of all predictions used to calculate overall detection accuracy for document
        final List<Map.Entry<Double, Integer>> overallAccuracy = new ArrayList<>();
        final Map<String, Integer> textLengthPerLanguage = new HashMap<>();
        int totalTextLength = 0; // total processed text length to calculate proportions of detected languages in document
        try {
            for (final String line : lines) {
                // try prediction for a whole line at first and if result is not enough accurate then run predictions on divided line
                final LanguagePrediction linePrediction = FastTextScriptExecutor.detect(line);
                if (linePrediction != null && linePrediction.getPrediction() >= MIN_LINE_ACCURACY) {
                    updatePredictionResults(linePrediction, line.length(), overallAccuracy, textLengthPerLanguage);
                    totalTextLength += line.length();
                } else {
                    for (final String text : sptlitTextIntoSentences(line)) {                
                        final LanguagePrediction sentencePrediction = FastTextScriptExecutor.detect(text);
                        if (sentencePrediction != null) {
                            if (sentencePrediction.getPrediction() < MIN_RELIABILITY) {
                                sentencePrediction.setLanguageCode(langPrefix + "unknown");
                            }
                            updatePredictionResults(sentencePrediction, text.length(), overallAccuracy, textLengthPerLanguage);
                            totalTextLength += text.length();
                        } else {
                            LOGGER.warn("Detection results not provided for text: {}", text);
                        }
                    }
                }
            }
        } catch (final JepException ex) {
            LOGGER.error("Detection failed.", ex);
        }
        final List<DetectedLanguage> languages = processResults(textLengthPerLanguage, totalTextLength);
        final float accuracy = (float) (Math.round(calculateWeightedAverage(overallAccuracy) * 100.0) / 100.0); // round to 2 decimal places
        return new LDResult(languages, accuracy);
    }

    private void updatePredictionResults(final LanguagePrediction prediction, final int textLength,
                                         final List<Map.Entry<Double, Integer>> overallAccuracy,
                                         final Map<String, Integer> textLengthPerLanguage) {
        overallAccuracy.add(new AbstractMap.SimpleEntry<>(prediction.getPrediction(), textLength));
        // add length of processed text to the total text length for detected language
        final Integer totalLangTextLength = textLengthPerLanguage.get(prediction.getLanguageCode());
        textLengthPerLanguage.put(prediction.getLanguageCode(),
            (totalLangTextLength != null ? totalLangTextLength : 0) + textLength);
    }

    private List<DetectedLanguage> processResults(final Map<String, Integer> textLengthPerLanguage, final int totalTextLength) {
        final Map<String, Integer> sortedByValue =
            textLengthPerLanguage.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        
        final List<DetectedLanguage> languages = new LinkedList<>();
        for (final Map.Entry<String, Integer> entry : sortedByValue.entrySet()) {
            final int langConfidence = (int)(((float)entry.getValue()/totalTextLength) * 100);
            if (langConfidence > 0) {
                final String langCode = entry.getKey().replace(langPrefix, "");
                final DetectedLanguage lang = new DetectedLanguage();
                lang.setLanguageCode(langCode); //ISO code of the language
                lang.setLanguageName(LangEncoding.getLangNameFromCode(langCode));
                lang.setConfidencePercentage(langConfidence);
                languages.add(lang);
                LOGGER.debug(langCode + ": " + langConfidence);
            }
        }
        return languages;
    }
    
    private double calculateWeightedAverage(final List<Map.Entry<Double, Integer>> duplicateKeyMap) {
        double numer = 0;
        double denom = 0;
        for (Map.Entry<Double, Integer> entry : duplicateKeyMap) {
            numer += entry.getKey() * entry.getValue();
            denom += entry.getValue();
        }
        return numer / denom;
    }
}
