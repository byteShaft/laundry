package com.byteshaft.laundry.utils;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.byteshaft.laundry.AddressesActivity;
import com.byteshaft.laundry.PickLDropLaundryActivity;
import com.byteshaft.laundry.R;
import com.byteshaft.requests.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.byteshaft.laundry.AddressesActivity.sSelectedPosition;

/**
 * Created by shahid on 04/01/2017.
 */

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    JSONArray mItems;

    public ExpandableListAdapter(Context context, JSONArray items) {
        mContext = context;
        mItems = items;
    }

    static class ViewHolder {
        TextView headerTextView;
        ImageView collapseExpandIndicator;
    }

    static class SubItemsViewHolder {
        TextView pickupCity;
        TextView pickupStreet;
        TextView pickupHouse;
        TextView pickupZipCode;
        TextView pickupLocation;
        ImageButton editButton;
        ImageButton deletButton;

        // drop textViews
        TextView dropCity;
        TextView dropStreet;
        TextView dropHouse;
        TextView dropZipCode;
        TextView dropLocation;
        RelativeLayout relativeLayout;

    }


    @Override
    public Object getChild(int groupPosition, int childPosition) {
        try {
            return mItems.get(groupPosition);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final SubItemsViewHolder holder;

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_item_delegate, null);
            holder = new SubItemsViewHolder();

            holder.relativeLayout = (RelativeLayout) convertView.findViewById(R.id.drop_layout);
            // pickup textViews
            holder.pickupCity = (TextView) convertView.findViewById(R.id.pickup_city);
            holder.pickupStreet = (TextView) convertView.findViewById(R.id.pickup_street_number);
            holder.pickupHouse = (TextView) convertView.findViewById(R.id.pickup_house_name);
            holder.pickupZipCode = (TextView) convertView.findViewById(R.id.pickup_zip_code_);
            holder.pickupLocation = (TextView) convertView.findViewById(R.id.pickup_location);
            holder.editButton = (ImageButton) convertView.findViewById(R.id.edit);
            holder.deletButton = (ImageButton) convertView.findViewById(R.id.delete);
            holder.pickupCity.setTypeface(AppGlobals.typefaceNormal);
            holder.pickupStreet.setTypeface(AppGlobals.typefaceNormal);
            holder.pickupHouse.setTypeface(AppGlobals.typefaceNormal);
            holder.pickupZipCode.setTypeface(AppGlobals.typefaceNormal);
            holder.pickupLocation.setTypeface(AppGlobals.typefaceNormal);
            // drop textViews
            holder.dropCity = (TextView) convertView.findViewById(R.id.drop_city);
            holder.dropStreet = (TextView) convertView.findViewById(R.id.drop_street_number);
            holder.dropHouse = (TextView) convertView.findViewById(R.id.drop_house_name);
            holder.dropZipCode = (TextView) convertView.findViewById(R.id.drop_zip_code_);
            holder.dropLocation = (TextView) convertView.findViewById(R.id.drop_location);
            holder.dropCity.setTypeface(AppGlobals.typefaceNormal);
            holder.dropStreet.setTypeface(AppGlobals.typefaceNormal);
            holder.dropHouse.setTypeface(AppGlobals.typefaceNormal);
            holder.dropZipCode.setTypeface(AppGlobals.typefaceNormal);
            holder.dropLocation.setTypeface(AppGlobals.typefaceNormal);
            convertView.setTag(holder);
        } else {
            holder = (SubItemsViewHolder) convertView.getTag();
        }
        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    JSONObject jsonObject = (JSONObject) getChild(groupPosition, childPosition);
                    Intent intent = new Intent(mContext, PickLDropLaundryActivity.class);
                    intent.putExtra("id", jsonObject.getString("id"));
                    intent.putExtra("title", jsonObject.getString("name"));
                    intent.putExtra("city", jsonObject.getString("pickup_city"));
                    intent.putExtra("street", jsonObject.getString("pickup_street"));
                    intent.putExtra("house", jsonObject.getString("pickup_house_number"));
                    intent.putExtra("zip", jsonObject.getString("pickup_zip"));
                    intent.putExtra("pick_location", jsonObject.getString("location"));
                    intent.putExtra("boolean", jsonObject.getBoolean("drop_on_pickup_location"));
                    intent.putExtra("drop_city", jsonObject.getString("drop_city"));
                    intent.putExtra("drop_street", jsonObject.getString("drop_street"));
                    intent.putExtra("drop_house", jsonObject.getString("drop_house_number"));
                    intent.putExtra("drop_zip", jsonObject.getString("drop_zip"));
                    intent.putExtra("drop_location", jsonObject.getString("location"));
                    mContext.startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        holder.deletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject subItems = (JSONObject) getChild(groupPosition, childPosition);
                try {
                    deleteLocation(subItems.getInt("id"), groupPosition);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        JSONObject subItems = (JSONObject) getChild(groupPosition, childPosition);
        try {
            holder.pickupCity.setText("City: " + subItems.getString("pickup_city"));
            holder.pickupStreet.setText("Street#: " + subItems.getString("pickup_street"));
            holder.pickupHouse.setText("House#: " + subItems.getString("pickup_house_number"));
            holder.pickupZipCode.setText("Zip Code: " + subItems.getString("pickup_zip"));
            String loc = subItems.getString("location");
            Log.i("TAG", "loc" + loc);
            String[] pickDrop = loc.split("\\|");
            String removeLatLng = pickDrop[0].replaceAll("lat/lng: ", "").replace("(", "").replace(")", "");
            String[] latLng = removeLatLng.split(",");
            final double latitude = Double.parseDouble(latLng[0]);
            final double longitude = Double.parseDouble(latLng[1]);
            holder.pickupLocation.setText(latitude+","+longitude);
            holder.pickupLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String uri = String.format(Locale.ENGLISH, "geo:%f,%f", latitude, longitude);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    mContext.startActivity(intent);
                }
            });

            boolean drop_on_pickup_location = subItems.getBoolean("drop_on_pickup_location");
            Log.i("TAG", "drop_on_pickup_location" + drop_on_pickup_location);
            if (!drop_on_pickup_location) {
                holder.relativeLayout.setVisibility(View.VISIBLE);
                holder.dropCity.setText("City: " + subItems.getString("drop_city"));
                holder.dropStreet.setText("Street#: " + subItems.getString("drop_street"));
                holder.dropHouse.setText("House#: " + subItems.getString("drop_house_number"));
                holder.dropZipCode.setText("Zip Code: " + subItems.getString("drop_zip"));
                Log.i("TAG", "drop" + pickDrop[1]);
                String replaceLatLng = pickDrop[1].replaceAll("lat/lng: ", "").replace("(", "").replace(")", "");;
                String[] dropLatLng = replaceLatLng.split(",");
                final double dropLatitude = Double.parseDouble(dropLatLng[0]);
                final double dropLongitude = Double.parseDouble(dropLatLng[1]);
                holder.dropLocation.setText(dropLatitude+"," +dropLongitude);
                holder.dropLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String uri = String.format(Locale.ENGLISH, "geo:%f,%f", dropLatitude, dropLongitude);
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        mContext.startActivity(intent);
                    }
                });
            } else {
                holder.relativeLayout.setVisibility(View.GONE);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return convertView;
    }

    public static List<JSONObject> asList(final JSONArray ja) {
        final int len = ja.length();
        final ArrayList<JSONObject> result = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            final JSONObject obj = ja.optJSONObject(i);
            if (obj != null) {
                result.add(obj);
            }
        }
        return result;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        super.registerDataSetObserver(observer);
        notifyDataSetChanged();
    }

    public JSONArray remove(final int idx, final JSONArray from) {
        final List<JSONObject> objs = asList(from);
        objs.remove(idx);

        final JSONArray ja = new JSONArray();
        for (final JSONObject obj : objs) {
            ja.put(obj);
        }
        AddressesActivity.getInstance().expListView.deferNotifyDataSetChanged();
        return ja;
    }

    private void deleteLocation(int id, final int index) {
        HttpRequest request = new HttpRequest(AppGlobals.getContext());
        request.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        WebServiceHelpers.dismissProgressDialog();
                        Log.i("TAG", ""+ request.getStatus());
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_NO_CONTENT:
                                remove(index, mItems);
                                notifyDataSetChanged();
                                break;

                        }
                }
            }
        });
        request.setOnErrorListener(new HttpRequest.OnErrorListener() {
            @Override
            public void onError(HttpRequest request, int readyState, short error, Exception exception) {

            }
        });
        request.open("DELETE", String.format("%suser/addresses/%s", AppGlobals.BASE_URL, id));
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        request.send();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        try {
            return mItems.get(groupPosition);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getGroupCount() {
        return mItems.length();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_header_delegate, null);
            viewHolder = new ViewHolder();
            viewHolder.headerTextView = (TextView) convertView.findViewById(R.id.text_view_location_header);
            viewHolder.collapseExpandIndicator = (ImageView) convertView.findViewById(R.id.image_view_location_expand_collapse);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.headerTextView.setTypeface(AppGlobals.typefaceBold);
        JSONObject header = (JSONObject) getGroup(groupPosition);
        try {
            viewHolder.headerTextView.setAllCaps(true);
            viewHolder.headerTextView.setText(header.getString("name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (sSelectedPosition != -1 && sSelectedPosition == groupPosition) {
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.card_selected_color));
        } else {
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.colorPrimary));
        }

        if (isExpanded) {
            viewHolder.collapseExpandIndicator.setImageResource(R.mipmap.ic_collapse);
        } else {
            viewHolder.collapseExpandIndicator.setImageResource(R.mipmap.ic_expand);
        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}