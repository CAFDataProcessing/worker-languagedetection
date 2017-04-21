package com.hpe.caf.languagedetection;

import java.io.InputStream;

/**
 * A Language Detector interface
 */
public interface LanguageDetector {

    /**
     * Interface method to detect language
     * @param textStream - the input stream containing the text
     * @param settings - used by implementation to produce result
     * @return LanguageDetectorResult
     * @throws LanguageDetectorException
     */
    LanguageDetectorResult detectLanguage(InputStream textStream, LanguageDetectorSettings settings)
            throws LanguageDetectorException;


    /**
     * Overloaded method passing in default settings
     * @param textStream
     * @return LanguageDetectorResult
     * @throws LanguageDetectorException
     */
    LanguageDetectorResult detectLanguage(InputStream textStream)
            throws LanguageDetectorException;


    /**
     * Interface method to detect language
     * @param textBytes - byte array containing bytes of the text
     * @param settings - used by implementation to produce result
     * @return LanguageDetectorResult
     * @throws LanguageDetectorException
     */
    LanguageDetectorResult detectLanguage(byte[] textBytes, LanguageDetectorSettings settings)
            throws LanguageDetectorException;


    /**
     * Overloaded method passing in default settings
     * @param textBytes
     * @return LanguageDetectorResult
     * @throws LanguageDetectorException
     */
    LanguageDetectorResult detectLanguage(byte[] textBytes)
            throws LanguageDetectorException;

}
