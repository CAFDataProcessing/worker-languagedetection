!not-ready-for-release!

#### Version Number
${version-number}

#### New Features
- US616155: This worker now supports message prioritization.  

  Messages intended for this worker can be redirected to one or more staging queues instead of the worker's target queue.  This feature can be enabled by setting the `CAF_WORKER_ENABLE_DIVERTED_TASK_CHECKING` environment variable to `false` on **this worker**, and setting the `CAF_WMP_ENABLED` environment variable to `true` on the **component that routes messages to this worker**.   

  The `CAF_WMP_USE_TARGET_QUEUE_CAPACITY_TO_REROUTE` environment variable (default: `false`) determines whether this worker's target queue capacity should be considered when making a decision on whether to reroute a message. This should be set on the **component that routes messages to this worker**.
If `true`, a message will only be rerouted to a staging queue if the target queue does not have capacity for it. If `false`, a message will **always** be rerouted to a staging queue, ignoring the target queue's capacity.

  The `CAF_WMP_KUBERNETES_NAMESPACES` environment variable used to specify the Kubernetes namespaces, comma separated, in which to search for this worker's labels.  This should be set on the **component that routes messages to this worker**. These labels contain information this worker's target queue, such as its name and maximum length. A non-null and non-empty value must be provided for this environment variable if `CAF_WMP_USE_TARGET_QUEUE_CAPACITY_TO_REROUTE` is true. If `CAF_WMP_USE_TARGET_QUEUE_CAPACITY_TO_REROUTE` is false, this environment variable is not used.  

  The `CAF_WMP_KUBERNETES_LABEL_CACHE_EXPIRY_MINUTES` (default: `60`) is used to specify the 'expire after write' minutes after which a Kubernetes label that has been added to the cache should be removed. This should be set on the **component that routes messages to this worker**. Set this to 0 to disable caching. Only used when `CAF_WMP_USE_TARGET_QUEUE_CAPACITY_TO_REROUTE` is true. If `CAF_WMP_USE_TARGET_QUEUE_CAPACITY_TO_REROUTE` is false, this environment variable is not used.  

#### Known Issues
