package com.hpe.caf.languagedetection;

/**
 * Created by smitcona on 04/12/2015.
 */
public class LanguageDetectorException extends Exception {

    public LanguageDetectorException(final String message){
        super(message);
    }

    public LanguageDetectorException(final String message, final Throwable cause){
        super(message, cause);
    }

}
