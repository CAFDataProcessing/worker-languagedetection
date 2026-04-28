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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Based on https://github.com/paypal/Gibberish-Detector-Java. Implementation of the Markov Chain algorithm.
 */
public class GibberishDetector
{
    private final Map<Character, Integer> alphabetPositionMap = new HashMap<Character, Integer>();
    private static final int MIN_COUNT_VAL = 10;

    private final String alphabet;
    private double[][] logProbabilityMatrix = null;
    private double threshold = 0d;

    public GibberishDetector(
        final List<String> trainingLinesList,
        final List<String> goodLinesList,
        final List<String> badLinesList,
        final String alphabet)
    {
        this.alphabet = alphabet;
        train(trainingLinesList, goodLinesList, badLinesList);
    }

    private void train(
        final List<String> trainingLinesList,
        final List<String> goodLinesList,
        final List<String> badLinesList)
    {
        initializePositionMap();

        final  int[][] alphabetCouplesMatrix = getAlphaBetCouplesMatrix(trainingLinesList);
        logProbabilityMatrix = getLogProbabilityMatrix(alphabetCouplesMatrix);

        final List<Double> goodProbability = getAvgTransitionProbability(goodLinesList, logProbabilityMatrix);
        final List<Double> badProbability = getAvgTransitionProbability(badLinesList, logProbabilityMatrix);

        final double minGood = Collections.min(goodProbability);
        final double maxBad = Collections.max(badProbability);

        if (minGood <= maxBad) {
            throw new AssertionError("cannot create a threshold");
        }
        threshold = getThreshold(minGood, maxBad);
    }

    // can be overridden for another threshold heuristic implementation
    protected double getThreshold(final double minGood, final double maxBad)
    {
        return (minGood + maxBad) / 2;
    }

    private void initializePositionMap() {
        final char[] alphabetChars = alphabet.toCharArray();
        for (int i = 0; i < alphabetChars.length; i++) {
            alphabetPositionMap.put(alphabetChars[i], i);
        }
    }

    private String normalize(final String line)
    {
        final StringBuilder normalizedLine = new StringBuilder();
        for (char c : line.toLowerCase().toCharArray()) {
            normalizedLine.append(alphabet.contains(Character.toString(c)) ? c : "");
        }
        return normalizedLine.toString();
    }

    private List<String> getNGram(final int n, final String line)
    {
        final String filteredLine = normalize(line);
        final List<String> nGram = new ArrayList<String>();
        for (int start = 0; start < filteredLine.length() - n + 1; start++) {
            nGram.add(filteredLine.substring(start, start + n));
        }
        return nGram;
    }

    private int[][] getAlphaBetCouplesMatrix(final List<String> trainingLinesList)
    {
        final int[][] counts = createArray(alphabet.length());
        for (final String line : trainingLinesList) {
            final List<String> nGram = getNGram(2, line);
            for (final String touple : nGram) {
                counts[alphabetPositionMap.get(touple.charAt(0))][alphabetPositionMap.get(touple.charAt(1))]++;
            }
        }
        return counts;
    }

    private double[][] getLogProbabilityMatrix(final int[][] alphabetCouplesMatrix)
    {
        final int alphabetLength = alphabet.length();
        final double[][] logProbabilityMatrix = new double[alphabetLength][alphabetLength];
        for (int i = 0; i < alphabetCouplesMatrix.length; i++) {
            final double sum = getSum(alphabetCouplesMatrix[i]);
            for (int j = 0; j < alphabetCouplesMatrix[i].length; j++) {
                logProbabilityMatrix[i][j] = Math.log(alphabetCouplesMatrix[i][j] / sum);
            }
        }
        return logProbabilityMatrix;
    }

    private List<Double> getAvgTransitionProbability(final List<String> lines, final double[][] logProbabilityMatrix)
    {
        final List<Double> result = new ArrayList<Double>();
        for (final String line : lines) {
            result.add(getAvgTransitionProbability(line, logProbabilityMatrix));
        }
        return result;
    }

    private double getAvgTransitionProbability(final String line, final double[][] logProbabilityMatrix)
    {
        double logProb = 0d;
        int transitionCount = 0;
        final List<String> nGram = getNGram(2, line);
        for (final String touple : nGram) {
            logProb += logProbabilityMatrix[alphabetPositionMap.get(touple.charAt(0))][alphabetPositionMap.get(touple.charAt(1))];
            transitionCount++;
        }
        return Math.exp(logProb / Math.max(transitionCount, 1));
    }

    private int[][] createArray(final int length)
    {
        final int[][] counts = new int[length][length];
        for (int i = 0; i < counts.length; i++) {
            Arrays.fill(counts[i], MIN_COUNT_VAL);
        }
        return counts;
    }

    private double getSum(final int[] array)
    {
        double sum = 0;
        for (int i = 0; i < array.length; i++) {
            sum += array[i];
        }
        return sum;
    }

    /**
     * Determines if a sentence is gibberish or not.
     * 
     * @param line a sentence to be classified as gibberish or not.
     * @return true if the sentence is gibberish, false otherwise.
     */
    public boolean isGibberish(final String line)
    {
        return !(getAvgTransitionProbability(line, logProbabilityMatrix) > threshold);
    }
}
