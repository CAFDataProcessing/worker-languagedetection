/*
 * Copyright 2015-2025 Open Text.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.cafdataprocessing.workers.languagedetection;

import com.github.cafdataprocessing.workers.document.exceptions.DocumentWorkerTransientException;
import com.github.cafdataprocessing.workers.document.extensibility.DocumentWorker;
import com.github.cafdataprocessing.workers.document.model.Document;
import com.github.cafdataprocessing.workers.document.model.HealthMonitor;

/**
 * A representation of a worker that cannot handle tasks and thus reports unhealthy and fails when receiving task messages.
 */
public class UnhealthyWorker implements DocumentWorker
{
    private final String message;

    public UnhealthyWorker(final String message)
    {
        this.message = message;
    }

    @Override
    public void checkLiveness(HealthMonitor healthMonitor)
    {
        healthMonitor.reportUnhealthy(message);
    }

    @Override
    public void checkHealth(HealthMonitor healthMonitor)
    {
        healthMonitor.reportUnhealthy(message);
    }

    @Override
    public void processDocument(final Document document) throws InterruptedException, DocumentWorkerTransientException
    {
        throw new DocumentWorkerTransientException("This Worker instance is unhealthy: " + message);
    }
}
