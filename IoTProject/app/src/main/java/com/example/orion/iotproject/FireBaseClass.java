package com.example.orion.iotproject;


        import android.util.Log;

        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.auth.FirebaseUser;
        import com.google.firebase.database.DatabaseReference;
        import com.google.firebase.database.FirebaseDatabase;
        import java.util.HashMap;
        import java.util.Map;

/**
 * Created by Orion on 13/03/2018.
 */

public class FireBaseClass {

    private final static String TAG = "FIREBASEERROR";
    private static FirebaseDatabase database;
    private static FirebaseUser currentFirebaseUser;

    public FireBaseClass() {
    }

    public static boolean addToDataBase(String lat, String lon) {
        try {
            //Date currentTime = Calendar.getInstance().getTime();
            currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            database = FirebaseDatabase.getInstance();
            Map<String, String> userData = new HashMap<>();
            userData.put("Lat", lat + "");
            userData.put("Lon", lon);
            DatabaseReference myRef = database.getReference().child(currentFirebaseUser.getUid());
            myRef.push().setValue(userData);
            return true;
        } catch (Exception e) {
            Log.d(TAG, e.toString());
            return false;
        }
    }

    public static boolean checkIfUserisOn() {
        return false;
    }
}
