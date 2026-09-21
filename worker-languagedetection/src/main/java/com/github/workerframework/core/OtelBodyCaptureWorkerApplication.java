/*
 * Copyright 2015-2026 Open Text.
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
package com.github.workerframework.core;

import com.github.cafdataprocessing.workers.languagedetection.tracing.HttpBodyTracingFilter;
import com.github.cafdataprocessing.workers.languagedetection.tracing.OtelBodyCaptureResource;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import jakarta.servlet.DispatcherType;
import java.util.EnumSet;

/**
 * Wrapper around the Worker Framework application that adds body-capture tracing hooks for this worker.
 */
public final class OtelBodyCaptureWorkerApplication extends Application<WorkerConfiguration>
{
    private final WorkerApplication delegate = new WorkerApplication();

    public static void main(final String[] args) throws Exception
    {
        new OtelBodyCaptureWorkerApplication().run(args);
    }

    @Override
    public void initialize(final Bootstrap<WorkerConfiguration> bootstrap)
    {
        delegate.initialize(bootstrap);
    }

    @Override
    public void run(final WorkerConfiguration workerConfiguration, final Environment environment) throws Exception
    {
        environment.servlets()
            .addFilter("otel-http-body-tracing-filter", new HttpBodyTracingFilter())
            .addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
        environment.jersey().register(new OtelBodyCaptureResource());
        delegate.run(workerConfiguration, environment);
    }
}
