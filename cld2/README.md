### Build Compact Language Detector 2

Build in the `libcld2.so` from [CLD2Owners/cld2: Compact Language Detector 2](https://github.com/CLD2Owners/cld2) source code.
Download [commit]((https://github.com/CLD2Owners/cld2/commit/b56fa78a2fe44ac2851bae5bf4f4693a0644da7b)) as zip file, unpack its contents and compile the code with gcc.


### Finding the correct mangled function name to use in `Cld2Library.java`
```
OTX-44M2N13:/mnt/d/Projects/github/worker-languagedetection-agg # docker container run -it --rm --network=host -e HTTP_PROXY -e HTTPS_PROXY -e NO_PROXY -v ~/.m2:/root/.m2 -v /var/run/docker.sock:/var/run/docker.sock -v $(pwd):/wd -w /wd dev/cafapi/buildenv-cpp:8.3.0-SNAPSHOT

OTX-44M2N13:/wd # nm -D cld2/target/libcld2.so | grep -i "ExtDetectLanguageSummary"
0000000000025050 T _ZN4CLD224ExtDetectLanguageSummaryEPKcibPKNS_8CLDHintsEiPNS_8LanguageEPiPdPSt6vectorINS_11ResultChunkESaISA_EES7_Pb
0000000000024e90 T _ZN4CLD224ExtDetectLanguageSummaryEPKcibPNS_8LanguageEPiS4_Pb
0000000000024f60 T _ZN4CLD224ExtDetectLanguageSummaryEPKcibS1_iNS_8LanguageEPS2_PiPdS4_Pb
0000000000024f00 T _ZN4CLD224ExtDetectLanguageSummaryEPKcibS1_iNS_8LanguageEPS2_PiS4_Pb
0000000000024fb0 T _ZN4CLD233ExtDetectLanguageSummaryCheckUTF8EPKcibPKNS_8CLDHintsEiPNS_8LanguageEPiPdPSt6vectorINS_11ResultChunkESaISA_EES7_PbS7_

nm -DC cld2/target/libcld2.so | grep "CLD2::ExtDetectLanguageSummary"
0000000000025050 T CLD2::ExtDetectLanguageSummary(char const*, int, bool, CLD2::CLDHints const*, int, CLD2::Language*, int*, double*, std::vector<CLD2::ResultChunk, std::allocator<CLD2::ResultChunk> >*, int*, bool*)
0000000000024e90 T CLD2::ExtDetectLanguageSummary(char const*, int, bool, CLD2::Language*, int*, int*, bool*)
0000000000024f60 T CLD2::ExtDetectLanguageSummary(char const*, int, bool, char const*, int, CLD2::Language, CLD2::Language*, int*, double*, int*, bool*)
0000000000024f00 T CLD2::ExtDetectLanguageSummary(char const*, int, bool, char const*, int, CLD2::Language, CLD2::Language*, int*, int*, bool*)
0000000000024fb0 T CLD2::ExtDetectLanguageSummaryCheckUTF8(char const*, int, bool, CLD2::CLDHints const*, int, CLD2::Language*, int*, double*, std::vector<CLD2::ResultChunk, std::allocator<CLD2::ResultChunk> >*, int*, bool*, int*)

nm -D cld2/target/libcld2.so | grep "0000000000024fb0"
0000000000024fb0 T _ZN4CLD233ExtDetectLanguageSummaryCheckUTF8EPKcibPKNS_8CLDHintsEiPNS_8LanguageEPiPdPSt6vectorINS_11ResultChunkESaISA_EES7_PbS7_
