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
