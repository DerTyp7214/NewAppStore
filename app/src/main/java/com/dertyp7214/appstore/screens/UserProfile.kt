/*
 *
 *  * Copyright (c) 2018.
 *  * Created by Josua Lengwenath
 *
 */

package com.dertyp7214.appstore.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.dertyp7214.appstore.Config
import com.dertyp7214.appstore.Config.API_URL
import com.dertyp7214.appstore.R
import com.dertyp7214.appstore.ThemeStore
import com.dertyp7214.appstore.Utils
import com.dertyp7214.appstore.dev.Logs
import com.dertyp7214.appstore.fragments.FragmentUserCard
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.util.*

@Suppress("DEPRECATION")
class UserProfile : Utils() {
    private var user: User? = User("Error", "Error", "Error", "Error")
    private var userImage: Drawable? = null
    private var profileImageView: ImageView? = null
    private var txtName: TextView? = null
    private var txtMail: TextView? = null
    private var relativeLayout: RelativeLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        logs = Logs.getInstance(this)

        themeStore = ThemeStore.getInstance(this)
        val extras = intent.extras

        val fragmentUserCard = fragmentManager.findFragmentById(R.id.fragmentUserCard) as FragmentUserCard
        fragmentUserCard.setContentView(R.layout.user_image_settings, object : FragmentUserCard.OnAttachListener {
            override fun onAttach(view: View) {
                val display = windowManager.defaultDisplay
                val size = Point()
                display.getSize(size)
                val width = size.x
                val b = Utils
                        .drawableToBitmap(Utils.userImageHashMap[Config.UID(this@UserProfile) + "_bg"])
                relativeLayout = view.findViewById(R.id.relative)
                relativeLayout!!.background = BitmapDrawable(resources, Bitmap.createScaledBitmap(b!!, width, ((width / 16).toFloat() * 9).toInt(), false))
                profileImageView = view.findViewById(R.id.user_image)
                txtName = view.findViewById(R.id.txt_name)
                txtMail = view.findViewById(R.id.txt_email)
                return setUser((checkExtraKey(extras!!, "uid") as String?)!!)
            }
        })
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (relativeLayout != null)
            relativeLayout!!.layoutParams.height = relativeLayout!!.width / 16 * 9
    }

    private fun setUser(uid: String) {
        Thread {
            if (!userHashMap.containsKey(uid)) {
                try {
                    val jsonObject = JSONObject(
                            Utils.getWebContent(Config.API_URL + "/apps/user.php?type=json&uid=" + uid))
                    user = User(jsonObject.getString("name"), jsonObject.getString("email"),
                            jsonObject.getString("created_at"), jsonObject.getString("uid"))
                    userImage = getUserImage(uid)
                    user!!.userImage = userImage
                    userHashMap[uid] = user!!
                } catch (e: Exception) {
                    e.printStackTrace()
                    logs.error("setUser", e.toString())
                    userImage = resources.getDrawable(R.mipmap.ic_launcher, null)
                }

            } else
                user = userHashMap[uid]
            runOnUiThread {
                val email = SpannableString(user!!.email)
                email.setSpan(UnderlineSpan(), 0, email.length, 0)
                profileImageView!!.setImageDrawable(user!!.userImage)
                txtName!!.text = user!!.name
                txtMail!!.text = email
                txtMail!!.setOnClickListener {
                    startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + user!!.email)))
                }
                title = user!!.name
                setColors()
                setBackButton()
            }
        }.start()
    }

    @Throws(Exception::class)
    private fun getUserImage(uid: String): Drawable? {
        val profilePic: Drawable?
        val url = "$API_URL/apps/pic/" + URLEncoder.encode(uid, "UTF-8")
                .replace("+", "_") + ".png"
        val imgFile = File(filesDir, "$uid.png")
        if (!imgFile.exists()) {
            if (Config.SERVER_ONLINE) {
                profilePic = Utils.drawableFromUrl(this, url)
                val fileOutputStream = FileOutputStream(imgFile)
                Utils.drawableToBitmap(profilePic)!!
                        .compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)

            } else
                profilePic = resources.getDrawable(R.mipmap.ic_launcher, null)
        } else {
            profilePic = Utils.userImageHashMap[uid]
        }
        return profilePic
    }

    private inner class User (val name: String, val email: String, private val createdAt: String, private val uid: String) {
        var userImage: Drawable? = null
            get() = if (field != null)
                field
            else
                resources
                        .getDrawable(R.mipmap.ic_launcher, null)
    }

    companion object {
        private val userHashMap = HashMap<String, User>()
    }
}
