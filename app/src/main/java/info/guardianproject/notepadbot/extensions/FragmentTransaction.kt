package info.guardianproject.notepadbot.extensions

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.transition.MaterialSharedAxis
import info.guardianproject.notepadbot.fragments.NotesFragment

/*inline fun <reified T : Fragment> FragmentTransaction.replace(navHost: Int) {

    val previousFragment = findFragmentById(navHost)
    val nextFragment = T::class.constructors.find { it.parameters.isEmpty() }!!.call()

    previousFragment!!.exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
    nextFragment.enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)

    replace(navHost, nextFragment)
}*/