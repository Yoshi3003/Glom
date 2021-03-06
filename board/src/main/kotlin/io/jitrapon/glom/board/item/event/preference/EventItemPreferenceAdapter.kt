package io.jitrapon.glom.board.item.event.preference

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.jitrapon.glom.base.model.AndroidImage
import io.jitrapon.glom.base.model.AndroidString
import io.jitrapon.glom.base.util.*
import io.jitrapon.glom.board.R

/**
 * Adapter responsible for rendering list of event item preferences
 *
 * Created by Jitrapon
 */
class EventItemPreferenceAdapter(private val context: Context, private val viewModel: EventItemPreferenceViewModel)
    : RecyclerView.Adapter<EventItemPreferenceAdapter.ListItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListItemViewHolder {
        return ListItemViewHolder(LayoutInflater.from(parent.context).inflate(io.jitrapon.glom.R.layout.list_item_small_icon, parent, false), viewModel::selectItem)
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun getItemCount(): Int {
        return viewModel.getPreferenceSize()
    }

    override fun onBindViewHolder(holder: ListItemViewHolder, position: Int) {
        val uiModel = viewModel.getPreferenceUiModel(position)
        holder.apply {
            setImage(leftImage, uiModel.leftImage)
            setImage(rightImage, uiModel.rightImage,
                if (uiModel.isExpanded == true) 180f
                else if (uiModel.isExpanded == false) 0f
                else null)
            setTitle(uiModel.title, uiModel.isTitleSecondaryText)
            setDescription(uiModel.subtitle)
            setCheckbox(uiModel.isToggled)
        }
    }

    /**
     * ViewHolder that holds the view for individual preference item
     */
    inner class ListItemViewHolder(itemView: View, onItemClicked: (Int) -> Unit): RecyclerView.ViewHolder(itemView) {

        val leftImage: ImageView = itemView.findViewById(io.jitrapon.glom.R.id.list_item_left_icon)
        val title: TextView = itemView.findViewById(io.jitrapon.glom.R.id.list_item_title)
        val description: TextView = itemView.findViewById(io.jitrapon.glom.R.id.list_item_description)
        val rightImage: ImageView = itemView.findViewById(io.jitrapon.glom.R.id.list_item_right_icon)
        private val checkbox: CheckBox = itemView.findViewById(io.jitrapon.glom.R.id.list_item_checkbox)

        init {
            itemView.setOnClickListener {
                onItemClicked(adapterPosition)

                viewModel.getPreferenceUiModel(adapterPosition).let {
                    if (it.isExpanded != null && it.headerTag != null) {
                        val rotationAngle = if (viewModel.isHeaderItemExpanded(it.headerTag!!)) 180f else 0f
                        rightImage.animate().rotation(rotationAngle).setDuration(200).start()
                    }
                    if (it.isToggled != null) {
                        checkbox.toggle()
                    }
                }
            }
            leftImage.hide(null, true)
            title.hide(null, true)
            description.hide()
            rightImage.hide(null, true)
            checkbox.hide()
            checkbox.isClickable = false
        }

        fun setImage(imageView: ImageView, image: AndroidImage?, rotationAngle: Float? = null) {
            if (image == null) {
                imageView.hide(invisible = true)
            }
            else {
                imageView.apply {
                    show()
                    load(context, image)
                    rotationAngle?.let {
                        rotation = it
                    }
                }
            }
        }

        fun setTitle(string: AndroidString?, isSecondary: Boolean) {
            title.apply {
                show()
                text = context.getString(string)
                setTextColor(if (isSecondary)
                    context.attrColor(android.R.attr.textColorSecondary) else
                    context.attrColor(android.R.attr.textColorPrimary))
            }
        }

        fun setDescription(string: AndroidString?) {
            description.apply {
                if (string == null) hide()
                else {
                    show()
                    text = context.getString(string)
                    setTextColor(context.attrColor(android.R.attr.textColorSecondary))
                }
            }
        }

        fun setCheckbox(isToggled: Boolean?) {
            if (isToggled == null) {
                checkbox.hide()
            }
            else {
                checkbox.apply {
                    show()
                    isChecked = isToggled
                }
            }
        }
    }
}
