/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class io_vproxy_xdp_NativeXDP */

#ifndef _Included_io_vproxy_xdp_NativeXDP
#define _Included_io_vproxy_xdp_NativeXDP
#ifdef __cplusplus
extern "C" {
#endif
#undef io_vproxy_xdp_NativeXDP_VP_CSUM_NO
#define io_vproxy_xdp_NativeXDP_VP_CSUM_NO 0L
#undef io_vproxy_xdp_NativeXDP_VP_CSUM_IP
#define io_vproxy_xdp_NativeXDP_VP_CSUM_IP 1L
#undef io_vproxy_xdp_NativeXDP_VP_CSUM_UP
#define io_vproxy_xdp_NativeXDP_VP_CSUM_UP 2L
#undef io_vproxy_xdp_NativeXDP_VP_CSUM_ALL
#define io_vproxy_xdp_NativeXDP_VP_CSUM_ALL 3L
/*
 * Class:     io_vproxy_xdp_NativeXDP
 * Method:    loadAndAttachBPFProgramToNic0
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IZ)J
 */
JNIEXPORT jlong JNICALL Java_io_vproxy_xdp_NativeXDP_loadAndAttachBPFProgramToNic0
  (JNIEnv *, jclass, jstring, jstring, jstring, jint, jboolean);

/*
 * Class:     io_vproxy_xdp_NativeXDP
 * Method:    detachBPFProgramFromNic0
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_io_vproxy_xdp_NativeXDP_detachBPFProgramFromNic0
  (JNIEnv *, jclass, jstring);

/*
 * Class:     io_vproxy_xdp_NativeXDP
 * Method:    findMapByNameInBPF0
 * Signature: (JLjava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_io_vproxy_xdp_NativeXDP_findMapByNameInBPF0
  (JNIEnv *, jclass, jlong, jstring);

/*
 * Class:     io_vproxy_xdp_NativeXDP
 * Method:    createUMem0
 * Signature: (IIIII)J
 */
JNIEXPORT jlong JNICALL Java_io_vproxy_xdp_NativeXDP_createUMem0
  (JNIEnv *, jclass, jint, jint, jint, jint, jint);

/*
 * Class:     io_vproxy_xdp_NativeXDP
 * Method:    shareUMem0
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_io_vproxy_xdp_NativeXDP_shareUMem0
  (JNIEnv *, jclass, jlong);

/*
 * Class:     io_vproxy_xdp_NativeXDP
 * Method:    getBufferFromUMem0
 * Signature: (J)Ljava/nio/ByteBuffer;
 */
JNIEXPORT jobject JNICALL Java_io_vproxy_xdp_NativeXDP_getBufferFromUMem0
  (JNIEnv *, jclass, jlong);

/*
 * Class:     io_vproxy_xdp_NativeXDP
 * Method:    getBufferAddressFromUMem0
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_io_vproxy_xdp_NativeXDP_getBufferAddressFromUMem0
  (JNIEnv *, jclass, jlong);

/*
 * Class:     io_vproxy_xdp_NativeXDP
 * Method:    createXSK0
 * Signature: (Ljava/lang/String;IJIIIZIZ)J
 */
JNIEXPORT jlong JNICALL Java_io_vproxy_xdp_NativeXDP_createXSK0
  (JNIEnv *, jclass, jstring, jint, jlong, jint, jint, jint, jboolean, jint, jboolean);

/*
 * Class:     io_vproxy_xdp_NativeXDP
 * Method:    addXSKIntoMap0
 * Signature: (JIJ)V
 */
JNIEXPORT void JNICALL Java_io_vproxy_xdp_NativeXDP_addXSKIntoMap0
  (JNIEnv *, jclass, jlong, jint, jlong);

/*
 * Class:     io_vproxy_xdp_NativeXDP
 * Method:    getFDFromXSK0
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_io_vproxy_xdp_NativeXDP_getFDFromXSK0
  (JNIEnv *, jclass, jlong);

/*
 * Class:     io_vproxy_xdp_NativeXDP
 * Method:    fillUpFillRing0
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_io_vproxy_xdp_NativeXDP_fillUpFillRing0
  (JNIEnv *, jclass, jlong);

/*
 * Class:     io_vproxy_xdp_NativeXDP
 * Method:    fetchPackets0
 * Signature: (J[J[J[I[I[I[I[I)I
 */
JNIEXPORT jint JNICALL Java_io_vproxy_xdp_NativeXDP_fetchPackets0
  (JNIEnv *, jclass, jlong, jlongArray, jlongArray, jintArray, jintArray, jintArray, jintArray, jintArray);

/*
 * Class:     io_vproxy_xdp_NativeXDP
 * Method:    rxRelease0
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_io_vproxy_xdp_NativeXDP_rxRelease0
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     io_vproxy_xdp_NativeXDP
 * Method:    writePacket0
 * Signature: (JJ)Z
 */
JNIEXPORT jboolean JNICALL Java_io_vproxy_xdp_NativeXDP_writePacket0
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     io_vproxy_xdp_NativeXDP
 * Method:    writePackets0
 * Signature: (JI[J)I
 */
JNIEXPORT jint JNICALL Java_io_vproxy_xdp_NativeXDP_writePackets0
  (JNIEnv *, jclass, jlong, jint, jlongArray);

/*
 * Class:     io_vproxy_xdp_NativeXDP
 * Method:    completeTx0
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_io_vproxy_xdp_NativeXDP_completeTx0
  (JNIEnv *, jclass, jlong);

/*
 * Class:     io_vproxy_xdp_NativeXDP
 * Method:    fetchChunk0
 * Signature: (J[J[J[I[I[I[I[I)Z
 */
JNIEXPORT jboolean JNICALL Java_io_vproxy_xdp_NativeXDP_fetchChunk0
  (JNIEnv *, jclass, jlong, jlongArray, jlongArray, jintArray, jintArray, jintArray, jintArray, jintArray);

/*
 * Class:     io_vproxy_xdp_NativeXDP
 * Method:    setChunk0
 * Signature: (JIII)V
 */
JNIEXPORT void JNICALL Java_io_vproxy_xdp_NativeXDP_setChunk0
  (JNIEnv *, jclass, jlong, jint, jint, jint);

/*
 * Class:     io_vproxy_xdp_NativeXDP
 * Method:    releaseChunk0
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_io_vproxy_xdp_NativeXDP_releaseChunk0
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     io_vproxy_xdp_NativeXDP
 * Method:    addChunkRefCnt0
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_io_vproxy_xdp_NativeXDP_addChunkRefCnt0
  (JNIEnv *, jclass, jlong);

/*
 * Class:     io_vproxy_xdp_NativeXDP
 * Method:    releaseXSK0
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_io_vproxy_xdp_NativeXDP_releaseXSK0
  (JNIEnv *, jclass, jlong);

/*
 * Class:     io_vproxy_xdp_NativeXDP
 * Method:    releaseUMem0
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_io_vproxy_xdp_NativeXDP_releaseUMem0
  (JNIEnv *, jclass, jlong, jboolean);

/*
 * Class:     io_vproxy_xdp_NativeXDP
 * Method:    releaseBPFObject0
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_io_vproxy_xdp_NativeXDP_releaseBPFObject0
  (JNIEnv *, jclass, jlong);

#ifdef __cplusplus
}
#endif
#endif