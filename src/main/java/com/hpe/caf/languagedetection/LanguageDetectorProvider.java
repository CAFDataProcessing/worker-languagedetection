package com.hpe.caf.languagedetection;

/**
 * Created by smitcona on 03/12/2015.
 */
public interface LanguageDetectorProvider {

    LanguageDetector getLanguageDetector()
            throws LanguageDetectorException;

}
