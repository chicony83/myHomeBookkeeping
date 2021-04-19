package com.chico.myhomebookkeeping.domain

import com.chico.myhomebookkeeping.db.dao.CurrenciesDao
import com.chico.myhomebookkeeping.db.entity.Currencies
import com.chico.myhomebookkeeping.ui.currencies.CurrenciesViewModel
import com.chico.myhomebookkeeping.utils.launchForResult
import com.chico.myhomebookkeeping.utils.launchIo
import kotlinx.coroutines.runBlocking

object CurrenciesUseCase {

    suspend fun getOneCurrency(db: CurrenciesDao, id: Int): List<Currencies>? {
        return launchForResult {
            db.getOneCurrency(id)
        }
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