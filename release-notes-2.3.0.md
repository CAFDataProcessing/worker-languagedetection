!not-ready-for-release!

#### Version Number
${version-number}

#### New Features
- [CAF-3113](https://jira.autonomy.com/browse/CAF-3113): Updated to consume latest worker-document-framework which includes change to how Composite Document Workers handle failures.
  Now if the Worker encounters a task failure e.g. due to a message being marked as poison, the response task will include the document sent on the task with the failure added as a field.
  
#### Known Issues
