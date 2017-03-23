package com.hpe.caf.worker.languagedetection;

import com.hpe.caf.languagedetection.LanguageDetector;
import com.hpe.caf.languagedetection.LanguageDetectorProvider;

public class LanguageDetectorProviderTest implements LanguageDetectorProvider {

    /**
     * returns a new LanguageDetectorTest object
     * @return LanguageDetector
     */
    public LanguageDetector getLanguageDetector() {
        return new LanguageDetectorTest();
    }
}
