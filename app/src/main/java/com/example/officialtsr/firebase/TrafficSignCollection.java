package com.example.officialtsr.firebase;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class TrafficSignCollection {

    private final FirebaseFirestore db;

    public TrafficSignCollection() {
        this.db = FirebaseFirestore.getInstance();
    }

    public CollectionReference getTrafficSignsCollection() {
        return db.collection("TrafficSign");
    }
}
