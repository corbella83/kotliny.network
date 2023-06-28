package com.kotliny.network.engine.core

import com.kotliny.network.engine.HttpEngine
import com.kotliny.network.engine.IosHttpEngine
import com.kotliny.network.engine.exceptions.NetworkException
import kotlinx.cinterop.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import platform.CoreFoundation.CFAbsoluteTimeGetCurrent
import platform.Foundation.*

actual object Dispatcher {
    actual fun io(): CoroutineDispatcher = Dispatchers.Default
    actual fun computation(): CoroutineDispatcher = Dispatchers.Default
}

actual fun newHttpEngine(): HttpEngine {
    return IosHttpEngine()
}

actual fun clockTime() = CFAbsoluteTimeGetCurrent().times(1000000000L).toLong()

actual fun epochTime() = NSDate.date().timeIntervalSince1970.times(1000).toLong()

actual fun newUUID() = NSUUID().UUIDString

internal fun ByteArray.toNSData(): NSData {
    return memScoped { NSData.create(bytes = allocArrayOf(this@toNSData), this@toNSData.size.toULong()) }
}

internal fun <T : Any> handleError(code: (CPointer<ObjCObjectVar<NSError?>>) -> T): T {
    return memScoped {
        val errorPtr = alloc<ObjCObjectVar<NSError?>>()
        val result = code(errorPtr.ptr)
        if (errorPtr.value != null) {
            throw NetworkException(errorPtr.value?.localizedDescription() ?: "")
        } else {
            result
        }
    }
}
