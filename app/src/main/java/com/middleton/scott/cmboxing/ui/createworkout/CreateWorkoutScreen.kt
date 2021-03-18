package com.middleton.scott.cmboxing.ui.createworkout

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.ui.base.BaseFragment
import com.middleton.scott.cmboxing.utils.DialogManager
import com.middleton.scott.cmboxing.utils.hideKeyboard
import kotlinx.android.synthetic.main.appbar_create_workout.*
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

    var tabComplete: Drawable? = null
    var tabIncomplete: Drawable? = null

    lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.audioFileBaseDirectory = context?.getExternalFilesDir(null)?.absolutePath + "/"

        // This callback will only be called when MyFragment is at least Started.
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    viewModel.onCancel()
                }
            }

        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_workout_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mContext = view.context

        activity?.findViewById<ImageView>(R.id.close_btn)?.setOnClickListener {
            hideKeyboard()
            viewModel.onCancel()
        }

        tabIncomplete = ContextCompat.getDrawable(view.context, R.drawable.shape_circle_incomplete)
        tabComplete = ContextCompat.getDrawable(view.context, R.drawable.shape_circle_complete)

        setupViewPagerAndTabLayout()

        viewModel.subscribeLD.observe(viewLifecycleOwner, Observer {
            if (it) {
                subscribeUI()
            }
        })
    }

    private fun subscribeUI() {
        viewModel.dbUpdateLD.observe(viewLifecycleOwner, Observer {
            findNavController().popBackStack()
        })

        viewModel.showCancellationDialogLD.observe(viewLifecycleOwner, Observer {
            if (it) {
                DialogManager.showDialog(
                    requireContext(),
                    R.string.cancel_this_workout,
                    R.string.unsaved_dialog_message,
                    R.string.yes_cancel,
                    { viewModel.cancelChanges() },
                    R.string.no,
                    {})
            } else {
                viewModel.cancelChanges()
            }
        })

        viewModel.selectedCommandsLD.observe(viewLifecycleOwner, Observer {
            if (!viewModel.isCancelling) {
                viewModel.validateTabTwo()
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

                val tabLayout = requireActivity().tab_layout

                for (i in 0 until tabLayout.tabCount) {
                    val currentTab = tabLayout.getTabAt(i)?.customView
                    when {
                        i < position -> {
                            currentTab?.textView?.background = tabComplete
                            currentTab?.divider_left?.setBackgroundColor(
                                ContextCompat.getColor(
                                    mContext,
                                    R.color.accent_faded_80
                                )
                            )
                            currentTab?.divider_right?.setBackgroundColor(
                                ContextCompat.getColor(
                                    mContext,
                                    R.color.accent_faded_80
                                )
                            )
                        }
                        i == position -> {
                            currentTab?.textView?.background = tabComplete
                            currentTab?.divider_left?.setBackgroundColor(
                                ContextCompat.getColor(
                                    mContext,
                                    R.color.accent_faded_80
                                )
                            )
                            currentTab?.divider_right?.setBackgroundColor(
                                ContextCompat.getColor(
                                    mContext,
                                    R.color.white_faded_20
                                )
                            )
                        }
                        else -> {
                            currentTab?.textView?.background = tabIncomplete
                            currentTab?.divider_left?.setBackgroundColor(
                                ContextCompat.getColor(
                                    mContext,
                                    R.color.white_faded_20
                                )
                            )
                            currentTab?.divider_right?.setBackgroundColor(
                                ContextCompat.getColor(
                                    mContext,
                                    R.color.white_faded_20
                                )
                            )
                        }
                    }
                }

                when (position) {
                    0 -> {
                        prev_btn.visibility = GONE
                        next_btn.text = getString(R.string.next)
                        next_btn.setOnClickListener {
                            if (viewModel.tabOneValidatedLD.value == true) {
                                val viewPager =
                                    parentFragment?.view?.findViewById(R.id.create_boxing_workout_vp) as ViewPager2
                                viewPager.currentItem = 1
                            } else {
                                viewModel.userHasAttemptedToProceedOne = true
                                viewModel.validateTabOne()
                            }
                        }
                    }
                    1 -> {
                        prev_btn.visibility = VISIBLE
                        prev_btn.setOnClickListener {
                            val viewPager =
                                parentFragment?.view?.findViewById(R.id.create_boxing_workout_vp) as ViewPager2
                            viewPager.currentItem = 0
                        }

                        next_btn.text = getString(R.string.next)
                        next_btn.setOnClickListener {
                            viewModel.userHasAttemptedToProceedTwo = true
                            if (viewModel.tabTwoValidatedLD.value == true) {
                                val viewPager =
                                    parentFragment?.view?.findViewById(R.id.create_boxing_workout_vp) as ViewPager2
                                viewPager.currentItem = 2
                            } else {
                                viewModel.validateTabTwo()
                            }
                        }
                    }
                    2 -> {
                        prev_btn.visibility = VISIBLE
                        prev_btn.setOnClickListener {
                            val viewPager =
                                parentFragment?.view?.findViewById(R.id.create_boxing_workout_vp) as ViewPager2
                            viewPager.currentItem = 1
                        }

                        next_btn.text = getString(R.string.save)
                        next_btn.setOnClickListener {
                            hideKeyboard()

                            if (!viewModel.validateRoundsNotEmpty()) {
                                DialogManager.showDialog(
                                    context = requireContext(),
                                    titleId = R.string.empty_rounds,
                                    messageId = R.string.empty_rounds_dialog_message,
                                    negativeBtnTextId = R.string.ok,
                                    negativeBtnClick = {}
                                )
                            } else {
                                viewModel.upsertWorkout()
                            }
                        }
                    }
                }
            }
        }

        create_boxing_workout_vp.registerOnPageChangeCallback(callback)

        val tabLayout = requireActivity().tab_layout

        TabLayoutMediator(tabLayout, create_boxing_workout_vp) { tab, position ->
            var title = ""
            when (position) {
                0 -> title = 1.toString()
                1 -> title = 2.toString()
                2 -> title = 3.toString()
            }
            tab.text = title
        }.attach()

        // Set up custom view for tabs
        for (i in 0 until tabLayout.tabCount) {
            val tab: TabLayout.Tab? = tabLayout.getTabAt(i)
            val customView = LayoutInflater.from(context).inflate(
                R.layout.create_workout_tab_item_layout,
                null
            )
            customView.textView.text = (i + 1).toString()
            tab?.customView = customView
        }

        // Set touch listeners for tabs
        for (i in 0 until tabLayout.tabCount) {
            val currentTab = tabLayout.getTabAt(i)?.customView?.parent_cl
            when (i) {
                1 -> {
                    currentTab?.setOnClickListener {
                        if (viewModel.tabOneValidatedLD.value == true) {
                            create_boxing_workout_vp.setCurrentItem(1, true)
                        } else {
                            viewModel.userHasAttemptedToProceedOne = true
                            viewModel.validateTabOne()
                        }
                    }
                }
                2 -> {
                    currentTab?.setOnClickListener {
                        if (viewModel.tabTwoValidatedLD.value == true && viewModel.tabOneValidatedLD.value == true) {
                            create_boxing_workout_vp.setCurrentItem(2, true)
                        } else {
                            if (viewModel.tabOneValidatedLD.value == false) {
                                viewModel.userHasAttemptedToProceedOne = true
                                viewModel.validateTabOne()
                            } else if (viewModel.tabTwoValidatedLD.value == false && viewModel.tabOneValidatedLD.value == true) {
                                viewModel.userHasAttemptedToProceedTwo = true
                                if (create_boxing_workout_vp.currentItem == 0) {
                                    create_boxing_workout_vp.setCurrentItem(1, true)
                                }
                                viewModel.validateTabTwo()
                            }
                        }
                    }
                }
            }

            create_boxing_workout_vp.isUserInputEnabled = false
        }

        tabLayout.getTabAt(0)?.customView?.divider_left?.visibility = GONE
        tabLayout.getTabAt(2)?.customView?.divider_right?.visibility = GONE
        tabLayout.tabRippleColor = null

        if (args.navigateToCombinations) {
            viewModel.tabOneValidatedLD.value = true
            create_boxing_workout_vp.setCurrentItem(1, false)
        }
    }
}