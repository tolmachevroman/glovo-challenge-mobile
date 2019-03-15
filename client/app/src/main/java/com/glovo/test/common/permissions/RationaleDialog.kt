package com.glovo.test.common.permissions

import android.Manifest
import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.glovo.test.R


/**
 * A dialog that explains the use of the location permission and requests the necessary
 * permission.
 *
 *
 * The activity should implement
 * [android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback]
 * to handle permit or denial of this permission request.
 */
class RationaleDialog : DialogFragment() {

    companion object {

        private const val ARGUMENT_PERMISSION_REQUEST_CODE = "requestCode"

        /**
         * Creates a new instance of a dialog displaying the rationale for the use of the location
         * permission.
         *
         *
         * The permission is requested after clicking 'ok'.
         *
         * @param requestCode    Id of the request that is used to request the permission. It is
         * returned to the
         * [android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback].
         * cancelled.
         */
        fun newInstance(requestCode: Int): RationaleDialog {
            val arguments = Bundle()
            arguments.putInt(ARGUMENT_PERMISSION_REQUEST_CODE, requestCode)
            val dialog = RationaleDialog()
            dialog.arguments = arguments
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity?.let { activity ->
            arguments?.let {
                val requestCode = it.getInt(ARGUMENT_PERMISSION_REQUEST_CODE)

                return AlertDialog.Builder(activity)
                    .setTitle(R.string.permission_rationale_location_title)
                    .setMessage(R.string.permission_rationale_location_body)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        // After click on Ok, request the permission.
                        ActivityCompat.requestPermissions(
                            activity,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            requestCode
                        )
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .create()
            }
        } ?: return super.onCreateDialog(savedInstanceState)
    }
}