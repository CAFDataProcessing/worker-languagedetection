package com.hpe.caf.languagedetection.cld2;

import com.hpe.caf.languagedetection.LanguageDetector;
import com.hpe.caf.languagedetection.LanguageDetectorProvider;

/**
 * Provider implementation returning a CLD2Detector object
 */
public class Cld2DetectorProvider implements LanguageDetectorProvider {

    /**
     * returns a new CLD2Detector object
     * @return LanguageDetector
     */
    public LanguageDetector getLanguageDetector() {
        return new Cld2Detector();
    }
}
