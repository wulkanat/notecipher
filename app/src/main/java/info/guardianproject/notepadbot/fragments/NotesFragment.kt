package info.guardianproject.notepadbot.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import info.guardianproject.notepadbot.NotesAdapter
import info.guardianproject.notepadbot.R
import info.guardianproject.notepadbot.StaggeredGridLayoutAnimationController
import info.guardianproject.notepadbot.databinding.NotesFragmentBinding
import kotlin.math.roundToInt
import kotlin.random.Random

class NotesFragment : Fragment(R.layout.notes_fragment) {
    private lateinit var _binding: NotesFragmentBinding
    private val binding get() = _binding

    /**
     * TODO: remove
     */
    private fun random(): String {
        val randomStringBuilder = StringBuilder()
        val randomLength: Int = Random.nextInt(500)
        var tempChar: Char
        for (i in 0 until randomLength) {
            tempChar = ((Random.nextInt(96) + 32).toChar())
            randomStringBuilder.append(tempChar)
        }
        return randomStringBuilder.toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = NotesFragmentBinding.inflate(inflater, container, false).also {
        _binding = it
        it.notesList.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        it.notesList.adapter = NotesAdapter(
            Array(40) { i -> "Test$i" to random() }
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

        val animation = AnimationUtils.loadAnimation(context, R.anim.item_anim_from_bottom)
        binding.notesList.layoutAnimation = StaggeredGridLayoutAnimationController(animation, 0.1f, 0.1f)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.notes_menu, menu)
    }
}