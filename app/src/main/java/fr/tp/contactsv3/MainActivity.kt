package fr.tp.contactsv3

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit

class MainActivity : FragmentActivity(R.layout.activity_main) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.container, ListeContactsFragment())
            }
        }
    }

    fun ouvrir(source: Fragment, destination: Fragment, id: Int) {
        val manager = supportFragmentManager
        if (manager.isStateSaved || manager.findFragmentById(R.id.container) !== source) return
        destination.arguments = bundleOf("contact_id" to id)
        manager.commit {
            setReorderingAllowed(true)
            replace(R.id.container, destination)
            addToBackStack(null)
        }
        manager.executePendingTransactions()
    }
}
