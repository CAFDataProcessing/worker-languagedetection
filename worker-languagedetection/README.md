# Language Detection Worker

The Language Detection Worker uses a language detection interface whose implementation wraps the Compact Language Detector 2 Library (CLD2) Language Detection Library. The library probabilistically detects over 80 languages in Unicode UTF-8 text. Legacy encodings must be converted to valid UTF-8. For mixed language input, the library returns the top three languages found and their approximate percentages of the total text bytes (e.g. 80% English and 20% French out of 1000 bytes of text means about 800 bytes of English and 200 bytes of French).

The library is not designed to do well on very short text, short lists of names, part numbers, etc.

Several hints can be supplied which add a bias to the language detection but do not force a specific language to be the detection result. For example: "en" boosts English, "mi, fr" boosts Maori and French, "ITALIAN" boosts Italian, "SJS" boosts Japanese. Hints should be supplied whenever possible as they improve detection accuracy.

## Configuration

### Global Settings

Worker configuration is supported through the following environment variables:

 - `WORKER_LANG_DETECT_SOURCE_FIELD`  
    Default: `CONTENT`  
    Used to specify which document field is used for data sources, however if fieldSpecs is passed as custom data this value is ignored.

-  `WORKER_LANG_DETECT_FIELD_PREFIX`
	Default: `LANGUAGE_CODE_`
    This is used when processing using the new mode of operation that can process multiple fields at the same time. It will specify the name of the field that is to be added to the document to record results. For example if the worker is checking the `CONTENT` field it will add the field `LANGUAGE_CODE_CONTENT` to the document.

 - `CAF_WORKER_THREADS`  
    Default: `1`  
    Specifies the number of threads used for parallel processing

 - `CAF_WORKER_OUTPUT_QUEUE`  
    Default: `worker-out`  
    Sets the output queue where results are returned

### Per Tenant Settings
 - `fieldSpecs`  
    This field specifies to the worker on what fields to perform language detection. It is a string of comma-seperated values, each value will either be a complete field name or a partial field name supplied with a wildcard character "*".
    **Example:** `{"fieldSpecs": "CONTENT_*, TITLE, SUBJECT"}`
	This Example will cause the worker to check the TITLE and SUBJECT fields of the document provided as well as any field that begins `CONTENT_`.

## Output Format

#### Standard processing:

The document is marked up with a list of the languages detected as well as the confidence in the result. The following fields are added:

- DetectedLanguages_Status : Indicates the processing result status. Any value other than `COMPLETED` means failure.
    
- DetectedLanguageN_Name : The name of the Nth language detected   
e.g. DetectedLanguage1\_Name:"English"

- DetectedLanguageN_Code : The ISO 639-1 language code of the Nth language detected.   
e.g. DetectedLanguage1\_Code:"en"

- DetectedLanguageN_ConfidencePercentage : The percentage of the language detected within the document text.   
e.g. DetectedLanguage1\_ConfidencePercentage:100

- DetectedLanguages_ReliableResult : A boolean flag that signals whether the result is reliable. 

#### Multi-Field Processing:

- `WORKER_LANG_DETECT_FIELD_PREFIX$FieldName`: 
   This field will contain two values at present, the first value in this string will be the percentage of the detected language, e.g. `98%`. The second value in the string after a space will be the language that was detected language code, e.g. `en`. If a field is multi value this result will be a combined result for all of the values.

**NOTE:** This fields value may be extended in future, between each value within this string will be a space separating the result values. In future confidence percentage could also be added.

### Temporary File Output Mode
The detected languages are always added to the document as fields, but the current version of the worker also supports a second mode for additionally returning the detected languages.  Setting the `CAF_LANG_DETECT_WORKER_OUTPUT_FOLDER` environment variable causes the detected languages to be written to disk.

Note that this option is temporary and will be removed in a future version.

It is controlled using the following environment variables:

 - `CAF_LANG_DETECT_WORKER_OUTPUT_FOLDER`:  
    Default: None  
    Setting this environment variable causes the detected languages to be written to files on disk, in addition to being added as fields to the document.  The location specifies the output folder.

 - `CAF_LANG_DETECT_WORKER_OUTPUT_FILENAME_FIELD`:  
    Default: `FILE_NAME`  
    Specifies the field which contains the output filename which should be used.

In addition, the following configuration option can be passed using the Document Worker 'customData' facility:

 - `outputSubfolder`:  
    Default: None  
    Specifies a subfolder within the output folder where the file should be written.  If this is not specified then the file is written directly to the output folder.

## Health Check

This worker provides a basic health check by testing if it can call into the language detection library and verifying that a language detector was correctly obtained from the implementation.

## Failure Modes

The main places where `worker-languagedetection` can fail are:

- Configuration errors: these will manifest on startup and cause the worker to
not start at all. Check the logs for clues, and double check your configuration
files.

- `DataStore` errors: caused when the worker fails to retrieve data from the store. This is most likely caused by the store connection settings being mis-configured.

- Language detection errors: when no languages can be detected by the language detection implementation. This may be due to incorrect encoding, if the text is not long enough or if the text does not contain a language.
Valid text should be over 200 characters encoded in UTF-8.
