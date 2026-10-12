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

import io.opentelemetry.api.trace.Span;
import java.nio.charset.StandardCharsets;
import net.bytebuddy.asm.Advice;

public final class RabbitBodyCaptureAdvice
{
    private static final int MAX_BODY_BYTES = 4096;

    private RabbitBodyCaptureAdvice()
    {
    }

    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void onEnter(@Advice.Argument(1) final byte[] body)
    {
        if (body == null || body.length == 0) {
            return;
        }

        final Span span = Span.current();
        if (!span.isRecording()) {
            return;
        }

        final int length = Math.min(body.length, MAX_BODY_BYTES);
        span.setAttribute("messaging.rabbitmq.message.body", new String(body, 0, length, StandardCharsets.UTF_8));
    }
}
