package com.example.androidapp

import android.app.AlertDialog
import android.app.ProgressDialog.show
import android.content.DialogInterface
import android.net.wifi.WifiConfiguration.AuthAlgorithm.strings
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidapp.databinding.FragmentInventoryBinding
import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView


private var _binding: FragmentInventoryBinding? = null
val ingredient_list = mutableListOf<String>("Beef", "Rice noodles", "Cilantro")

// This property is only valid between onCreateView and
// onDestroyView.
private val binding get() = _binding!!


class RecyclerTouchListener(
    context: Context,
    recyclerView: RecyclerView,
    private val clickListener: ClickListener
) : RecyclerView.OnItemTouchListener {

    private val gestureDetector: GestureDetector

    init {
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                val child: View? = recyclerView.findChildViewUnder(e.x, e.y)
                if (child != null) {
                    clickListener.onLongClick(child, recyclerView.getChildAdapterPosition(child))
                }
            }
        })
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        val childView: View? = rv.findChildViewUnder(e.x, e.y)
        if (childView != null && gestureDetector.onTouchEvent(e)) {
            clickListener.onClick(childView, rv.getChildAdapterPosition(childView))
            return true
        }
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

    interface ClickListener {
        fun onClick(view: View, position: Int)
        fun onLongClick(view: View, position: Int)
    }
}


class FragmentInventory : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInventoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun showPopup(itemSelected: String) {


        println("Show popup called")


        val popUpCardView = layoutInflater.inflate(R.layout.inventory_item_popup,
            null)


        popUpCardView.focusable = View.FOCUSABLE

        //popUpCardView.text

        //val popupTextView = popUpCardView.findViewById<TextView>(R.id.textView)




        val window = PopupWindow(popUpCardView, LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT, true)

        window.showAtLocation(view, Gravity.CENTER, 0, 100)



//        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
//        builder.setTitle("More info about item")
//        builder.setMessage("Quantity: 300 g")
//
//        builder.setMessage("Purchase Date: January 1, 2019")
//        builder.setMessage("Estimated expiry date: March 15, 2019")
//
//
//
//        builder.setPositiveButton("Close",
//            DialogInterface.OnClickListener { dialog, which -> // Do something when the button is clicked
//                //retVal = 1
//                dialog.dismiss()
//            })



    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // getting the ingredientlist
        // Assign ingredientlist to ItemAdapter
        val itemAdapter = Adapter(ingredient_list)
        // Set the LayoutManager that
        // this RecyclerView will use.
        val recyclerView: RecyclerView = view.findViewById(R.id.ingredient_list)
        recyclerView.layoutManager = LinearLayoutManager(context)
        // adapter instance is set to the
        // recyclerview to inflate the items.
        recyclerView.adapter = itemAdapter

        //recyclerView.addOnItemTouchListener()


        recyclerView.addOnItemTouchListener(

            RecyclerTouchListener(
                requireContext(),
                recyclerView,
                object : RecyclerTouchListener.ClickListener {
                    override fun onClick(view: View, position: Int) {
                        println("ShowPopup called from onClick listener ")
                        showPopup(ingredient_list[0])
                        // Handle click event
                    }

                    override fun onLongClick(view: View, position: Int) {
                        // Handle long click event
                    }
                }
            )
        )

        binding.ingredientNameField.setEndIconOnClickListener {
            val str = binding.ingredientNameField.editText?.text.toString()
            if(str.isNotEmpty()) {
                ingredient_list.add(str)
                binding.ingredientNameField.editText?.text?.clear()
                itemAdapter.notifyDataSetChanged()
            }
        }

        binding.select.setOnClickListener {
            var nextState = if (binding.delete.isVisible) View.GONE else View.VISIBLE
            binding.delete.visibility = nextState
            itemAdapter.toggleCheckBoxVisiblity()
        }

        binding.delete.setOnClickListener{
            val selectedItems = itemAdapter.getSelectedItems()
            itemAdapter.deleteItems(selectedItems)
        }


        binding.ingredientList.setOnClickListener{
//            val selectedItem = ingredient_list[0]
//            showPopup(selectedItem)

        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



class Adapter(private val ingredient_list: MutableList<String>) : RecyclerView.Adapter<Adapter.MyViewHolder>() {

    private val checkedItems = mutableListOf<Int>()
    private var checkboxVisibility = View.INVISIBLE
    // This method creates a new ViewHolder object for each item in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // Inflate the layout for each item and return a new ViewHolder object
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        return MyViewHolder(itemView)
    }

    // This method returns the total
    // number of items in the data set
    override fun getItemCount(): Int {
        return ingredient_list.size
    }

    // This method binds the data to the ViewHolder object
    // for each item in the RecyclerView
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.ingredient.text = ingredient_list[position]
        holder.checkBox.visibility = checkboxVisibility

        holder.itemView.setOnClickListener {
            // Retrieve the selected item data
            val selectedItem = ingredient_list[position]

            // Show the popup card with additional data
            //showPopup(selectedItem)
        }
    }

    fun toggleCheckBoxVisiblity() {
        if (checkboxVisibility == CheckBox.VISIBLE)
            checkboxVisibility = CheckBox.INVISIBLE
        else
            checkboxVisibility = CheckBox.VISIBLE
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<Int> {
        return checkedItems
    }

    fun deleteItems(itemsToDelete: List<Int>) {
        val sortedItems = itemsToDelete.sortedDescending()
        sortedItems.forEach { position ->
            print(position)
            ingredient_list.removeAt(position)
            notifyItemRemoved(position)
        }
        checkedItems.clear()
    }






    // This class defines the ViewHolder object for each item in the RecyclerView
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ingredient: TextView = itemView.findViewById(R.id.ingredient)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)

        init {
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    if (isChecked) {
                        checkedItems.add(position)
                    } else {
                        checkedItems.remove(position)
                    }
                }
            }
        }
    }
}

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentInventory.newInstance] factory method to
 * create an instance of this fragment.
 */

// This is the current hardcoded version



//This is the version that uses data from the database

//fun showPopup(view: View?, context: Context?) {
//    val builder: AlertDialog.Builder = AlertDialog.Builder(context)
//    builder.setTitle("More info about item")
//    builder.setMessage("Quantity" + ingredient_list.getAmount)
//
//    builder.setMessage("Purchase Date:" + ingredient_list.getDate)
//    builder.setMessage("Estimated expiry date: " + ingredient_list.getexpiry)
//
//
//
//    builder.setPositiveButton("Close",
//        DialogInterface.OnClickListener { dialog, which -> // Do something when the button is clicked
//            //retVal = 1
//            dialog.dismiss()
//        })
//
//    val alertDialog: AlertDialog = builder.create()
//    alertDialog.show()
//}







//    fun showPopup(primaryStage: Stage?){
//        val popupStage = Stage()
//        //popupStage.initOwner(primaryStage)
//        popupStage.initModality(Modality.APPLICATION_MODAL)
//        popupStage.initStyle(StageStyle.UTILITY)
//        popupStage.title = "Popup Window"
//
//        val cancelButton = Button("Cancel")
//        cancelButton.onAction = EventHandler<ActionEvent?> {
//            popupStage.close()
//
//        }
//
//        val popupRoot = VBox()
//
//        val confirmButton = Button("Confirm")
//        confirmButton.onAction = EventHandler<ActionEvent?> {
//            retVal = 1;
//            popupStage.close()
//        }
//
//        popupRoot.children.add(cancelButton)
//        popupRoot.children.add(confirmButton)
//
//
//        popupStage.scene = Scene(popupRoot, 500.0, 200.0)
//        popupStage.showAndWait()
//
//    }