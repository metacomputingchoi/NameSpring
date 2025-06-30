# namingengine/proguard-rules.pro
# NamingEngine SDK ProGuard Rules

# Keep public API classes and methods
-keep public class com.ssc.namingengine.NamingEngineSDK { *; }
-keep public class com.ssc.namingengine.NamingEngineException { *; }
-keep public interface com.ssc.namingengine.api.NamingEngine { *; }
-keep public class com.ssc.namingengine.api.model.** { *; }

# Keep Logger interface
-keep public interface com.ssc.namingengine.util.logger.Logger { *; }

# Keep data classes (Kotlin)
-keepclassmembers class com.ssc.namingengine.** {
    public ** component1();
    public ** component2();
    public ** component3();
    public ** component4();
    public ** component5();
    public ** copy(...);
}

# Keep JSON parsing related classes
-keep class org.json.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Keep LocalDateTime (Java 8 Time API)
-keep class java.time.** { *; }

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Kotlin metadata
-keepattributes RuntimeVisibleAnnotations
-keep class kotlin.Metadata { *; }

# Preserve line numbers for debugging
-keepattributes SourceFile,LineNumberTable