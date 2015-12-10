package com.hpe.caf.languagedetection.cld2.testing;

import com.google.common.io.ByteStreams;
import com.hpe.caf.languagedetection.*;
import com.hpe.caf.languagedetection.cld2.Cld2Detector;
import com.hpe.caf.languagedetection.cld2.Cld2DetectorProvider;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Created by smitcona on 08/12/2015.
 */
public class LanguageDetectionCld2IT {

    private Path inputPath;
    private String filename;
    private String filepath;
    private LanguageDetectorProvider provider;
    private LanguageDetector detector;
    private LanguageDetectorResult result;
    private LanguageDetectorSettings settings;
    private boolean multiLang;
    private FileInputStream fileInputStream;

    public LanguageDetectionCld2IT(){

    }

    @Before
    public void setup(){
        provider = new Cld2DetectorProvider();
        detector = new Cld2Detector();
    }

    @Test
    public void testSingleLanguageHTML() throws LanguageDetectorException, IOException {
        multiLang = false;

//        filepath = "C\\Workspace\\language-detection-cld2\\test\\resources\\";
        filename = "holahtml.txt";

//        byte[] bytes = getData(filename);

//        settings = new LanguageDetectorSettings(multiLang);

//        result = detector.detectLanguage(bytes, settings);

//        Assert.assertEquals(LanguageDetectorStatus.COMPLETED, result.getLanguageDetectorStatus());
//        Assert.assertTrue(result.isReliable());
//        Assert.assertEquals(1, result.getLanguages().size());
//        Assert.assertEquals("SPANISH", result.getLanguages().get(0));

    }

    @Test
    public void testMultiLanguage() throws LanguageDetectorException, IOException {
        multiLang = false;

//        filepath = "C\\Workspace\\language-detection-cld2\\test\\resources\\";
        filename = "holahtml.txt";

//        byte[] bytes = getData(filename);

//        settings = new LanguageDetectorSettings(multiLang);

//        result = detector.detectLanguage(bytes, settings);

//        Assert.assertEquals(LanguageDetectorStatus.COMPLETED, result.getLanguageDetectorStatus());
//        Assert.assertTrue(result.isReliable());
//        Assert.assertEquals(1, result.getLanguages().size());
//        Assert.assertEquals("SPANISH", result.getLanguages().get(0));

    }

    private byte[] getData(final String name)
            throws IOException
    {
        try ( InputStream stream = this.getClass().getResourceAsStream(name) ) {
            return ByteStreams.toByteArray(stream);
        } catch(IOException e){
        }
        return null;
    }

}
