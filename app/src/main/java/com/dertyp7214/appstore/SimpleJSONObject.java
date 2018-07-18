/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class SimpleJSONObject extends JSONObject {


    private static final String FIELDNAME_NAME_VALUE_PAIRS = "nameValuePairs";


    public SimpleJSONObject(String string) throws JSONException {
        super(string);
    }


    public SimpleJSONObject(JSONObject jsonObject) throws JSONException {
        super(jsonObject.toString());
    }


    @Override
    public JSONObject getJSONObject(String name) throws JSONException {

        final JSONObject jsonObject = super.getJSONObject(name);

        return new SimpleJSONObject(jsonObject.toString());
    }


    @Override
    public JSONArray getJSONArray(String name) throws JSONException {

        JSONArray jsonArray = null;

        try {

            final Map<String, Object> map = this.getKeyValueMap();

            final Object value = map.get(name);

            jsonArray = this.evaluateJSONArray(name, value);

        } catch (Exception e) {

            throw new RuntimeException(e);

        }

        return jsonArray;
    }


    private JSONArray evaluateJSONArray(String name, final Object value) throws JSONException {

        JSONArray jsonArray = null;

        if (value instanceof JSONArray) {

            jsonArray = this.castToJSONArray(value);

        } else if (value instanceof JSONObject) {

            jsonArray = this.createCollectionWithOneElement(value);

        } else {

            jsonArray = super.getJSONArray(name);

        }
        return jsonArray;
    }


    private JSONArray createCollectionWithOneElement(final Object value) {

        final Collection<Object> collection = new ArrayList<Object>();
        collection.add(value);

        return (JSONArray) new JSONArray(collection);
    }


    private JSONArray castToJSONArray(final Object value) {
        return (JSONArray) value;
    }


    private Map<String, Object> getKeyValueMap() throws NoSuchFieldException, IllegalAccessException {

        final Field declaredField = JSONObject.class.getDeclaredField(FIELDNAME_NAME_VALUE_PAIRS);
        declaredField.setAccessible(true);

        @SuppressWarnings("unchecked") final Map<String, Object> map =
                (Map<String, Object>) declaredField.get(this);

        return map;
    }


}