package com.os.cvCamera.features

import android.app.Activity
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.StringRes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.os.cvCamera.R

// Small helpers shared by the features, so each one does not repeat the same toast and dialog
// boilerplate.

/** Brief confirmation message. */
internal fun Activity.showToast(
    @StringRes message: Int,
) = Toast.makeText(this, getString(message), Toast.LENGTH_SHORT).show()

/**
 * Single-choice dialog with a cancel button, dismissed as soon as something is picked.
 *
 * @param checked index to pre-select, or -1 for none.
 * @param onPicked receives the chosen index. Not called when the choice is unchanged.
 */
internal fun Activity.showSingleChoiceDialog(
    @StringRes title: Int,
    labels: Array<String>,
    checked: Int,
    onPicked: (Int) -> Unit,
) {
    // A custom row layout is used because Material3 leaves android:listChoiceIndicatorSingle
    // unset, and the default single choice layout resolves its check mark from that attribute,
    // so the built in overload draws no radio button at all.
    val adapter = ArrayAdapter(this, R.layout.item_single_choice, labels)

    MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setSingleChoiceItems(adapter, checked) { dialog, which ->
            dialog.dismiss()
            if (which != checked) onPicked(which)
        }.setNegativeButton(android.R.string.cancel, null)
        .show()
}
