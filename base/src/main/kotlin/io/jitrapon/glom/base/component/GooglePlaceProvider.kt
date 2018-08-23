package io.jitrapon.glom.base.component

import android.app.Activity
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.graphics.Bitmap
import android.text.TextUtils
import com.google.android.gms.location.places.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.instantapps.InstantApps
import io.reactivex.Maybe
import io.reactivex.Single

/**
 * Lifecycle-aware components that abstracts away logic to retrieve Place information
 * using the Google Place API.
 *
 * This class should be initialized in one of the Android's view classes
 * (i.e. Activity or Fragment), and be passed onto the domain classes afterwards. This is
 * because those classes contain the Lifecycle and context necessary to construct this class.
 *
 * TODO results should be cached for 1 day
 *
 * Created by Jitrapon on 11/30/2017.
 */
class GooglePlaceProvider(lifeCycle: Lifecycle, context: Context? = null, activity: Activity? = null) : PlaceProvider {

    private var isInstantApp: Boolean = false

    //region constructors

    /**
     * Initializes the life cycle object from the constructor to be notified of any change in the
     * state in the life cycle
     */
    private var lifeCycle: Lifecycle = lifeCycle.apply {
        addObserver(this@GooglePlaceProvider)
    }

    /**
     * GeoDataClient instance to talk to a Google server
     */
    private val client: GeoDataClient by lazy {
        if (context == null && activity == null) throw IllegalArgumentException("Context and Activity must not be null")
        if (context != null) {
            isInstantApp = InstantApps.isInstantApp(context)
            Places.getGeoDataClient(context)
        }
        else {
            isInstantApp = InstantApps.isInstantApp(activity!!)
            Places.getGeoDataClient(activity)
        }
    }

    /**
     * Search LatLng boundary
     */
    private val bounds: LatLngBounds = LatLngBounds(LatLng(13.7244409, 100.3522243), LatLng(13.736717, 100.523186))

    /**
     * Search filter
     */
    private val filter: AutocompleteFilter = AutocompleteFilter.Builder()
            .setCountry("th")
            .setTypeFilter(AutocompleteFilter.TYPE_FILTER_NONE)
            .build()

    //endregion
    //region lifecycle

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun cleanup() {
        lifeCycle.removeObserver(this)
    }

    override fun getPlaces(placeIds: Array<String>): Single<Array<Place>> {
        return Single.create { single ->
            // place APIs doesn't work with Instant App yet
            if (isInstantApp || placeIds.isEmpty()) {
                single.onSuccess(emptyArray())
            }
            else {
                client.getPlaceById(*placeIds).let {
                    it.addOnCompleteListener {
                        try {
                            val places = it.result
                            if (it.isSuccessful) {
                                val result = ArrayList<Place>()
                                places.filter { it.isDataValid }
                                        .forEach { result.add(it.freeze()) }
                                places.release()
                                single.onSuccess(result.toTypedArray())
                            } else {
                                it.exception?.let {
                                    single.onError(it)
                                }
                            }
                        }
                        catch (ex: Exception) {
                            single.onError(ex)
                        }
                    }
                    it.addOnFailureListener {
                        single.onError(it)
                    }
                }
            }
        }
    }

    override fun getPlacePhoto(placeId: String): Maybe<Bitmap> {
        return Maybe.create { maybe ->
            if (isInstantApp || placeId.isEmpty()) {
                maybe.onComplete()
            }
            else {
                client.getPlacePhotos(placeId).let {
                    it.addOnCompleteListener {
                        val photosMetadata = it.result.photoMetadata
                        if (photosMetadata.count == 0) {
                            maybe.onComplete()
                        }
                        else {
                            val meta = photosMetadata[0]
                            client.getPhoto(meta).let {
                                it.addOnCompleteListener {
                                    maybe.onSuccess(it.result.bitmap)
                                }
                                it.addOnFailureListener {
                                    maybe.onError(it)
                                }
                            }
                        }
                    }
                    it.addOnFailureListener {
                        maybe.onError(it)
                    }
                }
            }
        }
    }

    override fun getAutocompletePrediction(query: String): Single<Array<AutocompletePrediction>> {
        return getAutocompletePrediction(query, bounds, filter)
    }

    private fun getAutocompletePrediction(query: String, bounds: LatLngBounds, filter: AutocompleteFilter): Single<Array<AutocompletePrediction>> {
        return Single.create { single ->
            if (TextUtils.isEmpty(query)) {
                single.onSuccess(emptyArray())
            }
            else {
                client.getAutocompletePredictions(query, bounds, filter).let {
                    it.addOnCompleteListener {
                        val result = ArrayList<AutocompletePrediction>()
                        if (it.isSuccessful) {
                            if (it.result != null && it.result.count > 0) {
                                it.result.filter { it.isDataValid }
                                        .forEach { result.add(it.freeze()) }
                                it.result.release()
                                single.onSuccess(result.toTypedArray())
                            }
                            else {
                                single.onSuccess(emptyArray())
                            }
                        }
                        else {
                            it.exception?.let {
                                single.onError(it)
                            }
                        }
                    }
                    it.addOnFailureListener {
                        single.onError(it)
                    }
                }
            }
        }
    }

    //endregion
}
