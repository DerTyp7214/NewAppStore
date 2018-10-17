/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.loginout

import android.accounts.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.dertyp7214.appstore.screens.RegisterActivity

/**
 * Created by lengw on 05.12.2017.
 */

class AccountAuthenticator(private val context: Context) : AbstractAccountAuthenticator(context) {

    @Throws(NetworkErrorException::class)
    override fun addAccount(accountAuthenticatorResponse: AccountAuthenticatorResponse, s: String, s2: String, strings: Array<String>, bundle: Bundle): Bundle {
        val intent = Intent(context, RegisterActivity::class.java)
        val bundle1 = Bundle()
        bundle1.putParcelable(AccountManager.KEY_INTENT, intent)
        return bundle1
    }

    override fun editProperties(accountAuthenticatorResponse: AccountAuthenticatorResponse, s: String): Bundle? {
        return null
    }

    @Throws(NetworkErrorException::class)
    override fun confirmCredentials(accountAuthenticatorResponse: AccountAuthenticatorResponse, account: Account, bundle: Bundle): Bundle? {
        return null
    }

    @Throws(NetworkErrorException::class)
    override fun getAuthToken(accountAuthenticatorResponse: AccountAuthenticatorResponse, account: Account, s: String, bundle: Bundle): Bundle? {
        return null
    }

    override fun getAuthTokenLabel(s: String): String? {
        return null
    }

    @Throws(NetworkErrorException::class)
    override fun updateCredentials(accountAuthenticatorResponse: AccountAuthenticatorResponse, account: Account, s: String, bundle: Bundle): Bundle? {
        return null
    }

    @Throws(NetworkErrorException::class)
    override fun hasFeatures(accountAuthenticatorResponse: AccountAuthenticatorResponse, account: Account, strings: Array<String>): Bundle? {
        return null
    }
}
