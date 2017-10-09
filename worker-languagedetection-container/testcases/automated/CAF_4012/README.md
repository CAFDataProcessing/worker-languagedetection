## CAF_4012 - Language Detection configured to output an Unknown Result Format ##

Process the specified text documents that are all in variety of languages with the result format configured to be an unknown value.

**Test Steps**

1. Set up system to perform language detection with the result format configured to be an unknown value
2. Examine output

**Test Data**

Variety of documents in varying languages

**Expected Result**

The files are all processed and an "InvalidResultFormat" error is thrown with the message "No enum constant com.hpe.caf.worker.languagedetection.LanguageDetectionResultFormat.RESULT_FORMAT"

**JIRA Link** - [CAF-3567](https://jira.autonomy.com/browse/CAF-3567)

