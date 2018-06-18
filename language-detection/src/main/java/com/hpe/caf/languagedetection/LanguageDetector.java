/*
 * Copyright 2015-2018 Micro Focus or one of its affiliates.
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

import java.io.InputStream;

/**
 * A Language Detector interface
 */
public interface LanguageDetector
{
    /**
     * Interface method to detect language
     *
     * @param textStream - the input stream containing the text
     * @param settings - used by implementation to produce result
     * @return LanguageDetectorResult
     * @throws LanguageDetectorException
     */
    LanguageDetectorResult detectLanguage(InputStream textStream, LanguageDetectorSettings settings)
        throws LanguageDetectorException;

    /**
     * Overloaded method passing in default settings
     *
     * @param textStream
     * @return LanguageDetectorResult
     * @throws LanguageDetectorException
     */
    LanguageDetectorResult detectLanguage(InputStream textStream)
        throws LanguageDetectorException;

    /**
     * Interface method to detect language
     *
     * @param textBytes - byte array containing bytes of the text
     * @param settings - used by implementation to produce result
     * @return LanguageDetectorResult
     * @throws LanguageDetectorException
     */
    LanguageDetectorResult detectLanguage(byte[] textBytes, LanguageDetectorSettings settings)
        throws LanguageDetectorException;

    /**
     * Overloaded method passing in default settings
     *
     * @param textBytes
     * @return LanguageDetectorResult
     * @throws LanguageDetectorException
     */
    LanguageDetectorResult detectLanguage(byte[] textBytes)
        throws LanguageDetectorException;
}
