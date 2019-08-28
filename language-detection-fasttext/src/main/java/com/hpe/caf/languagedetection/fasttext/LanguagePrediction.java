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

public final class LanguagePrediction {
    private String languageCode;
    private Double prediction;

    public LanguagePrediction(final String languageCode, final Double prediction) {
        this.languageCode = languageCode;
        this.prediction = prediction;
    }

    public String getLanguageCode() {
        return languageCode;
    }
    
    public void setLanguageCode(final String languageCode) {
        this.languageCode = languageCode;
    }

    public Double getPrediction() {
        return prediction;
    }
    
    public void setPrediction(final Double prediction) {
        this.prediction = prediction;
    } 
}
