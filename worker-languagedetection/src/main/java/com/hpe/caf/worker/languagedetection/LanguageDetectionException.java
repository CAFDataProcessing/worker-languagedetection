package com.hpe.caf.worker.languagedetection;

public final class LanguageDetectionException extends RuntimeException
{
    public LanguageDetectionException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    public LanguageDetectionException(final String message)
    {
        super(message);
    }
}
