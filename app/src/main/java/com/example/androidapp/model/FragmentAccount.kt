package com.example.androidapp.model

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import android.widget.TextView
import com.example.androidapp.R
import com.example.androidapp.databinding.FragmentAccountBinding

val itemList = listOf(
    AcountItem(R.drawable.baseline_build_24, "Preferences"),
    AcountItem(R.drawable.baseline_person_24, "Account"),
    AcountItem(R.drawable.baseline_lock_open_24, "Privacy")
)

class ListAdapter(private val items: List<AcountItem>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<ListAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImageView: ImageView = itemView.findViewById(R.id.itemAccountImage)
        val itemTextView: TextView = itemView.findViewById(R.id.accountListItem)
        val arrowImageView: ImageView = itemView.findViewById(R.id.arrowImageView)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_account_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.itemImageView.setImageResource(item.imageResId)
        holder.itemTextView.text = item.text
        holder.arrowImageView.setImageResource(R.drawable.baseline_navigate_next_24
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }
}


class FragmentAccount : Fragment(), ListAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var binding: FragmentAccountBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAccountBinding.inflate(inflater, container, false)
        recyclerView = binding.accountList

        val adapter = ListAdapter(itemList, this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        return binding.root
    }

    override fun onItemClick(position: Int) {
        val selectedItem = itemList[position].text
    }
}
