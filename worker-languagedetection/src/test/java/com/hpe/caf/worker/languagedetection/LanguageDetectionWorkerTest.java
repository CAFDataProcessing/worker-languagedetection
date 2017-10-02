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

import com.hpe.caf.api.worker.DataStore;
import com.hpe.caf.api.worker.DataStoreException;
import com.hpe.caf.worker.document.exceptions.DocumentWorkerTransientException;
import com.hpe.caf.worker.document.model.*;
import org.junit.*;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LanguageDetectionWorkerTest {

    private Application mockApplication;
    private DataStore mockDataStore;

    @Before
    public void setup(){
        mockApplication = Mockito.mock(Application.class);
        mockDataStore = Mockito.mock(DataStore.class);
        Mockito.when(mockApplication.getService(DataStore.class)).thenReturn(mockDataStore);
    }

    @Test
    public void processDocumentTest() throws InterruptedException, DocumentWorkerTransientException, DataStoreException {
        LanguageDetectionWorker ldw = new LanguageDetectionWorker(mockApplication);

        //  Create a test document object.
        Document document = createTestDocument("LanguageDetectionWorker Unit Test!");

        //  Test language detection. This test is configured to return 3 languages.
        //  See constructor for the LanguageDetectorTest class.
        ldw.processDocument(document);

        //  Verify mock methods are being called the expected number of times.
        Mockito.verify(mockApplication, Mockito.times(1)).getService(Mockito.any());
        Mockito.verify(mockDataStore, Mockito.times(0)).retrieve(Mockito.anyString());
        Mockito.verify(document, Mockito.times(12)).getField(Mockito.anyString());
        Mockito.verify(document, Mockito.times(0)).addFailure(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void processDocumentTest_GetDataFail() throws Throwable {
        LanguageDetectionWorker ldw = new LanguageDetectionWorker(mockApplication);

        //  Create a remote data store test document object. This is configured to throw a DataStoreException.
        Document document = createTestDocumentForDataFailure("LanguageDetectionWorker Unit Test!");

        //  Test data failure.
        ldw.processDocument(document);

        //  Verify mock methods are being called the expected number of times.
        Mockito.verify(mockApplication, Mockito.times(1)).getService(Mockito.any());
        Mockito.verify(mockDataStore, Mockito.times(1)).retrieve(Mockito.anyString());
        Mockito.verify(document, Mockito.times(1)).getField(Mockito.anyString());
        // LanguageDetectionConstants.ErrorCodes.FAILED_TO_ACQUIRE_SOURCE_DATA should be added as failure id.
        Mockito.verify(document, Mockito.times(1)).addFailure(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void processDocumentTest_LangDetectFail() throws InterruptedException, DocumentWorkerTransientException, DataStoreException {
        LanguageDetectionWorker ldw = new LanguageDetectionWorker(mockApplication);

        //  Create a test document object.
        //  Use specific document text to simulate a LanguageDetectorException being thrown.
        Document document = createTestDocument("Throw LanguageDetectorException!");

        //  Test data failure.
        ldw.processDocument(document);

        //  Verify mock methods are being called the expected number of times.
        Mockito.verify(mockApplication, Mockito.times(1)).getService(Mockito.any());
        Mockito.verify(mockDataStore, Mockito.times(0)).retrieve(Mockito.anyString());
        Mockito.verify(document, Mockito.times(1)).getField(Mockito.anyString());
        // LanguageDetectionConstants.ErrorCodes.FAILED_TO_DETECT_LANGUAGES should be added as failure id.
        Mockito.verify(document, Mockito.times(1)).addFailure(Mockito.anyString(), Mockito.anyString());
    }

    private Document createTestDocument(String documentText) {
        //  Mock FieldValue.
        FieldValue contentFieldValue = mock(FieldValue.class);

        //  Set isReference to false.
        when(contentFieldValue.isReference()).thenReturn(false);

        //  Assign input stream text.
        byte[] b = documentText.getBytes(StandardCharsets.UTF_8);
        when(contentFieldValue.getValue()).thenReturn(b);

        //  Mock Field.
        Field contentField = mock(Field.class);

        FieldValues fieldValueListMock = mock(FieldValues.class);
        Iterator<FieldValue> fieldValueIterator = mock(Iterator.class);
        when(fieldValueListMock.iterator()).thenReturn(fieldValueIterator);
        when(fieldValueIterator.hasNext()).thenReturn(true, false);
        when(fieldValueIterator.next()).thenReturn(contentFieldValue);

        when(contentField.getValues()).thenReturn(fieldValueListMock);

        //  Mock document object.
        Document document = mock(Document.class);
        when(document.getField(any(String.class))).thenReturn(contentField);

        return document;
    }

    private Document createTestDocumentForDataFailure(String documentText) throws DataStoreException {
        //  Mock FieldValue.
        FieldValue contentFieldValue = mock(FieldValue.class);

        //  Set isReference to true.
        when(contentFieldValue.isReference()).thenReturn(true);

        //  Assign input stream text.
        when(contentFieldValue.getReference()).thenReturn(documentText);

        //  Throw DataStoreException when attempting to retrieve from remote data store.
        when(mockDataStore.retrieve(documentText)).thenThrow(new DataStoreException("Test DataStoreException!"));

        //  Mock Field.
        Field contentField = mock(Field.class);

        FieldValues fieldValueListMock = mock(FieldValues.class);
        Iterator<FieldValue> fieldValueIterator = mock(Iterator.class);
        when(fieldValueListMock.iterator()).thenReturn(fieldValueIterator);
        when(fieldValueIterator.hasNext()).thenReturn(true, false);
        when(fieldValueIterator.next()).thenReturn(contentFieldValue);

        when(contentField.getValues()).thenReturn(fieldValueListMock);

        //  Mock document object.
        Document document = mock(Document.class);
        when(document.getField(any(String.class))).thenReturn(contentField);

        return document;
    }
}
