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
package com.hpe.caf.languagedetection.cld2;

import com.hpe.caf.languagedetection.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by smitcona on 30/11/2015.
 */
public class Cld2Tester
{
    public static void main(String[] args) throws LanguageDetectorException, IOException
    {
        /**
         * Whether to return multiple languages or just the top language
         */
        boolean multiLang = true;

        /**
         * Pass in the filename from command line and it gets read in e.g. "C:\\Users\\smitcona\\Desktop\\emailGerman.txt"
         */
//        byte[] bytes = getAllData(args[0]);
        byte[] bytes = Files.readAllBytes(Paths.get(args[0]));

        /**
         * Settings file takes either:
         * <li>(multilang) -e.g. (true)</li>
         * <li>(multilang, "hints") -e.g. (false, "ENGLISH")</li>
         * <li>("encoding", multilang, "hints") -e.g. ("utf8", true, "en", "fr")</li>
         */
        LanguageDetectorSettings settings = new LanguageDetectorSettings(multiLang);
//        LanguageDetectorSettings settings = new LanguageDetectorSettings(multiLang,"en", "it");

        /**
         * Provider to provide a detector implementation
         */
        LanguageDetectorProvider provider = new Cld2DetectorProvider();

        /**
         * Detector implementation
         */
        LanguageDetector detector = provider.getLanguageDetector();

        /**
         * this is the final result from the language detection, and you pass in the bytes from the text file and settings
         */
        LanguageDetectorResult result = detector.detectLanguage(bytes, settings);

        DetectedLanguage[] d = result.getLanguages().toArray(new DetectedLanguage[result.getLanguages().size()]);

        /**
         * output the results
         */
        for (int i = 0; i < d.length; i++) {
            System.out.println("" + i + ": " + d[i].getLanguageCode());
            System.out.println("" + i + ": " + d[i].getLanguageName());
            System.out.println("" + i + ": " + d[i].getConfidencePercentage());
        }

        /**
         * Get languages Collection into an array
         */
//        if(result.getLanguageDetectorStatus()==LanguageDetectorStatus.COMPLETED) {
//        } else {
//            System.out.println("Language detection failed. Make sure supplied text file encoding is UTF-8. \n");
//        }
    }
}
