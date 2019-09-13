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
import com.neovisionaries.i18n.LanguageAlpha3Code;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
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

public final class FastTextWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(FastTextWrapper.class);
    
    private static final double MIN_LONG_SENTENCE_ACCURACY = 0.8;
    private static final double MIN_SHORT_SENTENCE_ACCURACY = 0.3;
    private static final int WORDS_PER_LONG_SENTENCE = 10;
    private static final int WORDS_PER_SHORT_SENTENCE = 3;
    private static final String LANG_PREFIX = "__label__";
    
    private static final ExecutorService JEP_THREAD_POOL = Executors.newSingleThreadExecutor();
    
    private FastTextWrapper(){}
    
    public static LDResult detect(final InputStream textStream) {
        // accuracy of all predictions used to calculate overall detection accuracy for document
        final List<Map.Entry<Double, Integer>> overallAccuracy = new ArrayList<>();
        final Map<String, Integer> textLengthPerLanguage = new HashMap<>();
        int totalTextLength = 0; // total processed text length to calculate proportions of detected languages in document
        try (final BufferedReader buffer = new BufferedReader(new InputStreamReader(textStream, StandardCharsets.UTF_8))) {
            int wordsPerSentence = WORDS_PER_LONG_SENTENCE;
            Sentence sentence = new Sentence("", 0, 0);
            while(true) {
                // get sentence of n words
                sentence = getSentence(buffer, sentence, wordsPerSentence);
                // break the loop when there are no more words in the buffer
                if (sentence.getWordsInSentence() == 0) {
                    break;
                }
                
                final String text = sentence.getText().substring(0, sentence.getSentenceEndIndex());
                //get and process prediction results
                final Callable<LanguagePrediction> callPython = () -> FastTextScriptExecutor.detect(text);
                final Future<LanguagePrediction> futureResult = JEP_THREAD_POOL.submit(callPython);
                final LanguagePrediction prediction = futureResult.get();
                if (prediction != null) {
                    // if acuracy for a long sentence is not suffictient then run prediction on its substring
                    if (wordsPerSentence == WORDS_PER_LONG_SENTENCE && prediction.getPrediction() < MIN_LONG_SENTENCE_ACCURACY) {
                        wordsPerSentence = WORDS_PER_SHORT_SENTENCE;
                        sentence = new Sentence(sentence.getText(), 0, 0);
                        continue;
                    }
                    // after single prediction on a short sentence switch back to predictions on a long sentences
                    if (wordsPerSentence == WORDS_PER_SHORT_SENTENCE) {
                        if (prediction.getPrediction() < MIN_SHORT_SENTENCE_ACCURACY) {
                            prediction.setLanguageCode(LANG_PREFIX + "un"); // "__label__un" is identificator for undetermined language
                        }
                        wordsPerSentence = WORDS_PER_LONG_SENTENCE;
                    }
                    // create new Sentence with substring of yet not processed text
                    sentence = new Sentence(sentence.getText().substring(sentence.getSentenceEndIndex()), 0, 0);
                    
                    overallAccuracy.add(new AbstractMap.SimpleEntry<>(prediction.getPrediction(), text.length()));
                    // add length of processed text to the total text length for detected language
                    final Integer totalLangTextLength = textLengthPerLanguage.get(prediction.getLanguageCode());
                    textLengthPerLanguage.put(prediction.getLanguageCode(),
                        (totalLangTextLength != null ? totalLangTextLength : 0) + text.length());
                    totalTextLength += text.length();
                } else {
                    LOGGER.warn("Detection results not provided for text: {}", text);
                }
            }
        } catch (final InterruptedException | ExecutionException | IOException ex) {
            LOGGER.error("Detection failed.", ex);
        }
        final List<DetectedLanguage> languages = processResults(textLengthPerLanguage, totalTextLength);
        final float accuracy = (float) (Math.round(calculateWeightedAverage(overallAccuracy) * 100.0)/100.0); // round to 2 decimal places
        return new LDResult(languages, accuracy);
    }
    
    private static Sentence getSentence(final BufferedReader buffer, final Sentence sentence, final int wordsPerSentence) 
        throws IOException {
        
        Sentence s = sentence;
        while (s.getWordsInSentence() < wordsPerSentence) {
            final int textLength = s.getText().length();
            // if there isn't text available for processing or index of already processed text is in the end then read the next line 
            if ((textLength == 0 || textLength == s.getSentenceEndIndex())) {
                final String nextLine = buffer.readLine();
                if (nextLine == null) {
                    break;
                } 
                s = new Sentence(s.getText() + " " + nextLine, s.getSentenceEndIndex(), s.getWordsInSentence());
            }
            s = getNWordsSentence(s, wordsPerSentence);
        }
        
        return s;
    }
    
    /*
     * Iterates over characters in a string counting words separated by whitespace/s.
     * Returns a Sentence with index of last character. If text has less than N words, it returns Sentence with index of last character
     * and actual word count.
     */
    private static Sentence getNWordsSentence(final Sentence sentence, final int wordsPerSentence) {
        final String text = sentence.getText();
        int wordCount = sentence.getWordsInSentence();
        boolean lastCharWhitespace = true;
        int i = sentence.getSentenceEndIndex();
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

        return new Sentence(text, i, wordCount);
    }

    private static List<DetectedLanguage> processResults(final Map<String, Integer> textLengthPerLanguage, final int totalTextLength) {
        return textLengthPerLanguage.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .map(entry -> {
                final int langConfidence = (int)(((float)entry.getValue()/totalTextLength) * 100);
                if (langConfidence > 0) {
                    final String langCode = entry.getKey().replace(LANG_PREFIX, "");
                    final DetectedLanguage lang = new DetectedLanguage();
                    lang.setLanguageCode(langCode); //ISO code of the language
                    lang.setLanguageName(getLanguageName(langCode));
                    lang.setConfidencePercentage(langConfidence);
                    LOGGER.debug(langCode + ": " + langConfidence);
                    return lang;
                }
                return null;
            })
            .filter(val -> val!=null)
            .collect(Collectors.toCollection(LinkedList::new));
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
    
    private static double calculateWeightedAverage(final List<Map.Entry<Double, Integer>> duplicateKeyMap) {
        double numer = 0;
        double denom = 0;
        for (final Map.Entry<Double, Integer> entry : duplicateKeyMap) {
            numer += entry.getKey() * entry.getValue();
            denom += entry.getValue();
        }
        return numer / denom;
    }
}
