package org.andcreator.iconpack.dialog

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_changelog.*
import kotlinx.android.synthetic.main.fragment_update.*
import org.andcreator.iconpack.R
import org.andcreator.iconpack.util.Utils

class ChangesDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context!!)
        val inflater = activity?.layoutInflater
        val view = inflater?.inflate(R.layout.dialog_changelog, null)
        val tv = view?.findViewById<TextView>(R.id.updateContent)
        val version = view?.findViewById<TextView>(R.id.updateVersion)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tv?.text = HtmlCompat.fromHtml(getString(R.string.changelog_detail), Html.FROM_HTML_MODE_LEGACY)
        } else {
            tv?.text = Html.fromHtml(getString(R.string.changelog_detail))
        }
        version?.text = "${context!!.resources.getString(R.string.official)} ${Utils.getAppVersionName(context!!)}"
        builder
            .setView(view)
            .setTitle(getString(R.string.changelog_title))
            .setPositiveButton(getString(R.string.btn_ok)) { dialogInterface, _ -> dialogInterface.dismiss() }
        return builder.create()
    }
}
