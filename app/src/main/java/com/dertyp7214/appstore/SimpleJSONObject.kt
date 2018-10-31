/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class SimpleJSONObject : JSONObject {

    private val keyValueMap: Map<*, *>
        @Throws(NoSuchFieldException::class, IllegalAccessException::class)
        get() {

            val declaredField = JSONObject::class.java.getDeclaredField(FIELD_NAME_NAME_VALUE_PAIRS)
            declaredField.isAccessible = true

            return declaredField.get(this) as Map<*, *>
        }

    @Throws(JSONException::class)
    constructor(string: String) : super(string)

    @Throws(JSONException::class)
    constructor(jsonObject: JSONObject) : super(jsonObject.toString())

    @Throws(JSONException::class)
    override fun getJSONObject(name: String): JSONObject {

        val jsonObject = super.getJSONObject(name)

        return SimpleJSONObject(jsonObject.toString())
    }

    @Throws(JSONException::class)
    override fun getJSONArray(name: String): JSONArray? {
        return try {
            val map = this.keyValueMap
            val value = map[name]
            this.evaluateJSONArray(name, value)
        } catch (e: Exception) {
            null
        }
    }

    @Throws(JSONException::class)
    private fun evaluateJSONArray(name: String, value: Any?): JSONArray? {
        return when (value) {
            is JSONArray -> this.castToJSONArray(value)
            is JSONObject -> this.createCollectionWithOneElement(value)
            else -> super.getJSONArray(name)
        }
    }


    private fun createCollectionWithOneElement(value: Any): JSONArray {
        val collection = ArrayList<Any>()
        collection.add(value)
        return JSONArray(collection)
    }


    private fun castToJSONArray(value: Any): JSONArray {
        return value as JSONArray
    }

    companion object {
        private const val FIELD_NAME_NAME_VALUE_PAIRS = "nameValuePairs"
    }
}