/*
 * Copyright 2015-2017 EntIT Software LLC, a Micro Focus company.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hpe.caf.languagedetection.cld2;

import java.util.ArrayList;

/**
 * Main result class of the Cld2 implementation of the language detector.
 */
public class Cld2Result {

    /**
     * plain text will be true for CAF
     */
    private boolean isPlainText;


    /** most appropriate flag could be kCLDFlagBestEffort = 0x4000, instead of UNKNOWN_LANGUAGE
     * it will return a best answer, useful for short text.
     *
     * // Public use flags, debug output controls
     * static const int kCLDFlagScoreAsQuads = 0x0100;  // Force Greek, etc. => quads
     * static const int kCLDFlagHtml =         0x0200;  // Debug HTML => stderr
     * static const int kCLDFlagCr =           0x0400;  // <cr> per chunk if HTML
     * static const int kCLDFlagVerbose =      0x0800;  // More debug HTML => stderr
     * static const int kCLDFlagQuiet =        0x1000;  // Less debug HTML => stderr
     * static const int kCLDFlagEcho =         0x2000;  // Echo input => stderr
     * static const int kCLDFlagBestEffort =   0x4000;  // Give best-effort answer,
     * // even on short text
     *
     * Flag meanings:
     * kCLDFlagScoreAsQuads
     * Normally, several languages are detected solely by their Unicode script.
     * Combined with appropritate lookup tables, this flag forces them instead
     * to be detected via quadgrams. This can be a useful refinement when looking
     * for meaningful text in these languages, instead of just character sets.
     * The default tables do not support this use.
     * kCLDFlagHtml
     * For each detection call, write an HTML file to stderr, showing the text
     * chunks and their detected languages.
     * kCLDFlagCr
     * In that HTML file, force a new line for each chunk.
     * kCLDFlagVerbose
     * In that HTML file, show every lookup entry.
     * kCLDFlagQuiet
     * In that HTML file, suppress most of the output detail.
     * kCLDFlagEcho
     * Echo every input buffer to stderr.
     * kCLDFlagBestEffort
     * Give best-effort answer, instead of UNKNOWN_LANGUAGE. May be useful for
     * short text if the caller prefers an approximate answer over none.
     ***/
    private int flags;


    /**
     * array for the top 3 languages output by the detector
     */
    private int[] language3;


    /**
     * array for the top 3 languages' percent confidences of being correct
     */
    private int[] percent3;


    /**
     * output number of non-tag/letters-only text found
     */
    private int[] textBytes;


    /**
     * is_reliable set true if the returned Language is some amount more
     * probable than the second-best Language. Calculation is a complex function
     * of the length of the text and the different-script runs of text.
     */
    private boolean[] isReliable;


    /**
     * detector hint, such as "en" "en,it", ENGLISH
     */
    private String tld_hint;


    /**
     * encoding hint, is from an encoding detector applied to an input
     */
    private int encoding_hint;


    /**
     * integer values from Cld2Language. For CAF we will pass in tld_hint
     */
    private int language_hint;


    /**
     * Array containing the ISO 639-1 codes for the top three languages detected
     */
    private String[] languageCodes;


    /**
     * array containing the full names for the top three languages detected e.g. "English" "French"
     */
    private String[] languageNames;


    private boolean valid;

    /**
     * constructor setting up default values.
     * plaintext will be true for CAF
     * flags default set to 0
     * if there are no hints:
     * tld_hint is required to be null,
     * encoding_hint is required to be the integer value of UNKNOWN_ENCODING
     * language_hint is required to be the integer value of UNKNOWN_LANGUAGE
     */
    public Cld2Result(){
        this.isPlainText = true;
        this.flags = 0;
        language3  = new int[]{Cld2Language.UNKNOWN_LANGUAGE, Cld2Language.UNKNOWN_LANGUAGE, Cld2Language.UNKNOWN_LANGUAGE};
        percent3  = new int[3];
        textBytes  = new int[1];
        isReliable  = new boolean[1];
        tld_hint = null;
        encoding_hint = Cld2Encoding.UNKNOWN_ENCODING.getValue();
        language_hint = Cld2Language.UNKNOWN_LANGUAGE;
        languageCodes = new String[3];
        languageNames = new String[3];
        valid = true;
    }


    public String[] getLanguageCodes() {
        return languageCodes;
    }


    public String[] getLanguageNames() {
        return languageNames;
    }


    public int[] getConfidences() {
        return percent3;
    }


    /**
     * convert an integer array to an array list of strings
     * (for percent3 and language3)
     * @param iArray
     * @return strings
     */
    private ArrayList<String> convertToStringArrayList(int[] iArray){
        ArrayList<String> strings = new ArrayList<String>();
        for(Integer i : iArray){
            strings.add(i.toString());
        }
        return strings;
    }


    public boolean isPlainText() {
        return isPlainText;
    }


    public void setIsPlainText(boolean plainText) {
        isPlainText = plainText;
    }


    public int getFlags() {
        return flags;
    }


    public void setFlags(int flags) {
        this.flags = flags;
    }


    public int[] getLanguage3() {
        return language3;
    }


    public void setLanguage3(int[] language3) {
        this.language3 = language3;
    }


    public int[] getPercent3() {
        return percent3;
    }


    public void setPercent3(int[] percent3) {
        this.percent3 = percent3;
    }


    public int[] getTextBytes() {
        return textBytes;
    }


    public void setTextBytes(int[] textBytes) {
        this.textBytes = textBytes;
    }


    public boolean[] isReliable() {
        return isReliable;
    }


    public void setIsReliable(boolean[] isReliable) {
        this.isReliable = isReliable;
    }


    public String getTld_hint() {
        return tld_hint;
    }


    public void setTld_hint(String tld_hint) {
        this.tld_hint = tld_hint;
    }


    public int getEncoding_hint() {
        return encoding_hint;
    }


    public void setEncoding_hint(int encoding_hint) {
        this.encoding_hint = encoding_hint;
    }


    public int getLanguage_hint() {
        return language_hint;
    }


    public void setLanguage_hint(int language_hint) {
        this.language_hint = language_hint;
    }


    public void setLanguageCodes(String[] languageCodes) {
        this.languageCodes = languageCodes;
    }


    public void setLanguageNames(String[] languageNames) {
        this.languageNames = languageNames;
    }


    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
