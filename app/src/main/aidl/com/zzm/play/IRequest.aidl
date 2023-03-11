// IRequest.aidl
package com.zzm.play;

// Declare any non-default types here with import statements
import com.zzm.play.IClientCallback;

interface IRequest {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
   int set(int a);

   void registerCallback(IClientCallback callback);

   void unRegisterCallback(IClientCallback callback);

}