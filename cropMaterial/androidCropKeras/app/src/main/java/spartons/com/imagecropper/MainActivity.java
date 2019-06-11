package spartons.com.imagecropper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import spartons.com.imagecropper.enums.ImagePickerEnum;
import spartons.com.imagecropper.listeners.IImagePickerLister;
import spartons.com.imagecropper.utils.FileUtils;
import spartons.com.imagecropper.utils.UiHelper;

public class MainActivity extends AppCompatActivity implements IImagePickerLister {
    private static final String LOG_TAG = "minkj1992";

    private static final int CAMERA_ACTION_PICK_REQUEST_CODE = 610;
    private static final int PICK_IMAGE_GALLERY_REQUEST_CODE = 609;
    public static final int CAMERA_STORAGE_REQUEST_CODE = 611;
    public static final int ONLY_CAMERA_REQUEST_CODE = 612;
    public static final int ONLY_STORAGE_REQUEST_CODE = 613;
    private static final int RC_SIGN_IN = 123;
    private static final int CLASSIFY_REQUEST_CODE = 724;


    private String currentPhotoPath = "";
    private UiHelper uiHelper = new UiHelper();
    private ImageView imageView;


    Toolbar mToolbar;
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
    Drawer mDrawerResult;
    AccountHeader mHeaderResult;
    ProfileDrawerItem mProfileDrawerItem;
    RelativeLayout relativeLayout;
    LinearLayout firebaseLayout;
    FrameLayout fragment_container;


    ProgressBar progressBar;


    private boolean isPicExist = true;



    PrimaryDrawerItem mItemLogin, mItemLogout, mItemVerifiedProfile, mItemHome, mItemSettings, mItemUnverifiedProfile, mCurrentProfile, mClassifier, mGallery;
    DividerDrawerItem line = new DividerDrawerItem();
    DividerDrawerItem line2 = new DividerDrawerItem();
    private Bitmap bitmap = null;
    private Bitmap gray = null;
    private static final String PP_URL = "https://iteritory.com/msadrud/install-or-setup-apache-ignite-in-windows-step-by-step-tutorial/";
    private static final String TOS_URL = "https://iteritory.com/msadrud/install-or-setup-apache-ignite-in-windows-step-by-step-tutorial/";

    private float[] result = new float[5];
    private firestore firestore;
    private DocumentReference docRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("galleryIntent"));

        //@TODO HERE START
        //ClassifyIntent() nav바에서는 작동 안하고 Result page 보여지는거로 하기
        imageView = findViewById(R.id.imageView);
        relativeLayout = findViewById(R.id.relative);
        firebaseLayout = findViewById(R.id.firebaseLayout);
        fragment_container = findViewById(R.id.fragment_container);

        progressBar = findViewById(R.id.progress);


        //material & firebaseAuth start
        setupToolbar();
        //유저 setting하기
        intstantiateUser();
        //메뉴 생성하기
        instantiateMenuItems();

        //firebase storage and Database setup
        try {
            firestore = new firestore();
            firestore.setDb();
        } catch (Exception e) {
            Toast.makeText(this, R.string.error_firestore_init_db, Toast.LENGTH_LONG).show();
            Log.e(LOG_TAG, "init(): Failed to create firestore_db", e);
        }

        setupProfileDrawer();
        setupNavigationDrawerWithHeader();
        //로그인 하지 않았으면 로그인하도록 유도
        if (!isUserSignedIn()) {
            onNavDrawerItemSelected(3);
        }
        //UCROP start
        findViewById(R.id.selectPictureButton).setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                if (uiHelper.checkSelfPermissions(this))
                    uiHelper.showImagePickerDialog(this, this);
        });

        findViewById(R.id.analyzeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                classifyIntent();
            }
        });

        findViewById(R.id.fbBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //저장시키는 method call
                firestore.searchDB(mFirebaseUser.getUid(), result, bitmap);
            }
        });

        findViewById(R.id.resetBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                모든 데이터 싹다 지워주어야한다. reTry
            }
        });
    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //힘들다 그냥, 현재 로그인된 얼굴 보여주자.
//            gray = grayScale(getResizedBitmap((Bitmap)intent.getParcelableExtra("BitmapImage"), 299, 299));
//            gray = intent.getIntExtra("idx",1);
//            result = intent.getFloatArrayExtra("result");
            switchFragment(7);
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                uiHelper.showImagePickerDialog(this, this);
            else if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_DENIED) {
                uiHelper.toast(this, "ImageCropper needs Storage access in order to store your profile picture.");
                finish();
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                uiHelper.toast(this, "ImageCropper needs Camera access in order to take profile picture.");
                finish();
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED && grantResults[1] == PackageManager.PERMISSION_DENIED) {
                uiHelper.toast(this, "ImageCropper needs Camera and Storage access in order to take profile picture.");
                finish();
            }
        } else if (requestCode == ONLY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                uiHelper.showImagePickerDialog(this, this);
            else {
                uiHelper.toast(this, "ImageCropper needs Camera access in order to take profile picture.");
                finish();
            }
        } else if (requestCode == ONLY_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                uiHelper.showImagePickerDialog(this, this);
            else {
                uiHelper.toast(this, "ImageCropper needs Storage access in order to store your profile picture.");
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_ACTION_PICK_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri uri = Uri.parse(currentPhotoPath);
            openCropActivity(uri, uri);
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = UCrop.getOutput(data);
                showImage(uri);
            }
        } else if (requestCode == PICK_IMAGE_GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            try {
                Uri sourceUri = data.getData();
                File file = getImageFile();
                Uri destinationUri = Uri.fromFile(file);
                openCropActivity(sourceUri, destinationUri);
            } catch (Exception e) {
                Log.e("minkj1992", String.valueOf(e));
                uiHelper.toast(this, "Please select another image");
            }
        } else if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            // Successfully signed in
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, R.string.login_success, Toast.LENGTH_LONG).show();
                signInUser();
                return;
            } else {
                //User pressed back button
                if (response == null) {
                    Toast.makeText(this, R.string.login_failed, Toast.LENGTH_LONG).show();
                    mDrawerResult.deselect(mItemLogin.getIdentifier());
                    return;
                }
                //No internet connection.
                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, R.string.no_connectivity, Toast.LENGTH_LONG).show();
                    return;
                }
                //Unknown error
                if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Toast.makeText(this, R.string.login_unknown_Error, Toast.LENGTH_LONG).show();
                    return;
                }
            }
        } else if (requestCode == CLASSIFY_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                // Float 5개 값 받는다.
                Log.v("minkj1992", "딥러닝 분석 완료");
                result = data.getFloatArrayExtra("result");
                Log.v("minkj1992", "딥러닝 분석 저장완료");
                switchFragment(7);


            }
        }
    }



    //Firebase save button render
    //Reset button
    //우선 fragment 생성하고
    //fragment에서 login ,upload, Result, Gallery
    //생성된 Result fragment에서 값을 보여주고, 저장 버튼 보여준다.
    // result,
    //TODO 여기서 부터 만지면 됩니당.

    //#######################################  Ucrop & Camera  #########################################
    private void openImagesDocument() {
        Intent pictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pictureIntent.setType("image/*");
        pictureIntent.addCategory(Intent.CATEGORY_OPENABLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String[] mimeTypes = new String[]{"image/jpeg", "image/png"};
            pictureIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        }
        startActivityForResult(Intent.createChooser(pictureIntent, "Select Picture"), PICK_IMAGE_GALLERY_REQUEST_CODE);
    }

    private void showImage(Uri imageUri) {
        try {
            File file;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                file = FileUtils.getFile(this, imageUri);
            } else {
                file = new File(currentPhotoPath);
            }
            InputStream inputStream = new FileInputStream(file);
            bitmap = BitmapFactory.decodeStream(inputStream);
            imageView.setImageBitmap(bitmap);
            inputStream.close();
        } catch (Exception e) {
            uiHelper.toast(this, "Please select different profile picture.");
        }
    }

    @Override
    public void onOptionSelected(ImagePickerEnum imagePickerEnum) {
        if (imagePickerEnum == ImagePickerEnum.FROM_CAMERA)
            openCamera();
        else if (imagePickerEnum == ImagePickerEnum.FROM_GALLERY)
            openImagesDocument();
    }

    private void openCamera() {
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file;
        try {
            file = getImageFile(); // 1
        } catch (Exception e) {
            e.printStackTrace();
            uiHelper.toast(this, "Please take another image");
            return;
        }
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) // 2
            uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID.concat(".provider"), file);
        else
            uri = Uri.fromFile(file); // 3
        pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri); // 4
        startActivityForResult(pictureIntent, CAMERA_ACTION_PICK_REQUEST_CODE);
    }

    private File getImageFile() throws IOException {
        String imageFileName = "JPEG_" + System.currentTimeMillis() + "_";
        File storageDir = new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM
                ), "Camera"
        );
        System.out.println(storageDir.getAbsolutePath());
        if (storageDir.exists())
            System.out.println("File exists");
        else
            System.out.println("File not exists");
        File file = File.createTempFile(
                imageFileName, ".jpg", storageDir
        );
        currentPhotoPath = "file:" + file.getAbsolutePath();
        return file;
    }

    private void openCropActivity(Uri sourceUri, Uri destinationUri) {
        UCrop.Options options = new UCrop.Options();
        options.setCircleDimmedLayer(true);
        options.setCropFrameColor(ContextCompat.getColor(this, R.color.colorAccent));
        UCrop.of(sourceUri, destinationUri)
//                .withMaxResultSize(100, 100)
//                .withAspectRatio(5f, 5f)
                .start(this);
    }


    //#######################################  mike penz material  #########################################
    //material design 시작
    // ToolBar 가져와서 현재 app에 부착
    private void setupToolbar() {
        mToolbar = findViewById(R.id.toolbarMain);
        //안드로이드 기본 method
        setSupportActionBar(mToolbar);
    }

    // 메뉴 디자인 설정 함수
    private void instantiateMenuItems() {
        mItemVerifiedProfile = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.verified_profile).withIcon(getResources().getDrawable(R.mipmap.ic_verified_user_black_24dp));
        mItemUnverifiedProfile = new PrimaryDrawerItem().withIdentifier(2).withName(R.string.unverified_profile).withIcon(getResources().getDrawable(R.mipmap.ic_report_problem_black_24dp));

        mItemLogin = new PrimaryDrawerItem().withIdentifier(3).withName(R.string.login_menu_item).withIcon(getResources().getDrawable(R.mipmap.ic_login_black_48dp));
        mItemLogout = new PrimaryDrawerItem().withIdentifier(4).withName(R.string.logout_menu_item).withIcon(getResources().getDrawable(R.mipmap.ic_logout_black_48dp));
        ;

        mItemHome = new PrimaryDrawerItem().withIdentifier(5).withName(R.string.home).withIcon(getResources().getDrawable(R.mipmap.ic_home_black_48dp));
        mItemSettings = new PrimaryDrawerItem().withIdentifier(6).withName(R.string.settings).withIcon(getResources().getDrawable(R.mipmap.ic_settings_black_48dp));
        mClassifier = new PrimaryDrawerItem().withIdentifier(7).withName(R.string.classify).withIcon(getResources().getDrawable(R.mipmap.ic_classify));
        mGallery = new PrimaryDrawerItem().withIdentifier(8).withName(R.string.gallery).withIcon(getResources().getDrawable(R.mipmap.ic_gallery));
    }

    // 로그인 x시 디폴트 프로필 디자인 가져오기, 로그인시 profile image가져오기
    private void setupProfileDrawer() {
        //check if the user is logged in. If logged in, get details (name, email, pic etc) dynamically
        //For demonstration purpose, I have set a personal photo hard coded. In real-time, we can easily
        // pass the actual photo dynamically.
        if (mFirebaseUser != null) {
            // 로드된 이미지를 받을 Target을 생성한다. 생성할 때, 크기를 지정해준다,
            SimpleTarget target = new SimpleTarget<Bitmap>(299,299) {
                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    super.onLoadFailed(errorDrawable);
                    progressBar.setVisibility(View.GONE);
                    isPicExist = false;
                    Log.v("minkj1992","glide 실패");
                }

                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    Log.v("minkj1992","glide 성공");
                    isPicExist = true;
                    progressBar.setVisibility(View.GONE);
                    bitmap = resource;
                    gray = grayScale(bitmap);
                    switchFragment(7);
                }
            };

            //@TODO 프로필 사진 가져오기
            docRef = firestore.getDb().collection("users").document(mFirebaseUser.getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // 데이터도 가져가야 한다.
//                            Map<String, Object> tmpData = document.getData();
                            result[0] =  Float.valueOf(String.valueOf(document.getDouble("athlete")));
                            result[1] =Float.valueOf(String.valueOf(document.getDouble("celebrity")));
                            result[2] = Float.valueOf(String.valueOf(document.getDouble("ceo")));
                            result[3] = Float.valueOf(String.valueOf(document.getDouble("crime")));
                            result[4] = Float.valueOf(String.valueOf(document.getDouble("professor")));


                            Glide.with(getApplicationContext())
                                    .asBitmap()
                                    .load(Uri.parse((String) document.get("url")))
                                    .into(target);
                        }
                    } else {
                        Log.v("minkj1992","task.isSuccessful() 실패");
                    }
                }
            });

//            StorageReference storageReference = firestore.getStorageRef();




            mProfileDrawerItem = new ProfileDrawerItem()
                    .withName(mFirebaseUser.getDisplayName())
                    .withEmail(mFirebaseUser.getEmail())
//                    @TODO glide로 가져온것 withicon으로 setting하면 된다.
                    .withIcon(getResources().getDrawable(R.drawable.profile));
//                    .withIcon(firestore mFirebaseUser.getUid());
        } else {//else if the user is not logged in, show a default icon
            mProfileDrawerItem = new ProfileDrawerItem()
                    .withIcon(getResources().getDrawable(R.mipmap.ic_account_circle_black_48dp));
        }
    }

    // 프로필을 header에 올리는 함수
    private AccountHeader setupAccountHeader() {
        mHeaderResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(mProfileDrawerItem)
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                }).withSelectionListEnabledForSingleProfile(false)
                .build();
        return mHeaderResult;
    }

    // 네비게이션에 setupAccountHeader()를 붙여주고 이외의 데이터 넣어주기
    private void setupNavigationDrawerWithHeader() {
        //Depending on user is logged in or not, decide whether to show Log In menu or Log Out menu
        if (!isUserSignedIn()) {
            mDrawerResult = new DrawerBuilder()
                    .withActivity(this)
                    .withAccountHeader(setupAccountHeader())
                    .withToolbar(mToolbar)
                    .addDrawerItems(
                            mItemLogin,
                            line,
                            mItemHome,
                            mItemSettings
                    )
                    .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                        @Override
                        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                            onNavDrawerItemSelected((int) drawerItem.getIdentifier());
                            return true;
                        }
                    })
                    .build();
            mDrawerResult.deselect(mItemLogin.getIdentifier());
        } else if (isPicExist) {
            mCurrentProfile = checkCurrentProfileStatus();
            mDrawerResult = new DrawerBuilder()
                    .withActivity(this)
                    .withAccountHeader(setupAccountHeader())
                    .withToolbar(mToolbar)
                    .withShowDrawerOnFirstLaunch(true)
                    //@제민욱
                    //drawer가 사이드 메뉴판이라고 생각하면 되고, 여기에 PrimaryDrawerItem()인스턴스를 넣어주어야한다.
                    .addDrawerItems(
                            mCurrentProfile,
                            mItemLogout,
                            line,
                            mItemHome,
                            mItemSettings,
                            line2,
                            //analyzer 보여주기 not classification
                            mClassifier,
                            mGallery
                    )
                    .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                        @Override
                        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                            onNavDrawerItemSelected((int) drawerItem.getIdentifier());
                            return true;
                        }
                    })
                    .build();
            //여기서 analyzer로 이동하면 된다.

        } else {
            mCurrentProfile = checkCurrentProfileStatus();
            mDrawerResult = new DrawerBuilder()
                    .withActivity(this)
                    .withAccountHeader(setupAccountHeader())
                    .withToolbar(mToolbar)
                    .withShowDrawerOnFirstLaunch(true)
                    //drawer가 사이드 메뉴판이라고 생각하면 되고, 여기에 PrimaryDrawerItem()인스턴스를 넣어주어야한다.
                    .addDrawerItems(
                            mCurrentProfile,
                            mItemLogout,
                            line,
                            mItemHome,
                            mItemSettings
                    )
                    .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                        @Override
                        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                            onNavDrawerItemSelected((int) drawerItem.getIdentifier());
                            return true;
                        }
                    })
                    .build();
        }
        mDrawerResult.closeDrawer();
    }

    // nav 아이템 클릭되면 실행될 함수 정의해주면 되는 교통정리 함수
    private void onNavDrawerItemSelected(int drawerItemIdentifier) {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());
        //@제민욱
        switch (drawerItemIdentifier) {
            //Sign In
            case 3:
                switchFragment(drawerItemIdentifier);
                Toast.makeText(this, "Login menu selected", Toast.LENGTH_LONG).show();
                startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setTheme(R.style.AuthUITheme)
                        .setLogo(R.mipmap.ic_account_circle_black_48dp)
                        .setTosUrl(TOS_URL)
                        .setPrivacyPolicyUrl(PP_URL)
                        //이거 안해줘도 문제가 안생기네 ㅎㅎ
//                        .setAllowNewEmailAccounts(true)
                        .setIsSmartLockEnabled(false)
                        .build(), RC_SIGN_IN);
                break;
            //Sign Out
            case 4:
                signOutUser();
                switchFragment(drawerItemIdentifier);
                Toast.makeText(this, "Logout menu selected", Toast.LENGTH_LONG).show();
                break;
            //Home
            case 5:
                switchFragment(drawerItemIdentifier);
                Toast.makeText(this, "Home menu selected", Toast.LENGTH_LONG).show();
                break;
            //Settings
            case 6:
                Toast.makeText(this, "Settings menu selected", Toast.LENGTH_LONG).show();
                break;
            //classifier intent
            case 7:
                Log.v("minkj1992", "onNavDrawerItemSelected's Result case 불려짐");
                switchFragment(drawerItemIdentifier);

                break;
            // gallery
            case 8:
                switchFragment(drawerItemIdentifier);
//                galleryIntent();
                break;

        }


    }

    //material design 관련 method 끝
    private void refreshMenuHeader() {
        mDrawerResult.closeDrawer();
        mHeaderResult.clear();
        setupProfileDrawer();
        setupAccountHeader();
        mDrawerResult.setHeader(mHeaderResult.getView());
        mDrawerResult.resetDrawerContent();
    }

    private void signInUser() {
        intstantiateUser();
        if (!mFirebaseUser.isEmailVerified()) {
            //mFirebaseUser.sendEmailVerification();
        }
        mCurrentProfile = checkCurrentProfileStatus();
        mDrawerResult.updateItemAtPosition(mCurrentProfile, 1);
        mDrawerResult.addItemAtPosition(mItemLogout, 2);
        mDrawerResult.deselect(mItemLogout.getIdentifier());
        if (isPicExist) {
            mDrawerResult.addItem(line2);
            mDrawerResult.deselect(line2.getIdentifier());
            mDrawerResult.addItem(mClassifier);
            mDrawerResult.deselect(mClassifier.getIdentifier());
            mDrawerResult.addItem(mGallery);
            mDrawerResult.deselect(mGallery.getIdentifier());
            //pic,float[][] send to Result
        }

        refreshMenuHeader();
        relativeLayout.setVisibility(View.VISIBLE);

    }

    private void signOutUser() {
        //Sign out
        mFirebaseAuth.signOut();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (!isUserSignedIn()) {

            mDrawerResult.updateItemAtPosition(mItemLogin, 1);
            mDrawerResult.removeItemByPosition(2);
            mDrawerResult.deselect(mItemLogin.getIdentifier());


            mDrawerResult.removeItem(line2.getIdentifier());
            mDrawerResult.deselect(line2.getIdentifier());
            mDrawerResult.removeItem(mClassifier.getIdentifier());
            mDrawerResult.deselect(mClassifier.getIdentifier());
            mDrawerResult.removeItem(mGallery.getIdentifier());
            mDrawerResult.deselect(mGallery.getIdentifier());

            refreshMenuHeader();
            // login 페이지 뜨게하기
            onNavDrawerItemSelected(3);

        }
    }

    private void intstantiateUser() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
    }

    private boolean isUserSignedIn() {
        if (mFirebaseUser == null) {
            return false;
        } else {
            return true;
        }
    }

    private PrimaryDrawerItem checkCurrentProfileStatus() {
        if (mFirebaseUser.isEmailVerified()) {
            mCurrentProfile = new PrimaryDrawerItem().withIdentifier(2).withName(R.string.verified_profile).withIcon(getResources().getDrawable(R.mipmap.ic_verified_user_black_24dp));
            ;
        } else {
            mCurrentProfile = new PrimaryDrawerItem().withIdentifier(2).withName(R.string.unverified_profile).withIcon(getResources().getDrawable(R.mipmap.ic_report_problem_black_24dp));
        }
        return mCurrentProfile;
    }
    // firebase User auth method 끝


    public void switchFragment(int key) {
        Fragment fr = null;
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        switch (key) {
            case 3:
                break;
            case 4:
                relativeLayout.setVisibility(View.GONE);


                break;
            case 5:
                relativeLayout.setVisibility(View.VISIBLE);
                fragment_container.setVisibility(View.GONE);
                firebaseLayout.setVisibility(View.GONE);
                break;
            case 7:
                Log.v("minkj1992", "switchFragment's Result case 불려짐");
                fragment_container.setVisibility(View.VISIBLE);
                fr = new Result(result, gray);
                relativeLayout.setVisibility(View.GONE);

                firebaseLayout.setVisibility(View.VISIBLE);
                fragmentTransaction.replace(fragment_container.getId(), fr);
                fragmentTransaction.commit();
                Log.v("minkj1992", "FragmentManager 불려짐");
                break;
            case 8:
                relativeLayout.setVisibility(View.GONE);
                firebaseLayout.setVisibility(View.GONE);


                //@TODO 여기서 firebase 유저들 storage와 값들 가져오면 된다.
                fr = new Gallery();
                fragmentTransaction.replace(fragment_container.getId(), fr);
                fragmentTransaction.commit();
                break;
        }
    }
//#######################################  classifyIntent  #########################################

    //grayscale화 시키기
    private Bitmap grayScale(final Bitmap orgBitmap) {

        int width, height;
        width = orgBitmap.getWidth();
        height = orgBitmap.getHeight();
        Log.d("minkj1992", "original: " + String.valueOf(width) + String.valueOf(height));

        Bitmap bmpGrayScale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmpGrayScale);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(colorMatrixFilter);
        canvas.drawBitmap(orgBitmap, 0, 0, paint);

        width = bmpGrayScale.getWidth();
        height = bmpGrayScale.getHeight();
        Log.d("minkj1992", "grayscale: " + String.valueOf(width) + String.valueOf(height));
        return bmpGrayScale;

    }

    //Resize Bitmap
    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
//        bm.recycle();
        return resizedBitmap;
    }

    // 이미지 save했으며 && 이미 nav 바에 classifier와 gallery가 없을경우  -> 즉 처음 사용자일 경우에만 작동하는 함수
    private void addPicImageNav() {
        if (isPicExist && mDrawerResult.getPosition(mClassifier) == -1 && mDrawerResult.getPosition(mGallery) == -1) {
            mDrawerResult.addItem(line2);
            mDrawerResult.deselect(line2.getIdentifier());
            mDrawerResult.addItem(mClassifier);
            mDrawerResult.deselect(mClassifier.getIdentifier());
            mDrawerResult.addItem(mGallery);
            mDrawerResult.deselect(mGallery.getIdentifier());
            refreshMenuHeader();
        }
    }



    private void classifyIntent() {
        if (bitmap == null) {
            Toast.makeText(getApplicationContext(), "Bitmap is null from MainActivity", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = null;
        //@tmp
        gray = grayScale(getResizedBitmap(bitmap, 299, 299));

        intent = new Intent(this, Classifier.class);
        intent.putExtra("gray", gray);
        startActivityForResult(intent, CLASSIFY_REQUEST_CODE);
    }

//    private void galleryIntent() {
//        Intent intent = new Intent(this, Gallery.class);
//        startActivity(intent);
//    }
}

























//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        //add the values which need to be saved from the drawer to the bundle
//        outState = mDrawerResult.saveInstanceState(outState);
//        super.onSaveInstanceState(outState);
//    }
//
//    @Override
//    public void onBackPressed() {
//        //handle the back press :D close the drawer first and if the drawer is closed close the activity
//        if (mDrawerResult != null && mDrawerResult.isDrawerOpen()) {
//            mDrawerResult.closeDrawer();
//        } else {
//            super.onBackPressed();
//        }
//
//    }

//        @Override
//    protected void onResume() {
//        super.onResume();
//        if (mFirebaseUser == null) {
//            mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        }
//    }

