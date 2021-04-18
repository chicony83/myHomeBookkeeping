package com.chico.myhomebookkeeping.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.chico.myhomebookkeeping.R
import com.chico.myhomebookkeeping.`interface`.OnItemViewClickListener
import com.chico.myhomebookkeeping.constants.Constants
import com.chico.myhomebookkeeping.databinding.FragmentCategoriesBinding
import com.chico.myhomebookkeeping.db.dao.CategoryDao
import com.chico.myhomebookkeeping.db.dataBase
import com.chico.myhomebookkeeping.db.entity.Categories
import com.chico.myhomebookkeeping.domain.CategoriesUseCase
import com.chico.myhomebookkeeping.ui.UiHelper

class CategoriesFragment : Fragment() {

    private lateinit var categoriesViewModel: CategoriesViewModel
    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: CategoryDao

    private var selectedCategoryId = 0

    private val argsName: String by lazy { Constants.CATEGORY_KEY }
    private val uiHelper = UiHelper()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        db = dataBase.getDataBase(requireContext()).categoryDao()
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)

        categoriesViewModel = ViewModelProvider(this).get(CategoriesViewModel::class.java)

        categoriesViewModel.categoriesList.observe(viewLifecycleOwner, {
            binding.categoryHolder.adapter =
                CategoriesAdapter(it, object : OnItemViewClickListener {
                    override fun onClick(selectedId: Int) {
                        uiHelper.showHideUIElements(selectedId, binding.layoutConfirmation)
                        selectedCategoryId = selectedId
                    }
                })
        })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            showHideAddCategoryFragment.setOnClickListener {

                if (binding.addNewCategoryFragment.visibility == View.GONE) {
                    binding.addNewCategoryFragment.visibility = View.VISIBLE
                } else binding.addNewCategoryFragment.visibility = View.GONE
            }
            addNewCategoryButton.setOnClickListener {
                if (binding.addNewCategoryFragment.visibility == View.VISIBLE) {
                    if (binding.newCategoryName.text.isNotEmpty()
                        and
                        (binding.newCategoryIncoming.isChecked
                                or
                                binding.newCategorySpending.isChecked
                                )
                    ) {
                        val category = binding.newCategoryName.text.toString()
                        var isIncoming = false
                        var isSpending = false
                        if (binding.newCategoryIncoming.isChecked) isIncoming = true
                        if (binding.newCategorySpending.isChecked) isSpending = true
                        val newCategory = Categories(
                            categoryName = category,
                            isIncome = isIncoming,
                            isSpending = isSpending
                        )
                        CategoriesUseCase.addNewCategoryRunBlocking(db, newCategory,categoriesViewModel)
//                        Toast.makeText(context, "категория добавлена", Toast.LENGTH_SHORT).show()
                        binding.addNewCategoryFragment.visibility = View.GONE
                    }
                }
            }
            selectButton.setOnClickListener {
                if (selectedCategoryId > 0) {
                    val bundle = Bundle()
                    bundle.putInt(argsName, selectedCategoryId)
                    findNavController().navigate(
                        R.id.nav_new_money_moving,
                        bundle
                    )
                }
            }
            cancel.setOnClickListener {
                if (selectedCategoryId > 0) {
                    selectedCategoryId = 0
                    binding.layoutConfirmation.visibility = View.GONE
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}