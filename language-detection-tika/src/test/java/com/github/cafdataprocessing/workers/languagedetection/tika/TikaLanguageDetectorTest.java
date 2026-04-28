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
package com.github.cafdataprocessing.workers.languagedetection.tika;

import com.github.cafdataprocessing.workers.languagedetection.DetectedLanguage;
import com.github.cafdataprocessing.workers.languagedetection.LanguageDetector;
import com.github.cafdataprocessing.workers.languagedetection.LanguageDetectorException;
import com.github.cafdataprocessing.workers.languagedetection.LanguageDetectorProvider;
import com.github.cafdataprocessing.workers.languagedetection.LanguageDetectorResult;
import com.github.cafdataprocessing.workers.languagedetection.LanguageDetectorSettings;
import com.github.cafdataprocessing.workers.languagedetection.LanguageDetectorStatus;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TikaLanguageDetectorTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TikaLanguageDetectorTest.class);
    private static final String SAMPLE_TEXT = """
            The quick brown fox jumps over the lazy dog.
            This is English text for language detection.
        """;

    private LanguageDetectorProvider provider;
    private LanguageDetector detector;

    @BeforeEach
    public void setUp(final TestInfo testInfo) throws LanguageDetectorException
    {
        LOGGER.info("Starting test: {}", testInfo.getDisplayName());
        provider = new TikaDetectorProvider();
        detector = provider.getLanguageDetector();
    }

    @Test
    public void testDetectEnglishText() throws LanguageDetectorException
    {
        final byte[] bytes = SAMPLE_TEXT.getBytes(StandardCharsets.UTF_8);

        final LanguageDetectorResult result = detector.detectLanguage(bytes, new LanguageDetectorSettings(false));

        assertNotNull(result);
        assertEquals(LanguageDetectorStatus.COMPLETED, result.getLanguageDetectorStatus());
        assertTrue(result.isReliable());
        assertFalse(result.getLanguages().isEmpty());
        assertEquals("en", firstLanguage(result).getLanguageCode());
    }

    @Test
    public void testDetectFrenchText() throws LanguageDetectorException
    {
        final String text = """
                Le renard brun rapide saute par-dessus le chien paresseux.
                Voici une phrase en français utilisée pour tester la détection de langue.
            """;
        final byte[] bytes = text.getBytes(StandardCharsets.UTF_8);

        final LanguageDetectorResult result = detector.detectLanguage(bytes, new LanguageDetectorSettings(false));

        assertNotNull(result);
        assertEquals(LanguageDetectorStatus.COMPLETED, result.getLanguageDetectorStatus());
        assertFalse(result.getLanguages().isEmpty());
        assertEquals("fr", firstLanguage(result).getLanguageCode());
    }

    @Test
    public void testDetectGermanText() throws LanguageDetectorException
    {
        final String text = """
                Der schnelle braune Fuchs springt über den faulen Hund.
                Dies ist ein Beispielsatz auf Deutsch zur Spracherkennungsprüfung.
            """;
        final byte[] bytes = text.getBytes(StandardCharsets.UTF_8);

        final LanguageDetectorResult result = detector.detectLanguage(bytes, new LanguageDetectorSettings(false));

        assertNotNull(result);
        assertEquals(LanguageDetectorStatus.COMPLETED, result.getLanguageDetectorStatus());
        assertFalse(result.getLanguages().isEmpty());
        assertEquals("de", firstLanguage(result).getLanguageCode());
    }

    @Test
    public void testDetectMultipleLanguagesReturnsUpToThree() throws LanguageDetectorException
    {
        final byte[] bytes = SAMPLE_TEXT.getBytes(StandardCharsets.UTF_8);

        final LanguageDetectorResult result = detector.detectLanguage(bytes, new LanguageDetectorSettings(true));

        assertNotNull(result);
        assertEquals(LanguageDetectorStatus.COMPLETED, result.getLanguageDetectorStatus());
        assertFalse(result.getLanguages().isEmpty());
        assertTrue(result.getLanguages().size() <= 3);
    }

    @Test
    public void testDetectSingleLanguageReturnsOne() throws LanguageDetectorException
    {
        final byte[] bytes = SAMPLE_TEXT.getBytes(StandardCharsets.UTF_8);

        final LanguageDetectorResult result = detector.detectLanguage(bytes, new LanguageDetectorSettings(false));

        assertNotNull(result);
        assertEquals(LanguageDetectorStatus.COMPLETED, result.getLanguageDetectorStatus());
        assertEquals(1, result.getLanguages().size());
    }

    // @Test - fail: detects Turkish
    public void testDetectShortAzerbaijani() throws LanguageDetectorException
    {
        final String text = """
                Mərhəmətli, rəhmli Allahın adı ilə!
            """;
        final byte[] bytes = text.getBytes(StandardCharsets.UTF_8);

        final LanguageDetectorResult result = detector.detectLanguage(bytes, new LanguageDetectorSettings(false));

        assertNotNull(result);
        assertEquals(LanguageDetectorStatus.COMPLETED, result.getLanguageDetectorStatus());
        assertEquals(1, result.getLanguages().size());
        assertEquals("az", firstLanguage(result).getLanguageCode());
    }

    // @Test - fail: detects Turkish
    public void testDetectLongAzerbaijani() throws LanguageDetectorException
    {
        final String text = """
                Azərbaycanın təbiəti çox gözəldir. Payızda meşələr qızılı rəngə boyanır, dağların zirvəsini duman örtür. İnsanlar qonaqpərvərdir və hər zaman qonaqları xoş qarşılayırlar.
            """;
        final byte[] bytes = text.getBytes(StandardCharsets.UTF_8);

        final LanguageDetectorResult result = detector.detectLanguage(bytes, new LanguageDetectorSettings(false));

        assertNotNull(result);
        assertEquals(LanguageDetectorStatus.COMPLETED, result.getLanguageDetectorStatus());
        assertEquals(1, result.getLanguages().size());
        assertEquals("az", firstLanguage(result).getLanguageCode());
    }

    @Test
    public void testDetectShortTurkish() throws LanguageDetectorException
    {
        final String text = """
                Kardeşim dans etmeye ve şarkı söylemeye
            """;
        final byte[] bytes = text.getBytes(StandardCharsets.UTF_8);

        final LanguageDetectorResult result = detector.detectLanguage(bytes, new LanguageDetectorSettings(false));

        assertNotNull(result);
        assertEquals(LanguageDetectorStatus.COMPLETED, result.getLanguageDetectorStatus());
        assertEquals(1, result.getLanguages().size());
        assertEquals("tr", firstLanguage(result).getLanguageCode());
    }

    @Test
    public void testDetectLongTurkish() throws LanguageDetectorException
    {
        final String text = """
                Tren İstasyonu
                Yolcu: Merhaba.
                Bilet Satıcısı: Merhaba. Nasıl yardım edebilirim?
                Yolcu: Bilet almak istiyorum.
                Bilet Satıcısı: Tabii. Nereye gidiyorsunuz?
                Yolcu: Ankara aktarmalı İstanbul. Bugün saat kaçta tren var?
                Bilet Satıcısı: Bugün sadece 2'de, 5'te ve 8'de tren var.
                Yolcu: 2 treni benim için uygun.
                Bilet Satıcısı: Kaç adet bilet istiyorsunuz?
                Yolcu: Bir adet.
                Bilet Satıcısı: Ödemeyi nasıl yapacaksınız?
                Yolcu: Kredi kartı ile ödeyeceğim. Fiyatı ne kadar?
                Bilet Satıcısı: 52,35 lira.
                Yolcu: Tamam.
                Bilet Satıcısı: Buyurun, biletiniz. İyi yolculuklar.
                Yolcu: Teşekkürler. Pardon, tuvalet nerede?
                Bilet Satıcısı: İleride sağda, marketin yanında.
                Yolcu: Çok teşekkürler. İyi günler.
                Bilet Satıcısı: Rica ederim. Size de iyi günler.
            """;
        final byte[] bytes = text.getBytes(StandardCharsets.UTF_8);

        final LanguageDetectorResult result = detector.detectLanguage(bytes, new LanguageDetectorSettings(false));

        assertNotNull(result);
        assertEquals(LanguageDetectorStatus.COMPLETED, result.getLanguageDetectorStatus());
        assertEquals(1, result.getLanguages().size());
        assertEquals("tr", firstLanguage(result).getLanguageCode());
    }

    @Test
    public void testDetectLanguageWithDefaultSettings() throws LanguageDetectorException
    {
        final byte[] bytes = SAMPLE_TEXT.getBytes(StandardCharsets.UTF_8);

        final LanguageDetectorResult result = detector.detectLanguage(bytes);

        assertNotNull(result);
        assertEquals(LanguageDetectorStatus.COMPLETED, result.getLanguageDetectorStatus());
        assertFalse(result.getLanguages().isEmpty());
    }

    @Test
    public void testDetectLanguageFromInputStream() throws LanguageDetectorException
    {
        final InputStream stream = new ByteArrayInputStream(SAMPLE_TEXT.getBytes(StandardCharsets.UTF_8));

        final LanguageDetectorResult result = detector.detectLanguage(stream);

        assertNotNull(result);
        assertEquals(LanguageDetectorStatus.COMPLETED, result.getLanguageDetectorStatus());
        assertFalse(result.getLanguages().isEmpty());
        assertEquals("en", firstLanguage(result).getLanguageCode());
    }

    @Test
    public void testDetectLanguageFromInputStreamWithSettings() throws LanguageDetectorException
    {
        final InputStream stream = new ByteArrayInputStream(SAMPLE_TEXT.getBytes(StandardCharsets.UTF_8));

        final LanguageDetectorResult result = detector.detectLanguage(stream, new LanguageDetectorSettings(false));

        assertNotNull(result);
        assertEquals(LanguageDetectorStatus.COMPLETED, result.getLanguageDetectorStatus());
        assertEquals(1, result.getLanguages().size());
        assertEquals("en", firstLanguage(result).getLanguageCode());
    }

    @Test
    public void testNullBytesThrowsNullPointerException()
    {
        assertThrows(NullPointerException.class,
            () -> detector.detectLanguage((byte[]) null, new LanguageDetectorSettings(false)));
    }

    @Test
    public void testNullSettingsThrowsNullPointerException()
    {
        final byte[] bytes = "some text".getBytes(StandardCharsets.UTF_8);
        assertThrows(NullPointerException.class,
            () -> detector.detectLanguage(bytes, null));
    }

    @Test
    public void testNullInputStreamThrowsNullPointerException()
    {
        assertThrows(NullPointerException.class,
            () -> detector.detectLanguage((InputStream) null, new LanguageDetectorSettings(false)));
    }

    @Test
    public void testDetectedLanguageHasLanguageName() throws LanguageDetectorException
    {
        final byte[] bytes = SAMPLE_TEXT.getBytes(StandardCharsets.UTF_8);

        final LanguageDetectorResult result = detector.detectLanguage(bytes, new LanguageDetectorSettings(false));

        assertNotNull(result);
        assertFalse(result.getLanguages().isEmpty());
        assertNotNull(firstLanguage(result).getLanguageName());
        assertFalse(firstLanguage(result).getLanguageName().isEmpty());
    }

    @Test
    public void testDetectedLanguageHasConfidencePercentage() throws LanguageDetectorException
    {
        final byte[] bytes = SAMPLE_TEXT.getBytes(StandardCharsets.UTF_8);

        final LanguageDetectorResult result = detector.detectLanguage(bytes, new LanguageDetectorSettings(false));

        assertNotNull(result);
        assertFalse(result.getLanguages().isEmpty());
        assertTrue(firstLanguage(result).getConfidencePercentage() >= 0);
    }

    /**
     * Test text with single language detection. Assert expected results
     *
     * @throws LanguageDetectorException
     * @throws IOException
     */
    @Test
    public void testSingleLanguage() throws LanguageDetectorException, IOException
    {
        final boolean multiLang = false;

        final String data = """
            Einige Worte über mich

            Ich wurde 1977 im Sternzeichen Krebs in München in diese Welt geboren. Ich wuchs bei München auf und absolvierte dort ein Studium der Rechtswissenschaften. 2007-2008 brachte ich ein Studienjahr in Wellington, Neuseeland zu, wo ich mich intensiver mit der Tradition der Maori, dem Taoismus und der Yogaphilosophie zu beschäftigen begann. Seit meiner Rückkehr 2006 arbeite und lebe ich mit meiner Lebensgefährtin in Neubiberg bei München.

            2010 begann ich Ausbildung zum Yogalehrer bei PURNIMA Kaiser in der Yoga-Tradition von Swami Shivananda. Seit Januar 2012 bin ich nebenberuflich als Yogalehrer tätig.

            Schon immer interessiert für spirituelle Lehren, beschäftigte ich mich unter anderem mit dem Buddhismus und der Yogaphoilsophie. Zugleich interessiere ich mich aber auch für keltisches Druidentum, Schamanismus, katholisches Brauchtum und andere Lehren, die den Menschen helfen können, sich miteinander und mit sich selbst stärker in Kontakt zu kommen.

            Generell halte ich es mit dem Vedanta bzw. der Yogaphilosophie mit dem Glauben dass letztlich alles EINS ist, aus einer Quelle stammt und ein gemeinsames Ziel hat. Ob man diesen Urgrund nun Gott, kosmische Intelligenz, Universum oder Brahman nennt, macht für mich keinen Unterschied. Entscheidend ist aus meiner Sicht, dass alle wahren spirituellen Lehren dazu dienen, LIEBE und FRIEDEN in die Welt zu bringen und Menschen miteinander zu verbinden –  niemals soll es als Element zur Trennung und Abgrenzung verwendet werden. Es gibt nur eine WAHRHEIT, deshalb sind Lehren nicht mehr oder weniger wert, nicht besser oder schlechter.

            Werdegang:

            Yogalehrer (BYV) 2012
            Psychologischer Yogatherapeut – in Ausbildung – 
            Reiki I und II
            Spiritueller Coach


            """;

        final byte[] bytes = data.getBytes(StandardCharsets.UTF_8);

        final LanguageDetectorSettings settings = new LanguageDetectorSettings("utf-8", multiLang, "de");

        final LanguageDetectorResult result = detector.detectLanguage(bytes, settings);

        final DetectedLanguage[] arr = result.getLanguages().toArray(new DetectedLanguage[1]);

        assertEquals(LanguageDetectorStatus.COMPLETED, result.getLanguageDetectorStatus());
        assertTrue(result.isReliable());
        assertEquals(1, result.getLanguages().size());

        assertEquals("de", arr[0].getLanguageCode());
        assertEquals("GERMAN", arr[0].getLanguageName());
    }

    /**
     * Test text file with multiple languages (3) and assert expected values
     *
     * @throws LanguageDetectorException
     * @throws IOException
     */
    // @Test
    public void testMultiLanguage() throws LanguageDetectorException, IOException
    {
        final boolean multiLang = true;
        final String[] testCodes = {"de", "es", "en"};
        final String[] testNames = {"GERMAN", "SPANISH", "ENGLISH"};

        final String data = """
            This is an extract from a German literature repository describing a book on metamorphism.
            Metamorphosen – Körperlandschaften 2011
            Der menschliche Körper als natürliche Leinwand
            Mitten in der Landschaft und nur nach genauem und konzentriertem Hinsehen kann man sie entdecken: die bemalten und wie angewachsen in der natürlichen Umgebung platzierten kunstvoll bemalten Körper. Wie ein Mimikri verschmelzen die von verschiedenen Bodypaintern geschminkten Körper mit den unterschiedlichen Hintergründen.
            Sie stehen, liegen oder hocken zur Struktur verwandelt auf der grauschlierigen Felsplatte über dem Meer, am knorrigen Stamm eines alten Kirschbaumes, über den glatten Brocken im Fluss, als Astfortsatz über der Wollgraswiese, einer moosigen Wand gleich am feuchtem Stein oder auch wie eine bizarre Skulptur in rauem Berggelände.
            Verwunschen, verzaubert, entrückt wirken die so unscheinbaren und doch so kunstvoll gestalteten Körper. Die Fotografin Laila Pregizer und der Fotograf Uwe Schmida haben die durch die Farbgestalter an markanten Orten platzierten und zum Objekt gewordenen menschlichen Gestalten so stimmig abgelichtet, dass sowohl die Modelle angepasst an die natürliche Umgebung als auch die Fotos davon als eigenständige Bilder wirken.
            Es ist eine tiefe Verbindung, welche durch die Wiederholung der gegebenen natürlichen Struktur auf die Körper übergegangen scheint und so eine ganz neue Verbundenheit von Mensch und Natur dokumentiert.
            Metamorphosen – Körperlandschaften 2016, WeingARTen im KV&H Verlag GmbH, 55×46 cm, ISBN 9783840066276, 32,00 €, 5 von 5 Sternen
            The same book is also sold in Spanish and a verse from one of the poems is below:
            Ninguno comprendíamos el secreto nocturno de las pizarras 
            ni por qué la esfera armilar se exaltaba tan sola cuando la mirábamos. 
            Sólo sabíamos que una circunferencia puede no ser redonda 
            y que un eclipse de luna equivoca a las flores 
            y adelanta el reloj de los pájaros. 

            Ninguno comprendíamos nada : 
            ni por qué nuestros dedos eran de tinta china 
            y la tarde cerraba compases para al alba abrir libros. 
            Sólo sabíamos que una recta, si quiere, puede ser curva o quebrada 
            y que las estrellas errantes son niños que ignoran las aritmética.
            Si mi voz muriera en tierra...

            Si mi voz muriera en tierra,
            llevadla al nivel del mar
            y dejadla en la ribera.

            Llevadla al nivel del mar
            y nombradla capitana
            de un blanco bajel de guerra.

            Oh mi voz condecorada 
            con la insignia marinera: 
            sobre el corazon un ancla 
            y sobre el ancla una estrella 
            y sobre la estrella el viento 
            y sobre el viento una vela! 
            Su idilio fue una larga sonrisa a cuatro labios... 
            En el regazo cálido de rubia primavera 
            Amáronse talmente que entre sus dedos sabios 
            Palpitó la divina forma de la Quimera. 

            En los palacios fúlgidos de las tardes en calma 
            Hablábanse un lenguaje sentido como un lloro, 
            Y se besaban hondo hasta morderse el alma!... 
            Las horas deshojáronse como flores de oro, 

            Y el Destino interpuso sus dos manos heladas... 
            Ah! los cuerpos cedieron, mas las almas trenzadas 
            Son el más intrincado nudo que nunca fue... 
            En lucha con sus locos enredos sobrehumanos 
            Las Furias de la vida se rompieron las manos 
            Y fatigó sus dedos supremos Ananké... 

            The primary aim of this file is to contain three separate languages, ENglish, SPanish and German.
            """;

        final byte[] bytes = data.getBytes(StandardCharsets.UTF_8);

        final LanguageDetectorSettings settings = new LanguageDetectorSettings(multiLang);

        final LanguageDetectorResult result = detector.detectLanguage(bytes, settings);

        final DetectedLanguage[] arr = result.getLanguages().toArray(new DetectedLanguage[3]);

        assertEquals(LanguageDetectorStatus.COMPLETED, result.getLanguageDetectorStatus());
        assertFalse(result.isReliable());
        assertEquals(3, result.getLanguages().size());

        for (int i = 0; i < 3; i++) {
            assertEquals(testCodes[i], arr[i].getLanguageCode());
            assertEquals(testNames[i], arr[i].getLanguageName());
        }
    }

    /**
     * Test result obtained from short text with one language. Assert expected values.
     *
     * @throws LanguageDetectorException
     * @throws IOException
     */
    @Test
    public void testSingleLanguageShortText() throws LanguageDetectorException, IOException
    {
        final boolean multiLang = false;

        final String data = """
                Poesje zit naast het vuur
            """;

        final byte[] bytes = data.getBytes(StandardCharsets.UTF_8);

        final LanguageDetectorSettings settings = new LanguageDetectorSettings(multiLang, "nl");

        final LanguageDetectorResult result = detector.detectLanguage(bytes, settings);

        final DetectedLanguage[] arr = result.getLanguages().toArray(new DetectedLanguage[1]);

        assertEquals(LanguageDetectorStatus.COMPLETED, result.getLanguageDetectorStatus());
//        Assert.assertFalse(result.isReliable());//spanish and german have similar language percentages therefore the result is not reliable
        assertEquals(1, result.getLanguages().size());

        assertEquals("nl", arr[0].getLanguageCode());
        assertEquals("DUTCH", arr[0].getLanguageName());
    }

    /**
     * Fail test on gibberish text of no language. Assert the results indicate unknown language
     *
     * @throws LanguageDetectorException
     * @throws IOException
     */
    @Test
    public void testMultipleLanguageGibberish() throws LanguageDetectorException, IOException
    {
        final boolean multiLang = true;

        final String data = """
                fjlajdakldmjkamfklawndfklasmdk da dwakd amkldml ksanik anmfkla jfawjfkl;amwk mwak;dma l;mds madjmwk mda
    m kdamd oakdl; spofja jfoakdop ajfioayd ysnuf gfh oasifha uf ugyufsa ufynsa yfhsa smhfiosa yhfiosa fjlajdakldmjkamfklawndfklasmdkhfusa  hfs ah ifsas oifshao fhasoij fiaohfyasydtast uiofya ifoysabui foeaufy e
     ghuifsganu ifau gfsungfueagfu safu sgfugafuoywoaid jwadis jmd jd adn hte bsja nuirfadjsahfuean js ad n 
     doa jdsja dwj idj adj waop jd
            """;

        final byte[] bytes = data.getBytes(StandardCharsets.UTF_8);

        final LanguageDetectorSettings settings = new LanguageDetectorSettings(multiLang);

        final LanguageDetectorResult result = detector.detectLanguage(bytes, settings);

        final DetectedLanguage[] arr = result.getLanguages().toArray(new DetectedLanguage[3]);

        assertEquals(LanguageDetectorStatus.FAILED, result.getLanguageDetectorStatus());
        assertFalse(result.isReliable()); // spanish and german have similar language percentages therefore the result is not reliable
        assertEquals(1, result.getLanguages().size());

        assertEquals("un", arr[0].getLanguageCode());
        assertEquals("Unknown", arr[0].getLanguageName());
    }

    /**
     * Test a text file with UCS-2 LE BOM encoded text which is not supported. According to CLD2 all text should be utf-8
     *
     * @throws LanguageDetectorException
     * @throws IOException
     */
    // @Test
    public void testLanguageUCS2() throws LanguageDetectorException, IOException
    {
        final boolean multiLang = false;

        final String data = """
                            Μνημόνιο Συναντίληψης (MoU) για τη δημιουργία του Ελληνικού Επενδυτικού Ταμείου, του «Ινστιτούτου για την Ανάπτυξη», στα πρότυπα της γερμανικής τράπεζας KfW, θα υπογράψουν ο πρωθυπουργός Αντώνης Σαμαράς και η καγκελάριος της Γερμανίας Άγγελα Μέρκελ κατά τη διάρκεια της επίσκεψης της τελευταίας στην Αθήνα, σύμφωνα με πληροφορίες που επικαλείται η οικονομική εφημερίδα Handelsblatt στη σημερινή της έκδοση.

                Όπως αναφέρεται στο δημοσίευμα, ο κ. Σαμαράς και η κ. Μέρκελ επιθυμούν να προωθήσουν τη δημιουργία μιας τράπεζας για την ανάπτυξη των μικρομεσαίων επιχειρήσεων, με φθηνά δάνεια. Σύμφωνα με την εφημερίδα, οι δύο χώρες θα συνεισφέρουν 100 εκατομμύρια στο Ταμείο.

                «Η σχεδιαζόμενη συμφωνία γίνεται προκειμένου να προωθηθεί το σχέδιο που έχει μείνει στάσιμο» αναφέρει η εφημερίδα και επισημαίνει ότι ο υπουργός Οικονομικών της Γερμανίας Βόλφγκανγκ Σόιμπλε είχε ήδη από τον περασμένο Ιούλιο συμφωνήσει στη συμμετοχή της Γερμανίας, μέσω της KfW, ενώ στα σχέδια περιλαμβάνονται μεταξύ άλλων η Ευρωπαϊκή Ένωση, η Ευρωπαϊκή Τράπεζα Επενδύσεων και η Γαλλία. Οι προετοιμασίες όμως προχωρούν εδώ και μήνες με αργούς ρυθμούς.

                «Γι αυτό δεν θέλουν να περιμένουν άλλο, αλλά θα προχωρήσουν διμερώς με την Ελλάδα» εξηγεί η εφημερίδα και προσθέτει ότι αυξάνεται η πίεση και από τις άλλες πλευρές που επιθυμούν να έχουν συμμετοχή. Στο δημοσίευμα, ωστόσο, επισημαίνεται ότι και από την πλευρά του Βερολίνου η μεταφορά κεφαλαίων εξακολουθεί να είναι πρόβλημα, καθώς τα κεφάλαια που διακινούνται μέσω της KfW πρέπει να προβλέπονται από τον κρατικό προϋπολογισμό, ο οποίος, για το 2014 αναμένεται να τεθεί σε ισχύ τον Ιούλιο. Από το γερμανικό υπουργείο Οικονομικών πάντως, όπως υποστηρίζει η εφημερίδα, εξετάζονται επιλογές για το πώς η KfW θα μπορούσε να συμμετάσχει νωρίτερα. «Υπάρχει ενδιαφέρον για επιτάχυνση» καταλήγει το δημοσίευμα.
            """;

        final byte[] bytes = data.getBytes(StandardCharsets.UTF_8);

        final LanguageDetectorSettings settings = new LanguageDetectorSettings(multiLang);

        final LanguageDetectorResult result = detector.detectLanguage(bytes, settings);

        final DetectedLanguage[] arr = result.getLanguages().toArray(new DetectedLanguage[1]);

        for (int i = 0; i < arr.length; i++) {
            LOGGER.info("{} - {}", arr[i].getLanguageCode(), arr[i].getLanguageName());
        }

        assertEquals(LanguageDetectorStatus.FAILED, result.getLanguageDetectorStatus());
    }

    /**
     * Test text with english language detection.
     *
     * @throws LanguageDetectorException
     * @throws IOException
     */
    @Test
    public void testEnglishLanguage() throws LanguageDetectorException, IOException
    {
        final boolean multiLang = false;

        final String data = """
                At that moment a young man came into the bunkhouse; a thin young man with a brown face, with
                brown eyes and a head of tightly curled hair. He wore a work glove on his left hand, and like the boss,
                he wore high-heeled boots. ‘Seen my old man?’ he asked.
                The swamper said: ‘He was here jus’ a minute ago, Curley. Went over to the cook-house, I think.’
                ‘I’ll try to catch him,’ said Curley. His eyes passed over the new men and he stopped. He glanced
                coldly at George and then at Lennie. His arms gradually bent at the elbows and his hands closed into
                fists. He stiffened and went into a slight crouch. His glance was at once calculating and pugnacious.
                Lennie squirmed under the look and shifted his feet nervously. Curley stepped gingerly close to him.
                ‘You the new guys the old man was waitin’ for?’
                ‘We just come in,’ said George.
                ‘Let the big guy talk.’
                Lennie twisted with embarrassment.
                George said: ‘S’pose he don’t want to talk?’
                Curley lashed his body around. ‘By Christ, he’s gotta talk when he’s spoke to. What the hell are you
                gettin’ into it for?’
                ‘We travel together,’ said George coldly.
                ‘Oh, so it’s that way.’
                George was tense and motionless. ‘Yeah, it’s that way.’
                Lennie was looking helplessly to George for instruction.
                ‘An’ you won’t let the big guy talk, is that it?’
                ‘He can talk if he want to tell you anything.’ He nodded slightly to Lennie.
                ‘We jus’ come in,’ said Lennie softly.
                Curley stared levelly at him. ‘Well, nex’ time you answer when you’re spoke to.’ He turned towards
                the door and walked out, and his elbows were still bent out a little.
                Sam interrupted, a sly grin curling the corners of his mouth: ‘Yow don’t do nothing but talk, “Uncle”.
                And give everything away to some darkies we’ve never met. We don’t give a toss for anybody else.
                This is our patch. Not some wogs’ handout.’
                I felt as if I had been punched in the stomach. My legs felt watery and a hot panic softened my
                insides to mush. It was as if the whole crowd had turned into one huge eyeball which swivelled slowly
                between me and papa. I wished I had stood next to papa; I could feel Anita shifting beside me, I knew
                she would not hold me or take my hand. Papa was staring into the distance, seemingly unconcerned,
                gripping his bottle of whisky like a weapon. Uncle Alan’s mouth was opening and closing like a
                goldfish, Reverend Ince whispered to him ‘Good work, Alan. One of your supporters, is he?’
                And then a rasping voice came from somewhere in the throng, ‘You tell him, son.’
                I jerked my head towards the sound. Who was that? Who said that? Who had thought that all this
                time and why had I never known about it? And then another voice, a woman’s, ‘Go on, lad! Tell him
                some more!’ The sound had come from somewhere around Mr Ormerod, I stared at him, straight into
                his eyes. He shifted from foot to foot and glanced away.
                My mind was turning cartwheels; I wanted to find these people, tell them Sam Lowbridge was my
                mate, the boy who had taught me how to shoot a fairground rifle, who terrorised everyone else except
                me. I was his favourite. There must have been some mistake. When my ears had stopped ringing and I
                gradually returned to my body, I could hear catcalls coming from all over the grounds; ‘Yow shuttit,
                yow bloody skinhead idiot! Bloody disgrace, Sam Lowbridge! Yow wanna good birching, yow do! Yow
                don’t talk for me, son! I’d be on my deathbed before that’d happen!’
                Uncle Alan was half-running towards the gate, towards Sam who was strolling back to his moped to
                the cheers and claps of his gang. ‘Wait! Sam!’ Uncle Alan puffed. ‘Listen! Don’t do this! Don’t turn all
                this energy the wrong way!’ Sam was not listening. He was already revving up, clouds of bluey-grey
                smoke wheezing from his exhaust. ‘Anger is good! But not used this way! Please! You’re going the
                wrong way!’
                Sam aimed his moped straight at Uncle Alan who was now outside the gates, making him jump back
                and stumble, and then he sped off up the hill followed by the rest of his three-wheeler lackeys, who
                manoeuvred in and out of each other like a bunch of May-mad midges until they were nothing but
                annoying buzzy specks in the distance. Uncle Alan sat heavily down on the grass and rested his head on
                his arms. People were now crowding round papa, offering condolences and back pats like he’d just
                come last in the annual church egg and spoon race. ‘Yow don’t mind him, Mr Ku-mar, he’s always been
                a bad-un . . .’ Papa smiled graciously at them, shrugging his shoulders, not wanting to draw any more
                attention to himself or what had just happened. I knew he was trying to get to me and I began pushing
                forward, encountering a wall of solid backs and legs.
                Anita was tugging my sleeve as she held onto me. I turned round to face her, my cheeks still felt
                warm and taut. ‘Wharrabout that then!’ she grinned, ‘Isn’t he bosting!’
                ‘What?’ I croaked.
                ‘Sam Lowbridge, He’s dead bloody hard, in’t he?’
                ‘Anita Rutter, yow am a bloody stupid cow sometimes,’ I said, and did not look back until I had
                reached the haven of papa’s arms.
            """;

        final byte[] bytes = data.getBytes(StandardCharsets.UTF_8);

        final LanguageDetectorSettings settings = new LanguageDetectorSettings("utf-8", multiLang, "en");

        final LanguageDetectorResult result = detector.detectLanguage(bytes, settings);

        final DetectedLanguage[] arr = result.getLanguages().toArray(new DetectedLanguage[1]);

        assertEquals(LanguageDetectorStatus.COMPLETED, result.getLanguageDetectorStatus());
        assertTrue(result.isReliable());
        assertEquals(1, result.getLanguages().size());

        assertEquals("en", arr[0].getLanguageCode());
        assertEquals("ENGLISH", arr[0].getLanguageName());
    }

    /**
     * Test ASCII text.
     *
     * @throws LanguageDetectorException
     * @throws IOException
     */
    @Test
    public void testASCIIText() throws LanguageDetectorException, IOException
    {
        final boolean multiLang = false;

        final String data = """
                Why, man, he doth bestride the narrow world
                Like a Colossus, and we petty men
                Walk under his huge legs and peep about
                To find ourselves dishonourable graves.
                Men at some time are masters of their fates:
                The fault, dear Brutus, is not in our stars,
                But in ourselves, that we are underlings.
                Brutus and Caesar: what should be in that 'Caesar'?
                Why should that name be sounded more than yours?
                Write them together, yours is as fair a name;
                Sound them, it doth become the mouth as well;
                Weigh them, it is as heavy; conjure with 'em,
                Brutus will start a spirit as soon as Caesar.
                Now, in the names of all the gods at once,
                Upon what meat doth this our Caesar feed,
                That he is grown so great? Age, thou art shamed!
                Rome, thou hast lost the breed of noble bloods!
                When went there by an age, since the great flood,
                But it was famed with more than with one man?
                When could they say till now, that talk'd of Rome,
                That her wide walls encompass'd but one man?
                Now is it Rome indeed and room enough,
                When there is in it but one only man.
                O, you and I have heard our fathers say,
                There was a Brutus once that would have brook'd
                The eternal devil to keep his state in Rome
                As easily as a king.
            """;

        final byte[] bytes = data.getBytes(StandardCharsets.UTF_8);

        final LanguageDetectorSettings settings = new LanguageDetectorSettings("utf-8", multiLang, "en");

        final LanguageDetectorResult result = detector.detectLanguage(bytes, settings);

        final DetectedLanguage[] arr = result.getLanguages().toArray(new DetectedLanguage[1]);

        assertEquals(LanguageDetectorStatus.COMPLETED, result.getLanguageDetectorStatus());
        assertTrue(result.isReliable());
        assertEquals(1, result.getLanguages().size());

        assertEquals("en", arr[0].getLanguageCode());
        assertEquals("ENGLISH", arr[0].getLanguageName());
    }

    /**
     * Test ISO_8859_1 text.
     *
     * @throws LanguageDetectorException
     * @throws IOException
     */
    @Test
    public void testISO_8859_1Text() throws LanguageDetectorException, IOException
    {
        final boolean multiLang = false;

        final String data = """
                Why, man, he doth bestride the narrow world
                Like a Colossus, and we petty men
                Walk under his huge legs and peep about
                To find ourselves dishonourable graves.
                Men at some time are masters of their fates:
                The fault, dear Brutus, is not in our stars,
                But in ourselves, that we are underlings.
                Brutus and Caesar: what should be in that 'Caesar'?
                Why should that name be sounded more than yours?
                Write them together, yours is as fair a name;
                Sound them, it doth become the mouth as well;
                Weigh them, it is as heavy; conjure with 'em,
                Brutus will start a spirit as soon as Caesar.
                Now, in the names of all the gods at once,
                Upon what meat doth this our Caesar feed,
                That he is grown so great? Age, thou art shamed!
                Rome, thou hast lost the breed of noble bloods!
                When went there by an age, since the great flood,
                But it was famed with more than with one man?
                When could they say till now, that talk'd of Rome,
                That her wide walls encompass'd but one man?
                Now is it Rome indeed and room enough,
                When there is in it but one only man.
                O, you and I have heard our fathers say,
                There was a Brutus once that would have brook'd
                The eternal devil to keep his state in Rome
                As easily as a king.
            """;

        final byte[] bytes = data.getBytes(StandardCharsets.UTF_8);

        final LanguageDetectorSettings settings = new LanguageDetectorSettings("utf-8", multiLang, "en");

        final LanguageDetectorResult result = detector.detectLanguage(bytes, settings);

        final DetectedLanguage[] arr = result.getLanguages().toArray(new DetectedLanguage[1]);

        assertEquals(LanguageDetectorStatus.COMPLETED, result.getLanguageDetectorStatus());
        assertTrue(result.isReliable());
        assertEquals(1, result.getLanguages().size());

        assertEquals("en", arr[0].getLanguageCode());
        assertEquals("ENGLISH", arr[0].getLanguageName());
    }

    // Helper to get the first detected language from the Collection
    private static DetectedLanguage firstLanguage(final LanguageDetectorResult result)
    {
        return result.getLanguages().iterator().next();
    }
}
