package com.hpe.caf.languagedetection;

/**
 * for language objects detected by the implementation of language detector, found in
 * LanguageDetectorResult
 */
public class DetectedLanguage {

    /**
     * Name of language e.g. "FRENCH", "ENGLISH"
     */
    private String languageName;

    /**
     * ISO 639-1 language code e.g. "fr", "en"
     */
    private String languageCode;

    /**
     * Percent confidence that the language detected is correct.
     */
    private int confidencePercentage;


    public DetectedLanguage(){
        //empty constructor for serialisation
    }



    public String getLanguageName() {
        return languageName;
    }


    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }


    public String getLanguageCode() {
        return languageCode;
    }


    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }


    public int getConfidencePercentage() {
        return confidencePercentage;
    }


    public void setConfidencePercentage(int confidencePercentage) {
        this.confidencePercentage = confidencePercentage;
    }
}
