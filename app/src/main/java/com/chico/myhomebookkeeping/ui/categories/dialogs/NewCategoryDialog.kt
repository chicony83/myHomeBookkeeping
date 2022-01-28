package com.chico.myhomebookkeeping.ui.categories.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.chico.myhomebookkeeping.EditNameTextWatcher
import com.chico.myhomebookkeeping.R
import com.chico.myhomebookkeeping.db.dao.IconResourcesDao
import com.chico.myhomebookkeeping.db.dataBase
import com.chico.myhomebookkeeping.domain.IconResourcesUseCase
import com.chico.myhomebookkeeping.helpers.CheckString
import com.chico.myhomebookkeeping.helpers.Message
import com.chico.myhomebookkeeping.interfaces.OnItemSelectForSelectCallBackInt
import com.chico.myhomebookkeeping.interfaces.categories.OnAddNewCategoryCallBack
import com.chico.myhomebookkeeping.ui.dialogs.SelectIconDialog
import com.chico.myhomebookkeeping.utils.getString
import com.chico.myhomebookkeeping.utils.launchIo
import com.chico.myhomebookkeeping.utils.launchUi
import java.lang.IllegalStateException

class NewCategoryDialog(
    private val result: Any,
    private val onAddNewCategoryCallBack: OnAddNewCategoryCallBack
) : DialogFragment() {
//    private val dbIcon:IconResourcesDao = dataBase.getDataBase(requireActivity().applicationContext).iconResourcesDao()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val layout = inflater.inflate(R.layout.dialog_new_category, null)

            var namesList = listOf<String>()

            val nameEditText = layout.findViewById<EditText>(R.id.category_name)
            val errorTextView = layout.findViewById<TextView>(R.id.error_this_name_is_taken)
            val incomeRadioButton = layout.findViewById<RadioButton>(R.id.incoming_radio_button)
            val spendingRadioButton = layout.findViewById<RadioButton>(R.id.spending_radio_button)

            val iconImg = layout.findViewById<ImageView>(R.id.iconImg)

            val addButton = layout.findViewById<Button>(R.id.addNewCategoryButton)
            val addAndSelectButton = layout.findViewById<Button>(R.id.addAndSelectNewItemButton)
            val cancelButton = layout.findViewById<Button>(R.id.cancelCreateButton)

            if (result is List<*>) {
                namesList = (result as List<String>)
            }
            fun buttonsList() = listOf(
                addButton, addAndSelectButton
            )

            iconImg.setImageResource(R.drawable.no_image)

            nameEditText.addTextChangedListener(
                EditNameTextWatcher(
                    namesList = namesList,
                    buttonList = buttonsList(),
                    errorMessageTexView = errorTextView
                )
            )

            addAndSelectButton.setOnClickListener {
                checkAndAddCategory(
                    nameEditText,
                    incomeRadioButton,
                    spendingRadioButton,
                    isSelectAfterAdd = true
                )
            }

            addButton.setOnClickListener {
                checkAndAddCategory(
                    nameEditText,
                    incomeRadioButton,
                    spendingRadioButton,
                    isSelectAfterAdd = false
                )
            }

            iconImg.setOnClickListener { showSelectIconDialog()}

            cancelButton.setOnClickListener {
                dialogCancel()
            }
            builder.setView(layout)
            builder.create()
        } ?: throw IllegalStateException(getString(R.string.exceptions_activity_cant_be_null))
    }

    private fun showSelectIconDialog() {
        launchIo {
            val db:IconResourcesDao = dataBase.getDataBase(requireContext()).iconResourcesDao()
            val iconsList = IconResourcesUseCase.getIconsList(db)
            launchUi {
                val dialog = SelectIconDialog(iconsList, object: OnItemSelectForSelectCallBackInt{
                    override fun onSelect(id: Int) {
                        Message.log("selected icon Id = $id")
                    }

                }
                )
                dialog.show(childFragmentManager, getString(R.string.tag_show_dialog))
            }
        }
    }

    private fun checkAndAddCategory(
        nameEditText: EditText,
        incomeRadioButton: RadioButton,
        spendingRadioButton: RadioButton,
        isSelectAfterAdd: Boolean
    ) {
        val name = nameEditText.getString()
        if (nameEditText.text.isNotEmpty()) {
            val isLengthChecked: Boolean = CheckString.isLengthMoThan(name)
            val isTypeCategorySelected =
                isSelectTypeOfCategory(incomeRadioButton, spendingRadioButton)

            if (isLengthChecked) {
                if (isTypeCategorySelected) {
                    if (incomeRadioButton.isChecked) {
                        onAddNewCategoryCallBack.addAndSelect(
                            name = name,
                            isIncome = true,
                            isSelectAfterAdd
                        )
                        dialogCancel()
                    } else if (spendingRadioButton.isChecked) {
                        onAddNewCategoryCallBack.addAndSelect(
                            name = name,
                            isIncome = false,
                            isSelectAfterAdd
                        )
                        dialogCancel()
                    }
                } else if (!isTypeCategorySelected) {
                    showMessage(getString(R.string.message_select_type_of_category))
                }
            } else if (!isLengthChecked) {
                showMessage(getString(R.string.message_too_short_name))
            }
        } else if (nameEditText.text.isEmpty()) {
            showMessage(getString(R.string.message_too_short_name))
        }
    }

    private fun isSelectTypeOfCategory(
        incomeRadioButton: RadioButton,
        spendingRadioButton: RadioButton
    ): Boolean {
        return incomeRadioButton.isChecked or spendingRadioButton.isChecked
    }

    private fun dialogCancel() {
        dialog?.cancel()
    }

    private fun showMessage(s: String) {
        Toast.makeText(context, s, Toast.LENGTH_LONG).show()
    }
}