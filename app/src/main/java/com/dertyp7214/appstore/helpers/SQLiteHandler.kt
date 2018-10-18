/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.helpers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.util.*

class SQLiteHandler(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    val userDetails: HashMap<String, String>
        get() {
            val user = HashMap<String, String>()
            val selectQuery = "SELECT  * FROM $TABLE_USER"

            val db = this.readableDatabase
            val cursor = db.rawQuery(selectQuery, null)
            cursor.moveToFirst()
            if (cursor.count > 0) {
                user["name"] = cursor.getString(1)
                user["email"] = cursor.getString(2)
                user["uid"] = cursor.getString(3)
                user["created_at"] = cursor.getString(4)
            }
            cursor.close()
            db.close()
            Log.d(TAG, "Fetching user from Sqlite: " + user.toString())

            return user
        }

    override fun onCreate(db: SQLiteDatabase) {
        val LOGIN_TABLE = ("CREATE TABLE " + TABLE_USER + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_EMAIL + " TEXT UNIQUE," + KEY_UID + " TEXT,"
                + KEY_CREATED_AT + " TEXT" + ")")
        db.execSQL(LOGIN_TABLE)

        Log.d(TAG, "Database tables created")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER")

        onCreate(db)
    }

    fun addUser(name: String, email: String, uid: String, created_at: String) {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(KEY_NAME, name)
        values.put(KEY_EMAIL, email)
        values.put(KEY_UID, uid)
        values.put(KEY_CREATED_AT, created_at)

        val id = db.insert(TABLE_USER, null, values)
        db.close()

        Log.d(TAG, "New user inserted into sqlite: $id")
    }

    fun deleteUsers() {
        val db = this.writableDatabase
        db.delete(TABLE_USER, null, null)
        db.close()

        Log.d(TAG, "Deleted all user info from sqlite")
    }

    companion object {
        private val TAG = SQLiteHandler::class.java.simpleName

        private const val DATABASE_VERSION = 1

        private const val DATABASE_NAME = "android_api"

        private const val TABLE_USER = "user"

        private const val KEY_ID = "id"
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
        private const val KEY_UID = "uid"
        private const val KEY_CREATED_AT = "created_at"
    }

}