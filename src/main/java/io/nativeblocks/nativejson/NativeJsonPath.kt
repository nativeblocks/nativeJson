package io.nativeblocks.nativejson

import org.json.JSONArray
import org.json.JSONObject

class NativeJsonPath {

    private fun isObject(jsonString: String): Boolean {
        return try {
            JSONObject(jsonString)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun isArray(jsonString: String): Boolean {
        return try {
            JSONArray(jsonString)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun query(jsonString: String?, query: String?): Any? {
        if (query.isNullOrEmpty()) return null
        if (jsonString.isNullOrEmpty()) return null

        var rootObject: Any? = null
        if (isObject(jsonString)) {
            rootObject = mapToObject(JSONObject(jsonString))
        }
        if (isArray(jsonString)) {
            rootObject = mapToObject(JSONArray(jsonString))

        }
        return try {
            val pointers = queryParser(query)
            val result = queryMapper(rootObject, pointers)
            result
        } catch (e: Exception) {
            e.message
        }
    }

    private fun queryMapper(rootObject: Any?, pointers: MutableList<JsonPointer>): Any? {
        var p = rootObject
        pointers.forEach { pointer ->
            p = when (pointer) {
                is AllField -> (p as List<*>)
                is Index -> (p as List<*>)[pointer.pos]
                is Field -> (p as HashMap<*, *>)[pointer.name]
                is Root -> p
            }
        }
        return p
    }

    private fun queryParser(query: String): MutableList<JsonPointer> {
        val pointers = mutableListOf<JsonPointer>()
        var path = query
        path.forEach { c ->
            when (c) {
                '$' -> {
                    path = path.replaceFirst("$c", "")
                    pointers.add(Root(c.toString()))
                }

                '[' -> {
                    val bracketValue: String = path.substring(path.indexOfFirst { it == '[' } + 1,
                        path.indexOfFirst { it == ']' })
                    path = when {
                        bracketValue.matches("-?[0-9]+(\\.[0-9]+)?".toRegex()) -> {
                            pointers.add(Index(bracketValue.toInt()))
                            path.replaceFirst("[$bracketValue]", "")
                        }

                        bracketValue == ":" -> {
                            pointers.add(AllField(":"))
                            path.replaceFirst("[$bracketValue]", "")
                        }

                        else -> {
                            pointers.add(Field(bracketValue))
                            path.replaceFirst("[$bracketValue]", "")
                        }
                    }
                }
            }
        }
        return pointers
    }

    private fun mapToObject(source: Any): Any? {
        if (source is JSONArray) {
            val mapped: MutableList<Any?> = ArrayList()
            for (i in 0 until source.length()) {
                mapped.add(mapToObject(source[i]))
            }
            return mapped
        } else if (source is JSONObject) {
            val mapped: MutableMap<String, Any?> = HashMap()
            for (obj in source.keys()) {
                val key = obj.toString()
                mapped[key] = mapToObject(source[key])
            }
            return mapped
        } else if (source === JSONObject.NULL) {
            return null
        } else {
            return source
        }
    }
}