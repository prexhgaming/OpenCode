# Keep runtime annotation/signature metadata used by Kotlin and serialization.
-keepattributes RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,AnnotationDefault,Signature,InnerClasses,EnclosingMethod

# kotlinx.serialization generates serializers at compile time; avoid broad keep rules
# so R8 can remove unused serializers and application code.
