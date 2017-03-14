# Language Detection Worker

The Language Detection Worker will read a message from the queue and return a list of the top three languages present in the text data.

For more information on the functioning of the Language Detection Worker visit [Language Detection Worker](worker-languagedetection/README.md).

## Modules

### worker-languagedetection
This project contains the actual implementation of the Language Detection Worker. It can be found in [worker-languagedetection](worker-languagedetection).

### worker-languagedetection-container
This project builds a Docker image that packages the Language Detection Worker for deployment. It can be found in [worker-languagedetection-container](worker-languagedetection-container).
