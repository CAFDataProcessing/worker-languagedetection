/*
 * Copyright 2015-2017 EntIT Software LLC, a Micro Focus company.
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

import java.util.*;

/**
 * Possible values that describe how the output result should be formatted.
 */
public enum LanguageDetectionResultFormat {
    SIMPLE,
    COMPLEX;

    /**
     * Map of Enum values to support repeated case insensitive lookup of enum values by string representations.
     */
    private static final HashMap<String, LanguageDetectionResultFormat> enumNamesToValues = new HashMap<>();
    static
    {
        for(LanguageDetectionResultFormat enumValue: values())
        {
            enumNamesToValues.put(enumValue.name().toLowerCase(Locale.ENGLISH), enumValue);
        }
    }

    /**
     * Retrieve enum value the provided string value represents.
     * @param valueAsStr name of enum value
     * @return the enum value matching provided string value or null if there is no match
     */
    public static LanguageDetectionResultFormat tryGetValueOf(String valueAsStr)
    {
        return enumNamesToValues.get(valueAsStr.toLowerCase(Locale.ENGLISH));
    }
}
