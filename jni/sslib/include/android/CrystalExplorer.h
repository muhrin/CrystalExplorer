/*
 * CrystalExplorer.h
 *
 *  Created on: Aug 17, 2011
 *      Author: Martin Uhrin
 */

#ifndef CRYSTAL_EXPLORER_H
#define CRYSTAL_EXPLORER_H

// INCLUDES ////////////
#include "SSLib.h"

#include <jni.h>

// DEFINITION ///////////////////////

namespace sstbx {

// FORWARD DECLARATIONS ///////

namespace android {


}
}

JNIEXPORT jstring JNICALL Java_uk_ac_ucl_phys_NdKCrystalExplorer(
  JNIEnv * env,
  jobject javaThis,
  jint numAtoms
);


#endif /* CRYSTAL_EXPLORER_H */
