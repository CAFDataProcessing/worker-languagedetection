/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development LP.
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
package com.hpe.caf.worker.languagedetection;

import com.google.common.base.Strings;
import com.hpe.caf.api.worker.DataStore;
import com.hpe.caf.api.worker.DataStoreException;
import com.hpe.caf.languagedetection.*;
import com.hpe.caf.util.ModuleLoader;
import com.hpe.caf.util.ModuleLoaderException;
import com.hpe.caf.worker.document.exceptions.DocumentWorkerTransientException;
import com.hpe.caf.worker.document.extensibility.DocumentWorker;
import com.hpe.caf.worker.document.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.hpe.caf.worker.languagedetection.LanguageDetectionUtilities.outputDocumentFieldValueChanges;
import static com.hpe.caf.worker.languagedetection.LanguageDetectionUtilities.getFieldValuesAsStreams;
import static com.hpe.caf.worker.languagedetection.LanguageDetectionUtilities.addDetectedLanguageToDocument;

import java.io.*;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Language Detection Worker. This is an implementation of the DocumentWorker interface. The Language Detection Worker receives a Document
 * from the processDocument() method and detects languages present in the document text using the Language Detection library. The Document
 * is updated with languages detected as well as any failures that arise.
 */
public final class LanguageDetectionWorker implements DocumentWorker
{
    private static final Logger LOG = LoggerFactory.getLogger(LanguageDetectionWorker.class);

    private final DataStore dataStore;
    private final LanguageDetector languageDetector;

    public LanguageDetectionWorker(final Application application)
    {
        // Retrieve the DataStore
        dataStore = application.getService(DataStore.class);

        // Initialise language detection library implementation.
        final LanguageDetectorProvider provider;
        try {
            provider = ModuleLoader.getService(LanguageDetectorProvider.class);
        } catch (ModuleLoaderException mle) {
            LOG.error("Failed to load module.");
            throw new LanguageDetectionException("Failed to load module.", mle);
        }

        // Assign language detector.
        try {
            languageDetector = provider.getLanguageDetector();
        } catch (LanguageDetectorException lde) {
            LOG.error("Failed to get language detector");
            throw new LanguageDetectionException("Failed to get language detector", lde);
        }
    }

    /**
     * This method provides an opportunity for the worker to report if it has any problems which would prevent it processing documents
     * correctly. If the worker is healthy then it should simply return without calling the health monitor.
     *
     * @param healthMonitor used to report the health of the application
     */
    @Override
    public void checkHealth(HealthMonitor healthMonitor)
    {
        //  Make sure language detection library is available.
        if (languageDetector == null) {
            healthMonitor.reportUnhealthy("Language Detection Library unavailable.");
        }
    }

    /**
     * Processes a single document.
     *
     * @param document the document to be processed.
     * @throws InterruptedException if any thread has interrupted the current thread
     * @throws DocumentWorkerTransientException if the document could not be processed
     */
    @Override
    public void processDocument(final Document document) throws InterruptedException, DocumentWorkerTransientException
    {
        try {
            final String fields = document.getCustomData("fieldSpecs");
            if (fields == null) {
                final String workerLangDetectSourceFieldEnv = System.getenv(
                    LanguageDetectionConstants.EnvironmentVariables.WORKER_LANG_DETECT_SOURCE_FIELD);
                detectLanguage(document,
                               Strings.isNullOrEmpty(workerLangDetectSourceFieldEnv) ? "CONTENT" : workerLangDetectSourceFieldEnv, false);
            } else {
                //Split comma-separated list of filed to operate on and place the values in an array.
                final ArrayList<String> fieldsToDetect = new ArrayList<>();
                for (String field : fields.split(",")) {
                    if (field.contains("*")) {
                        final String fieldRegex = field.replace("*", "(.*)");
                        document.getFields().stream().forEach(documentField -> {
                            if (documentField.getName().toLowerCase(Locale.ENGLISH).matches(fieldRegex.toLowerCase(Locale.ENGLISH))) {
                                fieldsToDetect.add(documentField.getName());
                            }
                        });
                    } else {
                        fieldsToDetect.add(field.trim());
                    }
                }
                for (final String fieldName : fieldsToDetect) {
                    //detect language for each field requested.
                    detectLanguage(document, fieldName.trim(), true);
                }
            }
        } catch (RuntimeException re) {
            final Throwable cause = re.getCause();

            if (cause instanceof DataStoreException) {
                document.addFailure(LanguageDetectionConstants.ErrorCodes.FAILED_TO_ACQUIRE_SOURCE_DATA, cause.getMessage());
            } else {
                //  If unexpected RuntimeException is detected, then re-throw.
                throw re;
            }
        } catch (LanguageDetectorException e) {
            LOG.error(e.getMessage());
            document.addFailure(LanguageDetectionConstants.ErrorCodes.FAILED_TO_DETECT_LANGUAGES, e.getMessage());
        } catch (IOException ex) {
            //Thrown in the event that an input stream fails to close in one of the detect methods
            LOG.debug("Failed to close InputStream.");
        }
    }

    private void detectLanguage(final Document document, final String fieldName, final boolean inMultiFieldMode)
        throws RuntimeException, LanguageDetectorException, IOException
    {
        LOG.debug("Document source data field to be used {}.", fieldName);
        final Field sourceDataField = document.getField(fieldName);

        try (final SequenceInputStream sequenceInputStream = getFieldValuesAsStreams(sourceDataField, dataStore)) {

            // Perform language detection.
            LOG.debug("Perform language detection.");
            final LanguageDetectorResult detectorResult = languageDetector.detectLanguage(sequenceInputStream);

            if (detectorResult != null) {
                addDetectedLanguageToDocument(detectorResult, document, sourceDataField, inMultiFieldMode);
            }
            //  Output response data (i.e. document field value changes).
            outputDocumentFieldValueChanges(document);

        }
    }
}
