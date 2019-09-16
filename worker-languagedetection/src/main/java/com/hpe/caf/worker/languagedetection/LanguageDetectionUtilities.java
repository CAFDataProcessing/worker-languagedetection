/*
 * Copyright 2015-2019 Micro Focus or one of its affiliates.
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

import com.hpe.caf.languagedetection.DetectedLanguage;
import com.hpe.caf.languagedetection.LanguageDetectorResult;
import com.hpe.caf.worker.document.model.Document;
import com.hpe.caf.worker.document.model.Field;
import com.hpe.caf.worker.document.model.FieldValue;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public final class LanguageDetectionUtilities
{
    private static final Logger LOG = LoggerFactory.getLogger(LanguageDetectionUtilities.class);

    private LanguageDetectionUtilities()
    {
    }

    public static SequenceInputStream getFieldValuesAsStreams(final Field sourceDataField)
        throws RuntimeException
    {
        Objects.requireNonNull(sourceDataField);
        final List<InputStream> streams = new ArrayList<>();
        for (FieldValue fv : sourceDataField.getValues()) {
            final InputStream is = getInputStream(fv);
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

        boolean requiresUnknown = true;
        // For each language detected, add the name, language code and the percentage of the language detected within the text data to
        // the document.
        if (detectorResult.getLanguages() != null) {
            final Field fieldToAdd = document.getField(field);
            fieldToAdd.clear();
            for (DetectedLanguage detectedLanguage : detectorResult.getLanguages()) {
                if (!detectedLanguage.getLanguageCode().equals("un")) {
                    fieldToAdd.add(detectedLanguage.getConfidencePercentage()
                        + "% " + detectedLanguage.getLanguageCode());
                    requiresUnknown = false;
                }
            }
            if (requiresUnknown) {
                //Adding Field to document to signify that all of the fields content was of an unknown language
                fieldToAdd.add("100% un");
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

    /**
     * Updates the passed {@code document} with the language detection result passed in {@code detectorResult}.
     *
     * @param detectorResult result of performing language detection. Cannot be null.
     * @param document the document to update with result of language detection. Cannot be null.
     * @param sourceDataField the field that language detection was ran against. Depending on the values of {@code resultFormat} and
     * {@code inMultiFieldMode} this may be used in the output field name. Cannot be null.
     * @param resultFormat whether the result fields should be output in simple or complex format. If set to COMPLEX then
     * {@code inMultiFieldMode} has no effect on output fields. Cannot be null.
     * @param inMultiFieldMode whether the language detection was ran in multi-field mode. This will effect the fields output but only if
     * {@code resultFormat} is set to {@code LanguageDetectionResultFormat.SIMPLE}.
     * @throws RuntimeException if {@code detectorResult}, {@code document} or {@code sourceDataField} is null.
     */
    public static void addDetectedLanguageToDocument(final LanguageDetectorResult detectorResult, final Document document,
                                                     final Field sourceDataField,
                                                     final LanguageDetectionResultFormat resultFormat,
                                                     final boolean inMultiFieldMode)
        throws RuntimeException
    {
        Objects.requireNonNull(detectorResult);
        Objects.requireNonNull(document);
        Objects.requireNonNull(sourceDataField);
        Objects.requireNonNull(resultFormat);

        // Add detected languages to the document object.
        if (resultFormat == LanguageDetectionResultFormat.SIMPLE) {
            if (inMultiFieldMode) {
                LOG.debug("Adding metadata to the document for each language detected in multi-field mode. "
                    + "Fields will be output in simple format.");
                addDetectedLanguagesToDocument(detectorResult, document, sourceDataField.getName());
            } else {
                // Add detected languages to the document object.
                LOG.debug("Adding metadata to the document for each language detected. "
                    + "Fields will be output in simple format.");
                addDetectedLanguagesToDocument(detectorResult, document);
            }
        } else if (LanguageDetectionResultFormat.isComplexFormat(resultFormat)) {
            LOG.debug("Adding metadata to the document for each language detected. Fields will be output in complex format.");
            addDetectedLanguageToDocumentComplexMode(detectorResult, document, resultFormat);
        }
    }

    /**
     * Updates the passed @code document} with the language detection result passed in by adding a field to the document that records the
     * result in complex form.
     *
     * @param detectorResult result of performing language detection.
     * @param document the document to update with result of language detection
     * @param resultFormat the format to output result in. Should be a complex format type.
     */
    private static void addDetectedLanguageToDocumentComplexMode(final LanguageDetectorResult detectorResult,
                                                                 final Document document,
                                                                 final LanguageDetectionResultFormat resultFormat)
    {
        Collection<DetectedLanguage> detectedLanguages = detectorResult.getLanguages();
        if (detectedLanguages == null || detectedLanguages.isEmpty()) {
            LOG.debug("No languages detected for the document.");
            return;
        }

        List<JSONObject> languageCodesToAdd = new ArrayList<>();

        boolean unknownOnlyLanguageDetected = true;
        for (DetectedLanguage detectedLanguage : detectedLanguages) {
            String languageCode = detectedLanguage.getLanguageCode();
            // Only add an output entry for unknown language code if it is the only detected language. 3 languages
            // are always 'detected' so first may be English and then unknown twice.
            if ("un".equals(languageCode)) {
                continue;
            }
            unknownOnlyLanguageDetected = false;
            languageCodesToAdd.add(buildLanguageCodeEntry(languageCode,
                                                          String.valueOf(detectedLanguage.getConfidencePercentage())));
        }
        if (unknownOnlyLanguageDetected) {
            languageCodesToAdd.add(buildLanguageCodeEntry("un", "100"));
        }

        // Output in specific complex format
        if (LanguageDetectionResultFormat.COMPLEX.equals(resultFormat)
            || LanguageDetectionResultFormat.COMPLEX_COMBINED.equals(resultFormat)) {
            JSONArray languageCodes = new JSONArray();
            languageCodesToAdd.stream().forEach(lc -> languageCodes.put(lc));
            replaceDocumentField(document, "LANGUAGE_CODES", languageCodes.toString());
            return;
        } else if (LanguageDetectionResultFormat.COMPLEX_SPLIT.equals(resultFormat)) {
            Field langCodeField = document.getField("LANGUAGE_CODES");
            langCodeField.clear();
            languageCodesToAdd.stream().forEach(lc -> langCodeField.add(lc.toString()));
            return;
        } else {
            throw new RuntimeException("Unrecognized complex output format for language result. Format was: "
                + resultFormat.toString());
        }
    }

    private static JSONObject buildLanguageCodeEntry(String languageCode, String confidence)
    {
        JSONObject languageCodeEntry = new JSONObject();
        languageCodeEntry.put("CODE", languageCode);
        languageCodeEntry.put("CONFIDENCE", confidence);
        return languageCodeEntry;
    }

    private static void replaceDocumentField(final Document document, final String name, final String value)
    {
        LOG.debug("Replacing metadata field {} with value {} to the document.", name, value);
        document.getField(name).set(value);
    }

    private static InputStream getInputStream(final FieldValue fieldValue) throws RuntimeException
    {
        try {
            return fieldValue.openInputStream();
        } catch (IOException ex) {
            LOG.error("Failed to acquire source data from the remote data store");
            // Convert to unchecked exception for streams api usage.
            throw new RuntimeException(ex);
        }
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
