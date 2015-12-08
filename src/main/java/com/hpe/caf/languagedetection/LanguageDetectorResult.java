package com.hpe.caf.languagedetection;

import java.util.ArrayList;

/**
 * Created by smitcona on 04/12/2015.
 */
public class LanguageDetectorResult {

    /** The status of the language detection operation. **/
    private LanguageDetectorStatus languageDetectorStatus;


    /** List of languages obtained through detection **/
    private ArrayList<Language> languages;


    private boolean isReliable;


    public LanguageDetectorResult(){
        //empty constructor for serialisation
    }


    public LanguageDetectorStatus getLanguageDetectorStatus() {
        return languageDetectorStatus;
    }


    public void setLanguageDetectorStatus(LanguageDetectorStatus languageDetectorStatus) {
        this.languageDetectorStatus = languageDetectorStatus;
    }

    public ArrayList<Language> getLanguages() {
        return languages;
    }

    public void setLanguages(ArrayList<Language> languages) {
        this.languages = languages;
    }

    public boolean isReliable() {
        return isReliable;
    }

    public void setReliable(boolean reliable) {
        isReliable = reliable;
    }
}
