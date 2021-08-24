package com.chico.myhomebookkeeping.ui.newMoneyMoving

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.chico.myhomebookkeeping.R
import com.chico.myhomebookkeeping.checks.UiElementsCheck
import com.chico.myhomebookkeeping.databinding.FragmentNewMoneyMovingBinding
import com.chico.myhomebookkeeping.helpers.NavControlHelper

import com.chico.myhomebookkeeping.helpers.UiHelper
import com.chico.myhomebookkeeping.utils.hideKeyboard
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.runBlocking
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

class NewMoneyMovingFragment : Fragment() {

    private lateinit var newMoneyMovingViewModel: NewMoneyMovingViewModel
    private var _binding: FragmentNewMoneyMovingBinding? = null
    private val binding get() = _binding!!

    private var currentDateTimeMillis: Long = Calendar.getInstance().timeInMillis

    private lateinit var control: NavController
    private lateinit var navControlHelper: NavControlHelper
    private val uiHelper = UiHelper()
    private val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("select date")
        .setSelection(MaterialDatePicker.todayInUtcMilliseconds()).build()
    private val timePicker =
        MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setTitleText("select Time")
            .build()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewMoneyMovingBinding.inflate(inflater, container, false)

        newMoneyMovingViewModel = ViewModelProvider(this).get(NewMoneyMovingViewModel::class.java)

        return binding.root
    }

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.hideKeyboard()
        control = activity?.findNavController(R.id.nav_host_fragment)!!

        navControlHelper = NavControlHelper(controller = control)

        with(binding) {
//            dateTimeTimeStamp.text = currentDateTimeMillis.parseTimeFromMillis()

            dateTimeTimeStampButton.setOnClickListener {
                datePicker.show(parentFragmentManager, "TAG")
            }
            selectCashAccountButton.setOnClickListener {
                pressSelectButton(R.id.nav_cash_account)
            }
            selectCurrenciesButton.setOnClickListener {
                pressSelectButton(R.id.nav_currencies)
            }
            selectCategoryButton.setOnClickListener {
                pressSelectButton(R.id.nav_categories)
            }
            submitButton.setOnClickListener {
                pressSubmitButton()
            }
        }
        with(newMoneyMovingViewModel) {
            dataTime.observe(viewLifecycleOwner, {
                binding.dateTimeTimeStampButton.text = it.toString()
            })
            selectedCashAccount.observe(viewLifecycleOwner, {
                binding.selectCashAccountButton.text = it.accountName
            })
            selectedCurrency.observe(viewLifecycleOwner, {
                binding.selectCurrenciesButton.text = it.currencyName
            })
            selectedCategory.observe(viewLifecycleOwner, {
                binding.selectCategoryButton.text = it.categoryName
            })

            setDateTimeOnButton(currentDateTimeMillis)

            amountMoney.observe(viewLifecycleOwner, {
                binding.amount.setText(it.toString())
            })
            submitButton.observe(viewLifecycleOwner, {
                binding.submitButton.text = it.toString()
            })
        }

        datePicker.addOnPositiveButtonClickListener {
            newMoneyMovingViewModel.setDate(it)
            timePicker.show(parentFragmentManager, "TAG")
        }

        timePicker.addOnPositiveButtonClickListener {
            val hour: Int = timePicker.hour
//            Log.i("TAG","hour = $hour")
            var minute: Int = timePicker.minute
            with(newMoneyMovingViewModel) {
                setTime(hour, minute)
                setDateTimeOnButton()
            }
        }
        newMoneyMovingViewModel.getAndCheckArgsSp()



        super.onViewCreated(view, savedInstanceState)
    }

    private fun pressSubmitButton() {
        if (UiElementsCheck.isEntered(binding.amount.text)) {
            val amount: Double =
                BigDecimal(binding.amount.text.toString())
                    .setScale(2, RoundingMode.HALF_EVEN)
                    .toDouble()
            val description: String = binding.description.text.toString()
            newMoneyMovingViewModel.saveDataToSP()
            runBlocking {
                val result: Long =
                    newMoneyMovingViewModel.addInMoneyMovingDB(amount, description)
                if (result > 0) {
                    uiHelper.clearUiListEditText(
                        listOf(
                            binding.amount, binding.description
                        )
                    )
                    setBackgroundDefaultColor(binding.amount)
                    view?.hideKeyboard()
                    message("запись добавлена")
                    control.navigate(R.id.nav_money_moving)
                }
            }
        } else {
            setBackgroundWarningColor(binding.amount)
            message("введите сумму")
        }
    }

    private fun setBackgroundWarningColor(editText: EditText) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            editText.setBackgroundColor(resources.getColor(R.color.warning, null))
        }
    }

    private fun setBackgroundDefaultColor(editText: EditText) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            editText.setBackgroundColor(
                resources.getColor(
                    R.color.design_default_color_background,
                    null
                )
            )
        }
    }

    private fun pressSelectButton(fragment: Int) {
        newMoneyMovingViewModel.saveDataToSP()
        control.navigate(fragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun message(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }
}