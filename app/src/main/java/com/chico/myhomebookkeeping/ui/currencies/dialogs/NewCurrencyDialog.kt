package com.chico.myhomebookkeeping.ui.currencies.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.chico.myhomebookkeeping.EditNameTextWatcher
import java.lang.IllegalStateException
import com.chico.myhomebookkeeping.R
import com.chico.myhomebookkeeping.interfaces.currencies.AddNewCurrencyCallBack
import com.chico.myhomebookkeeping.helpers.CheckString
import com.chico.myhomebookkeeping.utils.getString


class NewCurrencyDialog(
    private val result: Any,
    private val addNewCurrencyCallBack: AddNewCurrencyCallBack
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val layout = inflater.inflate(R.layout.dialog_new_currency, null)

            val editText = layout.findViewById<EditText>(R.id.currency_name)
            var namesList = mutableListOf<String>()
            val errorTextView = layout.findViewById<TextView>(R.id.error_this_name_is_taken)

            val addButton = layout.findViewById<Button>(R.id.addNewCurrencyButton)
            val addAndSelectButton = layout.findViewById<Button>(R.id.addAndSelectNewItemButton)
            val cancelButton = layout.findViewById<Button>(R.id.cancelCreateButton)

            if (result is List<*>) {
                namesList = (result as List<String>).toMutableList()
//                Message.log("names list size= ${namesList.size}")
            }
            fun listButtons() = listOf(
                addButton,addAndSelectButton
            )
            editText.addTextChangedListener(
                EditNameTextWatcher(
                    namesList,
                    listButtons(),
                    errorTextView
                )
            )

            addAndSelectButton.setOnClickListener {
                val text = editText.getString()
                if (text.isNotEmpty()) {
                    val isLengthChecked: Boolean = checkLengthText(text)
                    if (isLengthChecked) {
                        addNewCurrencyCallBack.addAndSelect(text)
                        closeDialog()
                    }
                    if (!isLengthChecked) {
                        showMessage(getString(R.string.message_too_short_name))
                    }
                } else if (text.isEmpty()) {
                    showMessage(getString(R.string.message_too_short_name))
                }
            }

            addButton.setOnClickListener {
                val text = editText.getString()
                if (text.isNotEmpty()) {
                    val isLengthChecked: Boolean = checkLengthText(text)
                    if (isLengthChecked) {
                        addNewCurrencyCallBack.add(text)
                        closeDialog()
                    }
                    if (!isLengthChecked) {
                        showMessage(getString(R.string.message_too_short_name))
                    }
                } else if (text.isEmpty()) {
                    showMessage(getString(R.string.message_too_short_name))
                }
            }

            cancelButton.setOnClickListener {
                closeDialog()
            }

            builder.setView(layout)
            builder.create()

        } ?: throw IllegalStateException(getString(R.string.exceptions_activity_cant_be_null))
    }

    private fun closeDialog() {
        dialog?.cancel()
    }

    private fun checkLengthText(text: String): Boolean {
        return CheckString.isLengthMoThan(text)
    }

    private fun showMessage(s: String) {
        Toast.makeText(context, s, Toast.LENGTH_LONG).show()
    }
}