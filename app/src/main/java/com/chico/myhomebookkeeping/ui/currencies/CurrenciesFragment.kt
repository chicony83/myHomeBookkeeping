package com.chico.myhomebookkeeping.ui.currencies

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.chico.myhomebookkeeping.R
import com.chico.myhomebookkeeping.interfaces.OnItemSelectForSelectCallBack
import com.chico.myhomebookkeeping.interfaces.OnItemSelectForChangeCallBack
import com.chico.myhomebookkeeping.interfaces.OnItemViewClickListener
import com.chico.myhomebookkeeping.interfaces.currencies.OnAddNewCurrencyCallBack
import com.chico.myhomebookkeeping.databinding.FragmentCurrenciesBinding
import com.chico.myhomebookkeeping.db.dao.CurrenciesDao
import com.chico.myhomebookkeeping.db.dataBase
import com.chico.myhomebookkeeping.db.entity.Currencies
import com.chico.myhomebookkeeping.helpers.*
import com.chico.myhomebookkeeping.interfaces.currencies.OnChangeCurrencyCallBack
import com.chico.myhomebookkeeping.ui.currencies.dialogs.ChangeCurrencyDialog
import com.chico.myhomebookkeeping.ui.currencies.dialogs.NewCurrencyDialog
import com.chico.myhomebookkeeping.ui.currencies.dialogs.SelectCurrencyDialog
import com.chico.myhomebookkeeping.utils.hideKeyboard
import com.chico.myhomebookkeeping.utils.launchIo
import com.chico.myhomebookkeeping.utils.launchUi

class CurrenciesFragment : Fragment() {
    private lateinit var currenciesViewModel: CurrenciesViewModel
    private var _binding: FragmentCurrenciesBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: CurrenciesDao

    private val uiHelper = UiHelper()
    private lateinit var navControlHelper: NavControlHelper
    private lateinit var control: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        db = dataBase.getDataBase(requireContext()).currenciesDao()

        _binding = FragmentCurrenciesBinding.inflate(inflater, container, false)

        currenciesViewModel = ViewModelProvider(this).get(CurrenciesViewModel::class.java)

        control = activity?.findNavController(R.id.nav_host_fragment)!!

        with(currenciesViewModel) {
            currenciesList.observe(viewLifecycleOwner, {
                binding.currenciesHolder.adapter =
                    CurrenciesAdapter(it, object : OnItemViewClickListener {
                        override fun onClick(selectedId: Int) {
                            showSelectCurrencyDialog(selectedId)
                            Log.i("TAG", "---$selectedId---")
                        }
                    })
            })
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navControlHelper = NavControlHelper(control)

        if (navControlHelper.isPreviousFragment(R.id.nav_money_moving_query)) {
            uiHelper.hideUiElement(binding.showHideAddCurrencyFragmentButton)
            uiHelper.showUiElement(binding.selectAllButton)
        } else if (navControlHelper.isPreviousFragment(R.id.nav_money_moving)) {
            uiHelper.showUiElement(binding.selectAllButton)
        }
        view.hideKeyboard()
        with(binding) {
            selectAllButton.setOnClickListener {
                currenciesViewModel.saveData(navControlHelper, -1)
                navControlHelper.moveToMoneyMovingFragment()
            }
            showHideAddCurrencyFragmentButton.setOnClickListener {
                showNewCurrencyDialog()
            }
        }
    }

    private fun showSelectCurrencyDialog(selectedId: Int) {
        launchIo {
            val currencies: Currencies? = currenciesViewModel.loadSelectedCurrency(selectedId)
            launchUi {
                val dialog = SelectCurrencyDialog(currencies,
                    object : OnItemSelectForChangeCallBack {
                        override fun onSelect(id: Int) {
                            showChangeCurrencyDialog(currencies)
                        }
                    },
                    object : OnItemSelectForSelectCallBack {
                        override fun onSelect(id: Int) {
                            currenciesViewModel.saveData(navControlHelper, id)
                            navControlHelper.moveToPreviousPage()
                        }
                    })
                dialog.show(childFragmentManager, getString(R.string.tag_show_dialog))
            }
        }
    }

    private fun showChangeCurrencyDialog(currency: Currencies?) {
        launchIo {
            val dialog = ChangeCurrencyDialog(currency, object : OnChangeCurrencyCallBack {
                override fun change(id: Int, name: String) {
                    currenciesViewModel.saveChangedCurrency(id, name)
                }
            })
            dialog.show(childFragmentManager, getString(R.string.tag_show_dialog))
        }
    }

    private fun showNewCurrencyDialog() {
        val result = currenciesViewModel.getNamesList()
        launchUi {
            val dialog = NewCurrencyDialog(result, object : OnAddNewCurrencyCallBack {
                override fun add(name: String) {
                    val result = currenciesViewModel.addNewCurrency(Currencies(currencyName = name))
                    if (result > 0) {
                        showMessage(getString(R.string.message_currency_added))
                    }
                    if (result <= 0) {
                        showMessage(getString(R.string.message_currency_not_added))
                    }
                }

                override fun addAndSelect(name: String) {
                    val result = currenciesViewModel.addNewCurrency(Currencies(currencyName = name))
                    if (result > 0) {
                        showMessage(getString(R.string.message_currency_added))
                    }
                    if (result <= 0) {
                        showMessage(getString(R.string.message_currency_not_added))
                    }
                    currenciesViewModel.saveData(navControlHelper, result.toInt())
                    navControlHelper.moveToPreviousPage()
                }
            })

            dialog.show(childFragmentManager, getString(R.string.tag_show_dialog))
        }
    }

    private fun showMessage(s: String) {
        Toast.makeText(context, s, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}