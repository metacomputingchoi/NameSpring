# namingengine/consumer-rules.pro
# Consumer ProGuard rules for NamingEngine SDK

# Keep all public API
-keep class com.ssc.namingengine.NamingEngineSDK { *; }
-keep class com.ssc.namingengine.NamingEngineException { *; }
-keep interface com.ssc.namingengine.api.NamingEngine { *; }
-keep class com.ssc.namingengine.api.model.** { *; }
-keep interface com.ssc.namingengine.util.logger.Logger { *; }

# Keep implementations
-keep class com.ssc.namingengine.api.impl.** { *; }

# Keep data classes
-keep class com.ssc.namingengine.data.** { *; }
-keep class com.ssc.namingengine.common.** { *; }

# JSON parsing
-keep class org.json.** { *; }

# Kotlin
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }