# Language Detection Worker

The Language Detection Worker uses a language detection interface whose implementation wraps the Compact Language Detector 2 Library (CLD2) Language Detection Library. The library probabilistically detects over 80 languages in Unicode UTF-8 text. Legacy encodings must be converted to valid UTF-8. For mixed language input, the library returns the top three languages found and their approximate percentages of the total text bytes (e.g. 80% English and 20% French out of 1000 bytes of text means about 800 bytes of English and 200 bytes of French).

The library is not designed to do well on very short text, short lists of names, part numbers, etc.

Several hints can be supplied which add a bias to the language detection but do not force a specific language to be the detection result. For example: "en" boosts English, "mi, fr" boosts Maori and French, "ITALIAN" boosts Italian, "SJS" boosts Japanese. Hints should be supplied whenever possible as they improve detection accuracy.

The Language Detection worker has two modes of operation, Standard Processing and Multi-Field Processing. The first mode of operation will cause the worker to perform language detection on the `CONTENT` field should no other field be specified via an environment variable. However, if `fieldSpecs` is provided to the worker on the document's `customData` the worker will begin to use Multi-Field Processing mode and identify any languages present on all of the fields specified. See [Per Tenant Settings](#per-tenant-settings) for more information on this field.

## Configuration

### Global Settings

Worker configuration is supported through the following environment variables:

 - `WORKER_LANG_DETECT_SOURCE_FIELD`  
    Default: `CONTENT`  
    Used to specify which document field is used for data sources.  
	However if fieldSpecs is passed as custom data this value is ignored.

-  `WORKER_LANG_DETECT_FIELD_PREFIX`  
	Default: `LANGUAGE_CODE_`  
    This is used when processing using the Multi-Field mode of operation that can process multiple fields at the same time. It will specify the field name prefix to use when creating a field that will be added to the document to record results. For example if the worker is checking the `CONTENT` field it will add the field `LANGUAGE_CODE_CONTENT` to the document.

 - `CAF_WORKER_THREADS`  
    Default: `1`  
    Specifies the number of threads used for parallel processing

 - `CAF_WORKER_OUTPUT_QUEUE`  
    Default: `worker-out`  
    Sets the output queue where results are returned

 - `CAF_LANGUAGE_DETECTION_WORKER_RESULT_FORMAT`  
    Default: `SIMPLE`  
    Controls the default result format to use if none is passed in the custom data [resultFormat](#resultformat) property.

 - `CAF_WORKER_STATIC_SCRIPT_CACHE_SIZE`  
    Default: `50`  
    This setting controls the maximum number of scripts to hold in the static script cache. The static script cache is used to cache static scripts such as inline scripts and scripts downloaded from the remote data store (which doesn't support updating).

 - `CAF_WORKER_STATIC_SCRIPT_CACHE_DURATION`  
    Default: `1800` (30 minutes)  
    This setting controls the length of time that static scripts are cached for. If the script is not used for the specified duration then it will be flushed from the cache. If it is used from the cache then the timer will be reset.

 - `CAF_WORKER_DYNAMIC_SCRIPT_CACHE_SIZE`  
    Default: `50`  
    This setting controls the maximum number of scripts to hold in the dynamic script cache. The dynamic script cache is used to cache scripts which could potentially be changed whilst the worker is running, such as scripts specified by URL.

 - `CAF_WORKER_DYNAMIC_SCRIPT_CACHE_DURATION`  
    Default: `1800` (30 minutes)  
    This setting controls the length of time that static scripts are cached for. This is a fixed duration and the use of the script from the cache will not cause the timer to be reset.

### Per Tenant Settings

#### fieldSpecs

When `fieldSpecs` is provided to the worker via `customData` the worker's behaviour will change and the worker will begin [Multi-Field Processing](#multi-field-processing).

 - `fieldSpecs`  
    This parameter specifies to the worker the fields on which to perform language detection. It is a string of comma-separated values, each of which is either a complete field name or a partial field name supplied with a wildcard character "*". The wildcard character is supported at any position within any of the values of this parameter.
    
**Example:**   
	`{"fieldSpecs": "CONTENT_*, TITLE, SUBJECT"}`  
	This Example will cause the worker to check the TITLE and SUBJECT fields of the document provided as well as any field that begins `CONTENT_`.
  
#### resultFormat

The `resultFormat` property can be provided to the worker via `customData` to alter the format of the output language detection result. There are two supported formats.

##### SIMPLE

The output fields from language detection will output fields as described by [Standard Processing](#standard-processing) or [Multi-Field Processing](#multi-field-processing) depending on if `fieldSpecs` was passed.

##### COMPLEX or COMPLEX_COMBINED

This format will change the output result to be a single field on the document whose value is a string version of an object. The object will be an array of objects each having the properties 'CODE' (a language code detected) and 'CONFIDENCE' (the percentage of the language detected within the document text).

**Example Custom Data Inputs** 

```
{
  "resultFormat": "COMPLEX"
}
```

```
{
  "resultFormat": "COMPLEX_COMBINED"
}
```

**Example Output Field**

```
LANGUAGE_CODES: "[{\"CODE\":\"de\",\"CONFIDENCE\":\"37\"},{\"CODE\":\"fr\",\"CONFIDENCE\":\"35\"},{\"CODE\":\"en\",\"CONFIDENCE\":\"27\"}]"
```

If no known languages are detected then the language code "un" for 'unknown' will be output with a confidence of 100.

**Example**

```
LANGUAGE_CODES: "[{\"CODE\":\"un\",\"CONFIDENCE\":\"100\"}]"
```

##### COMPLEX_SPLIT

This format will change the output result to fields on the document for each language detected whose value is a string version of an object. The object will have the properties 'CODE' (a language code detected) and 'CONFIDENCE' (the percentage of the language detected within the document text).

**Example Custom Data Inputs** 

```
{
  "resultFormat": "COMPLEX_SPLIT"
}
```

**Example Output Field**

```
LANGUAGE_CODES: "[{\"CODE\":\"de\",\"CONFIDENCE\":\"37\"},{\"CODE\":\"fr\",\"CONFIDENCE\":\"35\"},{\"CODE\":\"en\",\"CONFIDENCE\":\"27\"}]"
```

If no known languages are detected then the language code "un" for 'unknown' will be output with a confidence of 100.

**Example**

```
LANGUAGE_CODES: "[{\"CODE\":\"un\",\"CONFIDENCE\":\"100\"}]"
```

The `fieldSpecs` property may be passed alongside `resultFormat` to control the field used in language detection however multiple fields in `fieldSpecs` when `resultFormat` is set to *COMPLEX*, *COMPLEX_COMBINED* or *COMPLEX_SPLIT* is not supported and will cause a failure to be added to the document.

### Simple Output Formats

These output formats can apply if `resultFormat` is not passed on `customData` or is passed with a value `SIMPLE`.

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

- `WORKER_LANG_DETECT_FIELD_PREFIX + $FieldName`: 
   This field will contain two values at present, the first value in this string will be the percentage of the detected language, e.g. `98%`. The second value in the string after a space will be the language that was detected language code, e.g. `en`. If a field is multi value this result will be a combined result for all of the values.       
   **e.g.** `LANGUAGE_CODE_CONTENT: 99% en`

**NOTE:** In future we may expand the field value to include whether the result is unreliable so it is recommended to split on spaces and take the second value to retrieve the language code. Any additions to the value will include a space before the new value.

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

- If the `customData` on a task defines `fieldSpecs` with multiple fields and `resultFormat` is passed with a value of "COMPLEX". This is not supported and a failure will be added to the document.
