/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class vproxy_xdp_NativeXDP */

#ifndef _Included_vproxy_xdp_NativeXDP
#define _Included_vproxy_xdp_NativeXDP
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     vproxy_xdp_NativeXDP
 * Method:    loadAndAttachBPFProgramToNic0
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IZ)J
 */
JNIEXPORT jlong JNICALL Java_vproxy_xdp_NativeXDP_loadAndAttachBPFProgramToNic0
  (JNIEnv *, jclass, jstring, jstring, jstring, jint, jboolean);

/*
 * Class:     vproxy_xdp_NativeXDP
 * Method:    findMapByNameInBPF0
 * Signature: (JLjava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_vproxy_xdp_NativeXDP_findMapByNameInBPF0
  (JNIEnv *, jclass, jlong, jstring);

/*
 * Class:     vproxy_xdp_NativeXDP
 * Method:    createUMem0
 * Signature: (IIIII)J
 */
JNIEXPORT jlong JNICALL Java_vproxy_xdp_NativeXDP_createUMem0
  (JNIEnv *, jclass, jint, jint, jint, jint, jint);

/*
 * Class:     vproxy_xdp_NativeXDP
 * Method:    getBufferFromUMem0
 * Signature: (J)Ljava/nio/ByteBuffer;
 */
JNIEXPORT jobject JNICALL Java_vproxy_xdp_NativeXDP_getBufferFromUMem0
  (JNIEnv *, jclass, jlong);

/*
 * Class:     vproxy_xdp_NativeXDP
 * Method:    createXSK0
 * Signature: (Ljava/lang/String;IJIIIZ)J
 */
JNIEXPORT jlong JNICALL Java_vproxy_xdp_NativeXDP_createXSK0
  (JNIEnv *, jclass, jstring, jint, jlong, jint, jint, jint, jboolean);

/*
 * Class:     vproxy_xdp_NativeXDP
 * Method:    addXSKIntoMap0
 * Signature: (JIJ)V
 */
JNIEXPORT void JNICALL Java_vproxy_xdp_NativeXDP_addXSKIntoMap0
  (JNIEnv *, jclass, jlong, jint, jlong);

/*
 * Class:     vproxy_xdp_NativeXDP
 * Method:    getFDFromXSK0
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_vproxy_xdp_NativeXDP_getFDFromXSK0
  (JNIEnv *, jclass, jlong);

/*
 * Class:     vproxy_xdp_NativeXDP
 * Method:    fillUpFillRing0
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_vproxy_xdp_NativeXDP_fillUpFillRing0
  (JNIEnv *, jclass, jlong);

/*
 * Class:     vproxy_xdp_NativeXDP
 * Method:    fetchPackets0
 * Signature: (JLvproxy/xdp/ChunkPrototypeObjectList;)V
 */
JNIEXPORT void JNICALL Java_vproxy_xdp_NativeXDP_fetchPackets0
  (JNIEnv *, jclass, jlong, jobject);

/*
 * Class:     vproxy_xdp_NativeXDP
 * Method:    rxRelease0
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_vproxy_xdp_NativeXDP_rxRelease0
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     vproxy_xdp_NativeXDP
 * Method:    writePacket0
 * Signature: (JJ)Z
 */
JNIEXPORT jboolean JNICALL Java_vproxy_xdp_NativeXDP_writePacket0
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     vproxy_xdp_NativeXDP
 * Method:    completeTx0
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_vproxy_xdp_NativeXDP_completeTx0
  (JNIEnv *, jclass, jlong);

/*
 * Class:     vproxy_xdp_NativeXDP
 * Method:    fetchChunk0
 * Signature: (JLvproxy/xdp/Chunk;)Z
 */
JNIEXPORT jboolean JNICALL Java_vproxy_xdp_NativeXDP_fetchChunk0
  (JNIEnv *, jclass, jlong, jobject);

/*
 * Class:     vproxy_xdp_NativeXDP
 * Method:    setChunk0
 * Signature: (JII)V
 */
JNIEXPORT void JNICALL Java_vproxy_xdp_NativeXDP_setChunk0
  (JNIEnv *, jclass, jlong, jint, jint);

/*
 * Class:     vproxy_xdp_NativeXDP
 * Method:    releaseChunk0
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_vproxy_xdp_NativeXDP_releaseChunk0
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     vproxy_xdp_NativeXDP
 * Method:    addChunkRefCnt0
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_vproxy_xdp_NativeXDP_addChunkRefCnt0
  (JNIEnv *, jclass, jlong);

/*
 * Class:     vproxy_xdp_NativeXDP
 * Method:    releaseXSK0
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_vproxy_xdp_NativeXDP_releaseXSK0
  (JNIEnv *, jclass, jlong);

/*
 * Class:     vproxy_xdp_NativeXDP
 * Method:    releaseUMem0
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_vproxy_xdp_NativeXDP_releaseUMem0
  (JNIEnv *, jclass, jlong, jboolean);

/*
 * Class:     vproxy_xdp_NativeXDP
 * Method:    releaseBPFObject0
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_vproxy_xdp_NativeXDP_releaseBPFObject0
  (JNIEnv *, jclass, jlong);

#ifdef __cplusplus
}
#endif
#endif
