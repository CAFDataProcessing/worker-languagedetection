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
package com.github.cafdataprocessing.workers.languagedetection.cld2;

import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public final class CLDHints extends Structure {
    public String content_language_hint;
    public String tld_hint;
    public int encoding_hint;
    public int language_hint;

    public CLDHints() {
        super();
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("content_language_hint", "tld_hint", "encoding_hint", "language_hint");
    }
}
