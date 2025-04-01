package com.example.dogscare

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DogAdapter(private val dogs: List<Dog>) : RecyclerView.Adapter<DogAdapter.DogViewHolder>() {
//dogs - przechowuje liste psow pobranych z bazy

    //DogViewHolder to widok do przedstawienia danych w recycleview
    class DogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dogName: TextView = view.findViewById(R.id.dogName)
        val dogAdmissionDate: TextView = view.findViewById(R.id.dogAdmissionDate)
    }

    //tworzenie pojedynczego elementu
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dog_list, parent, false)
        return DogViewHolder(view)
    }

    //przypisanie danych do konkretnego elementu
    override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
        val dog = dogs[position]
        holder.dogName.text = dog.name
        //holder.dogAdmissionDate.text = "Przyjęty: ${dog.admissionDate}"
    }

    //metoda zwracająca liczbe lementow w liscie, aby recycleview odgornie wiedzial ile elementow
    override fun getItemCount(): Int = dogs.size
}