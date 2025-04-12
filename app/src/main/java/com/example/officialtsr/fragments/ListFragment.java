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

        fetchTrafficSigns();

        return view;
    }

    private void fetchTrafficSigns() {
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

                        // Log the retrieved fields
                        Log.d(TAG, "Document ID: " + document.getId());
                        Log.d(TAG, "IMAGE_LINK: " + imageLink);
                        Log.d(TAG, "LAW_ID: " + lawId);
                        Log.d(TAG, "SIGN_NAME: " + signName);
                        Log.d(TAG, "TYPE: " + type);
                        Log.d(TAG, "DESCRIPTION: " + description);

                        // Handle null values gracefully
                        if (imageLink != null && lawId != null && signName != null && type != null) {
                            trafficSigns.add(new TrafficSign(
                                    null,
                                    description != null ? description : "No description available", // Default value for null
                                    imageLink,
                                    lawId,
                                    signName,
                                    type
                            ));
                        }
                    });
                    adapter.notifyDataSetChanged();
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch data: " + e.getMessage(), e);
                    Toast.makeText(getContext(), "Failed to fetch data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
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
