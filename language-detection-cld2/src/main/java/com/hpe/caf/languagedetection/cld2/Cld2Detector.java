/*
 * Copyright 2015-2017 EntIT Software LLC, a Micro Focus company.
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

import com.hpe.caf.languagedetection.*;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Cld2 implementation of LanguageDetector
 */
public class Cld2Detector implements LanguageDetector {

    public Cld2Detector(){
    }


    /**
     * Cld2 implementation of detectLanguage. This calls the Cld2Wrapper.detectLanguageSummaryWithHints and compiles
     * the LanguageDetectorResult using the Cld2Result obtained from the wrapper.
     * @param textBytes - byte array containing bytes of the text
     * @param settings - used by implementation to produce result
     * @return LanguageDetectorResult - the result returned to the consumer
     */
    @Override
    public LanguageDetectorResult detectLanguage(byte[] textBytes, LanguageDetectorSettings settings) {
        Objects.requireNonNull(textBytes);
        Objects.requireNonNull(settings);

        LanguageDetectorResult languageDetectorResult = new LanguageDetectorResult();
        Cld2Wrapper w = new Cld2Wrapper();
        ArrayList<DetectedLanguage> languages = new ArrayList<DetectedLanguage>();

        try {
            Cld2Result resultCLD2 = w.detectLanguageSummaryWithHints(textBytes, settings);

            int numLangs = (settings.isDetectMultipleLanguages()) ? 3 : 1;
            for(int i=0; i<numLangs; i++){
                DetectedLanguage l = new DetectedLanguage();
                l.setLanguageName(resultCLD2.getLanguageNames()[i]);
                l.setLanguageCode(resultCLD2.getLanguageCodes()[i]);
                l.setConfidencePercentage(resultCLD2.getConfidences()[i]);
                languages.add(l);
            }

            languageDetectorResult.setLanguages(languages);
            languageDetectorResult.setReliable(resultCLD2.isReliable()[0]);

            if(resultCLD2.isValid())
                languageDetectorResult.setLanguageDetectorStatus(LanguageDetectorStatus.COMPLETED);
            else
                languageDetectorResult.setLanguageDetectorStatus(LanguageDetectorStatus.FAILED);

        } catch (LanguageDetectorException e){
            languageDetectorResult.setLanguageDetectorStatus(LanguageDetectorStatus.FAILED);
        }
        return languageDetectorResult;
    }


    /**
     * calls the overload detectLanguage method with a default settings object with detectMultipleLanguages set to true
     * @param textBytes - bytes making up the text
     * @return LanguageDetectorResult
     * @throws LanguageDetectorException - Attempt to detect the language has been unsuccessful, causes LanguageDetectorException
     */
    @Override
    public LanguageDetectorResult detectLanguage(byte[] textBytes) throws LanguageDetectorException {
        return detectLanguage(textBytes, new LanguageDetectorSettings(true));
    }


    /**
     * Inputstream is converted to a byte array for CLD2 detection as it does not support InputStream.
     * Returns a LanguageDetectorResult with a FAILED status and isReliable set to false if it fails to
     * convert the stream to bytes
     * @param textStream - AN InputSteam object containing the text for detection
     * @param settings - used by implementation to produce result
     * @return LanguageDetectorResult
     * @throws LanguageDetectorException - Attempt to detect the language has been unsuccessful, causes LanguageDetectorException
     */
    @Override
    public LanguageDetectorResult detectLanguage(InputStream textStream, LanguageDetectorSettings settings) throws LanguageDetectorException {
        Objects.requireNonNull(textStream);

        byte[] bytes;
        try {
            bytes = IOUtils.toByteArray(textStream);
        } catch (IOException e) {
            return new LanguageDetectorResult(LanguageDetectorStatus.FAILED, false);
        }
        return detectLanguage(bytes, settings);
    }


    /**
     * calls into the overloaded detectLanguage method passing in a default settings object with detectMultipleLanguages
     * set to true.
     * @param textStream - AN InputSteam object containing the text for detection
     * @return LanguageDetectorResult
     * @throws LanguageDetectorException - Attempt to detect the language has been unsuccessful, causes LanguageDetectorException
     */
    @Override
    public LanguageDetectorResult detectLanguage(InputStream textStream) throws LanguageDetectorException {
        return this.detectLanguage(textStream, new LanguageDetectorSettings(true));
    }

}
