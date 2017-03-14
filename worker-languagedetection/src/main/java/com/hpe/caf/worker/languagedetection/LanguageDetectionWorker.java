package com.hpe.caf.worker.languagedetection;

import com.hpe.caf.api.worker.DataStore;
import com.hpe.caf.api.worker.DataStoreException;
import com.hpe.caf.languagedetection.*;
import com.hpe.caf.util.ModuleLoader;
import com.hpe.caf.util.ModuleLoaderException;
import com.hpe.caf.worker.document.exceptions.DocumentWorkerTransientException;
import com.hpe.caf.worker.document.extensibility.DocumentWorker;
import com.hpe.caf.worker.document.model.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

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
    public void processDocument(Document document) throws InterruptedException, DocumentWorkerTransientException
    {
        // Identify source data field.
        final String sourceDataFieldName;
        final String workerLangDetectSourceFieldEnv
            = System.getenv(LanguageDetectionConstants.EnvironmentVariables.WORKER_LANG_DETECT_SOURCE_FIELD);
        if (workerLangDetectSourceFieldEnv == null || workerLangDetectSourceFieldEnv.isEmpty()) {
            // Default to CONTENT field.
            sourceDataFieldName = "CONTENT";
        } else {
            sourceDataFieldName = workerLangDetectSourceFieldEnv;
        }

        LOG.debug("Document source data field to be used {}.", sourceDataFieldName);
        final Field sourceDataField = document.getField(sourceDataFieldName);

        // Identify all source datas for the language detection worker.
        final List<InputStream> streams = new ArrayList<>();
        try {
            // Combine all source data input streams.
            for (FieldValue fv : sourceDataField.getValues()) {
                final InputStream is = getInputStream(fv);
                streams.add(is);
            }
        } catch (RuntimeException re) {
            final Throwable cause = re.getCause();

            if (cause instanceof DataStoreException) {
                document.addFailure(LanguageDetectionConstants.ErrorCodes.FAILED_TO_ACQUIRE_SOURCE_DATA, cause.getMessage());
                return;
            } else {
                throw re;
            }
        }

        // Use language detection library to identify languages in in the document source data text.
        try (SequenceInputStream sequenceInputStream
            = new SequenceInputStream(Collections.enumeration(streams))) {

            // Perform language detection.
            LOG.debug("Perform language detection.");
            final LanguageDetectorResult detectorResult = languageDetector.detectLanguage(sequenceInputStream);

            // Add detected languages to the document object.
            if (detectorResult != null) {
                LOG.debug("Adding metadata to the document for each language detected.");
                addDetectedLanguagesToDocument(detectorResult, document);
            }

        } catch (LanguageDetectorException | IOException e) {
            LOG.error(e.getMessage());
            document.addFailure(LanguageDetectionConstants.ErrorCodes.FAILED_TO_DETECT_LANGUAGES, e.getMessage());
        }

        // Output response data (i.e. document field value changes).
        outputDocumentFieldValueChanges(document);
    }

    private static void addDetectedLanguagesToDocument(LanguageDetectorResult detectorResult, Document document)
    {
        // Add DetectedLanguages_Status field to the document.
        replaceDocumentField(
            document,
            LanguageDetectionConstants.Fields.DETECTED_LANGUAGES_STATUS,
            detectorResult.getLanguageDetectorStatus().toString());

        // Add DetectedLanguages_ReliableResult field to the document.
        replaceDocumentField(
            document,
            LanguageDetectionConstants.Fields.DETECTED_LANGUAGES_RELIABLERESULT,
            String.valueOf(detectorResult.isReliable()));

        // For each language detected, add the name, language code and the percentage of the language detected within the text data to
        // the document.
        if (detectorResult.getLanguages() != null) {
            int languageId = 0;
            for (DetectedLanguage detectedLanguage : detectorResult.getLanguages()) {
                languageId++;
                replaceDocumentField(
                    document,
                    getLanguageNameFieldName(languageId),
                    detectedLanguage.getLanguageName());

                replaceDocumentField(
                    document,
                    getLanguageCodeFieldName(languageId),
                    detectedLanguage.getLanguageCode());

                replaceDocumentField(
                    document,
                    getLanguagePercentageFieldName(languageId),
                    String.valueOf(detectedLanguage.getConfidencePercentage()));
            }
        }
    }

    private static void replaceDocumentField(Document document, String name, String value)
    {
        LOG.debug("Adding metadata field {} with value {} to the document.", name, value);

        // Get a field object for the specified field.
        final Field documentField = document.getField(name);

        // Remove all values from the field.
        documentField.clear();

        // Add the specified value to the field.
        documentField.add(value);
    }

    private InputStream getInputStream(FieldValue fieldValue) throws RuntimeException
    {
        final InputStream is;

        try {
            // Check if data is stored in the remote data store.
            if (fieldValue.isReference()) {
                LOG.debug("Field value data is stored in the remote data store.");
                is = dataStore.retrieve(fieldValue.getReference());
            } else {
                LOG.debug("Field value data is local.");
                is = new ByteArrayInputStream(fieldValue.getValue());
            }
        } catch (DataStoreException dse) {
            LOG.error("Failed to acquire source data from the remote data store");
            // Convert to unchecked exception for streams api usage.
            throw new RuntimeException(dse);
        }

        return is;
    }

    private static String getLanguageNameFieldName(int detectedLanguageId)
    {
        return LanguageDetectionConstants.Fields.DETECTED_LANGUAGE_PREFIX
            + String.valueOf(detectedLanguageId)
            + "_"
            + LanguageDetectionConstants.Fields.DETECTED_LANGUAGE_NAME_SUFFIX;
    }

    private static String getLanguageCodeFieldName(int detectedLanguageId)
    {
        return LanguageDetectionConstants.Fields.DETECTED_LANGUAGE_PREFIX
            + String.valueOf(detectedLanguageId)
            + "_"
            + LanguageDetectionConstants.Fields.DETECTED_LANGUAGE_CODE_SUFFIX;
    }

    private static String getLanguagePercentageFieldName(int detectedLanguageId)
    {
        return LanguageDetectionConstants.Fields.DETECTED_LANGUAGE_PREFIX
            + String.valueOf(detectedLanguageId)
            + "_"
            + LanguageDetectionConstants.Fields.DETECTED_LANGUAGE_PERCENTAGE_SUFFIX;
    }

    private static void outputDocumentFieldValueChanges(Document document)
    {
        final String cafResponseDataOutputFolder
            = System.getenv(LanguageDetectionConstants.EnvironmentVariables.CAF_RESPONSE_DATA_OUTPUT_FOLDER);
        final String cafResponseDataOutputFileName
            = System.getenv(LanguageDetectionConstants.EnvironmentVariables.CAF_RESPONSE_DATA_OUTPUT_FILE_NAME);
        final String dataOutputSubFolder = document.getCustomData("dataOutputSubFolder");

        // Only output document field value changes if configured to do so.
        if (cafResponseDataOutputFolder == null || cafResponseDataOutputFolder.isEmpty()
            || cafResponseDataOutputFileName == null || cafResponseDataOutputFileName.isEmpty()
            || dataOutputSubFolder == null || dataOutputSubFolder.isEmpty()) {
            LOG.debug("No response data output folder or file specified.");
            return;
        }

        LOG.debug("Outputting document field value changes.");

        // Identify output folder location.
        final String dataOutputFolder = FilenameUtils.concat(cafResponseDataOutputFolder, dataOutputSubFolder);

        final File dataOutputFile = new File(dataOutputFolder, cafResponseDataOutputFileName);

        // Iterate through each of the document fields and output changes where they exist.
        document.getFields().forEach(field -> {
            try {
                appendFieldValueChangesToFile(field, dataOutputFile);
            } catch (IOException ioe) {
                LOG.warn("Failed to output document field value changes", ioe);
            }
        });
    }

    private static void appendFieldValueChangesToFile(Field field, File dataOutputFile) throws IOException
    {
        // Output document field value changes.
        if (field.hasChanges() && field.hasValues()) {
            for (FieldValue fv : field.getValues()) {
                if (fv.isReference()) {
                    // Not expecting reference values.
                    continue;
                }
                final String changeValueDetails = field.getName() + " : " + fv.getStringValue() + System.lineSeparator();
                FileUtils.writeStringToFile(dataOutputFile, changeValueDetails, true);
            }
        }
    }
}
