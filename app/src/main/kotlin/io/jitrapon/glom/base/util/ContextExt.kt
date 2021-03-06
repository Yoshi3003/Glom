package io.jitrapon.glom.base.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.text.TextUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.Toast
import androidx.annotation.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.jitrapon.glom.R
import io.jitrapon.glom.base.model.AndroidPluralString
import io.jitrapon.glom.base.model.AndroidString
import io.jitrapon.glom.base.model.PreferenceItemUiModel
import io.jitrapon.glom.base.ui.widget.recyclerview.DialogRecyclerViewAdapter


/**
 * Contains various functions using the Context
 *
 * @author Jitrapon Tiachunpun
 */

/**
 * Show a simple toast message
 */
fun Context.showToast(message: AndroidString) {
    val text = getString(message)
    if (!TextUtils.isEmpty(text)) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }
}

/**
 * Show an alert dialog with optional buttons and callbacks
 */
fun Context.showAlertDialog(title: AndroidString?,
                            message: AndroidString,
                            positiveOptionText: AndroidString? = null,
                            onPositiveOptionClicked: (() -> Unit)? = null,
                            negativeOptionText: AndroidString? = null,
                            onNegativeOptionClicked: (() -> Unit)? = null,
                            isCancelable: Boolean = true,
                            onCancel: (() -> Unit)? = null): AlertDialog {
    return MaterialAlertDialogBuilder(this).apply {
        val titleText = getString(title)
        val messageText = getString(message)
        val positive = getString(positiveOptionText)
        val negative = getString(negativeOptionText)
        if (!TextUtils.isEmpty(titleText)) setTitle(titleText)
        if (!TextUtils.isEmpty(messageText)) setMessage(messageText)
        if (!TextUtils.isEmpty(positive)) {
            setPositiveButton(positive) { _, _ ->
                onPositiveOptionClicked?.invoke()
            }
        }
        if (!TextUtils.isEmpty(negative)) {
            setNegativeButton(negative) { _, _ ->
                onNegativeOptionClicked?.invoke()
            }
        }
        setOnCancelListener {
            onCancel?.invoke()
        }
    }.setCancelable(isCancelable).show()
}

/**
 * Show a dialog that displays a list of selectable choices
 */
fun Context.showChoiceDialog(title: AndroidString?, items: ArrayList<PreferenceItemUiModel>, onItemClick: (Int) -> Unit): AlertDialog {
    val customView = LayoutInflater.from(this).inflate(R.layout.dialog_list_view, null)
    customView.findViewById<RecyclerView>(R.id.dialog_list_view_recycler_view).apply {
        adapter = DialogRecyclerViewAdapter(this@showChoiceDialog, items, onItemClick)
        layoutManager = LinearLayoutManager(this@showChoiceDialog, RecyclerView.VERTICAL, false)
    }
    return MaterialAlertDialogBuilder(this)
        .setTitle(getString(title))
        .setView(customView)
        .show()
}

/**
 * Convenience wrapper to retrieve @ColorInt integer from Color ID
 */
@ColorInt
fun Context.color(@ColorRes colorId: Int) = ContextCompat.getColor(this, colorId)

/**
 * Converts an AndroidString object into a CharSequence, ready for display
 */
fun Context.getString(string: AndroidString?): CharSequence? {
    string ?: return null
    return if (!TextUtils.isEmpty(string.text)) string.text else {
        if (string.resId != null) {
            return if (string.formatArgs != null) getString(string.resId, *string.formatArgs)
            else getString(string.resId)
        }
        return null
    }
}

/**
 * Converts an AndroidPluralString object into a CharSequence, ready for display
 */
fun Context.getPluralString(string: AndroidPluralString?): CharSequence? {
    string ?: return null
    return if (string.formatArgs != null) resources.getQuantityString(string.resId, string.quantity, *string.formatArgs)
    else resources.getQuantityString(string.resId, string.quantity)
}

/**
 * Convenience wrapper to retrieve Integer in Pixel from @DimenRes
 */
fun Context.dimen(@DimenRes dimenId: Int): Int = resources.getDimensionPixelSize(dimenId)

/**
 * Retrieves the color primary of the theme
 */
@ColorInt
fun Context.colorPrimary(): Int {
    return TypedValue().let {
        val resolved = theme.resolveAttribute(R.attr.colorPrimary, it, true)
        if (resolved) color(it.resourceId)
        else color(R.color.dark_grey)
    }
}

@ColorInt
fun Context.attrColor(@AttrRes resId: Int): Int {
    return TypedValue().let {
        val resolved = theme.resolveAttribute(resId, it, true)
        if (resolved) color(it.resourceId)
        else colorPrimary()
    }
}

/**
 * Returns true if the current orientation is landscape
 */
fun Context.isLandscape(): Boolean = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

/**
 * Converts a res ID to a Drawable resource
 */
fun Context.drawable(@DrawableRes resId: Int) = ContextCompat.getDrawable(this, resId)

/**
 * Returns true iff Google Play Services is installed and available on this device
 */
fun Context.hasPlayServices(): Boolean {
    return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS
}

/**
 * Returns true iff the app has been granted the Location permissions
 */
fun Context.hasLocationPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
    else true
}

/**
 * Returns true iff the app has been granted the Read Calendar permission
 */
fun Context.hasReadCalendarPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
    }
    else true
}

/**
 * Returns true iff the app has been granted the Write Calendar permission
 */
fun Context.hasWriteCalendarPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
    }
    else true
}

/**
 * Given a list of permissions, returns the list of permissions not granted
 */
fun Context.getUngrantedPermissions(permissions: Array<out String>): Array<out String> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        arrayListOf<String>().apply {
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(this@getUngrantedPermissions, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                    add(permission)
                }
            }
        }.toTypedArray()
    }
    else arrayOf()
}
