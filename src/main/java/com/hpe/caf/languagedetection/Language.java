package com.hpe.caf.languagedetection;

/**
 * Created by smitcona on 07/12/2015.
 */
public class Language {

    private String languageName;
    private String languageCode;
    private int confidence;


    public Language(){
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


    public int getConfidence() {
        return confidence;
    }


    public void setConfidence(int confidence) {
        this.confidence = confidence;
    }
}
