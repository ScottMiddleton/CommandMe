import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import com.middleton.scott.commandMeBoxing.R
import com.middleton.scott.customboxingworkout.datasource.local.model.Combination
import kotlinx.android.synthetic.main.dialog_save_combination.*

class SaveCombinationDialog(
    private val isEditMode: Boolean,
    private val combination: Combination,
    private val onSave: ((Combination) -> Unit),
    private val onDelete: (() -> Unit)
) : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_save_combination, container)
    }

    private var timeToCompleteMillis: Long = 0L

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // Show soft keyboard automatically and request focus to field
        name_et.requestFocus()
        dialog?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
        )

        timeToCompleteMillis = combination.timeToCompleteMillis

        if (isEditMode) {
            name_et.setText(combination.name)
            time_to_complete_et.setText(getSecondsTextFromMillis(timeToCompleteMillis))
        }
        setClickListeners()
    }

    private fun setClickListeners() {
        save_btn.setOnClickListener {
                combination.name = name_et.text.toString()
                combination.timeToCompleteMillis = timeToCompleteMillis
                onSave(combination)
            dismiss()
        }

        delete_btn.setOnClickListener {
            onDelete()
            dismiss()
        }

        time_to_complete_et.setOnClickListener {
            val imm: InputMethodManager =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)

            var millis = 0L
            if (!time_to_complete_et.text.isNullOrBlank()) {
                millis = timeToCompleteMillis
            }

            NumberPickerSecondsDialog(millis, { newMillis ->
                timeToCompleteMillis = newMillis

                time_to_complete_et.setText(getSecondsTextFromMillis(newMillis))
                name_et.clearFocus()
            }, {

            }).show(childFragmentManager, "")
        }
    }

    private fun getSecondsTextFromMillis(millis: Long): String{
        return if ((millis % 1000) == 0L) {
            (millis / 1000).toString()
        } else {
            (millis / 1000.0).toString()
        }
    }
}