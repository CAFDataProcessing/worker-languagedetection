package com.hpe.caf.languagedetection.cld2;

import com.hpe.caf.languagedetection.LanguageDetectorException;
import com.hpe.caf.languagedetection.LanguageDetectorSettings;
import com.sun.jna.Native;

/**
 * Created by smitcona on 03/12/2015.
 */
public class Cld2Wrapper {

    private Cld2Library cld2Library;
    private int primaryLang;
    private Cld2Result cld2Result;

    public Cld2Wrapper(){
        cld2Library = (Cld2Library) Native.loadLibrary("libcld2", Cld2Library.class);
        cld2Result = new Cld2Result();
    }

    public Cld2Result detectLanguageSummaryWithHints(byte[] inputBytes, LanguageDetectorSettings settings) throws LanguageDetectorException {
        cld2Result.setIsPlainText(settings.isPlainText());

        /** Hints are passed in but here check if they are empty or not **/
        if(!settings.getHints().isEmpty() && settings.getHints().size()>0) {
            cld2Result.setTld_hint(settings.getHints().get(0));
            if(settings.getHints().size()>1) {
                try {
                    cld2Result.setEncoding_hint(Integer.parseInt(settings.getHints().get(1)));
                } catch (NumberFormatException e) {
                    cld2Result.setEncoding_hint(Cld2Encoding.UNKNOWN_ENCODING);
                }
            }
            if(settings.getHints().size()>2) {
                try {
                    cld2Result.setLanguage_hint(Integer.parseInt(settings.getHints().get(2)));
                } catch (NumberFormatException e) {
                    cld2Result.setLanguage_hint(Cld2Language.UNKNOWN_LANGUAGE);
                }
            }
        }
        try {
            primaryLang = cld2Library.DetectLanguageSummaryWithHints(inputBytes, inputBytes.length, cld2Result.isPlainText(), cld2Result.getTld_hint(),
                    cld2Result.getEncoding_hint(), cld2Result.getLanguage_hint(), cld2Result.getLanguage3(), cld2Result.getPercent3(), cld2Result.getTextBytes(), cld2Result.isReliable());
            getLanguageCodes();
            getLanguageNames();
            return cld2Result;
        }
        catch (Throwable e){
            throw new LanguageDetectorException("Language detection failed. ", e);
        }
    }

    /**
     * using jna, calls into the c++ code to retrieve the language code based on the integer language enum value.
     */
    private void getLanguageCodes(){
        for(int i=0; i<3; i++){
            cld2Result.getLanguageCodes()[i] = cld2Library._ZN4CLD212LanguageCodeENS_8LanguageE(cld2Result.getLanguage3()[i]);
        }
    }

    /**
     * using jna, calls into the c++ code to retrieve the language name based on the integer language enum value.
     */
    private void getLanguageNames(){
        for(int i=0; i<3; i++){
            cld2Result.getLanguageNames()[i] = cld2Library._ZN4CLD212LanguageNameENS_8LanguageE(cld2Result.getLanguage3()[i]);
        }
    }

//    public Cld2Result detectLanguageCheckUTF8(InputStream stream) throws LanguageDetectorException {
//        try {
////            String text = IOUtils.toString(stream, StandardCharsets.UTF_8);
//            byte[] b = IOUtils.toByteArray(stream);
//            stream.read(b);
//            primaryLang = cld2Library.DetectLanguageCheckUTF8(b, b.length, result.isPlainText(), result.isReliable(), result.getTextBytes());
//            getLanguageCodes();
//            getLanguageNames();
//            return result;
//        }
//        catch (Throwable e){
//            throw new LanguageDetectorException("Language detection failed. ", e);
//        }
//    }
//
//    public Cld2Result detectLanguageSummary(InputStream stream) throws LanguageDetectorException {
//        try {
//            byte[] b = IOUtils.toByteArray(stream);
//            stream.read(b);
//            primaryLang = cld2Library.DetectLanguageSummary(b, b.length, result.isPlainText(), result.getLanguage3(),
//                    result.getPercent3(), result.getTextBytes(), result.isReliable());
//            getLanguageCodes();
//            getLanguageNames();
//            return result;
//        }
//        catch (Throwable e){
//            throw new LanguageDetectorException("Language detection failed. ", e);
//        }
//    }

//    public Cld2Result detectLanguage(InputStream stream) throws LanguageDetectorException {
//        try {
//            /** for now, just read entire inputstream into a byte array **/
//            byte[] b = IOUtils.toByteArray(stream);
//            stream.read(b);
//            primaryLang = cld2Library.DetectLanguage(b, b.length, result.isPlainText(), result.isReliable());
//            getLanguageCodes();
//            getLanguageNames();
//            return result;
//        }
//        catch (Throwable e){
//            throw new LanguageDetectorException("Language detection failed. ", e);
//        }
//    }

}
