/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

@file:Suppress("DEPRECATION")

package com.dertyp7214.appstore.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.danielstone.materialaboutlibrary.MaterialAboutFragment
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import com.danielstone.materialaboutlibrary.items.MaterialAboutItem
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import com.dertyp7214.appstore.BuildConfig
import com.dertyp7214.appstore.R
import com.dertyp7214.appstore.SecretConfig.CONFIG_CLIENT_ID
import com.dertyp7214.appstore.SecretConfig.CONFIG_CLIENT_ID_SANDBOX
import com.dertyp7214.appstore.ThemeStore
import com.dertyp7214.appstore.Utils.Companion.addAlpha
import com.dertyp7214.appstore.Utils.Companion.drawableFromUrl
import com.dertyp7214.appstore.Utils.Companion.getResId
import com.dertyp7214.appstore.Utils.Companion.getSettings
import com.dertyp7214.appstore.Utils.Companion.manipulateColor
import com.dertyp7214.appstore.Utils.Companion.setCursorColor
import com.dertyp7214.appstore.Utils.Companion.sleep
import com.dertyp7214.appstore.Utils.Companion.tintWidget
import com.dertyp7214.appstore.adapter.TranslatorAdapter
import com.dertyp7214.changelogs.ChangeLog
import com.dertyp7214.changelogs.Version
import com.dertyp7214.githubsource.GitHubSource
import com.dertyp7214.githubsource.github.Repository
import com.dertyp7214.githubsource.helpers.ColorStyle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.paypal.android.sdk.payments.*
import com.shashank.sony.fancytoastlib.FancyToast
import de.psdev.licensesdialog.LicensesDialog
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20
import de.psdev.licensesdialog.licenses.BSD2ClauseLicense
import de.psdev.licensesdialog.licenses.MITLicense
import de.psdev.licensesdialog.model.Notice
import de.psdev.licensesdialog.model.Notices
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.math.BigDecimal
import java.net.HttpURLConnection
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

@SuppressLint("ValidFragment")
class FragmentAbout(private val activity: Activity) : MaterialAboutFragment() {
    var bottomSheetBehavior: BottomSheetBehavior<*>
    var bottomSheetBehaviorTranslators: BottomSheetBehavior<*>
    private var setUp = false
    private var counter = 0

    init {
        if (!setUp)
            setUpBottomSheet()

        val bottomSheet = activity.findViewById<LinearLayout>(R.id.bottom_sheet)
        val bottomSheetTranslators = activity.findViewById<LinearLayout>(R.id.bottom_sheet_translators)
        val backGround = activity.findViewById<View>(R.id.bg)

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehaviorTranslators = BottomSheetBehavior.from(bottomSheetTranslators)

        val callback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == STATE_HIDDEN)
                    backGround.visibility = View.GONE
                else
                    backGround.visibility = View.VISIBLE
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                var offset = (1 - -slideOffset) / 1 * 0.7f
                if (offset.toString() == "NaN")
                    offset = 0.7f
                try {
                    val color = Color.parseColor(addAlpha("#000000", offset.toDouble()))
                    backGround.setBackgroundColor(color)
                } catch (ignored: Exception) {
                }

            }
        }

        bottomSheetBehavior.state = STATE_HIDDEN
        bottomSheetBehavior.setBottomSheetCallback(callback)

        bottomSheetBehaviorTranslators.state = STATE_HIDDEN
        bottomSheetBehaviorTranslators.setBottomSheetCallback(callback)

        backGround.setOnClickListener {
            bottomSheetBehavior.state = STATE_HIDDEN
            bottomSheetBehaviorTranslators.setState(STATE_HIDDEN)
        }
    }

    override fun getMaterialAboutList(context: Context): MaterialAboutList {
        val notices = Notices()
        notices.addNotice(
                Notice("Android Support",
                        "https://android.googlesource.com",
                        "Copyright (C) 2006 The Android Open Source Project",
                        ApacheSoftwareLicense20()))
        notices.addNotice(
                Notice("GitHubSource",
                        "https://github.com/DerTyp7214/GitHubSource",
                        "Copyright (c) 2018 Josua Lengwenath",
                        MITLicense()))
        notices.addNotice(
                Notice("ThemeableComponents",
                        "https://github.com/DerTyp7214/ThemeableComponents",
                        "Copyright (c) 2018 Josua Lengwenath",
                        MITLicense()))
        notices.addNotice(
                Notice("QrCodePopup",
                        "https://github.com/DerTyp7214/QrCodePopup",
                        "Copyright (c) 2018 Josua Lengwenath",
                        MITLicense()))
        notices.addNotice(
                Notice("RootBeer",
                        "https://github.com/scottyab/rootbeer",
                        "Copyright (C) 2015, Scott Alexander-Bown, Mat Rollings",
                        ApacheSoftwareLicense20()))
        notices.addNotice(
                Notice("RoundedImageView",
                        "https://github.com/vinc3m1/RoundedImageView",
                        "Copyright 2017 Vincent Mi",
                        ApacheSoftwareLicense20()))
        notices.addNotice(
                Notice("material-about-library",
                        "https://github.com/daniel-stoneuk/material-about-library",
                        "Copyright 2016-2018 Daniel Stone",
                        ApacheSoftwareLicense20()))
        notices.addNotice(
                Notice("Android System Bar Tint",
                        "https://github.com/jgilfelt/SystemBarTint",
                        "Copyright 2013 readyState Software Limited",
                        ApacheSoftwareLicense20()))
        notices.addNotice(
                Notice("PayPal Android SDK",
                        "https://github.com/paypal/PayPal-Android-SDK",
                        "Copyright (c) 2014-2016 PayPal Holdings, Inc.",
                        BSD2ClauseLicense()))
        notices.addNotice(
                Notice("Material Dialogs",
                        "https://github.com/afollestad/material-dialogs",
                        "Copyright (c) 2014-2016 Aidan Michael Follestad",
                        MITLicense()))
        notices.addNotice(
                Notice("Lottie for Android",
                        "https://github.com/airbnb/lottie-android",
                        "Copyright 2018 Airbnb, Inc.",
                        ApacheSoftwareLicense20()))
        notices.addNotice(
                Notice("Volley",
                        "https://github.com/mcxiaoke/android-volley",
                        "Copyright (C) 2014,2015,2016 Xiaoke Zhang\nCopyright (C) 2011 The Android Open Source Project",
                        ApacheSoftwareLicense20()))
        notices.addNotice(
                Notice("Material SearchBar Android Material Design Search Bar for Android",
                        "https://github.com/mancj/MaterialSearchBar",
                        "Copyright (c) 2016 mancj",
                        MITLicense()))
        notices.addNotice(
                Notice("CircularImageView",
                        "https://github.com/lopspower/CircularImageView",
                        "Copyright 2018 LOPEZ Mikhael",
                        ApacheSoftwareLicense20()))
        notices.addNotice(
                Notice("Transitioner",
                        "https://github.com/dev-labs-bg/transitioner",
                        "Copyright (c) 2017 Radoslav Yankov",
                        MITLicense()))
        notices.addNotice(
                Notice("PRDownloader",
                        "https://github.com/MindorksOpenSource/PRDownloader",
                        "Copyright (C) 2017 MINDORKS NEXTGEN PRIVATE LIMITED",
                        ApacheSoftwareLicense20()))
        notices.addNotice(
                Notice("ExpansionPanel",
                        "https://github.com/florent37/ExpansionPanel",
                        "Copyright 2017 Florent37, Inc.",
                        ApacheSoftwareLicense20()))
        notices.addNotice(
                Notice("Butter Knife",
                        "https://github.com/JakeWharton/butterknife",
                        "Copyright 2013 Jake Wharton",
                        ApacheSoftwareLicense20()))
        notices.addNotice(
                Notice("FancyToast-Android",
                        "https://github.com/Shashank02051997/FancyToast-Android",
                        "Copyright 2017 Shashank Singhal",
                        ApacheSoftwareLicense20()))
        notices.addNotice(
                Notice("Android Image Cropper",
                        "https://github.com/ArthurHub/Android-Image-Cropper",
                        "Copyright 2016, Arthur Teplitzki 2013, Edmodo, Inc.",
                        ApacheSoftwareLicense20()))
        notices.addNotice(
                Notice("Firebase",
                        "https://developer.android.com/studio/terms",
                        "Copyright 2016, Google LLC.",
                        ApacheSoftwareLicense20()))

        val card = MaterialAboutCard.Builder()
                .title(R.string.text_authors)
                .addItem(MaterialAboutActionItem.Builder()
                        .text(R.string.text_main_author)
                        .subText(getUSerMap(getString(R.string.text_dertyp7214), context)!!["name"] as String?)
                        .icon(getUSerMap(getString(R.string.text_dertyp7214), context)!!["image"] as Drawable?)
                        .setIconGravity(MaterialAboutActionItem.GRAVITY_MIDDLE)
                        .setOnClickAction { openGitHubProfile("DerTyp7214") }
                        .build())
                .build()

        val translators = MaterialAboutCard.Builder()
                .title(R.string.text_translations)
                .addItem(language(context,
                        getString(R.string.text_english),
                        "us",
                        getString(R.string.text_dertyp7214)))
                .addItem(language(context,
                        getString(R.string.text_german),
                        "de",
                        getString(R.string.text_dertyp7214)))
                .addItem(language(context,
                        getString(R.string.text_ukrainian),
                        "ua",
                        getString(R.string.text_dertyp7214)))
                .addItem(language(context,
                        getString(R.string.text_czech),
                        "cz",
                        getString(R.string.text_dertyp7214)))
                .addItem(language(context,
                        getString(R.string.text_turkish),
                        "tr",
                        getString(R.string.text_dertyp7214)))
                .addItem(language(context, getString(R.string.text_spainish),
                        "es",
                        getString(R.string.text_enol_simon), getString(R.string.text_dertyp7214)))
                .build()
        val about = MaterialAboutCard.Builder()
                .addItem(MaterialAboutTitleItem.Builder()
                        .icon(R.mipmap.ic_launcher)
                        .text(R.string.app_name)
                        .build())
                .addItem(MaterialAboutActionItem.Builder()
                        .icon(R.drawable.ic_info_outline_black)
                        .text(R.string.text_version)
                        .subText(BuildConfig.VERSION_NAME)
                        .setOnClickAction {
                            if (!getSettings(activity).getBoolean("easter_egg", false)) {
                                Thread {
                                    sleep(5000)
                                    counter = 0
                                }.start()
                                counter++
                                if (counter in 5..7)
                                    FancyToast.makeText(context,
                                            (8 - counter).toString() + " " + getString(
                                                    R.string.text_steps_to_easter_egg),
                                            FancyToast.LENGTH_LONG, FancyToast.INFO, false)
                                            .show()
                                if (counter == 8) {
                                    getSettings(activity).edit().putBoolean("easter_egg", true)
                                            .apply()
                                    FancyToast.makeText(context,
                                            getString(R.string.text_easteregg_enabled),
                                            FancyToast.LENGTH_LONG, FancyToast.INFO, false)
                                            .show()
                                }
                            }
                        }
                        .build())
                .addItem(MaterialAboutActionItem.Builder()
                        .icon(R.drawable.ic_build_black_24dp)
                        .text(R.string.text_build_type)
                        .subText(BuildConfig.BUILD_TYPE)
                        .build())
                .addItem(MaterialAboutActionItem.Builder()
                        .icon(R.drawable.ic_update_black_24dp)
                        .text(R.string.text_changes)
                        .setOnClickAction {
                            ChangeLog.Builder(context)
                                    .addVersion(Version.Builder(context)
                                            .setVersionName("5.2")
                                            .addChange(Version.Change(Version.Change.ChangeType.IMPROVEMENT, "Changelog's based now on my Library"))
                                            .build())
                                    .addVersion(Version.Builder(context)
                                            .setVersionName("5.2")
                                            .addChange(Version.Change(Version.Change.ChangeType.IMPROVEMENT, "Optimized Downloads"))
                                            .build())
                                    .addVersion(Version.Builder(context)
                                            .setVersionName("5.1")
                                            .addChange(Version.Change(Version.Change.ChangeType.FIX, "Login crash"))
                                            .build())
                                    .addVersion(Version.Builder(context)
                                            .setVersionName("5.0")
                                            .addChange(Version.Change(Version.Change.ChangeType.IMPROVEMENT, "Complete app rewritten in Kotlin"))
                                            .build())
                                    .addVersion(Version.Builder(context)
                                            .setVersionName("4.9")
                                            .addChange(Version.Change(Version.Change.ChangeType.IMPROVEMENT, "QR-Code generator"))
                                            .build())
                                    .addVersion(Version.Builder(context)
                                            .setVersionName("4.8")
                                            .addChange(Version.Change(Version.Change.ChangeType.ADD, "Added QR-Code in share App"))
                                            .build())
                                    .addVersion(Version.Builder(context)
                                            .setVersionName("4.7")
                                            .addChange(Version.Change(Version.Change.ChangeType.FIX, "App-crash"))
                                            .build())
                                    .addVersion(Version.Builder(context)
                                            .setVersionName("4.6")
                                            .addChange(Version.Change(Version.Change.ChangeType.FIX, "Style improvements"))
                                            .build())
                                    .addVersion(Version.Builder(context)
                                            .setVersionName("4.5")
                                            .addChange(Version.Change(Version.Change.ChangeType.ADD, "Translations"))
                                            .build())
                                    .addVersion(Version.Builder(context)
                                            .setVersionName("4.4")
                                            .addChange(Version.Change(Version.Change.ChangeType.FIX, "Settingssyncing"))
                                            .build())
                                    .addVersion(Version.Builder(context)
                                            .setVersionName("4.3")
                                            .addChange(Version.Change(Version.Change.ChangeType.FIX, "Statusbar bug when swiping back"))
                                            .build())
                                    .addVersion(Version.Builder(context)
                                            .setVersionName("4.2")
                                            .addChange(Version.Change(Version.Change.ChangeType.ADD, "AndroidX support"))
                                            .addChange(Version.Change(Version.Change.ChangeType.FIX, "MaterialAbout library temporarily fixed by myself"))
                                            .build())
                                    .addVersion(Version.Builder(context)
                                            .setVersionName("4.1")
                                            .addChange(Version.Change(Version.Change.ChangeType.ADD, "Styles for android > 9"))
                                            .addChange(Version.Change(Version.Change.ChangeType.ADD, "Updatenotifications"))
                                            .build())
                                    .addVersion(Version.Builder(context)
                                            .setVersionName("4.0")
                                            .addChange(Version.Change(Version.Change.ChangeType.FIX, "Image Scaling"))
                                            .build())
                                    .addVersion(Version.Builder(context)
                                            .setVersionName("3.8")
                                            .addChange(Version.Change(Version.Change.ChangeType.ADD, "Outline Icons"))
                                            .build())
                                    .addVersion(Version.Builder(context)
                                            .setVersionName("3.6")
                                            .addChange(Version.Change(Version.Change.ChangeType.ADD, "Background Image"))
                                            .build())
                                    .addVersion(Version.Builder(context)
                                            .setVersionName("3.5")
                                            .addChange(Version.Change(Version.Change.ChangeType.ADD, "More translations"))
                                            .addChange(Version.Change(Version.Change.ChangeType.ADD, "Changelog's"))
                                            .addChange(Version.Change(Version.Change.ChangeType.FIX, "Crash after installing App"))
                                            .build())
                                    .addVersion(Version.Builder(context)
                                            .setVersionName("3.4")
                                            .addChange(Version.Change(Version.Change.ChangeType.ADD, "Install/Open Button to my Apps screen"))
                                            .addChange(Version.Change(Version.Change.ChangeType.ADD, "Spanish translations"))
                                            .build())
                                    .setLinkColor(Color.GREEN)
                                    .build().buildDialog("Changes").showDialog()
                        }
                        .build())
                .addItem(MaterialAboutActionItem.Builder()
                        .icon(R.drawable.ic_donate)
                        .text(R.string.text_donate_sub)
                        .setOnClickAction {
                            bottomSheetBehavior
                                    .setState(BottomSheetBehavior.STATE_EXPANDED)
                        }
                        .build())
                .addItem(MaterialAboutActionItem.Builder()
                        .icon(R.drawable.github)
                        .text(R.string.text_project_github)
                        .setOnClickAction { openSourceCode(context) }
                        .build())
                .addItem(MaterialAboutActionItem.Builder()
                        .text(R.string.text_licenses)
                        .icon(R.drawable.file_icon)
                        .setIconGravity(MaterialAboutActionItem.GRAVITY_MIDDLE)
                        .setOnClickAction {
                            try {
                                LicensesDialog.Builder(Objects.requireNonNull<FragmentActivity>(getActivity()))
                                        .setNotices(notices)
                                        .setIncludeOwnLicense(true)
                                        .build()
                                        .show()
                            } catch (ignored: Exception) {
                            }
                        }
                        .build())
                .build()

        return MaterialAboutList.Builder()
                .addCard(about)
                .addCard(card)
                .addCard(translators)
                .build()
    }

    private fun openGitHubProfile(userName: String) {
        openUrl("https://github.com/$userName")
    }

    private fun openUrl(url: String) {
        val gitIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(gitIntent)
    }

    private fun getUSerMap(userName: String, context: Context): HashMap<String, Any>? {
        val userMap = HashMap<String, Any>()
        if (users.containsKey(userName))
            return users[userName]
        try {
            val jsonObject = JSONObject(
                    getJSONObject("https://api.github.com/users/$userName", context))
            userMap["id"] = jsonObject.getString("id")
            val name = if (jsonObject.getString("name") == "null")
                jsonObject
                        .getString("login")
            else
                jsonObject.getString("name")
            userMap["name"] = name
            userMap["image"] = drawableFromUrl(context,
                    "https://avatars0.githubusercontent.com/u/" + userMap["id"]!!)
        } catch (e: Exception) {
            e.printStackTrace()
            userMap["image"] = context.resources.getDrawable(R.mipmap.ic_launcher)
            userMap["name"] = context.getString(R.string.app_name)
        }

        users[userName] = userMap
        return userMap
    }

    private fun getJSONObject(url: String, context: Context): String {
        return try {
            val s = getSettings(context).getString("API_KEY", null)
            val web = URL(url)
            val connection = web.openConnection() as HttpURLConnection
            connection.setRequestProperty("Authorization", "token " + s!!)
            val reader: BufferedReader

            reader = if (s == "")
                BufferedReader(InputStreamReader(web.openStream()))
            else
                BufferedReader(InputStreamReader(connection.inputStream))

            val ret = StringBuilder()
            var line: String? = null

            while ({ line = reader.readLine(); line }() != null)
                ret.append(line!!)

            reader.close()
            ret.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            "{\"message\": \"Something went wrong.\"}"
        }
    }

    private fun getLanguageFlag(langCode: String): Drawable? {
        if (!languages.containsKey(langCode))
            languages[langCode] = resources.getDrawable(getResId(langCode, R.drawable::class.java))
        return languages[langCode]
    }

    private fun language(context: Context, language: String, langCode: String, vararg userNames: String): MaterialAboutItem {
        if (userNames.isNotEmpty()) {
            return MaterialAboutActionItem.Builder()
                    .text(language)
                    .setOnClickAction {
                        val title = activity.findViewById<TextView>(R.id.title_translators)
                        title.text = language

                        val translatorList = ArrayList<TranslatorAdapter.Translator>()

                        for (userName in userNames)
                            translatorList.add(TranslatorAdapter.Translator(userName,
                                    getUSerMap(userName, context)!!["name"] as String,
                                    getUSerMap(userName, context)!!["image"] as Drawable))

                        val adapter = TranslatorAdapter(context, translatorList)
                        adapter.notifyDataSetChanged()

                        val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview_translators)
                        recyclerView.layoutManager = LinearLayoutManager(context)
                        recyclerView.adapter = adapter

                        val close = activity.findViewById<Button>(R.id.close_translations)
                        close.setTextColor(ThemeStore.getInstance(context)!!.accentColor)
                        close.setOnClickListener {
                            bottomSheetBehaviorTranslators
                                    .setState(BottomSheetBehavior.STATE_HIDDEN)
                        }
                        bottomSheetBehaviorTranslators
                                .setState(BottomSheetBehavior.STATE_EXPANDED)
                    }
                    .icon(getLanguageFlag(langCode))
                    .build()
        } else {
            return MaterialAboutActionItem.Builder()
                    .text(language)
                    .text(getString(R.string.text_error_data))
                    .subText(getString(R.string.text_error_data))
                    .icon(resources.getDrawable(R.drawable.ic_error_outline_black_24dp, null))
                    .setIconGravity(MaterialAboutActionItem.GRAVITY_MIDDLE)
                    .build()
        }
    }

    private fun openSourceCode(context: Context) {
        val progressDialog = ProgressDialog.show(context, "", "Loading")
        Thread {
            val store = ThemeStore.getInstance(context)
            try {
                GitHubSource.getInstance(
                        context,
                        Repository("dertyp7214", "NewAppStore",
                                getSettings(
                                        context)
                                        .getString(
                                                "API_KEY", null
                                        ))
                ).setColorStyle(ColorStyle(
                        activity.window.navigationBarColor,
                        manipulateColor(activity.window.navigationBarColor, 0.6f),
                        store!!.accentColor
                )).open()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                val handler = Handler(Looper.getMainLooper())
                handler.post { progressDialog.dismiss() }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun setUpBottomSheet() {
        setUp = true
        val editText = activity.findViewById<EditText>(R.id.text_amount)
        val button = activity.findViewById<Button>(R.id.btn_pay)

        val themeStore = ThemeStore.getInstance(activity)

        button.setTextColor(themeStore!!.accentColor)

        tintWidget(editText, themeStore.accentColor)
        setCursorColor(editText, themeStore.accentColor)

        val config = PayPalConfiguration()
                .environment(CONFIG_ENVIRONMENT)
                .clientId(if (BuildConfig.DEBUG) CONFIG_CLIENT_ID_SANDBOX else CONFIG_CLIENT_ID)
                .merchantName("AppStore")
                .merchantPrivacyPolicyUri(
                        Uri.parse("https://www.example.com/privacy"))
                .merchantUserAgreementUri(
                        Uri.parse("https://www.example.com/legal"))

        val service = Intent(activity, PayPalService::class.java)
        service.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config)
        activity.startService(service)

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                val text = s.toString()
                val length = text.length
                val strings = text.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (strings.size > 1)
                    if (length > 0 && !Pattern.matches("[0-9]{0,2}", strings[1]) && strings[1]
                                    .length > 1)
                        s.delete(length - 1, length)
            }
        })

        button.setOnClickListener {
            bottomSheetBehavior.state = STATE_HIDDEN
            val amount = editText.text.toString()
            if (amount.isNotEmpty()) {
                MaterialDialog.Builder(activity)
                        .title(String.format(String.format(getString(R.string.text_pay_amount),
                                amount.replace(".", ",") + "%s"
                        ), getString(R.string.currency)))
                        .content(R.string.text_pay_content)
                        .positiveText(android.R.string.yes)
                        .negativeText(android.R.string.no)
                        .onPositive { _, _ ->
                            val donating = PayPalPayment(BigDecimal(amount),
                                    getString(
                                            R.string.payment_lang),
                                    "Donation",
                                    PayPalPayment.PAYMENT_INTENT_SALE
                            )
                            val intent = Intent(
                                    activity,
                                    PaymentActivity::class.java
                            )

                            intent.putExtra(PaymentActivity.EXTRA_PAYMENT, donating)

                            startActivityForResult(intent, REQUEST_CODE_PAYMENT)
                        }
                        .show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                val confirm = data!!
                        .getParcelableExtra<PaymentConfirmation>(PaymentActivity.EXTRA_RESULT_CONFIRMATION)
                if (confirm != null) {
                    try {
                        val jsonObject = confirm.toJSONObject()
                        val response = jsonObject.getJSONObject("response")
                        val payment = confirm.payment.toJSONObject()

                        @SuppressLint("SimpleDateFormat")
                        val dateFormat = SimpleDateFormat(
                                "yyyy-MM-dd'T'HH:mm:ss'Z'")

                        val br = "<br/>"

                        val content = (""
                                + getH4("Id: ") + response.getString("id") + br
                                + getH4("Time: ") + dateFormat
                                .parse(response.getString("create_time")).toString() + br
                                + getH4("State: ") + response.getString("state") + br
                                + getH4("Amount: ") + payment.getString("amount") + getString(
                                R.string.currency) + br
                                + getH4("Description: ") + payment.getString("short_description"))

                        MaterialDialog.Builder(activity)
                                .title("Results")
                                .content(Html.fromHtml(content))
                                .positiveText(android.R.string.yes)
                                .show()

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }

                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.d("PAYPAL", "The user canceled.")
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.d(
                        "PAYPAL",
                        "An invalid Payment or PayPalConfiguration was submitted. Please see the docs."
                )
            }
        }
    }

    private fun getH4(string: String): String {
        return "<h4 style=\"display:inline\">$string</h4>"
    }

    override fun onDestroy() {
        try {
            activity.stopService(Intent(activity, PayPalService::class.java))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        super.onDestroy()
    }

    companion object {
        private val CONFIG_ENVIRONMENT = if (BuildConfig.DEBUG)
            PayPalConfiguration.ENVIRONMENT_SANDBOX
        else
            PayPalConfiguration.ENVIRONMENT_PRODUCTION
        private const val REQUEST_CODE_PAYMENT = 1
        var users = HashMap<String, HashMap<String, Any>>()
        private val languages = HashMap<String, Drawable>()
    }
}
