/*
 * Copyright 2015-2019 Micro Focus or one of its affiliates.
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
package com.hpe.caf.languagedetection.fasttext;

import com.hpe.caf.languagedetection.DetectedLanguage;
import java.util.List;

public final class LDResult {
    private final List<DetectedLanguage> languages;
    private final float overallAccuracy;

    public LDResult(final List<DetectedLanguage> languages, final float overallAccuracy) {
        this.languages = languages;
        this.overallAccuracy = overallAccuracy;
    }

    public List<DetectedLanguage> getLanguages() {
        return languages;
    }

    public float getOverallAccuracy() {
        return overallAccuracy;
    }
}
