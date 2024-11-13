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
package com.hpe.caf.languagedetection;

/**
 * for language objects detected by the implementation of language detector, found in LanguageDetectorResult
 */
public class DetectedLanguage
{
    /**
     * Name of language e.g. "FRENCH", "ENGLISH"
     */
    private String languageName;

    /**
     * ISO 639-1 language code e.g. "fr", "en"
     */
    private String languageCode;

    /**
     * Percent confidence that the language detected is correct.
     */
    private int confidencePercentage;

    public DetectedLanguage()
    {
        //empty constructor for serialisation
    }

    public String getLanguageName()
    {
        return languageName;
    }

    public void setLanguageName(String languageName)
    {
        this.languageName = languageName;
    }

    public String getLanguageCode()
    {
        return languageCode;
    }

    public void setLanguageCode(String languageCode)
    {
        this.languageCode = languageCode;
    }

    public int getConfidencePercentage()
    {
        return confidencePercentage;
    }

    public void setConfidencePercentage(int confidencePercentage)
    {
        this.confidencePercentage = confidencePercentage;
    }
}
