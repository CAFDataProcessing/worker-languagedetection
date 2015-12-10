package com.hpe.caf.languagedetection.cld2;

import com.hpe.caf.languagedetection.*;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Cld2 implementation of LanguageDetector
 */
public class Cld2Detector implements LanguageDetector {

    public Cld2Detector(){
    }


    /**
     * Cld2 implementation of detectLanguage. This calls the Cld2Wrapper.detectLanguageSummaryWithHints and compiles
     * the LanguageDetectorResult using the Cld2Result obtained from the wrapper.
     * @param textBytes - byte array containing bytes of the text
     * @param settings - used by implementation to produce result
     * @return LanguageDetectorResult - the result returned to the consumer
     */
    @Override
    public LanguageDetectorResult detectLanguage(byte[] textBytes, LanguageDetectorSettings settings) {
        Objects.requireNonNull(textBytes);
        Objects.requireNonNull(settings);

        LanguageDetectorResult languageDetectorResult = new LanguageDetectorResult();
        Cld2Wrapper w = new Cld2Wrapper();
        ArrayList<DetectedLanguage> languages = new ArrayList<DetectedLanguage>();

        try {
            Cld2Result resultCLD2 = w.detectLanguageSummaryWithHints(textBytes, settings);

            int numLangs = (settings.isDetectMultipleLanguages()) ? 3 : 1;
            for(int i=0; i<numLangs; i++){
                DetectedLanguage l = new DetectedLanguage();
                l.setLanguageName(resultCLD2.getLanguageNames()[i]);
                l.setLanguageCode(resultCLD2.getLanguageCodes()[i]);
                l.setConfidencePercentage(resultCLD2.getConfidences()[i]);
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


    /**
     * calls the overload detectLanguage method with a default settings object with detectMultipleLanguages set to true
     * @param textBytes - bytes making up the text
     * @return LanguageDetectorResult
     * @throws LanguageDetectorException
     */
    @Override
    public LanguageDetectorResult detectLanguage(byte[] textBytes) throws LanguageDetectorException {
        return detectLanguage(textBytes, new LanguageDetectorSettings(true));
    }


    /**
     * Inputstream is converted to a byte array for CLD2 detection as it does not support InputStream.
     * Returns a LanguageDetectorResult with a FAILED status and isReliable set to false if it fails to
     * convert the stream to bytes
     * @param textStream - the input stream containing the text
     * @param settings - used by implementation to produce result
     * @return LanguageDetectorResult
     * @throws LanguageDetectorException
     */
    @Override
    public LanguageDetectorResult detectLanguage(InputStream textStream, LanguageDetectorSettings settings) throws LanguageDetectorException {
        Objects.requireNonNull(textStream);

        byte[] bytes;
        try {
            bytes = IOUtils.toByteArray(textStream);
        } catch (IOException e) {
            return new LanguageDetectorResult(LanguageDetectorStatus.FAILED, false);
        }
        return detectLanguage(bytes, settings);
    }


    /**
     * calls into the overloaded detectLanguage method passing in a default settings object with detectMultipleLanguages
     * set to true.
     * @param textStream
     * @return LanguageDetectorResult
     * @throws LanguageDetectorException
     */
    @Override
    public LanguageDetectorResult detectLanguage(InputStream textStream) throws LanguageDetectorException {
        return this.detectLanguage(textStream, new LanguageDetectorSettings(true));
    }

}
