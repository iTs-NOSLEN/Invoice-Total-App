
package com.example.invoicetotal

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.NumberFormat
import android.widget.SeekBar
import android.widget.TextView.OnEditorActionListener
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.preference.PreferenceManager


class MainActivity : AppCompatActivity(), OnEditorActionListener, OnSeekBarChangeListener {
    private lateinit var subTotalAmountEditText: EditText
    private lateinit var percentTextView: TextView
    private lateinit var discountTextView: TextView
    private lateinit var totalTextView: TextView
    private lateinit var percentSeekBar: SeekBar

    private val DISCOUNT_ONE = 0
    private val DISCOUNT_TWO = 1
    private val DISCOUNT_THREE = 2
    private val DISCOUNT_FOUR = 3

    private lateinit var savedValues: SharedPreferences

    private var subTotalAmountString: String = ""
    private lateinit var aboutLabel: String
    private lateinit var aboutDescription: String

    private var rememberDiscountPercent = true

    private lateinit var discountLabels: Array<String>

    private var discountPercent =0.10f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        aboutLabel = getString(R.string.about_label)
        aboutDescription = getString(R.string.about_description)

        percentTextView = findViewById(R.id.percentTextView)

        discountLabels = resources.getStringArray(R.array.default_discount_keys)

        savedValues = PreferenceManager.getDefaultSharedPreferences(this)
        val defaultDiscountKey = savedValues.getString("pref_default_discount", "1") // Default value "1"

        // Find the index of the selected default discount key in the array
        val defaultDiscountIndex = resources.getStringArray(R.array.default_discount_values).indexOf(defaultDiscountKey)

        subTotalAmountEditText = findViewById(R.id.subTotalAmountEditText)
        discountTextView = findViewById(R.id.discountTextView)
        totalTextView = findViewById(R.id.totalTextView)

        percentSeekBar = findViewById(R.id.percentSeekBar)

        percentSeekBar.setOnSeekBarChangeListener(this)

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        savedValues = PreferenceManager.getDefaultSharedPreferences(this)
    }

    private fun calculateAndDisplay() {
        subTotalAmountString = subTotalAmountEditText.text.toString()
        val subTotalAmount =
            if (subTotalAmountString == "") 0f else subTotalAmountString.toFloat()

        val discountAmount = subTotalAmount * discountPercent
        val totalAmount: Float = subTotalAmount - discountAmount

        val percent = NumberFormat.getPercentInstance()
        percentTextView.text = percent.format(discountPercent)

        val currency = NumberFormat.getCurrencyInstance()
        discountTextView.text = currency.format(discountAmount)
        totalTextView.text = currency.format(totalAmount)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.invoice_menu, menu)
        return true
    }
    private fun setDefaultDiscountLabel(index: Int) {

        if ( index === DISCOUNT_ONE) {
            percentSeekBar.progress = 5
            percentSeekBar.min = 0
            percentSeekBar.max = 100
            percentTextView.text = "5%"
        } else if ( index === DISCOUNT_TWO) {
            percentSeekBar.progress = 10
            percentSeekBar.min = 0
            percentSeekBar.max = 100
            percentTextView.text = "10%"
        } else if (index === DISCOUNT_THREE) {
            percentSeekBar.progress = 15
            percentSeekBar.min = 0
            percentSeekBar.max = 100
            percentTextView.text = "15%"
        }else if (index === DISCOUNT_FOUR) {
            percentSeekBar.progress = 20
            percentSeekBar.min = 0
            percentSeekBar.max = 100
            percentTextView.text = "20%"
        }
    }

    // Called when the seek bar's value changes.
    override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
        percentTextView.text = "$progress%"
        this.discountPercent = progress.toFloat() / 100

        val editor: SharedPreferences.Editor = savedValues.edit()
        editor.putFloat("defaultDiscountPercent", discountPercent)

        // Save the seek bar progress to SharedPreferences only if rememberDiscountPercent is true
        if (rememberDiscountPercent) {
            editor.putInt("seekBarProgress", progress)
        }
        editor.apply()
        calculateAndDisplay()
    }

    // Called when the user begins to change the seek bar's value.
    override fun onStartTrackingTouch(bar: SeekBar?) { }

    // Called when the user finishes changing the seek bar's value.
    override fun onStopTrackingTouch(bar: SeekBar?) { }

    override fun onPause() {
        val editor: SharedPreferences.Editor = savedValues.edit()
        editor.putString("totalAmountString", subTotalAmountString)
        editor.putFloat("discountPercent", discountPercent)
        editor.apply()
        super.onPause()
    }
    override fun onResume() {
        super.onResume()

        rememberDiscountPercent = savedValues.getBoolean("pref_remember_percent", true)

        discountPercent =
            if (rememberDiscountPercent) savedValues.getFloat("defaultDiscountPercent", 0.10f)
            else 0.10f

        subTotalAmountString = savedValues.getString("billAmountString", "").toString()
        subTotalAmountEditText.setText(subTotalAmountString)

        if (rememberDiscountPercent) {
            // Retrieve seek bar progress from SharedPreferences and set it only if rememberDiscountPercent is true
            val seekBarProgress = savedValues.getInt("seekBarProgress", 0)
            percentSeekBar.progress = seekBarProgress
        }

        val defaultDiscountKey = savedValues.getString("pref_default_discount", "1") // Default value "1"

        //Find the index of the selected default discount key in the array
        val defaultDiscountIndex = resources.getStringArray(R.array.default_discount_values).indexOf(defaultDiscountKey)

        //Use the index to set the default discount label
        setDefaultDiscountLabel(defaultDiscountIndex)
    }

    override fun onEditorAction(view: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE)
            calculateAndDisplay()
        val defaultDiscountKey = savedValues.getString("pref_default_discount", "1") // Default value "1"

        // Find the index of the selected default discount key in the array
        val defaultDiscountIndex = resources.getStringArray(R.array.default_discount_values).indexOf(defaultDiscountKey)
        // Use the index to set the default discount label
        setDefaultDiscountLabel(defaultDiscountIndex)
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_settings -> {
                //Toast.makeText(this, "Settings Selected", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, SettingsActivity::class.java))
                calculateAndDisplay()
                return true
            }

            R.id.menu_reset -> {
                Toast.makeText(this, "Reset Aplicado", Toast.LENGTH_SHORT).show()
                percentTextView.text = "0"
                discountTextView.text = "0"
                totalTextView.text = "0"
                subTotalAmountEditText.text = null
                percentSeekBar.progress = 0
                return true
            }

            R.id.menu_about -> {
                val builder = AlertDialog.Builder(this)
                builder
                    .setTitle(aboutLabel)
                    .setMessage(aboutDescription)

                val dialog = builder.create()
                dialog.show()
                return true
            }

            else ->
                super.onOptionsItemSelected(item)
        }
        return true
    }

}



