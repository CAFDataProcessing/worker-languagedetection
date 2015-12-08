package com.hpe.caf.languagedetection.cld2;

import com.hpe.caf.languagedetection.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Created by smitcona on 30/11/2015.
 */
public class Cld2Tester {

    public static void main(String[] args) throws LanguageDetectorException {

        /** Set up strings for test with different langs **/
        String text = "Hello, this is English. The paragraph must be more than 200 characters for the Cld2Library detection" +
                " to work effectively. Therefore, I am writing some rubbish to fill out that 200 characters and will" +
                " soon find out if the detection works correctly. Signed, Conal. ";

        String textSpanish = "Hola, este es el español . El párrafo debe contener más de 200 caracteres para la detección " +
                "Cld2Library para trabajar con eficacia. Por lo tanto, estoy escribiendo un poco de basura para llenar los 200 " +
                "caracteres necesaria y pronto averiguar si la detección funciona correctamente. Firmado, Conal. ";

        String textDutch = "Hallo, dit is Spaans. De paragraaf moet meer dan 200 tekens voor de Cld2Library detectie om " +
                "effectief te werken . Daarom schrijf ik wat rommel nodig vul het 200 tekens en zal binnenkort te " +
                "achterhalen of de detectie correct werkt . Ondertekend , Conal. ";

        String textSwahili = "Hello, hii ni lugha ya Kihispaniola. Aya lazima wahusika zaidi ya 200 kwa Cld2Library kugundua" +
                " kufanya kazi kwa ufanisi . Kwa hiyo mimi kuandika baadhi takataka kujaza wahusika 200 muhimu na hivi" +
                " karibuni kujua kama kugundua kazi kwa usahihi . Saini , Conal. ";

        String textMaori = "Ko te wahi o te kuputuhi i roto i te reo motuhake, e kore e whakahuatia e ahau rite ai tiwhaiwhai" +
                " ai reira te pūoko ki te e taea ki te tiki ake te māmā tenei. Ka tāruatia te reira rite te faahitiraa i roto" +
                " i tetahi atu kuputuhi. Ko kuputuhi mania reira.";

        String textMix = text+textSpanish+textDutch+textSwahili;
        String textEnMa = "This is a piece of text that includes a quotation of a different language inside it. The " +
                "quotation will follow this line of code. \" "+textMaori +" \". The quotation is used by the eastern polynesian people " +
                "of New Zealand closely related to cook islands. ";


        /** setup bytes from text, and arrays which are passed by reference **/
        InputStream stream = new ByteArrayInputStream(textEnMa.getBytes());

//        LanguageDetectorSettings settings = new LanguageDetectorSettings(true, true, "", "", "");
//        LanguageDetectorSettings settings = new LanguageDetectorSettings(true, true, "null", ""+Cld2Encoding.UNKNOWN_ENCODING, ""+Cld2Language.UNKNOWN_LANGUAGE);
//        LanguageDetectorSettings settings = new LanguageDetectorSettings(true, true, "mi,en", ""+Cld2Encoding.UTF8, ""+Cld2Language.ENGLISH);
//        LanguageDetectorSettings settings = new LanguageDetectorSettings(false, true, "mi,en", ""+Cld2Encoding.UTF8, ""+Cld2Language.ENGLISH);
//        LanguageDetectorSettings settings = new LanguageDetectorSettings(false, true, "mi,en");
        LanguageDetectorSettings settings = new LanguageDetectorSettings(true, true);
//        settings.setDetectMultipleLanguages(true);
//        settings.setHints("","","");

        LanguageDetectorProvider provider = new Cld2DetectorProvider();
        LanguageDetector detector = provider.getLanguageDetector();

        LanguageDetectorResult result = detector.detectLanguage(stream, settings);

//        int x;
//        if(!result.isReliable())
//            x = 0;
//        else
//            x = 1;


//        Tika tika = new Tika();
////        try(InputStream stream = )
//
//        LanguageIdentifier li = new LanguageIdentifier(textMix);
//        li.getLanguage();
//        li.isReasonablyCertain();

//        Cld2Language lang = (Cld2Language)r.language3[0];
    }

}
