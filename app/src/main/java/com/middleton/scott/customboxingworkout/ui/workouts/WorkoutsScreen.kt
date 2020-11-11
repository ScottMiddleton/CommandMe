package com.middleton.scott.customboxingworkout.ui.workouts

import android.graphics.Canvas
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.middleton.scott.commandMeBoxing.R
import com.middleton.scott.customboxingworkout.ui.base.BaseFragment
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.android.synthetic.main.fragment_workouts.*
import org.koin.android.ext.android.inject


class WorkoutsScreen : BaseFragment() {
    private val viewModel: WorkoutsViewModel by inject()
    private lateinit var adapter: WorkoutsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = WorkoutsAdapter({ workoutId ->
            findNavController().navigate(
                R.id.createWorkoutScreen,
                bundleOf("workoutId" to workoutId)
            )
        }) { workoutId ->
            findNavController().navigate(
                R.id.workoutScreen,
                bundleOf("workoutId" to workoutId)
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_workouts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleFab()

        workout_rv.adapter = adapter

        val itemTouchHelperCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    val workout = viewModel.deleteWorkout(position)

                    Snackbar.make(
                        workout_rv,
                        getString(R.string.deleted_snackbar, workout.name),
                        Snackbar.LENGTH_LONG
                    ).setAction(getString(R.string.undo)) {
                        viewModel.undoPreviouslyDeletedWorkout()
                    }.show()
                }

                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )

                    RecyclerViewSwipeDecorator.Builder(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                        .addBackgroundColor(
                            ContextCompat.getColor(
                                requireContext(), R.color.red
                            )
                        )
                        .setIconHorizontalMargin(20)
                        .addActionIcon(R.drawable.ic_workout_delete_sweep)
                        .create()
                        .decorate()
                }
            }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(workout_rv)


        subscribeUI()

        add_workouts_btn.setOnClickListener {
            findNavController().navigate(R.id.createWorkoutScreen)
        }
    }

    private fun subscribeUI() {
        viewModel.getWorkoutsWithCombinationsLD().observe(viewLifecycleOwner, Observer {
            if (it.isNullOrEmpty()) {
                empty_list_layout.visibility = VISIBLE
                workout_rv.visibility = GONE
            } else {
                empty_list_layout.visibility = GONE
                workout_rv.visibility = VISIBLE
            }
            adapter.setAdapter(it)
        })
    }

    private fun handleFab() {
        nested_scroll_view.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            when {
                scrollY > oldScrollY -> {
                    fab_tv.visibility = View.GONE
                    val params = CoordinatorLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.BOTTOM or Gravity.END
                    }
                    params.setMargins(40, 40, 40, 40)
                    add_workouts_btn.layoutParams = params
                }
                scrollX == scrollY -> {
                    fab_tv.visibility = View.VISIBLE
                    val params = CoordinatorLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    }
                    params.setMargins(40, 40, 40, 40)
                    add_workouts_btn.layoutParams = params

                }
                else -> {
                    fab_tv.visibility = View.VISIBLE
                    val params = CoordinatorLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    }
                    params.setMargins(40, 40, 40, 40)
                    add_workouts_btn.layoutParams = params
                }
            }

        })
    }
}
