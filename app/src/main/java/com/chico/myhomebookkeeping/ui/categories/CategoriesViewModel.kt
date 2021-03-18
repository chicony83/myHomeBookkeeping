package com.chico.myhomebookkeeping.ui.categories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.chico.myhomebookkeeping.db.dao.CategoryDao
import com.chico.myhomebookkeeping.db.entity.Category
import com.chico.myhomebookkeeping.db.dataBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CategoriesViewModel(
    val app: Application
) : AndroidViewModel(app) {

    private val db: CategoryDao = dataBase.getDataBase(app.applicationContext).incomeDao()

    init {
        loadCategories()
    }

    private val _incomeCategoryList = MutableLiveData<List<Category>>()
    val categoryCategoryList: LiveData<List<Category>>
        get() = _incomeCategoryList

    fun loadCategories() {
        CoroutineScope(Dispatchers.IO).launch {
            _incomeCategoryList.postValue(db.getAllIncomeMoneyCategory())
        }
    }
}