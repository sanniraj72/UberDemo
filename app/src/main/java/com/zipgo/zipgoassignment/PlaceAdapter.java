package com.zipgo.zipgoassignment;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import io.realm.Realm;

/**
 * PlaceAdapter Class Definition
 */
public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceHolder> {

    private Context context;
    private List<com.zipgo.zipgoassignment.Place> places;

    /**
     * Constructor
     *
     * @param context context
     * @param places  places
     */
    PlaceAdapter(Context context, List<com.zipgo.zipgoassignment.Place> places) {
        this.places = places;
        this.context = context;
    }

    /**
     * Called when RecyclerView needs a new {@link android.support.v7.widget.RecyclerView.ViewHolder} of the given type to represent
     * an item.
     * <p>
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     * <p>
     * The new ViewHolder will be used to display items of the adapter using
     * {@link #onBindViewHolder(android.support.v7.widget.RecyclerView.ViewHolder, int, java.util.List)}. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary {@link View#findViewById(int)} calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(android.support.v7.widget.RecyclerView.ViewHolder, int)
     */
    @NonNull
    @Override
    public PlaceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return new PlaceHolder(inflater.inflate(R.layout.place_item, parent, false));
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link android.support.v7.widget.RecyclerView.ViewHolder#itemView} to reflect the item at the given
     * position.
     * <p>
     * Note that unlike {@link ListView}, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use {@link android.support.v7.widget.RecyclerView.ViewHolder#getAdapterPosition()} which will
     * have the updated adapter position.
     * <p>
     * Override {@link #onBindViewHolder(android.support.v7.widget.RecyclerView.ViewHolder, int, java.util.List)} instead if Adapter can
     * handle efficient partial bind.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull PlaceHolder holder, int position) {
        holder.nameView.setText(places.get(position).getPlaceName());
        double latitude = places.get(position).getLatitude();
        double longitude = places.get(position).getLongitude();
        String latLong = String.valueOf(latitude) + ", " + String.valueOf(longitude);
        holder.latView.setText(latLong);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return places.size();
    }

    /**
     * ViewHolder for Place
     */
    class PlaceHolder extends RecyclerView.ViewHolder {

        TextView nameView;
        TextView latView;
        ImageView deleteImageView;

        /**
         * Constructor
         *
         * @param itemView itemView
         */
        PlaceHolder(View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.place_name_view);
            latView = itemView.findViewById(R.id.place_lat_view);
            deleteImageView = itemView.findViewById(R.id.delete_image);
            deleteImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String id = places.get(getAdapterPosition()).getId();
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    Place place = realm.where(Place.class).contains("id", id).findFirst();
                    if (place != null) {
                        place.deleteFromRealm();
                        places.remove(getAdapterPosition());
                    }
                    realm.commitTransaction();
                    notifyDataSetChanged();
                }
            });
        }
    }
}
