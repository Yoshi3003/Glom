package io.jitrapon.glom.base.model

/**
 * Base UiModel class for all preference item
 *
 * Created by Jitrapon
 */
data class PreferenceItemUiModel(override var status: UiModel.Status = UiModel.Status.SUCCESS,
                                 val headerTag: Int?,
                                 val title: AndroidString,
                                 val isTitleSecondaryText: Boolean,
                                 val subtitle: AndroidString?,
                                 val isExpanded: Boolean?,
                                 var isToggled: Boolean?,
                                 val tag: String?,
                                 val leftImage: AndroidImage? = null,
                                 val rightImage: AndroidImage? = null) : UiModel {

    constructor(image: AndroidImage?, title: AndroidString):
            this(UiModel.Status.SUCCESS, null, title,
                false, null, null,
                null, null, image, null)

    constructor(image: AndroidImage?, title: AndroidString, tag: String?):
            this(UiModel.Status.SUCCESS, null, title,
                false, null, null,
                null, tag, image, null)
}

fun PreferenceItemUiModel.isHeaderItem(): Boolean = headerTag != null && isExpanded != null

fun PreferenceItemUiModel.isCheckedItem(): Boolean = !tag.isNullOrEmpty() && isToggled != null

const val EMPTY_ITEM_TAG = "empty"
