package com.ayush.expensemanager.ui.expense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ayush.expensemanager.databinding.FragmentAddParentBinding
import com.ayush.expensemanager.ui.category.CategoryFragment
import com.google.android.material.tabs.TabLayoutMediator

class AddParentFragment : Fragment() {

    private var _binding: FragmentAddParentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddParentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = AddPagerAdapter(this)
        binding.viewPager.adapter = adapter
        
        // Disable internal swiping to avoid clashing with the global MainActivity swipe listener
        binding.viewPager.isUserInputEnabled = false

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Expense"
                1 -> "Categories"
                else -> ""
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class AddPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> AddExpenseFragment()
                1 -> CategoryFragment()
                else -> throw IllegalArgumentException("Invalid position")
            }
        }
    }
}
