package info.guardianproject.notepadbot.fragments

import android.os.Bundle
import android.view.*
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import info.guardianproject.notepadbot.NotesAdapter
import info.guardianproject.notepadbot.R
import info.guardianproject.notepadbot.databinding.LockscreenFragmentBinding
import info.guardianproject.notepadbot.databinding.NotesFragmentBinding
import kotlin.math.roundToInt

class NotesFragment : Fragment(R.layout.notes_fragment) {
    private lateinit var _binding: NotesFragmentBinding
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = NotesFragmentBinding.inflate(inflater, container, false).also {
        _binding = it
        it.notesList.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        it.notesList.adapter = NotesAdapter(
            Array(40) { i -> "Test$i" }
        )
    }.root.also {
        ViewCompat.setOnApplyWindowInsetsListener(binding.notesList) { v, insets ->
            val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBarInsets.left, systemBarInsets.top,
                systemBarInsets.right, systemBarInsets.bottom +
                        (56 * resources.displayMetrics.density ).roundToInt() // app bar
            )

            insets
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.notes_menu, menu)
    }
}