package fr.tp.contactsv3

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ListeContactsFragment : Fragment(R.layout.fragment_liste) {
    private val model: ContactsViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recherche = view.findViewById<EditText>(R.id.recherche)
        val vide = view.findViewById<TextView>(R.id.vide)
        val liste = view.findViewById<RecyclerView>(R.id.liste)
        val adapter = ContactsAdapter { contact ->
            val clavier = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            clavier.hideSoftInputFromWindow(recherche.windowToken, 0)
            (requireActivity() as MainActivity).ouvrir(this, DetailContactFragment(), contact.id)
        }
        liste.layoutManager = LinearLayoutManager(requireContext())
        liste.adapter = adapter
        recherche.setText(model.filtre)

        fun afficher(contacts: List<Contact>) {
            val terme = model.filtre.trim()
            val resultats = contacts.filter {
                it.nom.contains(terme, true) || it.prenom.contains(terme, true) ||
                    it.telephone.contains(terme, true)
            }
            adapter.submitList(resultats)
            vide.isVisible = resultats.isEmpty()
        }

        recherche.doAfterTextChanged {
            model.filtre = it.toString()
            afficher(model.contacts.value.orEmpty())
        }
        model.contacts.observe(viewLifecycleOwner) { afficher(it) }
    }
}

private class ContactsAdapter(private val clic: (Contact) -> Unit) :
    ListAdapter<Contact, ContactsAdapter.Ligne>(object : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Contact, newItem: Contact) = oldItem == newItem
    }) {
    class Ligne(view: View) : RecyclerView.ViewHolder(view) {
        val nom = view.findViewById<TextView>(R.id.nom_complet)
        val numero = view.findViewById<TextView>(R.id.numero)
        val photo = view.findViewById<ImageView>(R.id.photo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Ligne =
        Ligne(LayoutInflater.from(parent.context).inflate(R.layout.ligne_contact, parent, false))

    override fun onBindViewHolder(holder: Ligne, position: Int) {
        val contact = getItem(position)
        holder.nom.text = contact.nomComplet
        holder.numero.text = contact.telephone
        holder.photo.afficherPhoto(contact.photo)
        holder.itemView.setOnClickListener { clic(contact) }
    }
}
