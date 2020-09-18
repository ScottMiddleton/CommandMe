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
import kotlinx.android.synthetic.main.dialog_save_combination.*

class SaveCombinationDialog(
    private val name: String? = null,
    private val timeToComplete: Int? = null,
    private val onSave: ((name: String, timeToComplete: Int) -> Unit),
    private val onDelete: (() -> Unit)
) : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_save_combination, container)
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // Show soft keyboard automatically and request focus to field
        name_et.requestFocus()
        dialog?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
        )

        name?.let { name_et.setText(it) }
        timeToComplete?.let { time_to_complete_et.setText(it.toString()) }
        setClickListeners()
    }

    private fun setClickListeners() {
        save_btn.setOnClickListener {
            onSave(name_et.text.toString(), time_to_complete_et.text.toString().toInt())
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
                millis = (time_to_complete_et.text.toString().toDouble() * 1000L).toLong()
            }
            NumberPickerSecondsDialog(millis, { newMillis ->
                var secondsText: String = if((newMillis % 1000) == 0L){
                    (newMillis / 1000).toString()
                } else {
                    (newMillis / 1000.0).toString()
                }
                time_to_complete_et.setText(secondsText)
                name_et.clearFocus()
            }, {

            }).show(childFragmentManager, "")
        }
    }
}