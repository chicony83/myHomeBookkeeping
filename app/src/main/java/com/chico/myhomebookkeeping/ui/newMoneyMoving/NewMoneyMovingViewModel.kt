package com.chico.myhomebookkeeping.ui.newMoneyMoving

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.chico.myhomebookkeeping.checks.ViewModelCheck
import com.chico.myhomebookkeeping.constants.Constants
import com.chico.myhomebookkeeping.db.dao.CashAccountDao
import com.chico.myhomebookkeeping.db.dao.CategoryDao
import com.chico.myhomebookkeeping.db.dao.CurrenciesDao
import com.chico.myhomebookkeeping.db.dao.MoneyMovementDao
import com.chico.myhomebookkeeping.db.dataBase
import com.chico.myhomebookkeeping.db.entity.CashAccount
import com.chico.myhomebookkeeping.db.entity.Categories
import com.chico.myhomebookkeeping.db.entity.Currencies
import com.chico.myhomebookkeeping.db.entity.MoneyMovement
import com.chico.myhomebookkeeping.domain.CashAccountsUseCase
import com.chico.myhomebookkeeping.domain.CategoriesUseCase
import com.chico.myhomebookkeeping.domain.CurrenciesUseCase
import com.chico.myhomebookkeeping.domain.NewMoneyMovingUseCase
import com.chico.myhomebookkeeping.utils.launchIo

class NewMoneyMovingViewModel(
    val app: Application,
) : AndroidViewModel(app) {
    private val argsCashAccountKey = Constants.FOR_SELECT_CASH_ACCOUNT_KEY
    private val argsCurrencyKey = Constants.FOR_SELECT_CURRENCY_KEY
    private val argsCategoryKey = Constants.FOR_SELECT_CATEGORY_KEY
    private val spName = Constants.SP_NAME

    private val dbCashAccount: CashAccountDao =
        dataBase.getDataBase(app.applicationContext).cashAccountDao()
    private val dbCurrencies: CurrenciesDao =
        dataBase.getDataBase(app.applicationContext).currenciesDao()
    private val dbCategory: CategoryDao =
        dataBase.getDataBase(app.applicationContext).categoryDao()

    private val dbMoneyMovement: MoneyMovementDao =
        dataBase.getDataBase(app.applicationContext).moneyMovementDao()
    private val sharedPreferences: SharedPreferences =
        app.getSharedPreferences(spName, MODE_PRIVATE)

    private val spEditor = sharedPreferences.edit()

    private val viewModelCheck = ViewModelCheck(sharedPreferences)

    private val _selectedCurrency = MutableLiveData<Currencies>()
    val selectedCurrency: LiveData<Currencies>
        get() = _selectedCurrency

    private val _selectedCashAccount = MutableLiveData<CashAccount>()
    val selectedCashAccount: LiveData<CashAccount>
        get() = _selectedCashAccount

    private val _selectedCategory = MutableLiveData<Categories>()
    val selectedCategory: LiveData<Categories>
        get() = _selectedCategory

    var cashAccountSP = -1
    var currencySP = -1
    var categorySP = -1

    fun checkArguments() {
        getSharedPreferencesArgs()
        setValuesViewModel()
    }

    private fun getSharedPreferencesArgs() {
        cashAccountSP = viewModelCheck.getValueSP(argsCashAccountKey)
        currencySP = viewModelCheck.getValueSP(argsCurrencyKey)
        categorySP = viewModelCheck.getValueSP(argsCategoryKey)
    }

    private fun setValuesViewModel() {
        launchIo {
            if (viewModelCheck.isPositiveValue(cashAccountSP)) postCashAccount(cashAccountSP)
        }
        launchIo {
            if (viewModelCheck.isPositiveValue(currencySP)) postCurrency(currencySP)
        }
        launchIo {
            if (viewModelCheck.isPositiveValue(categorySP)) postCategory(categorySP)
        }
    }

    private suspend fun postCategory(idNum: Int) {
        _selectedCategory.postValue(
            CategoriesUseCase.getOneCategory(dbCategory, idNum)
        )
    }

    private suspend fun postCurrency(idNum: Int) {
        _selectedCurrency.postValue(
            CurrenciesUseCase.getOneCurrency(dbCurrencies, idNum)
        )
    }

    private suspend fun postCashAccount(idNum: Int) {
        _selectedCashAccount.postValue(
            CashAccountsUseCase.getOneCashAccount(dbCashAccount, idNum)
        )
    }

    fun saveDataForAdding() {
        spEditor.putInt(argsCurrencyKey, _selectedCurrency.value?.currencyId ?: -1)
        spEditor.putInt(argsCashAccountKey, _selectedCashAccount.value?.cashAccountId ?: -1)
        spEditor.putInt(argsCategoryKey, _selectedCategory.value?.categoriesId ?: -1)

        spEditor.commit()
    }

    suspend fun addNewMoneyMoving(
        dataTime: Long,
        amount: Double,
        description: String
    ): Long {
        val cashAccountValue: Int = _selectedCashAccount.value?.cashAccountId ?: 0
        val categoryValue: Int = _selectedCategory.value?.categoriesId ?: 0
        val currencyValue: Int = _selectedCurrency.value?.currencyId ?: 0
        val moneyMovement = MoneyMovement(
            timeStamp = dataTime,
            amount = amount,
            cashAccount = cashAccountValue,
            category = categoryValue,
            currency = currencyValue,
            description = description
        )
        return NewMoneyMovingUseCase.addInDataBase(dbMoneyMovement, moneyMovement)
    }

    fun saveDataForShowMovement() {

    }
}