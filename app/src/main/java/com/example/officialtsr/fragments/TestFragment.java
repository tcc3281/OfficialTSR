package com.example.officialtsr.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.officialtsr.R;

public class TestFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_test, container, false);
        
        Log.d("TestFragment", "onCreateView called");
        
        // Set up button click
        Button testButton = view.findViewById(R.id.test_button);
        testButton.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Test button clicked", Toast.LENGTH_SHORT).show();
        });
        
        return view;
    }
}
