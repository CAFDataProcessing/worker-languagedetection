package com.hpe.caf.languagedetection.cld2;

import com.hpe.caf.languagedetection.LanguageDetector;
import com.hpe.caf.languagedetection.LanguageDetectorProvider;

/**
 * Created by smitcona on 03/12/2015.
 */
public class Cld2DetectorProvider implements LanguageDetectorProvider {

    public LanguageDetector getLanguageDetector() {
        return new Cld2Detector();
    }
}
