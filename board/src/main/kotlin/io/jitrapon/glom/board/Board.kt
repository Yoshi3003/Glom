package io.jitrapon.glom.board

import android.os.Parcel
import android.os.Parcelable
import io.jitrapon.glom.base.model.DataModel
import java.util.*

/**
 * DataModel class for the group's board
 *
 * @author Jitrapon Tiachunpun
 */
data class Board(val circleId: String,
                 val items: MutableList<BoardItem>,
                 override var retrievedTime: Date? = Date(),
                 override val error: Throwable? = null) : DataModel {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            ArrayList<BoardItem>().apply {
                parcel.readTypedList(this, BoardItem)
            },
            parcel.readLong().let {
                if (it == -1L) null
                else Date(it)
            })

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(circleId)
        parcel.writeTypedList(items)
        parcel.writeLong(retrievedTime?.time ?: -1L)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Board> {
        override fun createFromParcel(parcel: Parcel): Board = Board(parcel)

        override fun newArray(size: Int): Array<Board?> = arrayOfNulls(size)
    }
}