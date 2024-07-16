package com.example.jigesh.trendbuzz

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class ActivityFragment : Fragment() {

    private var activityClassName: String? = null
    private var activityIntent: Intent? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.let {
            activityClassName = it.getString(ARG_ACTIVITY_CLASS_NAME)
            activityIntent = it.getParcelable(ARG_ACTIVITY_INTENT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val activityClass = Class.forName(activityClassName)
        val intent = activityIntent ?: Intent(context, activityClass)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        return null // Fragment doesn't need to return a view
    }

    companion object {
        private const val ARG_ACTIVITY_CLASS_NAME = "activity_class_name"
        private const val ARG_ACTIVITY_INTENT = "activity_intent"

        @JvmStatic
        fun newInstance(activityClassName: String, intent: Intent? = null) =
            ActivityFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ACTIVITY_CLASS_NAME, activityClassName)
                    putParcelable(ARG_ACTIVITY_INTENT, intent)
                }
            }
    }
}
