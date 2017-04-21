package com.hpe.caf.languagedetection;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * Settings defining behaviours of the detector, and including hints for more accurate detection
 */
public class LanguageDetectorSettings{

    /**
     * boolean whether to detect multiple languages (true) or a single language (false)
     */
    private boolean detectMultipleLanguages;


    /**
     * list of hints as strings to give detector a bias for more accurate detection results (optional)
     * CLD2:
     *  "mi,en" boosts Maori and English
     *  "id" boosts Indonesian
     *  SJS boosts Japanese
     *  ITALIAN boosts it
     */
    private Collection<String> hints;


    /**
     * hint from an encoding detector applied to the input
     * indicates likely encoding of the text
     */
    private String encodingHint;


    public LanguageDetectorSettings() {
        //empty constructor for serialisation
    }


    /**
     * Constructors:
     * if no encoding hint is passed in it defaults to ""
     * if no hints are passed in it defaults to ""
     * detectMultipleLanguages must be passed in
     * @param encodingHint
     * @param detectMultipleLanguages
     * @param hints
     */
    public LanguageDetectorSettings(String encodingHint, boolean detectMultipleLanguages, String... hints) {
        Objects.requireNonNull(encodingHint);
        Objects.requireNonNull(hints);
//        this.hints = new ArrayList<String>();//do you need this line?
        setDetectMultipleLanguages(detectMultipleLanguages);
        setHints(hints);
        setEncodingHint(encodingHint);
    }


    public LanguageDetectorSettings(boolean detectMultipleLanguages, String... hints) {
        this("", detectMultipleLanguages, hints);
    }


    public LanguageDetectorSettings(String encodingHint, boolean detectMultipleLanguages) {
        this(encodingHint, detectMultipleLanguages, "");
    }


    public LanguageDetectorSettings(boolean detectMultipleLanguages) {
        this("", detectMultipleLanguages, "");
    }


    public boolean isDetectMultipleLanguages() {
        return detectMultipleLanguages;
    }


    public void setDetectMultipleLanguages(boolean detectMultipleLanguages) {
        this.detectMultipleLanguages = detectMultipleLanguages;
    }


    public Collection<String> getHints() {
        return hints;
    }


    public void setHints(String... hints) {
        this.hints = Arrays.asList(hints);
    }


    public String getEncodingHint() {
        return encodingHint;
    }


    public void setEncodingHint(String encodingHint) {
        this.encodingHint = encodingHint;
    }
}
