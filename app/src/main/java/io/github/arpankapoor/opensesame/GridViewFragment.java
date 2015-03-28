package io.github.arpankapoor.opensesame;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GridViewFragment extends Fragment {

    private CamInfoAdapter mCamAdapter;

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
        mCamAdapter = new CamInfoAdapter(getActivity());

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_grid_view, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.gridview);
        gridView.setAdapter(mCamAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                CamInfo camInfo = mCamAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), VideoActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, camInfo.id);
                startActivity(intent);
            }
        });

        return rootView;
    }

    private void updateCamInfo() {
        FetchCamTask camTask = new FetchCamTask();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String centralServer = preferences.getString(getString(R.string.pref_cs_addr_key),
                getString(R.string.pref_default_cs_addr));
        camTask.execute(centralServer);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if we have internet
        ConnectivityManager cm =
                (ConnectivityManager) getActivity()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (!isConnected) {
            Toast.makeText(getActivity(), R.string.err_no_net, Toast.LENGTH_LONG).show();
        } else {
            updateCamInfo();
        }
    }

    public class FetchCamTask extends AsyncTask<String, Void, CamInfo[]> {

        private final String LOG_TAG = FetchCamTask.class.getSimpleName();

        private CamInfo[] getCamDataFromJson(String camJsonStr) throws JSONException {

            JSONObject camJson = new JSONObject(camJsonStr);
            JSONArray camArray = camJson.getJSONArray(getString(R.string.json_list_key));

            int noOfCams = camArray.length();
            CamInfo[] camInfo = new CamInfo[noOfCams];

            for (int i = 0; i < noOfCams; ++i) {
                JSONObject cam = camArray.getJSONObject(i);

                camInfo[i] = new CamInfo();
                camInfo[i].id = cam.getInt(getString(R.string.json_id_key));
                camInfo[i].isPrivate = cam.getInt(getString(R.string.json_is_private_key)) == 1;
                camInfo[i].name = cam.getString(getString(R.string.json_name_key));
                camInfo[i].status = cam.getInt(getString(R.string.json_status_key)) == 1;
            }

            return camInfo;
        }

        @Override
        protected CamInfo[] doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String camJsonStr = null;

            try {
                final String BASE_URL = params[0];

                URL url = new URL("http", BASE_URL, getString(R.string.cam_list_query_url));

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
        protected void onPostExecute(CamInfo[] result) {
            if (result != null) {
                mCamAdapter.clear();
                for (CamInfo cam : result) {
                    mCamAdapter.add(cam);
                }
            }

            if (mCamAdapter == null || mCamAdapter.isEmpty()) {
                Toast.makeText(getActivity(), R.string.err_no_cams, Toast.LENGTH_LONG).show();
            }
        }
    }
}
