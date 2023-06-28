import com.kotliny.network.engine.IosHttpEngine
import com.kotliny.network.engine.core.*
import com.kotliny.network.engine.core.sources.inputStream
import com.kotliny.network.engine.core.sources.source
import com.kotliny.network.engine.core.surfaces.asSurface
import com.kotliny.network.engine.core.surfaces.surfaceOfByteArray
import com.kotliny.network.engine.test.utils.random
import com.kotliny.network.engine.test.utils.testFolder
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.Foundation.*
import kotlin.test.*

class IosStreamsTest {

    private val testFolder = testFolder()

    @AfterTest
    fun deleteOnExit() {
        testFolder.delete()
    }

    @Test
    fun `input as source to output as surface`() {
        val originFile = testFolder.random()
        val originSource = originFile.nsInputStream().source()
        assertNull(originSource.length())

        val destinationFile = File(testFolder, "newFile")
        assertFalse(destinationFile.exists())
        assertFalse(originSource.isConsumed())

        val destinationSurface = destinationFile.nsOutputStream().asSurface()
        assertEquals(0, destinationFile.length())

        originSource.transferTo(destinationSurface)
        destinationSurface.close()

        assertTrue(destinationFile.exists())
        assertEquals(originFile.length(), destinationFile.length())
    }

    @Test
    fun `source to input to source`() {
        val originFile = testFolder.random()
        val destinationFile = originFile.source().inputStream().source().transferToFile(testFolder)
        assertEquals(originFile.length(), destinationFile.length())
        assertContentEquals(originFile.source().transferToByteArray(), destinationFile.source().transferToByteArray())
    }

    @Test
    fun `input to source to input`() {
        val originFile = testFolder.random()
        val resultStream = originFile.nsInputStream().source().inputStream()
        assertContentEquals(originFile.nsInputStream().readBytes(), resultStream.readBytes())

    }

    @Test
    fun newHttp() {
        assertIs<IosHttpEngine>(newHttpEngine())
    }

    private fun File.nsInputStream(): NSInputStream {
        return NSInputStream.inputStreamWithFileAtPath(path)!!.apply { open() }
    }

    private fun File.nsOutputStream(): NSOutputStream {
        NSFileManager.defaultManager.createFileAtPath(path, null, null)
        return NSOutputStream.outputStreamToFileAtPath(path, false).apply { open() }
    }

    private fun NSInputStream.readBytes(): ByteArray {
        val buffer = ByteArray(1024)

        val surface = surfaceOfByteArray()
        while (true) {
            val result = buffer.usePinned { read(it.addressOf(0).reinterpret(), 1024) }
            if (result <= 0) break
            surface.write(buffer, result.toInt())
        }
        return surface.close()
    }
}
