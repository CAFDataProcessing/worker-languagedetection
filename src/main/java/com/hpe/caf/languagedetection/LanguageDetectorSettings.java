package com.hpe.caf.languagedetection;

import java.util.ArrayList;

/**
 * Created by smitcona on 04/12/2015.
 */
public class LanguageDetectorSettings{

    /**
     * boolean whether to detect multiple languages (true) or a single language (false)
     */
    private boolean detectMultipleLanguages;


    /**
     * boolean for whether the text is plain text, false if it contains html
     */
    private boolean isPlainText;


    /**
     * list of hints as strings to give detector a bias for more accurate detection results (optional)
     * CLD2:
     *  const char* tld_hint -- from the hostname of a URL
     *  int encoding_hint -- from an encoding detector applied to the input document
     *  Language language_hint -- from any other context you might have
     * Conversion from string to appropriate type will be carried out in the implementation
     */
    private ArrayList<String> hints;


    public LanguageDetectorSettings() {

    }


    public LanguageDetectorSettings(boolean detectMultipleLanguages, boolean isPlainText, String... hints) {
        this.hints = new ArrayList<String>();
        setPlainText(detectMultipleLanguages);
        setHints(hints);
        setPlainText(isPlainText);
    }


    public LanguageDetectorSettings(boolean detectMultipleLanguages, boolean isPlainText) {
        this.hints = new ArrayList<String>();
        setDetectMultipleLanguages(detectMultipleLanguages);
        setPlainText(isPlainText);
    }

    public boolean isDetectMultipleLanguages() {
        return detectMultipleLanguages;
    }


    public void setDetectMultipleLanguages(boolean detectMultipleLanguages) {
        this.detectMultipleLanguages = detectMultipleLanguages;
    }


    public ArrayList<String> getHints() {
        return hints;
    }


    public void setHints(String... hints) {
        for(String s : hints){
            this.hints.add(s);
        }
    }


    public boolean isPlainText() {
        return isPlainText;
    }


    public void setPlainText(boolean plainText) {
        isPlainText = plainText;
    }


}
