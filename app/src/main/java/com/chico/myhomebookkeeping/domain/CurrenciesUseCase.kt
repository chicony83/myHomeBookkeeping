package com.chico.myhomebookkeeping.domain

import com.chico.myhomebookkeeping.db.dao.CurrenciesDao
import com.chico.myhomebookkeeping.db.entity.Currencies
import com.chico.myhomebookkeeping.ui.currencies.CurrenciesViewModel
import com.chico.myhomebookkeeping.utils.launchForResult
import com.chico.myhomebookkeeping.utils.launchIo
import kotlinx.coroutines.runBlocking

object CurrenciesUseCase {

    suspend fun getOneCurrencyName(db: CurrenciesDao, id: Int): String {
        return launchForResult {
            db.getOneCurrency(id).first().currencyName
        }.toString()
    }

    fun addNewCurrencyRunBlocking(
        db: CurrenciesDao,
        addingCurrency: Currencies,
        currenciesViewModel: CurrenciesViewModel
    ) = runBlocking {
        db.addCurrency(addingCurrency)
        currenciesViewModel.loadCurrencies()
    }
}