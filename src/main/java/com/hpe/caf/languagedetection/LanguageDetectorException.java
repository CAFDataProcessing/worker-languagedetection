package com.hpe.caf.languagedetection;

/**
 * Exception for Language Detection
 */
public class LanguageDetectorException extends Exception {

    public LanguageDetectorException(final String message){
        super(message);
    }

    public LanguageDetectorException(final String message, final Throwable cause){
        super(message, cause);
    }

}
