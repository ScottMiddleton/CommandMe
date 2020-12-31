package com.middleton.scott.cmboxing.ui.createworkout

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.middleton.scott.cmboxing.MainActivity
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.ui.base.BaseFragment
import com.middleton.scott.cmboxing.utils.DialogManager
import kotlinx.android.synthetic.main.create_workout_tab_item_layout.view.*
import kotlinx.android.synthetic.main.fragment_create_workout_screen.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class CreateWorkoutScreen : BaseFragment() {
    private val args: CreateWorkoutScreenArgs by navArgs()
    private val viewModel: CreateWorkoutSharedViewModel by viewModel {
        parametersOf(
            args.workoutId,
            args.navigateToCombinations
        )
    }

    var tabUnselected: Drawable? = null
    var tabSelected: Drawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.audioFileBaseDirectory = context?.getExternalFilesDir(null)?.absolutePath + "/"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_workout_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabUnselected = ContextCompat.getDrawable(view.context, R.drawable.shape_circle_faded)
        tabSelected = ContextCompat.getDrawable(view.context, R.drawable.shape_circle_selected)

        val activity = activity as MainActivity
        activity.getCreateWorkoutCancelButton()?.setOnClickListener {
            hideKeyboard()
            viewModel.onCancel()
        }

        activity.getCreateWorkoutSaveButton()?.setOnClickListener {
            hideKeyboard()
            viewModel.validateSaveAttempt()
        }

        subscribeUI()

        setupViewPagerAndTabLayout()
    }

    private fun subscribeUI() {
        viewModel.workoutLD.observe(viewLifecycleOwner, Observer {
            val activity = activity as MainActivity
            if (it.name == "") {
                activity.setCreateWorkoutActionBarTitle(getString(R.string.create_workout))
            } else {
                activity.setCreateWorkoutActionBarTitle(viewModel.workout.name)
            }
        })

        viewModel.dbUpdateLD.observe(viewLifecycleOwner, Observer {
            findNavController().popBackStack()
        })

        viewModel.showCancellationDialogLD.observe(viewLifecycleOwner, Observer {
            if (it) {
                DialogManager.showDialog(
                    requireContext(),
                    R.string.cancel_this_workout,
                    R.string.unsaved_dialog_message,
                    R.string.save_and_exit,
                    { viewModel.validateSaveAttempt() },
                    R.string.yes_cancel,
                    {
                        viewModel.cancelChanges()
                    })
            } else {
                findNavController().popBackStack()
            }
        })

        viewModel.combinationsValidatedLD.observe(viewLifecycleOwner, Observer {
            if (!it) {
                DialogManager.showDialog(
                    context = requireContext(),
                    messageId = R.string.add_commands_dialog_message,
                    negativeBtnTextId = R.string.add_command,
                    negativeBtnClick = {
                        val viewPager =
                            parentFragment?.view?.findViewById(R.id.create_boxing_workout_vp) as ViewPager2
                        viewPager.currentItem = 1
                    })
            }
        })

        viewModel.requiredSummaryFieldLD.observe(viewLifecycleOwner, Observer {
            if (it) {
                val viewPager =
                    parentFragment?.view?.findViewById(R.id.create_boxing_workout_vp) as ViewPager2
                viewPager.currentItem = 0
            }
        })
    }

    private fun setupViewPagerAndTabLayout() {
        val pagerAdapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return 3
            }

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> TabFragmentOne.newInstance()
                    1 -> TabFragmentTwo.newInstance()
                    2 -> TabFragmentThree.newInstance()
                    else -> TabFragmentThree.newInstance()
                }
            }
        }

        create_boxing_workout_vp.adapter = pagerAdapter

        val callback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)

                for (i in 0 until tab_layout.tabCount) {
                    val currentTab = tab_layout.getTabAt(i)?.customView?.textView
                    if (i == position) {
                        currentTab?.background = tabSelected
                    } else {
                        currentTab?.background = tabUnselected
                    }
                }
            }
        }

        create_boxing_workout_vp.registerOnPageChangeCallback(callback)

        TabLayoutMediator(tab_layout, create_boxing_workout_vp) { tab, position ->
            var title = ""
            when (position) {
                0 -> title = "1"
                1 -> title = "2"
                2 -> title = "3"
            }
            tab.text = title
        }.attach()

        for (i in 0 until tab_layout.tabCount) {
            val tab: TabLayout.Tab? = tab_layout.getTabAt(i)
            val customView = LayoutInflater.from(context).inflate(
                R.layout.create_workout_tab_item_layout,
                null
            )
            customView.textView.text = (i + 1).toString()
            tab?.customView = customView
        }

        tab_layout.tabRippleColor = null

        if (args.navigateToCombinations) {
            create_boxing_workout_vp.setCurrentItem(1, false)
        }
    }

    private fun hideKeyboard() {
        val imm: InputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }
}