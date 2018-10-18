/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.items

import android.graphics.drawable.Drawable

class SearchItem @JvmOverloads constructor(title: String, val id: String, icon: Drawable, val version: String = "0", val isUpdate: Boolean = true) : AppItem(title, icon)
