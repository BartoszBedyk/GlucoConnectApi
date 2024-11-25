import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual


val customSerializersModule = SerializersModule {
    contextual(UUIDSerializer)
}
