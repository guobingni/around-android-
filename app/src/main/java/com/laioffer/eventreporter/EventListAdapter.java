package com.laioffer.eventreporter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.android.gms.internal.zzs.TAG;

/**
 * Created by guo on 2017/9/11.
 */

public class EventListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Event> eventList;
    private Context context;
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_ADS = 1;
    private AdLoader.Builder builder;
    private LayoutInflater inflater;
    private DatabaseReference databaseReference;
    private Map<Integer, NativeExpressAdView> map =
            new HashMap<Integer, NativeExpressAdView>();

    //constructor
    public EventListAdapter(List<Event> events, final Context context) {
        databaseReference = FirebaseDatabase.getInstance().getReference();

        Collections.sort(events, new Comparator<Event>() {
            @Override
            public int compare(Event A, Event B) {
                return B.getTime() - A.getTime() > 0 ? 1 : -1;
            }
        });
        eventList = new ArrayList<Event>();
        int count = 0;
        for (int i = 0; i < events.size(); i++) {
            if (i % 3 == 1) {
                //Use a set to record advertisement position
                map.put(i + count, new NativeExpressAdView(context));
                count++;
                eventList.add(new Event());
            }
            eventList.add(events.get(i));
        }

        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

    public Map<Integer, NativeExpressAdView> getMap() {
        return map;
    }

    public List<Event> getEventList() {
        return eventList;
    }

    //Compare this to view holder against ListView adapter
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView location;
        public TextView description;
        public TextView time;
        public ImageView imgview;
        public View layout;

        public ImageView img_view_good;
        public ImageView img_view_comment;

        public TextView good_number;
        public TextView comment_number;

        public ViewHolder(View v) {
            super(v);
            layout = v;
            title = (TextView) v.findViewById(R.id.event_item_title);
            location = (TextView) v.findViewById(R.id.event_item_location);
            description = (TextView) v.findViewById(R.id.event_item_description);
            time = (TextView) v.findViewById(R.id.event_item_time);
            imgview = (ImageView) v.findViewById(R.id.event_item_img);
            img_view_good = (ImageView) v.findViewById(R.id.event_good_img);
            img_view_comment = (ImageView) v.findViewById(R.id.event_comment_img);
            good_number = (TextView) v.findViewById(R.id.event_good_number);
            comment_number = (TextView) v.findViewById(R.id.event_comment_number);
        }
    }

    public class ViewHolderAds extends RecyclerView.ViewHolder {
        public ViewHolderAds(View v) {
            super(v);
        }
    }
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        switch (holder.getItemViewType()) {
            case TYPE_ITEM:
                ViewHolder viewHolderItem = (ViewHolder) holder;
                configureItemView(viewHolderItem, position);
                break;
            case TYPE_ADS:
                ViewHolderAds viewHolderAds = (ViewHolderAds) holder;
                configureAdsView(viewHolderAds, position);
                break;
        }
    }

    private void configureItemView(final ViewHolder holder, final int position) {
        final Event event = eventList.get(position);
        holder.title.setText(event.getTitle());
        String[] locations = event.getAddress().split(",");
        holder.location.setText(locations[1] + "," + locations[2]);
        holder.description.setText(event.getDescription());
        holder.time.setText(Utils.timeTransformer(event.getTime()));
        holder.good_number.setText(String.valueOf(event.getLike()));
        holder.comment_number.setText(String.valueOf(event.getCommentNumber()));


        if (event.getImgUri() != null) {
            final String url = event.getImgUri();
            holder.imgview.setVisibility(View.VISIBLE);
            new AsyncTask<Void, Void, Bitmap>(){
                @Override
                protected Bitmap doInBackground(Void... params) {
                    return Utils.getBitmapFromURL(url);
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    holder.imgview.setImageBitmap(bitmap);
                }
            }.execute();
        } else {
            holder.imgview.setVisibility(View.GONE);
        }

        Map<String, Boolean> voted = event.getMap();
        if (!voted.containsKey(Utils.username) || !voted.get(Utils.username)) {
            holder.img_view_good.setImageResource(R.drawable.like);
        } else {
            holder.img_view_good.setImageResource(R.drawable.liked);
        }

        holder.img_view_good.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.child("events").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Event recordedevent = snapshot.getValue(Event.class);
                            if (recordedevent.getId().equals(event.getId())) {
                                int number = recordedevent.getLike();
                                Map<String, Boolean> votedUsers = event.getMap();
                                Map<String, Object> childUpdates = new HashMap<>();
                                if (!votedUsers.containsKey(Utils.username) || !votedUsers.get(Utils.username)) {
                                    votedUsers.put(Utils.username, true);
                                    holder.img_view_good.setImageResource(R.drawable.liked);
                                    holder.good_number.setText(String.valueOf(number + 1));
                                    number++;
                                } else {
                                    votedUsers.put(Utils.username, false);
                                    holder.img_view_good.setImageResource(R.drawable.like);
                                    holder.good_number.setText(String.valueOf(number - 1));
                                    number--;
                                }
                                event.setMap(votedUsers);
                                event.setLike(number);
                                Map<String, Object> votedValues = event.toMap();
                                childUpdates.put("/events/" + event.getId(), votedValues);
                                databaseReference.updateChildren(childUpdates);
                                break;
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CommentActivity.class);
                String eventId = event.getId();
                intent.putExtra("EventID", eventId);
                context.startActivity(intent);
            }
        });
    }

    private void configureAdsView(final ViewHolderAds adsHolder, final int position) {
        ViewHolderAds nativeExpressHolder =
                (ViewHolderAds) adsHolder;
        if (!map.containsKey(position)) {
            return;
        }
        NativeExpressAdView adView = map.get(position);
        ViewGroup adCardView = (ViewGroup) nativeExpressHolder.itemView;
        if (adCardView.getChildCount() > 0) {
            adCardView.removeAllViews();
        }

        if (adView.getParent() != null) {
            ((ViewGroup) adView.getParent()).removeView(adView);
        }

        adCardView.addView(adView);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public void add(int position, Event event) {
        eventList.add(position, event);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        eventList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v;
        switch (viewType) {
            case TYPE_ITEM:
                v = inflater.inflate(R.layout.event_list_item, parent, false);
                viewHolder = new ViewHolder(v);
                break;
            case TYPE_ADS:
                v = inflater.inflate(R.layout.ads_container,
                        parent, false);
                viewHolder = new ViewHolderAds(v);
                break;
        }
        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        return map.containsKey(position) ? TYPE_ADS : TYPE_ITEM;
    }
}
