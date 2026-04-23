package cz.losoos.calculator

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class NoSoftKeyboardEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    init {
        showSoftInputOnFocus = false
    }
}
