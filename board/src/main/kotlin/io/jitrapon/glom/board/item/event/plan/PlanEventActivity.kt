package io.jitrapon.glom.board.item.event.plan

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import io.jitrapon.glom.base.ui.BaseActivity
import io.jitrapon.glom.base.util.*
import io.jitrapon.glom.board.Const
import io.jitrapon.glom.board.R
import io.jitrapon.glom.board.item.event.EventItem
import kotlinx.android.synthetic.main.plan_event_activity.*

const val EXTRA_FIRST_VISIBLE_PAGE = "android.intent.EXTRA_FIRST_VISIBLE_PAGE"
const val PLAN_EVENT_OVERVIEW_PAGE = 0
const val PLAN_EVENT_DATE_PAGE = 1
const val PLAN_EVENT_PLACE_PAGE = 2

/**
 * This activity hosts the entry point to the event overview, as well as, date and place polls for an event.
 * The ViewModel created in this activity will be shared across all fragments in the ViewPager
 *
 * Created by Jitrapon
 */
class PlanEventActivity : BaseActivity() {

    private lateinit var viewModel: PlanEventViewModel

    //region lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plan_event_activity)

        tag = "plan_event"

        // set up the toolbar
        setupActionBar(event_plan_actionbar) {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }

        // create all the fragments inside the viewpager
        createViewPager()
    }

    override fun onCreateViewModel() {
        // instantiate the view model to be shared across child fragments
        viewModel = obtainViewModel(PlanEventViewModel::class.java).apply {
            setItem(placeProvider, intent?.getParcelableExtra(Const.EXTRA_BOARD_ITEM))
        }
    }

    override fun onSubscribeToObservables() {
        subscribeToViewActionObservables(viewModel.observableViewAction)

        viewModel.apply {
            getObservableBackground().observe(this@PlanEventActivity, Observer {
                event_plan_viewpager_background.loadFromUrl(this@PlanEventActivity, it, null, null,
                        ColorDrawable(ContextCompat.getColor(this@PlanEventActivity, io.jitrapon.glom.R.color.blue_grey)),
                        Transformation.CENTER_CROP,
                        800)
            })
            getObservableNavigation().observe(this@PlanEventActivity, Observer {
                it?.let {
                    if (it.action == Const.NAVIGATE_BACK) {
                        setResult(RESULT_OK, Intent().apply {
                            putExtra(Const.EXTRA_BOARD_ITEM, it.payload as? EventItem?)
                        })
                        finish()
                    }
                    else if (it.action == Const.NAVIGATE_TO_PLACE_PICKER) {
                        //TODO
                    }
                }
            })
        }
    }

    override fun onBackPressed() {
        viewModel.saveItem()
    }

    private fun createViewPager() {
        event_plan_viewpager.apply {
            offscreenPageLimit = 2
            val firstVisiblePageIndex = viewModel.getFirstVisiblePageIndex(intent.getIntExtra(EXTRA_FIRST_VISIBLE_PAGE, -1))
            createWithFragments(this@PlanEventActivity, arrayOf(
                    PlanEventOverviewFragment.newInstance(),
                    PlanEventDateFragment.newInstance(firstVisiblePageIndex == PLAN_EVENT_DATE_PAGE),
                    PlanEventPlaceFragment.newInstance(firstVisiblePageIndex == PLAN_EVENT_PLACE_PAGE)))
            doOnFragmentSelected<PlanEventDateFragment>(supportFragmentManager) { it.onVisible() }
            doOnFragmentSelected<PlanEventPlaceFragment>(supportFragmentManager) { it.onVisible() }
            setCurrentItem(firstVisiblePageIndex, false)
        }
    }

    override fun showLoading(show: Boolean) {
        if (show) {
            progressDialog.show(this)
        }
        else {
            progressDialog.dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Const.PLACE_PICKER_RESULT_CODE) {
            //TODO
//            viewModel.addPlaceToPoll()
        }
    }

    //endregion
}
