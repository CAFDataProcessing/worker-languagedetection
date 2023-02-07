!not-ready-for-release!

#### Version Number
${version-number}

#### New Features
- US616155: This worker now supports message prioritization.  

  Messages intended for this worker can be redirected to one or more staging queues instead of the worker's target queue.  This feature can be enabled by setting the `CAF_WORKER_ENABLE_DIVERTED_TASK_CHECKING` environment variable to `false` on **this worker**, and setting the `CAF_WMP_ENABLED` environment variable to `true` on the **worker that routes messages to this worker**.   

  The `CAF_WMP_USE_TARGET_QUEUE_CAPACITY_TO_REROUTE` environment variable (default: `false`) determines whether a worker should use the target queue's capacity when making a decision on whether to reroute a message. This should be set on the **worker that routes messages to this worker**.
If `true`, a message will only be rerouted to a staging queue if the target queue does not have capacity for it. If `false`, a message will **always** be rerouted to a staging queue, irregardless of the target queue's capacity.

#### Known Issues
