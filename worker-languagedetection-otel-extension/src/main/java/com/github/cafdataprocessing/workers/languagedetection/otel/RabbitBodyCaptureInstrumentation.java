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
package com.github.cafdataprocessing.workers.languagedetection.otel;

import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

public final class RabbitBodyCaptureInstrumentation implements TypeInstrumentation
{
    @Override
    public ElementMatcher<TypeDescription> typeMatcher()
    {
        return named("com.github.workerframework.util.rabbitmq.DefaultRabbitConsumer");
    }

    @Override
    public void transform(final TypeTransformer transformer)
    {
        transformer.applyAdviceToMethod(
            named("getDeliverEvent")
                .and(takesArguments(3))
                .and(takesArgument(1, byte[].class)),
            RabbitBodyCaptureAdvice.class.getName());
    }
}
