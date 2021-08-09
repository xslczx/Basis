#include <jni.h>

extern "C"
{
void Java_github_hotstu_lame4droid_SimpleLame_close(JNIEnv *env, jclass type);

jint Java_github_hotstu_lame4droid_SimpleLame_encode(JNIEnv *env, jclass type, jshortArray buffer_l_,
                                             jshortArray buffer_r_, jint samples, jbyteArray mp3buf_);

jint Java_github_hotstu_lame4droid_SimpleLame_flush(JNIEnv *env, jclass type, jbyteArray mp3buf_);

void Java_github_hotstu_lame4droid_SimpleLame_init(JNIEnv *env, jclass type, jint inSampleRate,
                                                   jint outChannel, jint outSampleRate, jint outBitrate, jint quality);
}