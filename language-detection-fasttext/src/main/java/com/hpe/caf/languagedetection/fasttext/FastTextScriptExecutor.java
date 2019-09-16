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

import jep.Jep;
import jep.JepException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jep.NDArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses Jep to call the FastText library in python 3 for language detection.
 */
public final class FastTextScriptExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(FastTextScriptExecutor.class);
    
    /**
     * An instance of the Jep library with imported FastText library and loaded model. 
     * FastText is using Numpy Python Library and <a href="https://github.com/mrj0/jep/issues/28">closing the Jep instance breaks the library</a>.
     * Try to reimport it will fail and as a result of this issue FastText can be imported only once.
     * Jep will only execute calls on the thread it was instantiated on therefore is Jep instatance required here to be thread local, 
     * more at <a href="https://github.com/mrj0/jep/wiki/Performance-Considerations">Performance-Considerations</a>.
     */
    private static final ThreadLocal<Jep> JEP_POOL = new ThreadLocal<Jep>() {
        @Override
        protected Jep initialValue() {
            try {
                final Jep jep = new Jep();
                jep.eval("import fasttext");
                LOGGER.debug("Loading fasttext model.");
                jep.eval("model = fasttext.load_model('/maven/resources/lid.176.bin')");
                return jep;
            } catch (final JepException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void remove() {
            final Jep jep = this.get();
            if (jep != null) {
                try {
                    jep.close();
                } catch (final JepException ex) {
                    throw new RuntimeException(ex);
                }
            }
            super.remove();
        }
    };
    
    /*
     * In python are prediction results represented by a tuple of list and array e.g. (('__label__en',), array([0.92452818])). First 
     * value represents list of identified languages and the second one their probabilities.
     * Prediction allows to return more than one/highest probability by calling e.g. model.predict(text, 3), but this implementation 
     * uses only the most relevant result.
     */
    public static LanguagePrediction detect(final String text) throws JepException {
        final Jep jep = JEP_POOL.get();
        jep.set("text", text);
        jep.eval("prediction = model.predict(text)");
        final Object prediction = jep.getValue("prediction");
        jep.eval("del text, prediction");
         
        final List list = new ArrayList((Collection) prediction);
        final List<String> languages = new ArrayList<>((Collection) list.get(0));
        final double[] probabilities = (double[]) ((NDArray)list.get(1)).getData();
        
        return new LanguagePrediction(languages.get(0), probabilities[0]);
    }
}
