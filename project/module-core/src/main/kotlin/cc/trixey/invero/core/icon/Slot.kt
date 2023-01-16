package cc.trixey.invero.core.icon

import cc.trixey.invero.common.Pos
import cc.trixey.invero.common.Scale
import kotlinx.serialization.*
import kotlinx.serialization.encoding.*
import taboolib.common5.cint

/**
 * Invero
 * cc.trixey.invero.core.icon.Slot
 *
 * @author Arasple
 * @since 2023/1/16 10:49
 */
@Serializable
class Slot(private val raw: String) {

    @Transient
    private val uncalculatedSlots = mutableSetOf<Int>()

    // 1~10 or 1-10 ->> Slots: 1,2,3,4,5,6,7,8,9,10
    // 1;2;3 ->> Slots: 1,2,3
    // 0x2;0x3 ->> Slots (x=0,y=2), (x=0,y=3)

    private val locations by lazy {
        raw.split(";").mapNotNull { str ->
            // range in slot
            if ('~' in str || '-' in str || ".." in str) {
                val (from, to) = str.split("~", "-", "..")
                uncalculatedSlots += (from.toInt() to to.toInt()).toList().filter { it >= 0 }
            } else if ('x' in str) {
                val (x, y) = str.split('x')
                return@mapNotNull Pos(x.toInt() to y.toInt())
            } else {
                (str.toIntOrNull() ?: -1).let { slot ->
                    if (slot >= 0) uncalculatedSlots += slot
                }
            }

            return@mapNotNull null
        }
    }

    fun release(scale: Scale) = uncalculatedSlots.map { scale.convertToPosition(it) } + locations

    override fun toString(): String {
        return raw
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Serializer(forClass = Slot::class)
    companion object : KSerializer<Slot> {

        override fun serialize(encoder: Encoder, value: Slot) {
            if (value.raw.toIntOrNull() != null) {
                encoder.encodeInt(value.raw.cint)
            } else {
                encoder.encodeString(value.raw)
            }
        }

        override fun deserialize(decoder: Decoder): Slot {
            return try {
                decoder.decodeStructure(descriptor) {
                    Slot(decodeIntElement(descriptor, decodeElementIndex(descriptor)).toString())
                }
            } catch (e: Throwable) {
                Slot(decoder.decodeString())
            }
        }

    }

}