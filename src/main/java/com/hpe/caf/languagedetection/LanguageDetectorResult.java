package com.hpe.caf.languagedetection;

import java.util.Collection;
import java.util.Objects;

/**
 * The result returned from the LanguageDetector. Implementations will create and fill the result
 * with the result data
 */
public class LanguageDetectorResult {

    /**
     * The status of the language detection operation.
     */
    private LanguageDetectorStatus languageDetectorStatus;


    /**
     * List of languages obtained through detection
     */
    private Collection<DetectedLanguage> languages;


    /**
     * if this is false, the result from the detector is not reliable due to the detector failing
     * to detect the language. This may happen if an unsupported encoding of text is used or
     * if there are not enough characters in the text.
     */
    private boolean isReliable;


    public LanguageDetectorResult(){
        //empty constructor for serialisation
    }

    public LanguageDetectorResult(LanguageDetectorStatus status, boolean isReliable){
        this.languageDetectorStatus = status;
        this.isReliable = isReliable;
    }

    public LanguageDetectorStatus getLanguageDetectorStatus() {
        return languageDetectorStatus;
    }


    public void setLanguageDetectorStatus(LanguageDetectorStatus languageDetectorStatus)  {
        Objects.requireNonNull(languageDetectorStatus);
        this.languageDetectorStatus = languageDetectorStatus;
    }


    public Collection<DetectedLanguage> getLanguages() {
        return languages;
    }


    public void setLanguages(Collection<DetectedLanguage> languages) {
        this.languages = languages;
    }


    public boolean isReliable() {
        return isReliable;
    }


    public void setReliable(boolean reliable) {
        isReliable = reliable;
    }
}
