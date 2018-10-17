/*
 *
 *  * Copyright (c) 2018.
 *  * Created by Josua Lengwenath
 *
 */

package com.dertyp7214.appstore.helpers

import java.util.concurrent.atomic.AtomicInteger

object NotificationID {
    private val c = AtomicInteger(0)
    val id: Int
        get() = c.incrementAndGet()
}