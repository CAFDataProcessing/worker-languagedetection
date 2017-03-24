# Language Detection Worker

The Language Detection Worker uses a language detection interface whose implementation wraps the Compact Language Detector 2 Library (CLD2) Language Detection Library. The library probabilistically detects over 80 languages in Unicode UTF-8 text. Legacy encodings must be converted to valid UTF-8. For mixed language input, the library returns the top three languages found and their approximate percentages of the total text bytes (e.g. 80% English and 20% French out of 1000 bytes of text means about 800 bytes of English and 200 bytes of French).

The library is not designed to do well on very short text, short lists of names, part numbers, etc.

Several hints can be supplied which add a bias to the language detection but do not force a specific language to be the detection result. For example: "en" boosts English, "mi, fr" boosts Maori and French, "ITALIAN" boosts Italian, "SJS" boosts Japanese. Hints should be supplied whenever possible as they improve detection accuracy.

## Configuration

### Global Settings

#### DocumentWorkerConfiguration

The worker uses the [worker-document](https://github.hpe.com/caf/worker-document) system of `DocumentWorkerConfiguration`. The configuration class is [DocumentWorkerConfiguration](https://github.hpe.com/caf/worker-document/blob/develop/worker-document/src/main/java/com/hpe/caf/worker/document/DocumentWorkerConfiguration.java), which has the following relevant options:

- `workerName`: the name of the worker
- `workerVersion`: the version number of the worker
- `outputQueue`: the output queue to return results to RabbitMQ
- `threads`: the number of threads to be used to host this Worker

#### Environment Variables
Additional worker configuration is supported through the following environment variables:

- `WORKER_LANG_DETECT_SOURCE_FIELD`: Optional. This is used to specify which document field is used for data sources. e.g. CONTENT 

#### Temporary Environment Variables
The following environment variables are also supported however these are temporary and will be removed in a future version:

- `CAF_RESPONSE_DATA_OUTPUT_FOLDER`: Optional. This is used to specify the mounted location in the worker container where response data will be output to.  
- `CAF_RESPONSE_DATA_OUTPUT_FILE_NAME`: Optional. This is the filename which will be used to record the response data.

### Per-tenant Configuration Settings
The following per-tenant configuration is supplied through the ‘customData’ facility:

- `dataOutputSubFolder`: Optional. This is used to specify the output sub folder where the response data will be written to. It is used along with the environment variable setting values for `CAF_RESPONSE_DATA_OUTPUT_FOLDER` and `CAF_RESPONSE_DATA_OUTPUT_FILE_NAME` to specify the full path of the response data output location and filename.

## Output Format

The document is marked up with a list of the languages detected as well as the confidence in the result. The following fields are added:

- DetectedLanguages_Status : Indicates the processing result status. Any value other than `COMPLETED` means failure.
    
- DetectedLanguageN_Name : The name of the Nth language detected   
e.g. DetectedLanguage1\_Name:"English"

- DetectedLanguageN_Code : The ISO 639-1 language code of the Nth language detected.   
e.g. DetectedLanguage1\_Code:"en"

- DetectedLanguageN_ConfidencePercentage : The percentage of the language detected within the document text.   
e.g. DetectedLanguage1\_ConfidencePercentage:100

- DetectedLanguages_ReliableResult : A boolean flag that signals whether the result is reliable. 

These fields are written out to disk if the environment and per-tenant settings for `CAF_RESPONSE_DATA_OUTPUT_FOLDER`, `CAF_RESPONSE_DATA_OUTPUT_FILE_NAME` and  `dataOutputSubFolder` are configured.

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
