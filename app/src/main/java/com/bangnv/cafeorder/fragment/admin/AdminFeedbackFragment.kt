package com.bangnv.cafeorder.fragment.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.bangnv.cafeorder.ControllerApplication
import com.bangnv.cafeorder.adapter.admin.FeedbackAdapter
import com.bangnv.cafeorder.databinding.FragmentAdminFeedbackBinding
import com.bangnv.cafeorder.model.Feedback

class AdminFeedbackFragment : Fragment() {

    private lateinit var mFragmentAdminFeedbackBinding: FragmentAdminFeedbackBinding
    private var mListFeedback: MutableList<Feedback> = mutableListOf()
    private lateinit var mFeedbackAdapter: FeedbackAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mFragmentAdminFeedbackBinding = FragmentAdminFeedbackBinding.inflate(inflater, container, false)
        initView()
        getListFeedback()
        return mFragmentAdminFeedbackBinding.root
    }

    private fun initView() {
        if (activity == null) {
            return
        }
        val linearLayoutManager = LinearLayoutManager(activity)
        mFragmentAdminFeedbackBinding.rcvFeedback.layoutManager = linearLayoutManager
    }

    fun getListFeedback() {
        if (activity == null) {
            return
        }
        ControllerApplication[requireActivity()].feedbackDatabaseReference
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        mListFeedback.clear()
                        for (dataSnapshot in snapshot.children) {
                            val feedback = dataSnapshot.getValue(Feedback::class.java)
                            if (feedback != null) {
                                mListFeedback.add(0, feedback)
                            }
                        }
                        mFeedbackAdapter = FeedbackAdapter(mListFeedback)
                        mFragmentAdminFeedbackBinding.rcvFeedback.adapter = mFeedbackAdapter
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
    }
}