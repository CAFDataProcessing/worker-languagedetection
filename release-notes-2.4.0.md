#### Version Number
${version-number}

#### New Features
- [CAF-3567](https://jira.autonomy.com/browse/CAF-3567): New output mode added.
    A new output mode has been added for detected languages which outputs the language codes as a serialized object.
    e.g.    
      ```“LANGUAGE_CODES”: "[{ \"CODE\":\"fr\", \"CONFIDENCE\":\"60\" }, { \"CODE\":\"en\", \"CONFIDENCE\":\"20\" }, { \"CODE\":\"tib\", \"CONFIDENCE\":\"10\" }]"
      ```
    Detail on enabling this 'COMPLEX' mode can be found under 'Output Formats' in the worker documentation.
    
#### Known Issues