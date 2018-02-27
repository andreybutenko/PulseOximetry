package edu.washington.cs.sensor.pulseoximetry;


import android.content.Context;
import android.content.Intent;
import android.icu.util.Measure;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import edu.washington.cs.sensor.pulseoximetry.R;
import edu.washington.cs.sensor.pulseoximetry.models.Measurement;

public class HistoryFragment extends Fragment {
    private ListView historyListView;
    private MeasurementArrayAdapter measurementArrayAdapter;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();

        final List<Measurement> measurementList = Measurement.find(Measurement.class, "");
        Collections.reverse(measurementList);

        historyListView = (ListView) getActivity().findViewById(R.id.history_list);
        measurementArrayAdapter = new MeasurementArrayAdapter(getContext(), measurementList);
        historyListView.setAdapter(measurementArrayAdapter);

        historyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String dateTime = measurementList.get(i).getRawTime();

                Intent intent = new Intent(getContext(), MeasurementDetailActivity.class);
                intent.putExtra(MeasurementDetailActivity.DATE_TIME_EXTRA, dateTime);
                startActivity(intent);
            }
        });

        historyListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String dateTime = measurementList.get(i).getRawTime();

                Intent intent = new Intent(getContext(), FilterPlaygroundActivity.class);
                intent.putExtra(MeasurementDetailActivity.DATE_TIME_EXTRA, dateTime);
                startActivity(intent);

                return true;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    private class MeasurementArrayAdapter extends ArrayAdapter<Measurement> {
        private final Context context;
        private final List<Measurement> values;

        MeasurementArrayAdapter(Context context, List<Measurement> values) {
            super(context, -1, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.list_measurement, parent, false);

            Measurement measurement = values.get(position);

            TextView dateTimeTextView = (TextView) rowView.findViewById(R.id.date_time_text);
            dateTimeTextView.setText(measurement.getFormattedTime());

            return rowView;
        }
    }
}
