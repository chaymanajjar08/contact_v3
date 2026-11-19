package fr.tp.contactsv3

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class Contact(
    val id: Int, val nom: String, val prenom: String, val telephone: String,
    val email: String, val photo: String? = null,
    val latitude: Double? = null, val longitude: Double? = null
) {
    val nomComplet get() = "$prenom $nom"
}

class ContactsViewModel : ViewModel() {
    private val data = MutableLiveData(listOf(
        Contact(1, "Mansour", "Leïla", "00000001", "leila@example.com", latitude = 33.875, longitude = 10.858),
        Contact(2, "Trabelsi", "Amine", "00000002", "amine@example.com", latitude = 36.807, longitude = 10.181),
        Contact(3, "Ben Salem", "Nour", "00000003", "nour@example.com")
    ))
    val contacts: androidx.lifecycle.LiveData<List<Contact>> = data
    var filtre = ""

    fun trouver(id: Int) = data.value?.firstOrNull { it.id == id }
    fun enregistrer(contact: Contact) {
        data.value = data.value.orEmpty().map { if (it.id == contact.id) contact else it }
    }
}

fun ImageView.afficherPhoto(photo: String?) {
    setImageDrawable(null)
    if (photo != null) {
        try { setImageURI(Uri.parse(photo)) } catch (_: SecurityException) { }
    }
    if (drawable == null) setImageResource(android.R.drawable.ic_menu_myplaces)
}
