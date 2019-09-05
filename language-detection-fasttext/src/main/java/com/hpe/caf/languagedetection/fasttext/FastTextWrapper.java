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
import com.neovisionaries.i18n.LanguageAlpha3Code;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FastTextWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(FastTextWrapper.class);
    
    private static final double MIN_LONG_SENTENCE_ACCURACY = 0.8;
    private static final int WORDS_PER_LONG_SENTENCE = 10;
    private static final int WORDS_PER_SHORT_SENTENCE = 3;
    private final String langPrefix = "__label__";
    
    private static final ExecutorService JEP_THRED_POOL = Executors.newSingleThreadExecutor();
    
    public LDResult detect(final String text) {
        // accuracy of all predictions used to calculate overall detection accuracy for document
        final List<Map.Entry<Double, Integer>> overallAccuracy = new ArrayList<>();
        final Map<String, Integer> textLengthPerLanguage = new HashMap<>();
        int totalTextLength = 0; // total processed text length to calculate proportions of detected languages in document
        try {
            int wordsPerSentence = WORDS_PER_LONG_SENTENCE;
            int lastSubstringEndPosition = 0;
            while(lastSubstringEndPosition > -1 && lastSubstringEndPosition < text.length()) {
                // get sentence of n words
                final int startPosition = lastSubstringEndPosition;
                lastSubstringEndPosition = getNthWordEndPosition(text, lastSubstringEndPosition, wordsPerSentence);
                // fasttext library does not accept text with new line characters
                final String sentence = text.substring(startPosition, lastSubstringEndPosition).replaceAll("\n", "");
                
                //get and process prediction results
                final Callable<LanguagePrediction> callPython = () -> FastTextScriptExecutor.detect(sentence);
                final Future<LanguagePrediction> futureResult = JEP_THRED_POOL.submit(callPython);
                final LanguagePrediction prediction = futureResult.get();
                if (prediction != null) {
                    // if acuracy for a long sentence is not suffictient then run prediction on its substring
                    if (wordsPerSentence == WORDS_PER_LONG_SENTENCE && prediction.getPrediction() < MIN_LONG_SENTENCE_ACCURACY) {
                        wordsPerSentence = WORDS_PER_SHORT_SENTENCE;
                        lastSubstringEndPosition = startPosition;
                        continue;
                    }
                    // after single prediction on a short sentence swithch back to predictions on a long sentences
                    if (wordsPerSentence == WORDS_PER_SHORT_SENTENCE) {
                        if (prediction.getPrediction() < MIN_RELIABILITY) {
                            prediction.setLanguageCode(langPrefix + "un"); // undetermined
                        }
                        wordsPerSentence = WORDS_PER_LONG_SENTENCE;
                    }
                    overallAccuracy.add(new AbstractMap.SimpleEntry<>(prediction.getPrediction(), sentence.length()));
                    // add length of processed text to the total text length for detected language
                    final Integer totalLangTextLength = textLengthPerLanguage.get(prediction.getLanguageCode());
                    textLengthPerLanguage.put(prediction.getLanguageCode(),
                        (totalLangTextLength != null ? totalLangTextLength : 0) + sentence.length());
                    totalTextLength += sentence.length();
                } else {
                    LOGGER.warn("Detection results not provided for text: {}", sentence);
                }
            }
        } catch (InterruptedException | ExecutionException ex) {
            LOGGER.error("Detection failed.", ex);
        }
        final List<DetectedLanguage> languages = processResults(textLengthPerLanguage, totalTextLength);
        final float accuracy = (float) (Math.round(calculateWeightedAverage(overallAccuracy) * 100.0) / 100.0); // round to 2 decimal places
        return new LDResult(languages, accuracy);
    }
    
    /**
     * Iterates over characters in a string counting words separated by whitespace/s and returns position of character after Nth word. 
     * If text has les than N words, it returns position of last character.
     */
    private int getNthWordEndPosition(final String text, final int startPosition, final int wordsPerSentence) {
        int wordCount = 0;
        boolean lastCharWhitespace = true;
        int i = startPosition;
        for (; i < text.length(); i++) {
            if (Character.isWhitespace(text.charAt(i)))  {
                if (lastCharWhitespace == false && wordCount == wordsPerSentence) {
                    break;
                }
                lastCharWhitespace = true;
            } else if (lastCharWhitespace == true) {
                wordCount++;
                lastCharWhitespace = false;
            }
        }

        return i;
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
                lang.setLanguageName(getLanguageName(langCode));
                lang.setConfidencePercentage(langConfidence);
                languages.add(lang);
                LOGGER.debug(langCode + ": " + langConfidence);
            }
        }
        return languages;
    }
    
    private static String getLanguageName(final String languageCode) {
        final LanguageAlpha3Code language = LanguageAlpha3Code.getByCode(languageCode);        
        if (language != null) {
            return language.getName().toUpperCase();
        } else {
            // check languages not included in LanguageAlpha3Code
            switch(languageCode) {
                case "un": 
                    return "UNKNOWN";
                case "yue": 
                    return "CANTONESE";
                case "sh": 
                    return "SERBO-CROATIAN";
                // less often detected languages based on tascases
                case "als": 
                    return "TOSK ALBANIAN";
                case "arz": 
                    return "EGYPTIAN ARABIC";
                case "azb": 
                    return "SOUTH AZERBAIJANI";
                case "bcl": 
                    return "CENTRAL BIKOL";
                case "bpy": 
                    return "BISHNUPRIYA";
                case "bxr": 
                    return "RUSSIA BURIAT";
                case "cbk": 
                    return "CHAVACANO";
                case "ckb": 
                    return "CENTRAL KURDISH";
                case "diq": 
                    return "DIMLI";
                case "dty": 
                    return "DOTYALI";
                case "eml": 
                    return "EMILIANO-ROMAGNOLO";
                case "fih": 
                    return "FIJI HINDI";
                case "lmo": 
                    return "LOMBARD";
                case "lrc": 
                    return "NORTHERN LURI";
                case "mhr": 
                    return "EASTERN MARI";
                case "mrj": 
                    return "WESTERN MARI";
                case "mzn": 
                    return "MAZANDERANI";
                case "pfl": 
                    return "PFAELZISCH";
                case "pms": 
                    return "PIEMENTESE";
                case "pnb": 
                    return "WESTERN PANJABI";
                case "rue": 
                    return "RUSYN";
                case "vec": 
                    return "VENETIAN";
                case "vep": 
                    return "VEPS";
                case "vls": 
                    return "VLAAMS";
                case "wuu": 
                    return "WU CHINESE";
                case "xmf": 
                    return "MINGRELIAN";
                default:
                    LOGGER.warn("Language code '{}' was not found.", languageCode);
                    return "UNKNOWN_LANGUAGE_CODE";
            }
        }
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
