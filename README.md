# Language Detection Worker

The Language Detection Worker will read a message from the queue and return a list of the top three languages present in the text data.

For more information on the functioning of the Language Detection Worker visit [Language Detection Worker](worker-languagedetection/README.md).

## Modules

### worker-languagedetection
This project contains the actual implementation of the Language Detection Worker. It can be found in [worker-languagedetection](worker-languagedetection).

### worker-languagedetection-container
This project builds a Docker image that packages the Language Detection Worker for deployment. It can be found in [worker-languagedetection-container](worker-languagedetection-container).

### language-detection
An interface for language detection implementations. It can be found in [language-detection](language-detection)

### language-detection-fasttext
An implementation of the [language-detection](language-detection) api which wraps the fasttext python library. It can be found in [language-detection-fasttext](language-detection-fasttext).

## Feature Testing
The testing for the Language Detection Worker is defined in [testcases](worker-languagedetection-container/testcases).