package fr.tp.contactsv3

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import java.io.File
import java.io.IOException

class DetailContactFragment : Fragment(R.layout.fragment_detail) {
    private val model: ContactsViewModel by activityViewModels()
    private var photo: String? = null
    private var initialise = false
    private val galerie = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                requireContext().contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                choisirPhoto(uri)
            } catch (_: SecurityException) {
                message("Impossible de lire cette photo.")
            }
        }
    }
    private val camera = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            try {
                val fichier = File.createTempFile("contact-", ".jpg", requireContext().cacheDir)
                fichier.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
                choisirPhoto(Uri.fromFile(fichier))
            } catch (_: IOException) {
                message("Impossible d’enregistrer la photo.")
            }
        }
    }

    private fun choisirPhoto(uri: Uri) {
        photo = uri.toString()
        view?.findViewById<ImageView>(R.id.photo)?.afficherPhoto(photo)
    }

    private fun message(texte: String) {
        Toast.makeText(requireContext(), texte, Toast.LENGTH_SHORT).show()
    }

    private fun lancer(messageErreur: String, action: () -> Unit) {
        try { action() } catch (_: ActivityNotFoundException) { message(messageErreur) }
        catch (_: SecurityException) { message(messageErreur) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("photo", photo)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val id = requireArguments().getInt("contact_id")
        val contact = model.trouver(id) ?: run {
            message("Contact introuvable.")
            parentFragmentManager.popBackStack()
            return
        }
        fun champ(idChamp: Int) = view.findViewById<EditText>(idChamp)
        val nom = champ(R.id.nom)
        val prenom = champ(R.id.prenom)
        val tel = champ(R.id.telephone)
        val email = champ(R.id.email)
        val lat = champ(R.id.latitude)
        val lng = champ(R.id.longitude)
        nom.setText(contact.nom)
        prenom.setText(contact.prenom)
        tel.setText(contact.telephone)
        email.setText(contact.email)
        lat.setText(contact.latitude?.toString().orEmpty())
        lng.setText(contact.longitude?.toString().orEmpty())
        if (!initialise) {
            photo = if (savedInstanceState != null) savedInstanceState.getString("photo") else contact.photo
            initialise = true
        }
        view.findViewById<ImageView>(R.id.photo).afficherPhoto(photo)

        fun clic(bouton: Int, action: () -> Unit) {
            view.findViewById<View>(bouton).setOnClickListener { action() }
        }
        clic(R.id.appeler) {
            val numero = tel.text.toString().trim()
            if (numero.isBlank()) tel.error = "Téléphone obligatoire"
            else lancer("Composeur indisponible.") {
                startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", numero, null)))
            }
        }
        clic(R.id.envoyer) {
            val adresse = email.text.toString().trim()
            if (!Patterns.EMAIL_ADDRESS.matcher(adresse).matches()) email.error = "Adresse e-mail invalide"
            else lancer("Aucune application de messagerie disponible.") {
                startActivity(Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", adresse, null)))
            }
        }
        clic(R.id.galerie) { lancer("Galerie indisponible.") { galerie.launch(arrayOf("image/*")) } }
        clic(R.id.camera) { lancer("Caméra indisponible.") { camera.launch(null) } }
        clic(R.id.annuler) { parentFragmentManager.popBackStack() }
        clic(R.id.carte) { (requireActivity() as MainActivity).ouvrir(this, CarteContactFragment(), id) }
        clic(R.id.enregistrer) {
            val nomTexte = nom.text.toString().trim()
            val prenomTexte = prenom.text.toString().trim()
            val telTexte = tel.text.toString().trim()
            val emailTexte = email.text.toString().trim()
            val latTexte = lat.text.toString().trim()
            val lngTexte = lng.text.toString().trim()
            val latitude = latTexte.replace(',', '.').toDoubleOrNull()
            val longitude = lngTexte.replace(',', '.').toDoubleOrNull()

            when {
                nomTexte.isEmpty() -> nom.error = "Nom obligatoire"
                prenomTexte.isEmpty() -> prenom.error = "Prénom obligatoire"
                telTexte.none { it.isDigit() } -> tel.error = "Téléphone invalide"
                !Patterns.EMAIL_ADDRESS.matcher(emailTexte).matches() -> email.error = "Adresse e-mail invalide"
                latTexte.isNotEmpty() && (latitude == null || latitude !in -90.0..90.0) ->
                    lat.error = "Latitude entre −90 et 90"
                lngTexte.isNotEmpty() && (longitude == null || longitude !in -180.0..180.0) ->
                    lng.error = "Longitude entre −180 et 180"
                latTexte.isEmpty() != lngTexte.isEmpty() ->
                    message("Renseigne les deux coordonnées ou laisse les deux champs vides.")
                else -> {
                    model.enregistrer(contact.copy(nom = nomTexte, prenom = prenomTexte, telephone = telTexte,
                        email = emailTexte, photo = photo, latitude = latitude, longitude = longitude))
                    parentFragmentManager.popBackStack()
                }
            }
        }
    }
}
