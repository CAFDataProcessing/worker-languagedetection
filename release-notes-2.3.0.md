
#### Version Number
${version-number}

#### New Features
- [CAF-1389](https://jira.autonomy.com/browse/CAF-1389): Support language detection on multiple fields in Workflow  
  The Worker now supports multi-field processing, allowing multiple field names to be specified for language detection. The response will indicate the languages that were detected in each field.
- [CAF-3113](https://jira.autonomy.com/browse/CAF-3113): Updated to consume latest worker-document-framework which includes change to how Composite Document Workers handle failures.
  Now if the Worker encounters a task failure e.g. due to a message being marked as poison, the response task will include the document sent on the task with the failure added as a field.
  
#### Known Issues
