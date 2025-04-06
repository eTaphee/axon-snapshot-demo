package im.etap.config

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.ByteBufInputStream
import io.netty.buffer.ByteBufOutputStream
import org.axonframework.eventsourcing.AggregateCacheEntry
import org.axonframework.serialization.Serializer
import org.axonframework.serialization.SimpleSerializedObject
import org.redisson.client.codec.BaseCodec
import org.redisson.client.protocol.Decoder
import org.redisson.client.protocol.Encoder
import java.io.IOException
import java.nio.charset.StandardCharsets

class AxonCacheCodec(
    private val serializer: Serializer,
) : BaseCodec() {

    private val encoder = Encoder { obj ->
        val buf: ByteBuf = ByteBufAllocator.DEFAULT.buffer()
        try {
            ByteBufOutputStream(buf).use { os ->
                val serialized = serializer.serialize(obj, ByteArray::class.java)
                val data = serialized.data as ByteArray
                os.write(data)
                os.flush()
            }
            buf
        } catch (e: IOException) {
            buf.release()
            throw e
        } catch (e: Exception) {
            buf.release()
            throw IOException(e)
        }
    }

    private val decoder = Decoder { buf, _ ->
        ByteBufInputStream(buf).use { input ->
            val bytes = ByteArray(buf.readableBytes())
            input.read(bytes)

            val xmlString = String(bytes, StandardCharsets.UTF_8)
            val simpleSerializedObject = SimpleSerializedObject(
                xmlString,
                String::class.java,
                AggregateCacheEntry::class.qualifiedName,
                ""
            )

            return@Decoder serializer.deserialize<String, Any>(simpleSerializedObject)
        }
    }

    override fun getValueDecoder(): Decoder<Any> {
        return decoder
    }

    override fun getValueEncoder(): Encoder {
        return encoder
    }
}
