package com.jamal2367.deepl

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class FloatingTextSelection : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val floatingText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()

            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("FLOATING_TEXT", floatingText)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

}
