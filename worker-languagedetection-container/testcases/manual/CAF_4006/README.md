## CAF_4006 - Null or Empty Storage Reference sent to LangDetect Worker ##

Verify that a task sent to language detection worker with a null or empty storage reference is returned as an INVALID_TASK

**Test Steps**

1. Set up system to perform LangDetect and send a task message to the worker that contains a null or empty storage reference
2. Examine the output

**Test Data**

Plain text files

**Expected Result**

The output message is returned with a status of INVALID_TASK

**JIRA Link** - [CAF-1244](https://jira.autonomy.com/browse/CAF-1244)




