## CAF_4011 - Language Detection configured to output a Complex result field with multiple FieldSpecs defined ##

Process the specified text documents that are all in variety of languages with the result format configured to be COMPLEX with multiple FieldSpecs defined

**Test Steps**

1. Set up system to perform language detection with the result format configured to be COMPLEX in the Custom Data with multiple FieldSpecs defined
2. Examine output

**Test Data**

Variety of documents in varying languages

**Expected Result**

The files are all processed and an "InvalidCustomData" error is thrown with the message "Multiple fields are not supported on the 'fieldSpecs' task property when 'resultFormat' is set to a complex format."

**JIRA Link** - [CAF-3567](https://jira.autonomy.com/browse/CAF-3567)

