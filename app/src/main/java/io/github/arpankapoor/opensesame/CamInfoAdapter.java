package io.github.arpankapoor.opensesame;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CamInfoAdapter extends ArrayAdapter<CamInfo> {
    public CamInfoAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CamInfo camInfo = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item, parent, false);
        }

        TextView textView = (TextView) convertView.findViewById(R.id.grid_item_textview);
        textView.setText(camInfo.name);

        // Set background according to the status
        int background;
        if (camInfo.status) {
            background = Color.argb(50, 0, 255, 0);
        } else {
            background = Color.argb(50, 255, 0, 0);
        }

        textView.setBackgroundColor(background);
        return convertView;
    }
}
