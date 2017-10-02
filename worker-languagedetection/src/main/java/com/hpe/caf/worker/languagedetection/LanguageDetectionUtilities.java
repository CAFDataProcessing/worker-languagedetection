/*
 * Copyright 2015-2017 EntIT Software LLC, a Micro Focus company.
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

import com.hpe.caf.api.worker.DataStore;
import com.hpe.caf.api.worker.DataStoreException;
import com.hpe.caf.languagedetection.DetectedLanguage;
import com.hpe.caf.languagedetection.LanguageDetectorResult;
import com.hpe.caf.worker.document.model.Document;
import com.hpe.caf.worker.document.model.Field;
import com.hpe.caf.worker.document.model.FieldValue;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LanguageDetectionUtilities
{

    private static final Logger LOG = LoggerFactory.getLogger(LanguageDetectionUtilities.class);

    private LanguageDetectionUtilities()
    {
    }

    public static SequenceInputStream getFieldValuesAsStreams(final Field sourceDataField, final DataStore dataStore)
        throws RuntimeException
    {
        Objects.requireNonNull(sourceDataField);
        Objects.requireNonNull(dataStore);
        final List<InputStream> streams = new ArrayList<>();
        for (FieldValue fv : sourceDataField.getValues()) {
            final InputStream is = getInputStream(fv, dataStore);
            streams.add(is);
        }
        return new SequenceInputStream(Collections.enumeration(streams));
    }

    public static void addDetectedLanguagesToDocument(final LanguageDetectorResult detectorResult, final Document document)
    {
        Objects.requireNonNull(detectorResult);
        Objects.requireNonNull(document);
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

    public static void addDetectedLanguagesToDocument(final LanguageDetectorResult detectorResult, final Document document,
                                                      final String fieldName)
    {
        Objects.requireNonNull(detectorResult);
        Objects.requireNonNull(document);
        Objects.requireNonNull(fieldName);
        final String field = getLanguageFieldName(fieldName);

        if (document.getField(field).hasValues()) {
            document.getField(field).clear();
        }

        boolean requiresUnknown = true;
        // For each language detected, add the name, language code and the percentage of the language detected within the text data to
        // the document.
        if (detectorResult.getLanguages() != null) {
            final Field fieldToAdd = document.getField(field);
            for (DetectedLanguage detectedLanguage : detectorResult.getLanguages()) {
                if (!detectedLanguage.getLanguageCode().equals("un")) {
                    fieldToAdd.add(detectedLanguage.getConfidencePercentage()
                        + "% " + detectedLanguage.getLanguageCode());
                    requiresUnknown = false;
                }
            }
            if (requiresUnknown) {
                //Adding Field to document to signify that all of the fields content was of an unknown language
                document.getField(field).add("100% un");
            }
        }
    }

    public static void outputDocumentFieldValueChanges(final Document document)
    {
        Objects.requireNonNull(document);
        final String baseOutputDir = System.getenv("CAF_LANG_DETECT_WORKER_OUTPUT_FOLDER");
        final String outputSubdir = document.getCustomData("outputSubfolder");

        // Only output document field value changes if configured to do so
        if (baseOutputDir == null || baseOutputDir.isEmpty()) {
            LOG.debug("No response data output folder specified.");
            return;
        }

        LOG.debug("Outputting document field value changes.");

        final Path outputFir = getFullOutputPath(baseOutputDir, outputSubdir);
        final File outputFile = getFilePath(outputFir, document).toFile();

        // Iterate through each of the document fields and output changes where they exist.
        document.getFields().forEach(field -> {
            try {
                appendFieldValueChangesToFile(field, outputFile);
            } catch (IOException ioe) {
                LOG.warn("Failed to output document field value changes", ioe);
            }
        });
    }

    public static void addDetectedLanguageToDocument(final LanguageDetectorResult detectorResult, final Document document,
                                                     final Field sourceDataField, final boolean inMultiFeildMode)
    {
        Objects.requireNonNull(detectorResult);
        Objects.requireNonNull(document);
        Objects.requireNonNull(sourceDataField);
        Objects.requireNonNull(inMultiFeildMode);
        // Add detected languages to the document object.
        if (inMultiFeildMode) {
            LOG.debug("Adding metadata to the document for each language detected.");
            addDetectedLanguagesToDocument(detectorResult, document, sourceDataField.getName());
        } else {
            // Add detected languages to the document object.
            LOG.debug("Adding metadata to the document for each language detected.");
            addDetectedLanguagesToDocument(detectorResult, document);
        }
    }

    private static void replaceDocumentField(final Document document, final String name, final String value)
    {
        LOG.debug("Replacing metadata field {} with value {} to the document.", name, value);

        // Get a field object for the specified field.
        final Field documentField = document.getField(name);

        // Remove all values from the field.
        documentField.clear();

        // Add the specified value to the field.
        documentField.add(value);
    }

    private static InputStream getInputStream(final FieldValue fieldValue, final DataStore dataStore) throws RuntimeException
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

    private static String getLanguageNameFieldName(final int detectedLanguageId)
    {
        return LanguageDetectionConstants.Fields.DETECTED_LANGUAGE_PREFIX
            + String.valueOf(detectedLanguageId)
            + "_"
            + LanguageDetectionConstants.Fields.DETECTED_LANGUAGE_NAME_SUFFIX;
    }

    private static String getLanguageCodeFieldName(final int detectedLanguageId)
    {
        return LanguageDetectionConstants.Fields.DETECTED_LANGUAGE_PREFIX
            + String.valueOf(detectedLanguageId)
            + "_"
            + LanguageDetectionConstants.Fields.DETECTED_LANGUAGE_CODE_SUFFIX;
    }

    private static String getLanguagePercentageFieldName(final int detectedLanguageId)
    {
        return LanguageDetectionConstants.Fields.DETECTED_LANGUAGE_PREFIX
            + String.valueOf(detectedLanguageId)
            + "_"
            + LanguageDetectionConstants.Fields.DETECTED_LANGUAGE_PERCENTAGE_SUFFIX;
    }

    private static String getLanguageFieldName(final String detectedLanguageField)
    {
        final String languagePrefix = System.getenv("WORKER_LANG_DETECT_FIELD_PREFIX");

        if (languagePrefix == null) {
            return "LANGUAGE_CODE_" + detectedLanguageField;
        }
        return languagePrefix + detectedLanguageField;
    }

    private static Path getFullOutputPath(final String outputDir, final String outputSubdir)
    {
        return (outputSubdir == null)
            ? Paths.get(outputDir)
            : Paths.get(outputDir, outputSubdir);
    }

    private static Path getFilePath(final Path dataOutputFolder, final Document document)
    {
        final String filenameField = getFilenameField();

        final String filename = document.getField(filenameField).getValues()
            .stream()
            .filter(fieldValue -> (!fieldValue.isReference()) && fieldValue.isStringValue())
            .map(FieldValue::getStringValue)
            .filter(fieldValue -> {
                try {
                    dataOutputFolder.resolve(fieldValue);
                    return true;
                } catch (InvalidPathException ex) {
                    return false;
                }
            })
            .findFirst()
            .orElse("out.txt");

        return dataOutputFolder.resolve(filename);
    }

    private static String getFilenameField()
    {
        final String filenameField = System.getenv("CAF_LANG_DETECT_WORKER_OUTPUT_FILENAME_FIELD");

        return (filenameField == null || filenameField.isEmpty())
            ? "FILE_NAME"
            : filenameField;
    }

    private static void appendFieldValueChangesToFile(Field field, File dataOutputFile) throws IOException
    {
        // Output document field value changes.
        if (field.hasChanges() && field.hasValues()) {
            for (final FieldValue fv : field.getValues()) {
                if (!fv.isReference()) {
                    final String changeValueDetails = field.getName() + ": " + fv.getStringValue() + "\r\n";
                    FileUtils.writeStringToFile(dataOutputFile, changeValueDetails, StandardCharsets.UTF_8, true);
                }
            }
        }
    }
}
