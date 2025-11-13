#include "jni.h"
#include "nback.h"
#include "nback.c"


JNIEXPORT jintArray JNICALL
Java_mobappdev_example_nback_1cimpl_NBackHelper_createNBackString(JNIEnv *env, jobject this,
                                                                  jint size, jint combinations,
                                                                  jint matchPercentage, jint nBack) {
    Nback s1;
    s1 = create(size, combinations, matchPercentage, nBack);
    // 2. JNI-bryggan anropar den "rena" C-funktionen (från nback.c) för att få datan.

    // Create a new jintArray to store the content array
    jintArray contentArray = (*env)->NewIntArray(env, s1->size); // 2. Skapar en *Java*-array (jintArray) som Kotlin kan förstå.

    // Check if allocation was successful
    if (contentArray == NULL) {
        // Handle allocation failure if needed
        return NULL;
    }

    // Set the content of the jintArray to the content array from the struct
    (*env)->SetIntArrayRegion(env, contentArray, 0, s1->size, s1->content);// 2. Kopierar datan från C-arrayen (s1->content) till den nya Java-arrayen.

    return contentArray;
}