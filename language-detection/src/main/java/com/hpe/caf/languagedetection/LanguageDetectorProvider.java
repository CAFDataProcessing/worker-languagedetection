package com.hpe.caf.languagedetection;

/**
 * A Lanugage Detector Provider implementation
 */
public interface LanguageDetectorProvider {

    /**
     * Interface to provide a Language detector
     * @return LanguageDetector
     * @throws LanguageDetectorException
     */
    LanguageDetector getLanguageDetector()
            throws LanguageDetectorException;

}
