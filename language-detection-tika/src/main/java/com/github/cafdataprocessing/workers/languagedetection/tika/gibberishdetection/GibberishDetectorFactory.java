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
package com.github.cafdataprocessing.workers.languagedetection.tika.gibberishdetection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Gibberish detector factory for creating an instance of GibberishDetector or another detector that extends it.
 */
public final class GibberishDetectorFactory
{
    /**
     * Creates a gibberish detector trained by the given lines and alphabet.
     * 
     * @param trainingList list of lines for training
     * @param goodList     list of good valid lines
     * @param badList      list of bad gibberish lines
     * @param alphabet     String that contains all the alphabet of the language plus the white space character. for example:
     *                     "abcdefghijklmnopqrstuvwxyz "
     * @return gibberish detector
     */
    public static GibberishDetector createGibberishDetector (
        final List<String> trainingList,
        final List<String> goodList,
        final List<String> badList,
        final String alphabet)
    {
        try {
            return new GibberishDetector(trainingList, goodList, badList, alphabet);
        } catch (final Exception e) {
            throw new IllegalArgumentException(
                "Exception in GibberishDetectorFactory: " + (e.getCause() != null ? e.getCause().getMessage() : ""));
        }
    }

    /**
     * Creates a gibberish detector trained by the given Files and alphabet.
     * 
     * @param trainingFile file object that contains lines for training
     * @param goodFile     file object that contains good valid lines
     * @param badFile      file object that contains bad gibberish lines
     * @param alphabet     String that contains all the alphabet of the language plus the white space character. for example:
     *                     "abcdefghijklmnopqrstuvwxyz "
     * @return gibberish detector
     */
    public static GibberishDetector createGibberishDetector (
        final File trainingFile,
        final File goodFile,
        final File badFile,
        final String alphabet)
    {
        try {
            return createGibberishDetector(
                getLinesFromFile(trainingFile),
                getLinesFromFile(goodFile),
                getLinesFromFile(badFile),
                alphabet);
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        } catch (final RuntimeException e) {
            throw new RuntimeException("Exception in GibberishDetectorFactory: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a gibberish detector trained by the given file paths and alphabet.
     * 
     * @param trainingFilePath path of a file that contains lines for training
     * @param goodFilePath     path of a file that contains good valid lines
     * @param badFilePath      path of a file that contains bad gibberish lines
     * @param alphabet         String that contains all the alphabet of the language plus the white space character. for example:
     *                         "abcdefghijklmnopqrstuvwxyz "
     * @return gibberish detector
     */
    public GibberishDetector createGibberishDetector (
        final String trainingFilePath,
        final String goodFilePath,
        final String badFilePath,
        final String alphabet)
    {
        try {
            return createGibberishDetector(
                new File(trainingFilePath),
                new File(goodFilePath),
                new File(badFilePath),
                alphabet);
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (final RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * creates a gibberish detector trained by the given files names and alphabet. Assumes UTF-8 encoding.
     * 
     * @param trainingFileName name of a local file that contains lines for training
     * @param goodFileName     name of a local file that contains good valid lines
     * @param badFileName      name of a local file that contains bad gibberish lines
     * @param alphabet         String that contains all the alphabet of the language plus the white space character. for example:
     *                         "abcdefghijklmnopqrstuvwxyz "
     * @return gibberish detector
     */
    public static GibberishDetector createGibberishDetectorFromLocalFile(
        final String trainingFileName,
        final String goodFileName,
        final String badFileName,
        final String alphabet)
    {
        try {
            return createGibberishDetector(
                getLinesFromLocalFile(trainingFileName),
                getLinesFromLocalFile(goodFileName),
                getLinesFromLocalFile(badFileName),
                alphabet);
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (final RuntimeException e) {
            throw new RuntimeException("Exception in GibberishDetectorFactory: " + e.getMessage());
        }
    }

    private static List<String> getLinesFromFile(final File file)
    {
        final List<String> lines = new ArrayList<String>();

        try (final BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line.trim());
            }
        } catch (final IOException | NullPointerException e) {
            throw new RuntimeException("Cannot initiate file: " + file.getAbsolutePath(), e);
        }
        return lines;
    }

    private static List<String> getLinesFromLocalFile(final String fileName)
    {
        final List<String> lines = new ArrayList<String>();

        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(
            GibberishDetectorFactory.class.getResourceAsStream(fileName), Charset.forName("UTF-8")))) {
            while (reader.ready()) {
                lines.add(reader.readLine().trim());
            }
        } catch (final IOException | NullPointerException e) {
            throw new RuntimeException("Cannot initiate file: " + fileName, e);
        }
        return lines;
    }
}
