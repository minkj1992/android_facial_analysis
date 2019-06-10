package spartons.com.imagecropper;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Matrix;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class firestore {
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private Map<String, Object> user = null;

    public void setDb() {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://fir-ui-4330a.appspot.com");
//        gs://fir-ui-4330a.appspot.com/faces
    }
    
    public StorageReference getStorageRef() {
        return storageRef;
    }
    public FirebaseFirestore getDb() {
        return db;
    }
    // UID search -> UID table make or alter(delete pic) -> store pic at fire storage -> show analysis -> 다시 select 하면 모든 classifier clear
    // UID search


    public void searchDB(String uid, float[] score, Bitmap bitmap) {
        DocumentReference docRef = db.collection("users").document(uid);
        Log.v("minkj1992", "searchDB start DocumentReference: " + docRef);
        final boolean[] flag = {false};
        String fileName = uid+".jpg";
        StorageReference faceRef = storageRef.child("faces/"+fileName);
        Log.v("minkj1992", "searchDB StorageReference: " + storageRef);
        Log.v("minkj1992", "searchDB faceRef: " + faceRef);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        final Uri[] downloadUrl = new Uri[1];


        UploadTask uploadTask = faceRef.putBytes(data);


        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return faceRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    downloadUrl[0] = task.getResult();
                    Log.v("minkj1992", "Task url save successed: "+downloadUrl[0]);


                    // url 저장이 완료되어야 다음으로 진행하겠다.(이렇게 하지 않으니까 url 값이 null이 된다.)
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    flag[0] = true;
                                    alterUserDB(docRef, score,faceRef.getName(),downloadUrl[0]);
                                    Log.d("minkj1992", "DocumentSnapshot data: " + document.getData());
                                } else {
                                    makeUserDB(uid, score,faceRef.getName(),downloadUrl[0]);
                                    Log.d("minkj1992", "No such document");
                                }
                            } else {
                                Log.d("minkj1992", "get failed with ", task.getException());
                            }
                        }
                    });

                } else {
                    Log.v("minkj1992", "Task url save onFailured: 사진url 저장이 원할하지 않아, 저장을 취소합니다. ");

                }
            }
        });


    }

    //UID make
    private void makeUserDB(String uid, float[] score, String pid, Uri url) {
        user = new HashMap<>();
        user.put("athlete", score[0]);
        user.put("celebrity", score[1]);
        user.put("ceo", score[2]);
        user.put("crime", score[3]);
        user.put("professor", score[4]);
        user.put("pid", pid);
        user.put("url", url.toString());

        // Add a new document with a generated ID
        db.collection("users")
                .document(uid)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "신규회원님의 자료가 Save되었습니다.", Toast.LENGTH_LONG).show();
                        Log.v("minkj1992","신규회원님의 자료가 Save되었습니다.");
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

    private void alterUserDB(DocumentReference document, float[] score, String pid, Uri url) {
        document.update("athlete", score[0]);
        document.update("celebrity", score[1]);
        document.update("ceo", score[2]);
        document.update("crime", score[3]);
        document.update("professor", score[4]);
        document.update("pid", pid);
        document.update("url", url.toString())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "사진 및 분석 데이터가 Update되었습니다.", Toast.LENGTH_LONG).show();
                        Log.v("minkj1992","사진 및 분석 데이터가 Update되었습니다.");
                        Log.w("minkj1992", "alter user data Updated Successfully");
                    }
                });
    }
}
