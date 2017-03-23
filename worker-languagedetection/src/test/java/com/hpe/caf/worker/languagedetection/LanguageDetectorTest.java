package com.hpe.caf.worker.languagedetection;

import com.hpe.caf.languagedetection.*;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class LanguageDetectorTest implements LanguageDetector {

    private LanguageDetectorResult languageDetectorResult;

    public LanguageDetectorTest(){
        //  Initialize LanguageDetectorResult for unit testing purposes.
        languageDetectorResult = new LanguageDetectorResult();
        final ArrayList<DetectedLanguage> languages = new ArrayList<DetectedLanguage>();

        final DetectedLanguage l1 = new DetectedLanguage();
        l1.setLanguageName("GERMAN");
        l1.setLanguageCode("de");
        l1.setConfidencePercentage(37);
        languages.add(l1);

        final DetectedLanguage l2 = new DetectedLanguage();
        l2.setLanguageName("FRENCH");
        l2.setLanguageCode("fr");
        l2.setConfidencePercentage(35);
        languages.add(l2);

        final DetectedLanguage l3 = new DetectedLanguage();
        l3.setLanguageName("ENGLISH");
        l3.setLanguageCode("en");
        l3.setConfidencePercentage(27);
        languages.add(l3);

        languageDetectorResult.setLanguages(languages);
        languageDetectorResult.setLanguageDetectorStatus(LanguageDetectorStatus.COMPLETED);
        languageDetectorResult.setReliable(false);
    }

    @Override
    public LanguageDetectorResult detectLanguage(byte[] textBytes, LanguageDetectorSettings settings) {
        return languageDetectorResult;
    }

    @Override
    public LanguageDetectorResult detectLanguage(byte[] textBytes) throws LanguageDetectorException {
        return languageDetectorResult;
    }

    @Override
    public LanguageDetectorResult detectLanguage(InputStream textStream, LanguageDetectorSettings settings) throws LanguageDetectorException {
        return languageDetectorResult;
    }

    @Override
    public LanguageDetectorResult detectLanguage(InputStream textStream) throws LanguageDetectorException {
        String textString;
        try {
            textString = IOUtils.toString(textStream, StandardCharsets.UTF_8);

            // For unit testing purposes, if special string has been detected then throw a new LanguageDetectorException.
            if (textString != null && !textString.isEmpty() && textString.equals("Throw LanguageDetectorException!")) {
                throw new LanguageDetectorException("Test LanguageDetectorException!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return languageDetectorResult;
    }

}
