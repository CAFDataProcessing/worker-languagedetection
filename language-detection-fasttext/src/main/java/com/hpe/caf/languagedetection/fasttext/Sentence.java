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

public final class Sentence
{
    private final String text;
    private final int sentenceEndIndex;
    private final int wordsInSentence;

    public Sentence(final String text, final int sentenceEndIndex, final int wordsInSentence){
        this.text = text;
        this.sentenceEndIndex = sentenceEndIndex;
        this.wordsInSentence = wordsInSentence;
    }

    public String getText() {
        return text;
    }

    public int getSentenceEndIndex() {
        return sentenceEndIndex;
    }

    public int getWordsInSentence() {
        return wordsInSentence;
    }
}
