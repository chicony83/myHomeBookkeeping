package com.chico.myhomebookkeeping.ui.MoneyMoving

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.chico.myhomebookkeeping.R
import com.chico.myhomebookkeeping.databinding.FragmentMoneyMovingBinding
import com.chico.myhomebookkeeping.db.dao.MoneyMovementDao
import com.chico.myhomebookkeeping.db.dataBase
import com.chico.myhomebookkeeping.utils.hideKeyboard

class MoneyMovingFragment : Fragment() {

    private lateinit var db: MoneyMovementDao
    private lateinit var control:NavController

    private lateinit var moneyMovingViewModel: MoneyMovingViewModel
    private var _binding: FragmentMoneyMovingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        db = dataBase.getDataBase(requireContext()).moneyMovementDao()
        _binding = FragmentMoneyMovingBinding.inflate(inflater, container, false)

        moneyMovingViewModel =
            ViewModelProvider(this).get(MoneyMovingViewModel::class.java)
        moneyMovingViewModel.moneyMovementList.observe(viewLifecycleOwner, {
            binding.moneyMovingHolder.adapter = MoneyMovingAdapter(it)
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.hideKeyboard()
        control = activity?.findNavController(R.id.nav_host_fragment)!!
        moneyMovingViewModel.loadMoneyMovement()

        with(binding){
            selectCurrency.setOnClickListener {
                pressSelectButton(R.id.nav_currencies)
            }
        }
    }

    private fun pressSelectButton(nav: Int) {
        control.navigate(nav)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}