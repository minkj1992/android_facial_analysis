package spartons.com.imagecropper;



import java.util.ArrayList;
import java.util.List;


public class galleryUtils {
    public static final List<Friend> friends = new ArrayList<>();
//    private FirebaseFirestore db;
//    private FirebaseStorage storage;
//    private StorageReference storageRef;
//    private Map<String, Object> user = null;
//
//
//    public galleryUtils(){
//        db = FirebaseFirestore.getInstance();
//        storage = FirebaseStorage.getInstance();
//        storageRef = storage.getReferenceFromUrl("gs://fir-ui-4330a.appspot.com");
//
//        db.collection("users").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (document.exists()) {
//                        // 데이터도 가져가야 한다.
////                            Map<String, Object> tmpData = document.getData();
//                        result[0] =  Float.valueOf(String.valueOf(document.getDouble("athlete")));
//                        result[1] =Float.valueOf(String.valueOf(document.getDouble("celebrity")));
//                        result[2] = Float.valueOf(String.valueOf(document.getDouble("ceo")));
//                        result[3] = Float.valueOf(String.valueOf(document.getDouble("crime")));
//                        result[4] = Float.valueOf(String.valueOf(document.getDouble("professor")));
//
//
//                        Glide.with(getApplicationContext())
//                                .asBitmap()
//                                .load(Uri.parse((String) document.get("url")))
//                                .into(target);
//                    }
//                } else {
//                    Log.v("minkj1992","task.isSuccessful() 실패");
//                }
//            }
//        });
//
//    }
    //@COVERTHIS firebase 모든 float데이터를 바탕으로 값들 가져오기, 이미지 색깔 random, 루프 돌면서 add 시켜주기.
    // 여기에 첫인자로 bitmap을 넣어주는 건?
    static {
//        friends.add(new Friend((String)url,id,R.color.sienna,"86점", "56점", "Music", "Art", "Technology"));
//        friends.add(new Friend(R.drawable.anastasia, "ANASTASIA", R.color.sienna, "86점", "56점", "Music", "Art", "Technology"));
        friends.add(new Friend(R.drawable.anastasia, "ANASTASIA", R.color.sienna, "근력: 86점", "매력: 56점", "카리스마: 14점", "범죄력: 48점", "지력: 14점"));
        friends.add(new Friend(R.drawable.irene, "IRENE", R.color.saffron, "근력: 12점", "매력: 56점", "카리스마: 6점", "범죄력: 48점", "지력: 14점"));
        friends.add(new Friend(R.drawable.kate, "KATE", R.color.green, "근력: 27점", "매력: 56점", "카리스마: 85점", "범죄력: 48점", "지력: 14점"));
        friends.add(new Friend(R.drawable.paul, "PAUL", R.color.pink, "근력: 65점", "매력: 56점", "카리스마: 43점", "범죄력: 48점", "지력: 14점"));
        friends.add(new Friend(R.drawable.daria, "DARIA", R.color.orange, "근력: 24점", "매력: 56점", "카리스마: 12점", "범죄력: 57점", "지력: 14점"));
        friends.add(new Friend(R.drawable.kirill, "KIRILL", R.color.saffron, "근력: 18점", "매력: 56점", "카리스마: 17점", "범죄력: 48점", "지력: 14점"));
        friends.add(new Friend(R.drawable.julia, "JULIA", R.color.green, "근력: 27점", "매력: 56점", "카리스마: 47점", "범죄력: 71점", "지력: 14점"));
        friends.add(new Friend(R.drawable.yalantis, "YALANTIS", R.color.purple, "근력: 84점", "매력: 56점", "카리스마: 32점", "범죄력: 38점", "지력: 14점"));
    }
}