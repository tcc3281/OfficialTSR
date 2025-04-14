package com.example.officialtsr.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.officialtsr.R;
import com.example.officialtsr.models.TrafficSign;
import com.example.officialtsr.adapters.TrafficSignAdapter;
import com.example.officialtsr.activities.MainActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ListFragment extends Fragment {

    private static final String TAG = "ListFragment";

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TrafficSignAdapter adapter;
    private List<TrafficSign> trafficSigns;
    private boolean isDataLoaded = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        trafficSigns = new ArrayList<>();
        adapter = new TrafficSignAdapter(getContext(), trafficSigns, this::showTrafficSignDetails);
        recyclerView.setAdapter(adapter);

        loadTrafficSigns();

        return view;
    }

    private void loadTrafficSigns() {
        MainActivity mainActivity = (MainActivity) requireActivity();
        List<TrafficSign> cachedTrafficSigns = mainActivity.getCachedTrafficSigns();

        if (!cachedTrafficSigns.isEmpty()) {
            trafficSigns.clear();
            trafficSigns.addAll(cachedTrafficSigns);
            adapter.notifyDataSetChanged();
            showLoading(false);
        } else if (!isDataLoaded) {
            showLoading(true);
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("TrafficSign")
                .orderBy("SIGN_NAME", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    trafficSigns.clear();
                    queryDocumentSnapshots.forEach(document -> {
                        String imageLink = document.getString("IMAGE_LINK");
                        String lawId = document.getString("LAW_ID");
                        String signName = document.getString("SIGN_NAME");
                        String type = document.getString("TYPE");
                        String description = document.getString("DESCRIPTION");

                        if (imageLink != null && lawId != null && signName != null && type != null) {
                            trafficSigns.add(new TrafficSign(
                                null,
                                description != null ? description : "No description available",
                                imageLink,
                                lawId,
                                signName,
                                type
                            ));
                        }
                    });

                    mainActivity.saveTrafficSigns(trafficSigns);
                    adapter.notifyDataSetChanged();
                    isDataLoaded = true;
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load traffic signs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
        }
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void showTrafficSignDetails(TrafficSign trafficSign) {
        TrafficSignDetailsFragment detailsFragment = new TrafficSignDetailsFragment();

        Bundle bundle = new Bundle();
        bundle.putString("imageLink", trafficSign.getImageLink());
        bundle.putString("lawId", trafficSign.getLawId());
        bundle.putString("signName", trafficSign.getSignName());
        bundle.putString("type", trafficSign.getType());
        bundle.putString("description", trafficSign.getDescription());
        detailsFragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, detailsFragment)
            .addToBackStack(null)
            .commit();
    }
}
