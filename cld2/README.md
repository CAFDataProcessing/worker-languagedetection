### Build Compact Language Detector 2

Build in the `libcld2.so` from [CLD2Owners/cld2: Compact Language Detector 2](https://github.com/CLD2Owners/cld2) source code.
Check out [commit]((https://github.com/CLD2Owners/cld2/commit/b56fa78a2fe44ac2851bae5bf4f4693a0644da7b)) and compile the code with gcc.

### Finding the correct function name mapping
```
OTX-44M2N13:/mnt/d/Projects/github/worker-languagedetection-agg # docker container run -it --rm --network=host -e HTTP_PROXY -e HTTPS_PROXY -e NO_PROXY -v ~/.m2:/root/.m2 -v /var/run/docker.sock:/var/run/docker.sock -v $(pwd):/wd -w /wd dev/cafapi/buildenv-cpp:8.3.0-SNAPSHOT

OTX-44M2N13:/wd # nm -D cld2/target/libcld2.so | grep -i "Detect"
0000000000024cd0 T _ZN4CLD214DetectLanguageEPKcibPb
0000000000024db0 T _ZN4CLD221DetectLanguageSummaryEPKcibPNS_8LanguageEPiS4_Pb
0000000000024e20 T _ZN4CLD221DetectLanguageSummaryEPKcibS1_iNS_8LanguageEPS2_PiS4_Pb
00000000000285d0 T _ZN4CLD221DetectLanguageVersionEv
0000000000024d50 T _ZN4CLD223DetectLanguageCheckUTF8EPKcibPbPi
00000000000288e0 T _ZN4CLD223DetectLanguageSummaryV2EPKcibPKNS_8CLDHintsEbiNS_8LanguageEPS5_PiPdPSt6vectorINS_11ResultChunkESaISA_EES7_Pb
0000000000025050 T _ZN4CLD224ExtDetectLanguageSummaryEPKcibPKNS_8CLDHintsEiPNS_8LanguageEPiPdPSt6vectorINS_11ResultChunkESaISA_EES7_Pb
0000000000024e90 T _ZN4CLD224ExtDetectLanguageSummaryEPKcibPNS_8LanguageEPiS4_Pb
0000000000024f60 T _ZN4CLD224ExtDetectLanguageSummaryEPKcibS1_iNS_8LanguageEPS2_PiPdS4_Pb
0000000000024f00 T _ZN4CLD224ExtDetectLanguageSummaryEPKcibS1_iNS_8LanguageEPS2_PiS4_Pb
0000000000024fb0 T _ZN4CLD233ExtDetectLanguageSummaryCheckUTF8EPKcibPKNS_8CLDHintsEiPNS_8LanguageEPiPdPSt6vectorINS_11ResultChunkESaISA_EES7_PbS7_

OTX-44M2N13:/wd # nm -DC cld2/target/libcld2.so | grep "CLD2::ExtDetectLanguageSummary"
0000000000025050 T CLD2::ExtDetectLanguageSummary(char const*, int, bool, CLD2::CLDHints const*, int, CLD2::Language*, int*, double*, std::vector<CLD2::ResultChunk, std::allocator<CLD2::ResultChunk> >*, int*, bool*)
0000000000024e90 T CLD2::ExtDetectLanguageSummary(char const*, int, bool, CLD2::Language*, int*, int*, bool*)
0000000000024f60 T CLD2::ExtDetectLanguageSummary(char const*, int, bool, char const*, int, CLD2::Language, CLD2::Language*, int*, double*, int*, bool*)
0000000000024f00 T CLD2::ExtDetectLanguageSummary(char const*, int, bool, char const*, int, CLD2::Language, CLD2::Language*, int*, int*, bool*)
0000000000024fb0 T CLD2::ExtDetectLanguageSummaryCheckUTF8(char const*, int, bool, CLD2::CLDHints const*, int, CLD2::Language*, int*, double*, std::vector<CLD2::ResultChunk, std::allocator<CLD2::ResultChunk> >*, int*, bool*, int*)

OTX-44M2N13:/wd # nm -DC cld2/target/libcld2.so | grep "0000000000024f00"
0000000000024f00 T CLD2::ExtDetectLanguageSummary(char const*, int, bool, char const*, int, CLD2::Language, CLD2::Language*, int*, int*, bool*)

OTX-44M2N13:/wd # nm -D cld2/target/libcld2.so | grep "0000000000024f00"
0000000000024f00 T _ZN4CLD224ExtDetectLanguageSummaryEPKcibS1_iNS_8LanguageEPS2_PiS4_Pb
```
