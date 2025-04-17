package com.example.dogscare

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DogAdapter(
    private var dogs: List<Dog>
) : RecyclerView.Adapter<DogAdapter.DogViewHolder>() {
//dogs - przechowuje liste psow pobranych z bazy

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener){
        this.listener = listener
    }

    //odswiezanie recycleview
    fun updateList(newDogs: List<Dog>){
        this.dogs = newDogs
        notifyDataSetChanged() //odswiez
    }

    //DogViewHolder to widok do przedstawienia danych w recycleview
    class DogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dogName: TextView = itemView.findViewById(R.id.dogName)
        val dogAdmissionDate: TextView = itemView.findViewById(R.id.dogAdmissionDate)

        fun bind(dog: Dog, listener: OnItemClickListener?) {
            val data = timestampToString(dog.arrivalDate)
            dogName.text = dog.name
            dogAdmissionDate.text = data

            itemView.setOnClickListener {
                listener?.onItemClick(dog)
            } // Set click listener
        }
    }

    //tworzenie pojedynczego elementu
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dog_list, parent, false)
        return DogViewHolder(view)
    }

    //przypisanie danych do konkretnego elementu
    override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
        val dog = dogs[position]
        val data = timestampToString(dog.arrivalDate)

        holder.dogName.text = dog.name
        holder.dogAdmissionDate.text = data

        holder.bind(dog, listener)
    }

    //metoda zwracająca liczbe lementow w liscie, aby recycleview odgornie wiedzial ile elementow
    override fun getItemCount(): Int = dogs.size

    interface OnItemClickListener{
        fun onItemClick(dog: Dog)
    }
}