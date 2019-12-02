/*
 * Copyright 2015-2020 Micro Focus or one of its affiliates.
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
package com.hpe.caf.worker.languagedetection;

import com.hpe.caf.api.ConfigurationException;
import com.hpe.caf.api.ConfigurationSource;
import com.hpe.caf.worker.document.extensibility.DocumentWorker;
import com.hpe.caf.worker.document.extensibility.DocumentWorkerFactory;
import com.hpe.caf.worker.document.model.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating an instance of LanguageDetectionWorker.
 */
public class LanguageDetectionWorkerFactory implements DocumentWorkerFactory
{
    private static final Logger LOG = LoggerFactory.getLogger(LanguageDetectionWorkerFactory.class);

    @Override
    public DocumentWorker createDocumentWorker(Application application)
    {
        LanguageDetectionWorkerConfiguration configuration;
        try {
            configuration = application.getService(ConfigurationSource.class)
                .getConfiguration(LanguageDetectionWorkerConfiguration.class);
        } catch (ConfigurationException e) {
            LOG.error("Unable to retrieve worker configuration.", e);
            return new UnhealthyWorker("Unable to retrieve worker configuration. " + e.getMessage());
        }

        return new LanguageDetectionWorker(application, configuration);
    }
}
