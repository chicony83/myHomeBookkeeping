package com.chico.myhomebookkeeping.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chico.myhomebookkeeping.R
import com.chico.myhomebookkeeping.databinding.FragmentCategoriesBinding
import com.chico.myhomebookkeeping.db.dao.CategoryDao
import com.chico.myhomebookkeeping.db.dao.ChildCategoriesDao
import com.chico.myhomebookkeeping.db.dataBase
import com.chico.myhomebookkeeping.db.entity.Categories
import com.chico.myhomebookkeeping.db.entity.ChildCategory
import com.chico.myhomebookkeeping.db.entity.ParentCategory
import com.chico.myhomebookkeeping.domain.entities.NormalizedCategory
import com.chico.myhomebookkeeping.enums.SortingCategories
import com.chico.myhomebookkeeping.helpers.NavControlHelper
import com.chico.myhomebookkeeping.helpers.UiHelper
import com.chico.myhomebookkeeping.interfaces.OnCategoryClickListener
import com.chico.myhomebookkeeping.interfaces.OnItemSelectForChangeCallBack
import com.chico.myhomebookkeeping.interfaces.OnItemSelectForSelectCallBackInt
import com.chico.myhomebookkeeping.interfaces.OnItemViewClickListener
import com.chico.myhomebookkeeping.interfaces.categories.OnAddNewCategoryCallBack
import com.chico.myhomebookkeeping.interfaces.categories.OnChangeCategoryCallBack
import com.chico.myhomebookkeeping.ui.cashAccount.CashAccountViewModel
import com.chico.myhomebookkeeping.ui.categories.dialogs.ChangeCategoryDialog
import com.chico.myhomebookkeeping.ui.categories.dialogs.NewCategoryDialog
import com.chico.myhomebookkeeping.ui.categories.dialogs.SelectCategoryDialog
import com.chico.myhomebookkeeping.utils.*
import java.util.*

class CategoriesFragment : Fragment() {

    private val viewModel: CategoriesViewModel by viewModels(
        ownerProducer = { requireActivity() }
    )
    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: CategoryDao
    private lateinit var navControlHelper: NavControlHelper
    private lateinit var control: NavController
//    private val uiHelper = UiHelper()
    private var skipScroll = false
    private var searchMode = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        db = dataBase.getDataBase(requireContext()).categoryDao()
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)

        control = activity?.findNavController(R.id.nav_host_fragment)!!
        binding.onClear = {
            requireView().hideKeyboard()
            visibleInvisible(binding.searchTil, false)
            searchMode = false
            skipScroll = true
        }

        with(viewModel) {
            sortedByTextOnButton.observe(viewLifecycleOwner) {
                binding.sortingButton.text = it
            }
            categoriesList.observe(viewLifecycleOwner) {
                binding.categoryHolder.adapter =
                    CategoriesAdapter(it,
                        object : OnCategoryClickListener {
                            override fun onShortClick(category: NormalizedCategory) {
                                viewModel.setSelectedCategory(category)
                                findNavController().popBackStack()
                            }

                            override fun onLongClick(category: NormalizedCategory) {
                                showSelectCategoryDialog(category)
                            }
                        })
            }
        }

        binding.categoryHolder.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!skipScroll) {
                    when ((recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()) {
                        0 -> {
                            visibleInvisible(binding.searchTil, searchMode)
                            if (!searchMode) requireView().hideKeyboard()
                        }
                        else -> {
                            visibleInvisible(binding.searchTil, true)
                        }
                    }
                } else {
                    skipScroll = false
                    searchMode = false
                }
            }
        })

        with(binding) {
            searchTil.editText?.doAfterTextChanged { char ->
                searchMode = true
                if (char.toString().isEmpty()) {
                    (categoryHolder.adapter as CategoriesAdapter).updateList(viewModel.categoriesList.value.orEmpty())
                } else {
                    val filteredCategories = viewModel.categoriesList.value.orEmpty().filter {
                        it.nameRes?.let { it1 ->
                            getString(it1).toLowerCase(Locale.getDefault())
                                .contains(char.toString())
                        }?:false|| it.name?.contains(char.toString()) ?: false

                    }
                    (categoryHolder.adapter as CategoriesAdapter).updateList(filteredCategories)
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideBottomNavigation()
        navControlHelper = NavControlHelper(control)

        view.hideKeyboard()
        with(binding) {
            sortingButton.setOnClickListener {
                val popupMenu = PopupMenu(context, sortingButton)
                popupMenu.menuInflater.inflate(
                    R.menu.pop_up_menu_sorting_categories,
                    popupMenu.menu
                )
                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.sort_by_numbers_ASC -> {
//                            Message.log("sort by numbers ASC")
                            sortingCategories(SortingCategories.NumbersByASC.toString())
                        }
                        R.id.sort_by_numbers_DESC -> {
                            sortingCategories(SortingCategories.NumbersByDESC.toString())
//                            Message.log("Sort by numbers DESC")
                        }
                        R.id.sort_by_alphabet_ASC -> {
                            sortingCategories(SortingCategories.AlphabetByASC.toString())
//                            Message.log("Sorting by alphabet ASC")
                        }
                        R.id.sort_by_alphabet_DESC -> {
                            sortingCategories(SortingCategories.AlphabetByDESC.toString())
//                            Message.log("Sorting by alphabet DESC")
                        }
                    }
                    true
                }
                popupMenu.show()
            }
//            showHideAddCategoryFragmentButton.setOnClickListener {
//                showNewCategoryDialog()
//            }
        }
    }

    private fun sortingCategories(sorting: String) {
        with(viewModel) {
            setSortingCategories(sorting)
            reloadCategories()
        }
    }

    private fun showSelectCategoryDialog(normalizedCategory: NormalizedCategory) {
//        launchIo {
//            val category: ChildCategory? = viewModel.loadSelectedCategory(selectedId)
//            launchUi {
//                val dialog = SelectCategoryDialog(category,
//                    object : OnItemSelectForChangeCallBack {
//                        override fun onSelect(id: Int) {
//                            showChangeCategoryDialog(category)
//                        }
//                    },
//                    object : OnItemSelectForSelectCallBackInt {
//                        override fun onSelect(id: Int) {
//                            viewModel.saveData(navControlHelper, id)
//                            navControlHelper.moveToPreviousFragment()
//                        }
//                    })
//                dialog.show(childFragmentManager, getString(R.string.tag_show_dialog))
//            }
//        }
    }

    private fun showChangeCategoryDialog(category: ChildCategory?) {
//        launchIo {
//            val dialog = ChangeCategoryDialog(category, object : OnChangeCategoryCallBack {
////                override fun changeWithoutIcon(id: Int, name: String, isIncome: Boolean) {
////                    categoriesViewModel.saveChangedCategory(id,name,isIncome)
////                }
//
//                override fun changeWithIcon(
//                    id: Long,
//                    nameRes: Int,
//                    parentNameRes: Int,
//                    iconResource: Int
//                ) {
//                    viewModel.saveChangedCategory(
//                        id = id,
//                        nameRes = nameRes,
//                        parentNameRes = parentNameRes,
//                        iconResource = iconResource
//                    )
//                }
//            })
//            dialog.show(childFragmentManager, getString(R.string.tag_show_dialog))
//        }
    }

    private fun showNewCategoryDialog() {
//        val result = viewModel.getNamesList()
//        launchUi {
//            val dialog = NewCategoryDialog(result, object : OnAddNewCategoryCallBack {
//
//                override fun addAndSelect(
//                    isSelect: Boolean,
//                    nameRes: Int,
//                    parentNameRes: Int,
//                    iconResource: Int
//                ) {
//                    val category = ChildCategory(
//
//                        categoryName = name,
//                        isIncome = isIncome,
//                        icon = icon
//                    )
//                    val result: Long = viewModel.addNewCategory(category)
//                    if (isSelect) {
//                        viewModel.saveData(navControlHelper, result.toInt())
//                        navControlHelper.moveToPreviousFragment()
//                    }
//                }
//            })
//            dialog.show(childFragmentManager, getString(R.string.tag_show_dialog))
//        }
    }

    private fun showMessage(s: String) {
        Toast.makeText(context, s, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}