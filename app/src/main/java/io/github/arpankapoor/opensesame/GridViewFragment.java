package io.github.arpankapoor.opensesame;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class GridViewFragment extends Fragment {

    private ArrayAdapter<String> mCamAdapter;

    public GridViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mCamAdapter =
                new ArrayAdapter<>(
                        getActivity(),
                        R.layout.grid_item,
                        R.id.grid_item_textview,
                        new ArrayList<String>());

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_grid_view, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.gridview);
        gridView.setAdapter(mCamAdapter);

        return rootView;
    }

    private void updateCamInfo() {
        FetchCamTask camTask = new FetchCamTask();
        camTask.execute();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateCamInfo();
    }

    public class FetchCamTask extends AsyncTask<Void, Void, String[]> {

        private final String LOG_TAG = FetchCamTask.class.getSimpleName();

        private String[] getCamDataFromJson(String camJsonstr) throws JSONException {

            JSONObject camJson = new JSONObject(camJsonstr);
            JSONArray camArray = camJson.getJSONArray("cameras");

            int noOfCams = camArray.length();
            String[] resultStrs = new String[noOfCams];

            for (int i = 0; i < noOfCams; ++i) {

                JSONObject cam = camArray.getJSONObject(i);
                resultStrs[i] = cam.getString("name");
            }

            return resultStrs;
        }

        @Override
        protected String[] doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String camJsonStr = null;

            try {
                final String BASE_URL = "http://192.168.0.101/cgi-bin/get_cam_info.py";

                URL url = new URL(BASE_URL);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                    buffer.append('\n');
                }

                if (buffer.length() == 0) {
                    return null;
                }

                camJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }

                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getCamDataFromJson(camJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                mCamAdapter.clear();
                for (String cam : result) {
                    mCamAdapter.add(cam);
                }
            }
        }
    }
}
