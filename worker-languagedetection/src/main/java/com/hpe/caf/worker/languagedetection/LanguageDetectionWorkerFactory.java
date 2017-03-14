package com.hpe.caf.worker.languagedetection;

import com.hpe.caf.worker.document.extensibility.DocumentWorker;
import com.hpe.caf.worker.document.extensibility.DocumentWorkerFactory;
import com.hpe.caf.worker.document.model.Application;

/**
 * Factory for creating an instance of LanguageDetectionWorker.
 */
public class LanguageDetectionWorkerFactory implements DocumentWorkerFactory
{
    @Override
    public DocumentWorker createDocumentWorker(Application application)
    {
        return new LanguageDetectionWorker(application);
    }
}
