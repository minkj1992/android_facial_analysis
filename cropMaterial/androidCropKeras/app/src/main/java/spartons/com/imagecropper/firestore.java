package spartons.com.imagecropper;

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class firestore {
    private FirebaseFirestore db;
    private Map<String, Object> user = null;

    public void setDb() {
        db = FirebaseFirestore.getInstance();
    }

    public FirebaseFirestore getDb() {
        return db;
    }
    // UID search -> UID table make or alter(delete pic) -> store pic at fire storage -> show analysis -> 다시 select 하면 모든 classifier clear
    // UID search


    public boolean searchDB(String uid, float[] score) {
        DocumentReference docRef = db.collection("users").document(uid);
        final boolean[] flag = {false};
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        flag[0] = true;
                        alterUserDB(docRef, score);
                        Log.d("minkj1992", "DocumentSnapshot data: " + document.getData());
                    } else {
                        makeUserDB(uid, score);
                        Log.d("minkj1992", "No such document");
                    }
                } else {
                    Log.d("minkj1992", "get failed with ", task.getException());
                }
            }
        });

        //if exist return true -> alterUserDB
        //if not exist return false -> makeUserDB
        if (flag[0]) {
            return true;
        }
        return false;
    }

    //UID make
    private void makeUserDB(String uid, float[] score) {
        user = new HashMap<>();
        user.put("athlete", score[0]);
        user.put("celebrity", score[1]);
        user.put("ceo", score[2]);
        user.put("crime", score[3]);
        user.put("professor", score[4]);
//        user.put("pid",);

        // Add a new document with a generated ID
        db.collection("users")
                .document(uid)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("minkj1992", "firestore successfully saved!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("minkj1992", "Error adding document", e);
                    }
                });
    }

    private void alterUserDB(DocumentReference document, float[] score) {
        document.update("athlete", score[0]);
        document.update("celebrity", score[1]);
        document.update("ceo", score[2]);
        document.update("crime", score[3]);
        document.update("professor", score[4])
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.w("minkj1992", "alter user data Updated Successfully");
                    }
                });
    }
}
