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

import java.util.Collection;
import java.util.Objects;

/**
 * The result returned from the LanguageDetector. Implementations will create and fill the result with the result data
 */
public class LanguageDetectorResult
{
    /**
     * The status of the language detection operation.
     */
    private LanguageDetectorStatus languageDetectorStatus;

    /**
     * List of languages obtained through detection
     */
    private Collection<DetectedLanguage> languages;

    /**
     * if this is false, the result from the detector is not reliable due to the detector failing to detect the language. This may happen
     * if an unsupported encoding of text is used or if there are not enough characters in the text.
     */
    private boolean isReliable;

    public LanguageDetectorResult()
    {
        //empty constructor for serialisation
    }

    public LanguageDetectorResult(LanguageDetectorStatus status, boolean isReliable)
    {
        this.languageDetectorStatus = status;
        this.isReliable = isReliable;
    }

    public LanguageDetectorStatus getLanguageDetectorStatus()
    {
        return languageDetectorStatus;
    }

    public void setLanguageDetectorStatus(LanguageDetectorStatus languageDetectorStatus)
    {
        Objects.requireNonNull(languageDetectorStatus);
        this.languageDetectorStatus = languageDetectorStatus;
    }

    public Collection<DetectedLanguage> getLanguages()
    {
        return languages;
    }

    public void setLanguages(Collection<DetectedLanguage> languages)
    {
        this.languages = languages;
    }

    public boolean isReliable()
    {
        return isReliable;
    }

    public void setReliable(boolean reliable)
    {
        isReliable = reliable;
    }
}
