package com.example.studentform

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.InputStream
import java.util.Calendar


class MainActivity : AppCompatActivity() {

    private lateinit var editTextMSSV: EditText
    private lateinit var editTextName: EditText
    private lateinit var radioGroupGender: RadioGroup
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPhone: EditText
    private lateinit var buttonSelectDate: Button
    private lateinit var textViewDateOfBirth: TextView
    private lateinit var spinnerTinh: Spinner
    private lateinit var spinnerHuyen: Spinner
    private lateinit var spinnerXa: Spinner
    private lateinit var checkBoxTerms: CheckBox

    private lateinit var tinhList: ArrayList<String>
    private lateinit var huyenMap: HashMap<String, ArrayList<String>>
    private lateinit var xaMap: HashMap<String, HashMap<String, String>>

    private lateinit var selectedDate: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextMSSV = findViewById(R.id.editTextMSSV)
        editTextName = findViewById(R.id.editTextName)
        radioGroupGender = findViewById(R.id.radioGroupGender)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPhone = findViewById(R.id.editTextPhone)
        buttonSelectDate = findViewById(R.id.buttonSelectDate)
        textViewDateOfBirth = findViewById(R.id.textViewDateOfBirth)
        spinnerTinh = findViewById(R.id.spinnerTinh)
        spinnerHuyen = findViewById(R.id.spinnerHuyen)
        spinnerXa = findViewById(R.id.spinnerXa)
        checkBoxTerms = findViewById(R.id.checkBoxTerms)

        tinhList = ArrayList()
        huyenMap = HashMap()
        xaMap = HashMap()

        loadJSONData()

        buttonSelectDate.setOnClickListener {
            showDatePicker()
        }

        spinnerTinh.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (position > 0) { // Nếu đã chọn tỉnh
                    val selectedTinh = tinhList[position]
                    loadHuyenData(selectedTinh)
                } else {
                    spinnerHuyen.adapter = null // Reset Spinner huyện
                    spinnerXa.adapter = null // Reset Spinner xã
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spinnerHuyen.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (position > 0) { // Nếu đã chọn huyện
                    val selectedHuyen = huyenMap[spinnerTinh.selectedItem.toString()]?.get(position - 1) // Đúng chỉ số
                    loadXaData(selectedHuyen)
                } else {
                    spinnerXa.adapter = null // Reset Spinner xã
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        findViewById<Button>(R.id.buttonSubmit).setOnClickListener { submitForm() }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Tạo DatePickerDialog
        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            textViewDateOfBirth.text = selectedDate // Hiển thị ngày đã chọn
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun loadJSONData() {
        val inputStream: InputStream = assets.open("data.json")
        val size: Int = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()
        val jsonString = String(buffer)
        val jsonObject = JSONObject(jsonString)

        jsonObject.keys().forEach { key ->
            val tinhObject = jsonObject.getJSONObject(key)
            tinhList.add(tinhObject.getString("name"))
            huyenMap[tinhObject.getString("name")] = ArrayList()

            val huyenObject = tinhObject.getJSONObject("quan-huyen")
            huyenObject.keys().forEach { huyenKey ->
                val huyen = huyenObject.getJSONObject(huyenKey)
                huyenMap[tinhObject.getString("name")]?.add(huyen.getString("name"))
                xaMap[huyen.getString("name")] = HashMap()

                val xaObject = huyen.getJSONObject("xa-phuong")
                xaObject.keys().forEach { xaKey ->
                    val xa = xaObject.getJSONObject(xaKey)
                    xaMap[huyen.getString("name")]?.put(xa.getString("name"), xa.getString("code"))
                }
            }
        }

        tinhList.add(0, "Chọn tỉnh/thành") // Thêm gợi ý
        val tinhAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tinhList)
        tinhAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTinh.adapter = tinhAdapter
    }

    private fun loadHuyenData(selectedTinh: String?) {
        selectedTinh?.let {
            val huyenList = huyenMap[it]?.toMutableList() ?: mutableListOf()

            huyenList.remove("Chọn quận/huyện")

            if (!huyenList.contains("Chọn quận/huyện")) {
                huyenList.add(0, "Chọn quận/huyện")
            }

            val huyenAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, huyenList)
            huyenAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerHuyen.adapter = huyenAdapter

            spinnerXa.adapter = null
        }
    }

    private fun loadXaData(selectedHuyen: String?) {
        selectedHuyen?.let {
            val xaList = xaMap[it]?.keys?.toMutableList() ?: mutableListOf()

            xaList.remove("Chọn xã/phường")

            if (!xaList.contains("Chọn xã/phường")) {
                xaList.add(0, "Chọn xã/phường")
            }

            val xaAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, xaList)
            xaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerXa.adapter = xaAdapter
        }
    }

    private fun submitForm() {
        val mssv = editTextMSSV.text.toString().trim()
        val name = editTextName.text.toString().trim()
        val email = editTextEmail.text.toString().trim()
        val phone = editTextPhone.text.toString().trim()

        val genderId = radioGroupGender.checkedRadioButtonId
        val gender = if (genderId == -1) "" else findViewById<RadioButton>(genderId).text.toString()

        if (mssv.isEmpty() || name.isEmpty() || email.isEmpty() || phone.isEmpty() || gender.isEmpty() ||
            spinnerTinh.selectedItemPosition == 0 || spinnerHuyen.selectedItemPosition == 0 ||
            spinnerXa.selectedItemPosition == 0 || !checkBoxTerms.isChecked) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Thông tin đã được gửi!", Toast.LENGTH_SHORT).show()
        }
    }
}
