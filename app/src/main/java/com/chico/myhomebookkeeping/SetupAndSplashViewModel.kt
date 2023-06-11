package com.chico.myhomebookkeeping

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.chico.myhomebookkeeping.db.dao.*
import com.chico.myhomebookkeeping.db.dataBase
import com.chico.myhomebookkeeping.db.entity.*
import com.chico.myhomebookkeeping.domain.CategoriesUseCase
import com.chico.myhomebookkeeping.domain.FastPaymentsUseCase
import com.chico.myhomebookkeeping.domain.IconCategoriesUseCase
import com.chico.myhomebookkeeping.domain.IconResourcesUseCase
import com.chico.myhomebookkeeping.enums.ChildCategoriesEnum
import com.chico.myhomebookkeeping.enums.ParentCategoriesEnum
import com.chico.myhomebookkeeping.enums.icon.names.CashAccountIconNames
import com.chico.myhomebookkeeping.enums.icon.names.CategoryIconNames
import com.chico.myhomebookkeeping.helpers.Message
import com.chico.myhomebookkeeping.helpers.UiHelper
import com.chico.myhomebookkeeping.icons.AddIconCategories
import com.chico.myhomebookkeeping.icons.AddIcons
import com.chico.myhomebookkeeping.obj.Constants
import com.chico.myhomebookkeeping.sp.GetSP
import com.chico.myhomebookkeeping.sp.SetSP
import com.chico.myhomebookkeeping.ui.firstLaunch.SelectedItemOfImageAndCheckBox
import com.chico.myhomebookkeeping.utils.launchIo
import com.chico.myhomebookkeeping.utils.launchUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class SetupAndSplashViewModel(
    val app: Application
) : AndroidViewModel(app) {

    private val argsIsFirstLaunch = Constants.IS_FIRST_LAUNCH

    private val dbCashAccount: CashAccountDao =
        dataBase.getDataBase(app.applicationContext).cashAccountDao()
    private val dbCategories: CategoryDao =
        dataBase.getDataBase(app.applicationContext).categoryDao()
    private val parentCategoryDao: ParentCategoriesDao =
        dataBase.getDataBase(app.applicationContext).parentCategoriesDao()
    private val childCategoryDao: ChildCategoriesDao =
        dataBase.getDataBase(app.applicationContext).childCategoriesDao()
    private val dbFastPayments: FastPaymentsDao =
        dataBase.getDataBase(app.applicationContext).fastPaymentsDao()
    private val dbIconCategories: IconCategoryDao =
        dataBase.getDataBase(app.applicationContext).iconCategoryDao()
    private val dbIconResources: IconResourcesDao =
        dataBase.getDataBase(app.applicationContext).iconResourcesDao()

    private val _cardCashAccountItem = MutableLiveData<Int>()
    val cardCashAccountItem: LiveData<Int> get() = _cardCashAccountItem

    private val _cashCashAccountItem = MutableLiveData<Int>()
    val cashCashAccountItem: LiveData<Int> get() = _cashCashAccountItem

    private val _salaryCategoryItem = MutableLiveData<Int>()
    val salaryCategoryItem: LiveData<Int> get() = _salaryCategoryItem

    private val _productsCategoryItem = MutableLiveData<Int>()
    val productsCategoryItem: LiveData<Int> get() = _productsCategoryItem

    private val _fuelForCarCategoryItem = MutableLiveData<Int>()
    val fuelForCarCategoryItem: LiveData<Int> get() = _fuelForCarCategoryItem

    private val _cellularCommunicationCategoryItem = MutableLiveData<Int>()
    val cellularCommunicationCategoryItem: LiveData<Int> get() = _cellularCommunicationCategoryItem

    private val _creditsCategoryItem = MutableLiveData<Int>()
    val creditsCategoryItem: LiveData<Int> get() = _creditsCategoryItem

    private val _medicinesCategoryItem = MutableLiveData<Int>()
    val medicinesCategoryItem: LiveData<Int> get() = _medicinesCategoryItem

    private val _publicTransportCategoryItem = MutableLiveData<Int>()
    val publicTransportCategoryItem: LiveData<Int> get() = _publicTransportCategoryItem

    private val spName = Constants.SP_NAME
    private val sharedPreferences: SharedPreferences =
        app.getSharedPreferences(spName, Context.MODE_PRIVATE)
    private val spEditor = sharedPreferences.edit()
    private val setSP = SetSP(spEditor)
    private var getSP = GetSP(sharedPreferences)
    private val uiHelper = UiHelper()

    private var listIconResource = listOf<IconsResource>()

    @SuppressLint("NewApi")
    private val addIcons = AddIcons(
        dbIconResources = dbIconResources,
        resources = app.resources,
        appPackageName = app.opPackageName
    )

    fun checkIsFirstLaunch(): Boolean {
        return getSP.getBooleanElseReturnTrue(Constants.IS_FIRST_LAUNCH)
    }

   suspend fun addFirstLaunchAccounts(
        listImageAndCheckBoxes: List<SelectedItemOfImageAndCheckBox>,
    ) {
        addCashAccounts(listImageAndCheckBoxes)

//        val resultAddedIncomeCategories =
//            async(Dispatchers.IO) { addIncomeCategories(listIncomeCategories) }
//        val resultAddSpendingCategories =
//            async(Dispatchers.IO) { addSpendingCategories(listSpendingCategories) }

//        val sizeCategoriesList: Int = listIncomeCategories.size + listSpendingCategories.size

//        launchIo {
//            while (getCategoriesList().size < sizeCategoriesList) {
//                delay(100)
//                addFreeFastPayments()
//            }
//        }
    }

    suspend fun populateParentCategories() {
        val categories = ParentCategoriesEnum.values().map {
            ParentCategory(
                nameRes = it.nameRes,
                isIncome = it == ParentCategoriesEnum.INCOME,
                iconRes = null
            )
        }
        parentCategoryDao.addParentCategories(categories)
    }

    suspend fun populateChildCategories() {
        val categories = ChildCategoriesEnum.values().map {
            ChildCategory(
                nameRes = it.nameRes,
                parentNameRes = it.parentCategory.nameRes,
                iconRes = null
            )
        }
        childCategoryDao.addChildCategories(categories)
    }

    suspend fun populateFreeFastPayments(ctx:Context) {
        ParentCategoriesEnum.values().forEach { parent->
            FastPaymentsUseCase.addNewFastPayment(
                db = dbFastPayments,
                FastPayments(
                    icon = null,
                    description = null,
                    amount = null,
                    cashAccountId = 1,
                    categoryId = parent.categoryId,
                    nameFastPayment = ctx.getString(parent.nameRes),
                    currencyId = 1,
                    rating = 0,
                    childCategories = ChildCategoriesEnum.values().map {child->
                        ChildCategory(
                            nameRes = child.nameRes,
                            parentNameRes = child.parentCategory.nameRes,
                            iconRes = null
                        )
                    }.filter { it.parentNameRes==parent.nameRes }
                )
            )
        }
    }

    private suspend fun getCategoriesList(
    ): List<Categories> {
        return CategoriesUseCase.getAllCategoriesSortIdAsc(dbCategories)
    }

    private fun addSpendingCategories(listSpendingCategories: List<SelectedItemOfImageAndCheckBox>): Long {
        var result: Long = 0
        launchIo {
            for (i in listSpendingCategories.indices) {
                result += addCategory(listSpendingCategories[i], false)
            }
        }
        return result
    }

    private fun addIncomeCategories(listIncomeCategories: List<SelectedItemOfImageAndCheckBox>): Long {
        var result: Long = 0
        launchIo {
            for (i in listIncomeCategories.indices) {
                result += addCategory(listIncomeCategories[i], true)
            }
        }
        return result
    }

    private suspend fun addCategory(item: SelectedItemOfImageAndCheckBox, isIncome: Boolean): Long {
        return dbCategories.addCategory(
            Categories(
                item.checkBox.text.toString(), isIncome, item.img
            )
        )
    }

    private suspend fun addCashAccounts(listImageAndCheckBoxes: List<SelectedItemOfImageAndCheckBox>): Boolean {
        for (i in listImageAndCheckBoxes.indices) {
            if (uiHelper.isCheckedCheckBox(listImageAndCheckBoxes[i].checkBox)) {
                addCashAccount(listImageAndCheckBoxes[i])
            }
        }
        return true
    }

    private suspend fun addCashAccount(item: SelectedItemOfImageAndCheckBox) {
        val cashAccount = CashAccount(
            accountName = item.checkBox.text.toString(),
            bankAccountNumber = "",
            isCashAccountDefault = false,
            icon = item.img
        )
        dbCashAccount.addCashAccount(cashAccount)
    }

    fun addIconCategories() {
        launchIo {
            AddIconCategories.add(dbIconCategories)
        }
    }

    suspend fun addIconsResources() {
        launchIo {
            var iconCategoriesList = listOf<IconCategory>()
            while (iconCategoriesList.size < 3) {
                delay(100)
//                Message.log("--- get icon categories")
                iconCategoriesList = IconCategoriesUseCase.getAllIconCategories(dbIconCategories)
//                Message.log("--- size of Icon Categories ${iconCategories.size} ---")
            }
            addIcons.addIconResources(iconCategoriesList)
        }
    }

    fun updateValues() {
        Message.log("update value")
        launchIo {
            listIconResource = getListOfIconResources()

            Message.log("listOfIconResources size = ${listIconResource.size}")

            if (listIconResource.isEmpty()) {
                listIconResource = getListOfIconResources()
            }
            updateValuesOfCashAccounts()
            updateValuesOfCategories()
        }
    }

    private suspend fun getListOfIconResources(): List<IconsResource> {
        delay(1000)
        return IconResourcesUseCase.getIconsList(dbIconResources)
    }

    private fun updateValuesOfCategories() {

        _salaryCategoryItem.postValue(
            getIconResource(CategoryIconNames.Wallet.name)
        )
        _productsCategoryItem.postValue(
            getIconResource(CategoryIconNames.ShoppingCart.name)
        )

        _fuelForCarCategoryItem.postValue(
            getIconResource(CategoryIconNames.GasStation.name)
        )
        _cellularCommunicationCategoryItem.postValue(
            getIconResource(CategoryIconNames.PhoneAndroid.name)
        )
        _creditsCategoryItem.postValue(
            getIconResource(CategoryIconNames.Bank.name)
        )
        _medicinesCategoryItem.postValue(
            getIconResource(CategoryIconNames.Medical.name)
        )
        _publicTransportCategoryItem.postValue(
            getIconResource(CategoryIconNames.Bus.name)
        )
    }

    private fun updateValuesOfCashAccounts() {
        launchUi {
            _cardCashAccountItem.postValue(
                getIconResource(CashAccountIconNames.Card.name)
            )
            _cashCashAccountItem.postValue(
                getIconResource(CashAccountIconNames.Cash.name)
            )
        }
    }

    private fun getIconResource(name: String) =
        listIconResource.find {
            it.iconName == name
        }?.iconResources
}