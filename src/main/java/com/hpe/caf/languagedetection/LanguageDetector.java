package com.hpe.caf.languagedetection;

import java.io.InputStream;

/**
 * Created by smitcona on 03/12/2015.
 */
public interface LanguageDetector {

    LanguageDetectorResult detectLanguage(InputStream textStream, LanguageDetectorSettings settings)
            throws LanguageDetectorException;

    LanguageDetectorResult detectLanguage(InputStream textStream)
            throws LanguageDetectorException;

    LanguageDetectorResult detectLanguage(byte[] textBytes, LanguageDetectorSettings settings)
            throws LanguageDetectorException;

    LanguageDetectorResult detectLanguage(byte[] textBytes)
            throws LanguageDetectorException;

}
