## CAF_4010 - Language Detection output a Complex result field for an Unknown Language ##

Process the specified text documents that are all in unknown languages with the result format configured to be COMPLEX

**Test Steps**

1. Set up system to perform language detection with the result format configured to be COMPLEX in the Custom Data
2. Examine output

**Test Data**

Variety of documents in unknown languages

**Expected Result**

The files are all processed and the top 3 most occurring languages are in a complex field called "LANGUAGE_CODES" with the value "un" in the code field.

**JIRA Link** - [CAF-3567](https://jira.autonomy.com/browse/CAF-3567)

