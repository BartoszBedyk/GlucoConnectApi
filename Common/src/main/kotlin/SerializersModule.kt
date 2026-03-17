import kotlinx.serialization.modules.SerializersModule
import java.time.Instant
import java.util.UUID

val customSerializersModule = SerializersModule {
    contextual(UUID::class, UUIDSerializer)
    contextual(Instant::class, InstantSerializer)
}
