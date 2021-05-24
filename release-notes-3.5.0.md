#### Version Number
${version-number}

#### New Features
- SCMOD-12730: Added pause task support.
  - When a worker receives a task, it will now check if the task has been paused.
  - If the task has been paused, and the `CAF_WORKER_PAUSED_QUEUE` environment variable is set, the worker will publish the task to the
  `CAF_WORKER_PAUSED_QUEUE` instead of processing it.
  - If the task has been paused, and the `CAF_WORKER_PAUSED_QUEUE` environment variable is NOT set, the worker process the task as
  normal (as if the task was not paused).

#### Known Issues
- None
