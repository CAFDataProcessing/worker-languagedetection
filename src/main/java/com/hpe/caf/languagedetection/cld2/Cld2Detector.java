package com.hpe.caf.languagedetection.cld2;

import com.hpe.caf.languagedetection.*;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by smitcona on 03/12/2015.
 */
public class Cld2Detector implements LanguageDetector {

    private LanguageDetectorResult languageDetectorResult;

    public Cld2Detector(){
        languageDetectorResult = new LanguageDetectorResult();
    }

    public LanguageDetectorResult detectLanguage(byte[] textBytes, LanguageDetectorSettings settings) {

        /** Pass in the created cld2 result **/
        Cld2Wrapper w = new Cld2Wrapper();

        ArrayList<Language> languages = new ArrayList<Language>();
        try {
//            resultCLD2 = w.detectLanguageCheckUTF8(text);
            Cld2Result resultCLD2 = w.detectLanguageSummaryWithHints(textBytes, settings);

            int numLangs = (settings.isDetectMultipleLanguages()) ? 3 : 1;

            for(int i=0; i<numLangs; i++){
                Language l = new Language();
                l.setLanguageName(resultCLD2.getLanguageNames()[i]);
                l.setLanguageCode(resultCLD2.getLanguageCodes()[i]);
                l.setConfidence(resultCLD2.getConfidences()[i]);
                languages.add(l);
            }

            languageDetectorResult.setLanguages(languages);
            languageDetectorResult.setLanguageDetectorStatus(LanguageDetectorStatus.COMPLETED);
            languageDetectorResult.setReliable(resultCLD2.isReliable()[0]);

        } catch (LanguageDetectorException e){
            languageDetectorResult.setLanguageDetectorStatus(LanguageDetectorStatus.FAILED);
        }
        return languageDetectorResult;
    }

    @Override
    public LanguageDetectorResult detectLanguage(byte[] textBytes) throws LanguageDetectorException {
        return detectLanguage(textBytes, new LanguageDetectorSettings(true, true));
    }

    public LanguageDetectorResult detectLanguage(InputStream textStream, LanguageDetectorSettings settings) {

        /** convert inputstream to bytes **/
        byte[] bytes = new byte[0];
        try {
            bytes = IOUtils.toByteArray(textStream);
        } catch (IOException e) {
            languageDetectorResult.setLanguageDetectorStatus(LanguageDetectorStatus.FAILED);
        }
        return detectLanguage(bytes, settings);
    }

    public LanguageDetectorResult detectLanguage(InputStream textStream) {
        return this.detectLanguage(textStream, new LanguageDetectorSettings(true, true));
    }

}
