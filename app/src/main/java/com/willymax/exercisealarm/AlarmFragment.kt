package com.willymax.exercisealarm

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.willymax.exercisealarm.utils.SharedPreferencesHelper
import java.util.Calendar

/**
 * A fragment representing a list of Items.
 */
class AlarmFragment : Fragment() {

    private var columnCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_item_list, container, false)
        val calendar = Calendar.getInstance()
        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                val retrieveList = SharedPreferencesHelper(
                    requireContext(),
                    "AlarmList"
                ).retrieveList("AlarmList")

//                val alarmItems = mutableListOf<AlarmItem>()
//                alarmItems.addAll(retrieveList)
//                val temp = AlarmItem(
//                    calendar.time.toString(),
//                    calendar.get(Calendar.HOUR_OF_DAY),
//                    calendar.get(Calendar.MINUTE) + 1,
//                    arrayListOf(LocalDate.now().dayOfWeek),
//                    "Wake up",
//                    AlarmActivities.WALKING,
//                    repeats = true,
//                    isOn = true
//                )
//                // TODO: delete this
//                AlarmSchedulerImpl(requireActivity()).scheduleAlarm(temp)
//                alarmItems.add(temp)
                Log.d("AlarmFragment", "retrieveList: $retrieveList")
                adapter = MyAlarmRecyclerViewAdapter(
                    retrieveList
                )
            }
        }
        return view
    }

    companion object {
        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            AlarmFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}