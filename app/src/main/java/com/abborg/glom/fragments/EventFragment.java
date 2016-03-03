package com.abborg.glom.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

import com.abborg.glom.AppState;
import com.abborg.glom.Const;
import com.abborg.glom.R;
import com.abborg.glom.activities.EventActivity;
import com.abborg.glom.activities.MainActivity;
import com.abborg.glom.adapters.EventRecyclerViewAdapter;
import com.abborg.glom.data.DataUpdater;
import com.abborg.glom.interfaces.EventChangeListener;
import com.abborg.glom.model.Event;

import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

/**
 * A simple {@link Fragment} subclass.
 */
public class EventFragment extends Fragment implements View.OnClickListener, EventChangeListener {

    private static final String TAG = "EventFragment";

    /* Whether or not this fragment is visible */
    public boolean isFragmentVisible;

    /* The view to be used for listing event cards */
    private RecyclerView recyclerView;

    /* Adapter to the recycler view */
    private EventRecyclerViewAdapter adapter;

    /* Layout manager for the view */
    private RecyclerView.LayoutManager layoutManager;

    /* Main activity's data updater */
    private DataUpdater dataUpdater;

    /* The list of events in this circle */
    private List<Event> events;

    private MainActivity activity;

    private Handler handler;

    private AppState appState;

    private static final int ITEM_APPEARANCE_ANIM_TIME = 650;
    private static final long ITEM_ADD_ANIM_TIME = 650;
    private static final long ITEM_REMOVE_ANIM_TIME = 350;
    private static final long ITEM_MOVE_ANIM_TIME = 350;
    private static final long ITEM_CHANGE_ANIM_TIME = 350;

    /**********************************************************
     * View Initializations
     **********************************************************/

    public EventFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = context instanceof Activity ? (MainActivity) context : null;
        if (activity != null) handler = activity.getHandler();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataUpdater = AppState.getInstance(getContext()).getDataUpdater();
        adapter = new EventRecyclerViewAdapter(getContext(), getEvents(), this, handler);
        appState = AppState.getInstance(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_event, container, false);
        recyclerView = (RecyclerView) root.findViewById(R.id.circle_event_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // set up adapter and its appearance animation
        recyclerView.setAdapter(adapter);

        // set up item animations
        recyclerView.setItemAnimator(new SlideInUpAnimator(new OvershootInterpolator(1f)));
        recyclerView.getItemAnimator().setAddDuration(ITEM_ADD_ANIM_TIME);
        recyclerView.getItemAnimator().setRemoveDuration(ITEM_REMOVE_ANIM_TIME);
        recyclerView.getItemAnimator().setMoveDuration(ITEM_MOVE_ANIM_TIME);
        recyclerView.getItemAnimator().setChangeDuration(ITEM_CHANGE_ANIM_TIME);

        return root;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            isFragmentVisible = true;
            adapter.update(null);   // force re-updating of events
            Log.i(TAG, "Event is now visible to user");
        }
        else {
            isFragmentVisible = false;
            Log.i(TAG, "Event is now INVISIBLE to user");
        }
    }

    /**********************************************************
     * Item Click Handler
     **********************************************************/

    @Override
    public void onClick(View view) {
        int position = recyclerView.getChildAdapterPosition(view);
        Event selected = events.get(position - 1);
        if (selected != null) {
            Log.d(TAG, "Event (" + selected.getName() + ") selected");
            Intent intent = new Intent(activity, EventActivity.class);
            intent.putExtra(getResources().getString(R.string.EXTRA_EVENT_ID), selected.getId());
            intent.setAction(getResources().getString(R.string.ACTION_UPDATE_EVENT));
            AppState.getInstance(getActivity()).setKeepGoogleApiClientAlive(true);
            getActivity().startActivityForResult(intent, Const.UPDATE_EVENT_RESULT_CODE);
        }
    }

    /**********************************************************
     * Item Change Handler
     **********************************************************/

    @Override
    public void onEventAdded(String id) {
        if (activity != null) {
            layoutManager.scrollToPosition(0);
            adapter.notifyItemInserted(0);
            Log.d(TAG, "Inserted item at " + 0);
        }
    }

    @Override
    public void onEventModified(String id) {
        if (activity != null) {
            int index = -1;
            List<Event> events = appState.getActiveCircle().getEvents();
            for (int i = 0; i < events.size(); i++) {
                if (events.get(i).getId().equals(id)) {
                    index = i;
                    break;
                }
            }
            if (index == -1) return;

            index = index + 1;
            layoutManager.scrollToPosition(index);
            adapter.notifyItemChanged(index);
            Log.d(TAG, "Updated item at " + index);
        }
    }

    @Override
    public void onEventDeleted(String id) {
        if (activity != null) {
            int index = -1;
            List<Event> events = appState.getActiveCircle().getEvents();
            for (int i = 0; i < events.size(); i++) {
                if (events.get(i).getId().equals(id)) {
                    index = i;
                    break;
                }
            }
            if (index == -1) return;

            index = index + 1;
            Log.d(TAG, "Removed item at " + index);
            adapter.notifyItemRemoved(index);
        }
    }

    /**********************************************************
     * Helpers
     **********************************************************/

    public List<Event> getEvents() {
        events = AppState.getInstance(getContext()).getActiveCircle().getEvents();
        return events;
    }

    public void update() {
        if (activity != null) {
            events = AppState.getInstance(getContext()).getActiveCircle().getEvents();
            adapter.update(events);
        }
    }
}
