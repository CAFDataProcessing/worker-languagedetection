## CAF_4003 - Tag processed items with Worker name and version ##

Verify that when an item is processed by the Language Detect worker it gets tagged with the name and version of the worker

**Test Steps**

1. Set up system to perform Language Detect using the debug parameter
2. Examine the output messages from the Language Detect worker

**Test Data**

Any text file

**Expected Result**

The output task message will contain a "sourceInfo" section that has the name and version of the Language Detect worker

**JIRA Link** - [CAF-188](https://jira.autonomy.com/browse/CAF-188)

