package com.middleton.scott.cmboxing

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_packs_screen.*


class PacksScreen : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_packs_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        get_in_touch_btn.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.commandme.io"))
            startActivity(browserIntent)
        }
    }
}