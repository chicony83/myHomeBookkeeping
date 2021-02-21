package com.chico.myhomebookkeeping.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.chico.myhomebookkeeping.databinding.FragmentCategoriesBinding
import com.chico.myhomebookkeeping.db.IncomeCategoryDB
import com.chico.myhomebookkeeping.db.dao.IncomeDao
import com.chico.myhomebookkeeping.db.entity.Income
import com.chico.myhomebookkeeping.db.incomeCategoryDB
import com.chico.myhomebookkeeping.ui.alertdialog.AddCategoryFragment
import com.chico.myhomebookkeeping.utils.launchForResult
import com.chico.myhomebookkeeping.utils.launchIo
import com.chico.myhomebookkeeping.utils.launchUi

class CategoriesFragment : Fragment() {

    private lateinit var categoriesViewModel: CategoriesViewModel
    private lateinit var addIncomeCategoryButton: Button
    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    private lateinit var editText: EditText



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        val view = binding.root
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        categoriesViewModel = ViewModelProvider(this).get(CategoriesViewModel::class.java)
        categoriesViewModel.text.observe(viewLifecycleOwner, Observer {
            binding.textCategories.text = it
        })
        var db:IncomeDao = incomeCategoryDB.getCategoryDB(requireContext()).incomeDao()
        launchIo {
            launchForResult {
                val result:List<Income> = db.getAllIncomeMoneyCategory()

                launchUi {
                    binding.textCategories.text = result.toString()
                }
            }
        }


        binding.addIncomeCategory.setOnClickListener {
            val addCategoryFragment = AddCategoryFragment()
            val manager = childFragmentManager

           addCategoryFragment.show(manager,"add category")
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}