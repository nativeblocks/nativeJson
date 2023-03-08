package io.nativeblocks.nativejson

internal sealed interface JsonPointer
internal data class Root(val type: String) : JsonPointer
internal data class AllField(val type: String) : JsonPointer
internal data class Index(val pos: Int) : JsonPointer
internal data class Field(val name: String) : JsonPointer