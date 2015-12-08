package com.hpe.caf.languagedetection.cld2.testing;

import com.google.common.io.ByteStreams;
import com.hpe.caf.languagedetection.*;
import com.hpe.caf.languagedetection.cld2.Cld2Detector;
import com.hpe.caf.languagedetection.cld2.Cld2DetectorProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
    private boolean plainText;
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
        plainText = false;

        filename = "holahtml.txt";
        filepath = "C\\Workspace\\language-detection-cld2\\test\\resources\\";

//        fileInputStream = getStream(filepath+filename);
        byte[] bytes = getData(filename);
//        fileInputStream = new FileInputStream(getData(filename));

        settings = new LanguageDetectorSettings(multiLang, plainText);

        result = detector.detectLanguage(bytes, settings);

        Assert.assertEquals(LanguageDetectorStatus.COMPLETED, result.getLanguageDetectorStatus());
        Assert.assertTrue(result.isReliable());
        Assert.assertEquals(1, result.getLanguages().size());
        Assert.assertEquals("SPANISH", result.getLanguages().get(0));

    }

    private FileInputStream getStream(String p) throws FileNotFoundException {
        if(Files.notExists(Paths.get(p)))
            Assert.fail("Input file was not found. " + p);
        FileInputStream fis = new FileInputStream(p);
        return fis;
    }

    private byte[] getData(final String name)
            throws IOException
    {
        try ( InputStream stream = this.getClass().getResourceAsStream(name) ) {
            return ByteStreams.toByteArray(stream);
        } catch(IOException e){
            Assert.fail("Input file not found. ");
        }
        return null;
    }

}
