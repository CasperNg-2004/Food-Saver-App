package com.example.group12_asgmt2;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class FragmentStatistics extends Fragment {

    private LineChart lineChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 1. Inflate the XML layout we made earlier
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        // 2. Link the chart from the XML to this Java code
        lineChart = view.findViewById(R.id.lineChartWaste);

        // 3. Draw the chart!
        setupLineChart();

        return view;
    }

    private void setupLineChart() {
        // --- MOCK DATA ---
        // X = Day of week (0=Mon, 1=Tue...), Y = Waste amount
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 2f)); // Mon
        entries.add(new Entry(1, 4f)); // Tue
        entries.add(new Entry(2, 3f)); // Wed
        entries.add(new Entry(3, 8f)); // Thu
        entries.add(new Entry(4, 5f)); // Fri
        entries.add(new Entry(5, 9f)); // Sat
        entries.add(new Entry(6, 6f)); // Sun

        // --- STYLING THE LINE ---
        LineDataSet dataSet = new LineDataSet(entries, "Weekly Waste");
        dataSet.setColor(Color.parseColor("#2ECC71")); // Neon Green
        dataSet.setLineWidth(4f); // Thick line
        dataSet.setCircleColor(Color.parseColor("#2ECC71"));
        dataSet.setCircleRadius(6f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // This makes the line curvy and smooth!
        dataSet.setDrawValues(false); // Hides the numbers floating above the dots

        // --- APPLY TO CHART ---
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // --- CLEAN UP THE UI ---
        lineChart.getDescription().setEnabled(false); // Remove "Description Label"
        lineChart.getLegend().setEnabled(false); // Remove the legend at the bottom

        // Hide the right axis, format the left axis
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getAxisLeft().setTextColor(Color.parseColor("#8B9D92"));
        lineChart.getAxisLeft().setDrawGridLines(false); // Remove background grid

        // Format the bottom axis (X-Axis)
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.parseColor("#8B9D92"));
        xAxis.setDrawGridLines(false);

        // Refresh the chart to show it
        lineChart.invalidate();
    }
}