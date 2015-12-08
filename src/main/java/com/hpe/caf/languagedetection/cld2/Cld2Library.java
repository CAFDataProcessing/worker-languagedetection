package com.hpe.caf.languagedetection.cld2;

import com.sun.jna.Library;

/**
 * Created by smitcona on 03/12/2015.
 */
public interface Cld2Library extends Library {

    int DetectLanguage(
            byte[] buffer,
            int buffer_length,
            boolean is_plain_text,
            boolean[] is_reliable
    );


    int DetectLanguageCheckUTF8(
            byte[] buffer,
            int buffer_length,
            boolean is_plain_text,
            boolean[] is_reliable,
            int[] valid_prefix_bytes
    );


    int DetectLanguageSummary(
            byte[] buffer,
            int buffer_length,
            boolean is_plain_text,
            int[] language3,
            int[] percent3,
            int[] text_bytes,
            boolean[] is_reliable
    );


    int DetectLanguageSummaryWithHints(
            byte[] buffer,
            int buffer_length,
            boolean is_plain_text,
            String tld_hint,
            int encoding_hint,
            int language_hint,
            int[] language3,
            int[] percent3,
            int[] text_bytes,
            boolean[] is_reliable
    );

    /** To get language name from kLanguageToName array in CLD2 **/
    String _ZN4CLD212LanguageNameENS_8LanguageE(int language);

    /** To get language code from kLanguageToCode array in CLD2 **/
    String _ZN4CLD212LanguageCodeENS_8LanguageE(int language);

    /** To get language number from kNameToLanguage array in CLD2 **/
    int _ZN4CLD219GetLanguageFromNameEPKc(String name);
}
