package com.umd.terptrack.model

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore


// This is for the firebase repository letting the app push data
// to the cloud. Sets up the firestore connection and creates
// a func to save a new item.
class ItemRepository {
    // get a reference to  the firestore database
    private val db = FirebaseFirestore.getInstance()
    // the collection name where items are stored
    private val collectionRef = db.collection("lost_items")

    // func to add a new item to the database (POST request)
    fun addItem(item: LostItem, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        // Use timestamp as a unique document ID for simplicity
        val documentId = item.timestamp.toString()

        collectionRef.document(documentId).set(item)
            .addOnSuccessListener {
                Log.d("Firebase", "Item successfully written!")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.w("Firebase", "Error writing document", e)
                onFailure(e)
            }
    }

    // retrieve all items from the database (GET request)
    fun getAllItems(onResult: (List<LostItem>) -> Unit, onFailure: (Exception) -> Unit) {
        collectionRef.get()
            .addOnSuccessListener { result ->
                val itemList = mutableListOf<LostItem>()
                for (document in result) {
                    // fb automatically maps the database fields to the LostItem data class
                    val item = document.toObject(LostItem::class.java)
                    itemList.add(item)
                }
                Log.d("Firebase", "Successfully fetched ${itemList.size} items.")
                onResult(itemList)
            }
            .addOnFailureListener { e ->
                Log.w("Firebase", "Error getting documents", e)
                onFailure(e)
            }
    }
}