({
    workerName: "worker-languagedetection",
    workerVersion: "${project.version}",
    outputQueue: getenv("CAF_WORKER_OUTPUT_QUEUE")
            || (getenv("CAF_WORKER_BASE_QUEUE_NAME") || getenv("CAF_WORKER_NAME") || "worker") + "-out",
    threads: getenv("CAF_WORKER_THREADS") || 1,
    maxBatchSize: getenv("CAF_WORKER_MAX_BATCH_SIZE") || undefined,
    maxBatchTime: getenv("CAF_WORKER_MAX_BATCH_TIME") || undefined
});
