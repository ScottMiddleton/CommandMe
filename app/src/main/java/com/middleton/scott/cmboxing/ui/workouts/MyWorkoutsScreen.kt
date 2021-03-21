package com.middleton.scott.cmboxing.ui.workouts

import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.middleton.scott.cmboxing.R
import com.middleton.scott.cmboxing.ui.base.BaseFragment
import com.middleton.scott.cmboxing.ui.createworkout.WorkoutType
import com.middleton.scott.cmboxing.utils.DialogManager
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.android.synthetic.main.fragment_my_workouts.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MyWorkoutsScreen : BaseFragment() {
    private val viewModel: WorkoutsViewModel by viewModel()
    private lateinit var adapter: WorkoutsAdapter
    var undoSnackbarVisible = false
    var workoutsEmpty = true

    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = WorkoutsAdapter({ workoutId ->
            val action = MyWorkoutsScreenDirections.actionMyWorkoutsScreenToCreateWorkoutScreen(
                workoutId, false
            )
            findNavController().navigate(
                action
            )
        },
            { workoutWithCombinations, workoutType ->
                viewModel.previouslyClickedWorkout = workoutWithCombinations.workout!!

                if (workoutWithCombinations.commands.isEmpty()) {
                    DialogManager.showDialog(
                        context = requireContext(),
                        messageId = R.string.no_command_dialog_message,
                        positiveBtnTextId = R.string.exit,
                        positiveBtnClick = {
                        },
                        negativeBtnTextId = R.string.ok,
                        negativeBtnClick = {
                            val action = workoutWithCombinations.workout?.id?.let {
                                MyWorkoutsScreenDirections.actionMyWorkoutsScreenToCreateWorkoutScreen(
                                    it, false
                                )
                            }
                            action?.let {
                                findNavController().navigate(
                                    it
                                )
                            }
                        }
                    )
                } else {
                    viewModel.validateWorkout(workoutWithCombinations.workout)
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_workouts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.workoutValidatedLD.observe(viewLifecycleOwner, Observer {
            if (it == WORKOUT_VALID_TRUE) {
                when (viewModel.previouslyClickedWorkout.workout_type) {
                    WorkoutType.STRUCTURED -> {
                        val action =
                            MyWorkoutsScreenDirections.actionMyWorkoutsScreenToStructuredWorkoutScreen(
                                viewModel.previouslyClickedWorkout.id
                            )

                        findNavController().navigate(
                            action
                        )
                    }
                    WorkoutType.RANDOM -> {
                        val action =
                            MyWorkoutsScreenDirections.actionMyWorkoutsScreenToRandomWorkoutScreen(
                                viewModel.previouslyClickedWorkout.id
                            )

                        findNavController().navigate(
                            action
                        )
                    }
                }
            } else if (it == WORKOUT_VALID_FALSE) {
                DialogManager.showDialog(
                    context = requireContext(),
                    messageId = R.string.empty_rounds_on_begin_workout_dialog_message,
                    positiveBtnTextId = R.string.exit,
                    positiveBtnClick = {},
                    negativeBtnTextId = R.string.ok,
                    negativeBtnClick = {
                        val action = MyWorkoutsScreenDirections.actionMyWorkoutsScreenToCreateWorkoutScreen(
                            viewModel.previouslyClickedWorkout.id, false
                            )

                        findNavController().navigate(
                            action
                        )
                    }
                )
            }
        })

        handleFab()

        workout_rv.adapter = adapter

        undo_btn.setOnClickListener {
            undo_btn.visibility = GONE
            undo_tv.visibility = GONE
            viewModel.undoPreviouslyDeletedWorkout()
        }

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
                    undo_btn.visibility = VISIBLE
                    undo_tv.visibility = VISIBLE
                    undoSnackbarVisible = true

                    val position = viewHolder.adapterPosition
                    val workout = viewModel.deleteWorkout(position)

                    undo_tv.text = getString(R.string.deleted_snackbar, workout.name)

                    handler.removeCallbacksAndMessages(null)
                    handler.postDelayed({
                        undo_btn?.visibility = GONE
                        undo_tv?.visibility = GONE
                        undoSnackbarVisible = false
                        if (workoutsEmpty) {
                            empty_list_layout?.visibility = VISIBLE
                        }
                    }, 3000)
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
                        .addActionIcon(R.drawable.ic_delete_sweep_large)
                        .create()
                        .decorate()
                }
            }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(workout_rv)


        subscribeUI()

        add_workouts_btn.setOnClickListener {
            val action =
                MyWorkoutsScreenDirections.addWorkoutActionWorkoutsScreenToCreateWorkoutScreen(
                    -1L, false
                )
            findNavController().navigate(
                action
            )
        }
    }

    private fun subscribeUI() {
        viewModel.getWorkoutsWithCommandsLD().observe(viewLifecycleOwner, Observer {
            if (it.isNullOrEmpty()) {
                workoutsEmpty = true
                if (!undoSnackbarVisible) {
                    empty_list_layout.visibility = VISIBLE
                }
                workout_rv.visibility = GONE
            } else {
                workoutsEmpty = false
                empty_list_layout.visibility = GONE
                workout_rv.visibility = VISIBLE
            }
            adapter.setAdapter(it)
        })
    }

    private fun handleFab() {
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        }
        params.setMargins(80, 80, 80, 80)
        add_workouts_btn.layoutParams = params

        nested_scroll_view.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            when {
                scrollY > oldScrollY -> {
                    fab_tv.visibility = View.GONE
                    val params1 = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.BOTTOM or Gravity.END
                    }
                    params1.setMargins(80, 80, 120, 80)
                    add_workouts_btn.layoutParams = params1
                }
                scrollX == scrollY -> {
                    fab_tv.visibility = View.VISIBLE
                    val params2 = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    }
                    params2.setMargins(80, 80, 80, 80)
                    add_workouts_btn.layoutParams = params2

                }
                else -> {
                    fab_tv.visibility = View.VISIBLE
                    val params3 = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    }
                    params3.setMargins(80, 80, 80, 80)
                    add_workouts_btn.layoutParams = params3
                }
            }

        })
    }

    override fun onPause() {
        super.onPause()
        viewModel.workoutValidatedLD.value = WORKOUT_NOT_YET_VALIDATED
    }
}
