#### Version Number
${version-number}

#### New Features
- [CAF-3859](https://jira.autonomy.com/browse/CAF-3859): New complex output format variation. 
  A new variation for the complex output format has been added where instead of a single field added to a document containing a JSON encoded array of the matches (the original `COMPLEX` output format) each match will instead be output as a separate field with a value set to the JSON encoded object containing the match properties. This new format can be activated by passing `resultFormat` as `COMPLEX_SPLIT`.

#### Known Issues
