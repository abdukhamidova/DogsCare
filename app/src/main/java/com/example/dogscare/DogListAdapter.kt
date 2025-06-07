package com.example.dogscare

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop

class DogAdapter(
    private var dogs: List<Dog>
) : RecyclerView.Adapter<DogAdapter.DogViewHolder>() {

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener){
        this.listener = listener
    }

    // Odświeżenie RecyclerView
    fun updateList(newDogs: List<Dog>){
        this.dogs = newDogs
        notifyDataSetChanged()
    }

    // DogViewHolder to widok pojedynczego elementu
    class DogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dogName: TextView = itemView.findViewById(R.id.dogName)
        val dogAdmissionDate: TextView = itemView.findViewById(R.id.dogAdmissionDate)
        val dogImage: ImageView = itemView.findViewById(R.id.dogPicture)  // ImageView dla zdjęcia

        fun bind(dog: Dog, listener: OnItemClickListener?) {
            val data = timestampToString(dog.arrivalDate)
            dogName.text = dog.name
            dogAdmissionDate.text = data


            if (dog.imageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(dog.imageUrl)
                    .placeholder(R.drawable.profile_picture)
                    .error(R.drawable.profile_picture)
                    .circleCrop()
                    .into(itemView.findViewById(R.id.dogPicture))
            } else {
                Glide.with(itemView.context)
                    .load(R.drawable.profile_picture)
                    .circleCrop()
                    .into(itemView.findViewById(R.id.dogPicture))
            }

            itemView.setOnClickListener {
                listener?.onItemClick(dog)
            }
        }

    }

    //tworzenie elementu w RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dog_list, parent, false)
        return DogViewHolder(view)
    }

    //przypisanie danych do pojedynczego elementu
    override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
        val dog = dogs[position]
        holder.bind(dog, listener)
    }

    override fun getItemCount(): Int = dogs.size

    interface OnItemClickListener {
        fun onItemClick(dog: Dog)
    }
}
