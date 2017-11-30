## CAF_4013 - Language Detection output a Complex Split result field ##

Process the specified text documents that are all in variety of languages with the result format configured to be COMPLEX_SPLIT

**Test Steps**

1. Set up system to perform language detection with the result format configured to be COMPLEX_SPLIT in the Custom Data
2. Examine output

**Test Data**

Variety of documents in varying languages

**Expected Result**

The files are all processed and the top 3 most occurring languages are in a complex field called "LANGUAGE_CODES" and in the format:

<code>
"LANGUAGE_CODES": [
   "{\"CODE\":\"es\",\"CONFIDENCE\":\"34\"}",
   "{\"CODE\":\"eu\",\"CONFIDENCE\":\"34\"}",
   "{\"CODE\":\"ca\",\"CONFIDENCE\":\"31\"}"
]
</code>

**JIRA Link** - [CAF-3859](https://jira.autonomy.com/browse/CAF-3859)

