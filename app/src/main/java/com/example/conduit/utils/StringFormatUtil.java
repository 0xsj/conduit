package com.example.conduit.utils;

import android.widget.EditText;

public class StringFormatUtil {

    public static void resetFields(EditText... editTexts) {
        for (EditText editText: editTexts) {
            editText.setText("");
        }
    }

    public void clearFields(EditText... editTexts) {
        for (EditText editText : editTexts) {
            editText.getText().clear();
        }
    }
}
