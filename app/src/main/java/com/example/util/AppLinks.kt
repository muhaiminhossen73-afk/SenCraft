package com.example.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object AppLinks {
    const val PRIVACY_POLICY_URL = "https://sites.google.com/view/sencraft/home"
    const val ACCOUNT_DELETION_URL = "https://sites.google.com/view/sencraft/delete-account"
    const val DEVELOPER_FACEBOOK_URL = "https://www.facebook.com/share/19T9ZTkLd1/"

    /**
     * Starts an ACTION_VIEW intent for the URL and catches ActivityNotFoundException.
     * Shows message "No app found to open this link".
     * Does NOT use resolveActivity.
     */
    fun openUrl(context: Context, url: String, onNoAppFound: (() -> Unit)? = null) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            if (onNoAppFound != null) {
                onNoAppFound()
            } else {
                Toast.makeText(context, "No app found to open this link", Toast.LENGTH_SHORT).show()
            }
        } catch (_: Exception) {
            if (onNoAppFound != null) {
                onNoAppFound()
            } else {
                Toast.makeText(context, "No app found to open this link", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

fun openUrl(context: Context, url: String, onNoAppFound: (() -> Unit)? = null) {
    AppLinks.openUrl(context, url, onNoAppFound)
}
