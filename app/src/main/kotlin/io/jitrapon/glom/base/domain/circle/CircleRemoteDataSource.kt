package io.jitrapon.glom.base.domain.circle

import io.jitrapon.glom.base.model.PlaceInfo
import io.jitrapon.glom.base.model.RepeatInfo
import io.jitrapon.glom.base.repository.RemoteDataSource
import io.reactivex.Flowable
import java.util.*

class CircleRemoteDataSource : RemoteDataSource(), CircleDataSource {

    private val api = retrofit.create(CircleApi::class.java)

    override fun getCircle(id: String, refresh: Boolean): Flowable<Circle> {
        return api.getCircleInfo(id).map {
            it.deserialize()
        }
    }

    //region deserializers

    private fun CircleInfoResponse.deserialize(): Circle {
        return Circle(id, name, avatar, info, interests.toMutableList(), repeatInfo?.deserialize(), places.deserialize(), Date())
    }

    private fun RepeatInfoResponse.deserialize(): RepeatInfo {
        return RepeatInfo(null, null, null, unit, interval, until, meta, 0L, null, null)
    }

    private fun List<PlaceInfoResponse>.deserialize(): MutableList<PlaceInfo> {
        return ArrayList<PlaceInfo>().apply {
            this@deserialize.map {
                add(PlaceInfo(it.name, it.description, null, it.avatar, it.lat, it.long, it.googlePlaceId, it.placeId, it.status))
            }
        }
    }

    //endregion
}
