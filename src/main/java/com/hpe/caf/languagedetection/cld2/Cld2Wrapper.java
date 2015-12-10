package com.hpe.caf.languagedetection.cld2;

import com.google.common.collect.Iterables;
import com.hpe.caf.languagedetection.LanguageDetectorException;
import com.hpe.caf.languagedetection.LanguageDetectorSettings;
import com.sun.jna.Native;

/**
 * Wrapper for the CLD2 library
 */
public class Cld2Wrapper {

    /**
     * JNA interface access class
     */
    private Cld2Library cld2Library;


    /**
     * Using JNA to load the libcld2 library and use the cld2Library object as an access point
     */
    public Cld2Wrapper(){
        cld2Library = (Cld2Library) Native.loadLibrary("libcld2", Cld2Library.class);
    }


    /**
     * Using JNA, Calls into the cld2Library passing in the required fields to carry out hte language detection.
     * @param inputBytes - bytes of text data utf-8
     * @param settings - settings object with hints
     * @return Cld2Result - contains the languages and is handled by the Cld2 class to produce a LanguageDetectorResult
     * @throws LanguageDetectorException
     */
    public Cld2Result detectLanguageSummaryWithHints(byte[] inputBytes, LanguageDetectorSettings settings) throws LanguageDetectorException {
        Cld2Result cld2Result = new Cld2Result();

        /** HInts are put into tld_hint, encoding hint comes from settings **/
        if(Iterables.size(settings.getHints()) > 0) {
            cld2Result.setTld_hint(String.join(",",settings.getHints()));
        }

        cld2Result.setEncoding_hint(Cld2Encoding.getValueFromString(settings.getEncodingHint()));

        try {
            cld2Library.DetectLanguageSummaryWithHints(inputBytes, inputBytes.length, true, cld2Result.getTld_hint(),
                    cld2Result.getEncoding_hint(), cld2Result.getLanguage_hint(), cld2Result.getLanguage3(), cld2Result.getPercent3(), cld2Result.getTextBytes(), cld2Result.isReliable());
            cld2Result.setLanguageCodes(getLanguageCodes(cld2Result.getLanguage3()));
            cld2Result.setLanguageNames(getLanguageNames(cld2Result.getLanguage3()));
            return cld2Result;
        }
        catch (Throwable e){
            throw new LanguageDetectorException("Language detection failed. ", e);
        }
    }


    /**
     * using jna, calls into the c++ code to retrieve the language code based on the integer language enum value.
     * the name of the C++ method is mangled
     * @param lang3
     * @return String[] containing the codes
     */
    private String[] getLanguageCodes(int[] lang3){
        String[] codes = new String[3];
        for(int i=0; i<3; i++){
            codes[i] = cld2Library._ZN4CLD212LanguageCodeENS_8LanguageE(lang3[i]);
        }
        return codes;
    }


    /**
     * using jna, calls into the c++ code to retrieve the language name based on the integer language enum value.
     * @param lang3
     * @return String[] containing the language names
     */
    private String[] getLanguageNames(int[] lang3){
        String[] names = new String[3];
        for(int i=0; i<3; i++){
            names[i] = cld2Library._ZN4CLD212LanguageNameENS_8LanguageE(lang3[i]);
        }
        return names;
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
