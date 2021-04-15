package info.guardianproject.notepadbot

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import info.guardianproject.notepadbot.databinding.NoteSmallFragmentBinding

class NotesAdapter(private val dataSet: Array<String>) :
    RecyclerView.Adapter<NotesAdapter.ViewHolder>() {

    class ViewHolder(val binding: NoteSmallFragmentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        // TODO
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            NoteSmallFragmentBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.titleText.text = dataSet[position]
    }

    override fun getItemCount() = dataSet.size
}