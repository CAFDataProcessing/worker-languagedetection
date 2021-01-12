/*
 * Copyright 2015-2021 Micro Focus or one of its affiliates.
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
package com.hpe.caf.worker.languagedetection;

/**
 * Constants for the Language Detection Worker.
 */
public final class LanguageDetectionConstants
{
    private LanguageDetectionConstants()
    {
    }

    /**
     * Recognized 'customData' property names
     */
    public static class CustomData
    {
        public static final String FIELD_SPECS = "fieldSpecs";
        public static final String RESULT_FORMAT = "resultFormat";
    }

    /**
     * The following failure identifiers are possible.
     */
    public static class ErrorCodes
    {
        /**
         * Language detection failed to detect the languages.
         */
        public static final String FAILED_TO_DETECT_LANGUAGES = "LNG-LangDetectFail";

        /**
         * Failed to acquire source data from the datastore.
         */
        public static final String FAILED_TO_ACQUIRE_SOURCE_DATA = "LNG-GetDataFail";

        /**
         * Custom data provided has invalid values.
         */
        public static final String INVALID_CUSTOM_DATA_VALUES = "LNG-InvalidCustomData";

        /**
         * Result format provided has invalid value
         */
        public static final String INVALID_RESULT_FORMAT = "LNG-InvalidResultFormat";
    }

    public static class EnvironmentVariables
    {
        /**
         * Configurable setting to identify which document field corresponds to the source data.
         */
        public static final String WORKER_LANG_DETECT_SOURCE_FIELD = "WORKER_LANG_DETECT_SOURCE_FIELD";
    }

    /**
     * Fields to be added to the document.
     */
    public static class Fields
    {
        public static final String DETECTED_LANGUAGES_STATUS = "DetectedLanguages_Status";
        public static final String DETECTED_LANGUAGES_RELIABLERESULT = "DetectedLanguages_ReliableResult";
        public static final String DETECTED_LANGUAGE_PREFIX = "DetectedLanguage";
        public static final String DETECTED_LANGUAGE_NAME_SUFFIX = "Name";
        public static final String DETECTED_LANGUAGE_CODE_SUFFIX = "Code";
        public static final String DETECTED_LANGUAGE_PERCENTAGE_SUFFIX = "ConfidencePercentage";
    }
}
