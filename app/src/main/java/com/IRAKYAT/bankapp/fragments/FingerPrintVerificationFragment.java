package com.IRAKYAT.bankapp.fragments;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.IRAKYAT.bankapp.R;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import static android.content.Context.FINGERPRINT_SERVICE;
import static android.content.Context.KEYGUARD_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FingerPrintVerificationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class FingerPrintVerificationFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TEXT = "param1";
    // Declare a string variable for the key we’re going to use in our fingerprint authentication
    private static final String KEY_NAME = "yourKey";
    private Cipher cipher;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private TextView textView;
    private FingerprintManager.CryptoObject cryptoObject;
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    // TODO: Rename and change types of parameters
    private String mText;
    private static final String TAG = "FingerPrintVerification";;
    private OnFragmentInteractionListener mListener;

    public FingerPrintVerificationFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static FingerPrintVerificationFragment newInstance(String text) {
        FingerPrintVerificationFragment fragment = new FingerPrintVerificationFragment();
        Bundle args = new Bundle();
        args.putString(TEXT, text);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate: Called" );
        if (getArguments() != null) {
            mText = getArguments().getString(TEXT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView: Called" );
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_finger_print_verification, container, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Get an instance of KeyguardManager and FingerprintManager//
            keyguardManager =
                    (KeyguardManager) getActivity().getSystemService(KEYGUARD_SERVICE);
            fingerprintManager =
                    (FingerprintManager) getActivity().getSystemService(FINGERPRINT_SERVICE);

            textView = view.findViewById(R.id.textView);

            //Check whether the device has a fingerprint sensor//
            if (!fingerprintManager.isHardwareDetected()) {
                // If a fingerprint sensor isn’t available, then inform the user that they’ll be unable to use your app’s fingerprint functionality//
                textView.setText("Your device doesn't support fingerprint authentication");
            }
            //Check whether the user has granted your app the USE_FINGERPRINT permission//
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                // If your app doesn't have this permission, then display the following text//
                textView.setText("Please enable the fingerprint permission");
            }

            //Check that the user has registered at least one fingerprint//
            if (!fingerprintManager.hasEnrolledFingerprints()) {
                // If the user hasn’t configured any fingerprints, then display the following message//
                textView.setText("No fingerprint configured. Please register at least one fingerprint in your device's Settings");
            }

            //Check that the lockscreen is secured//
            if (!keyguardManager.isKeyguardSecure()) {
                // If the user hasn’t secured their lockscreen with a PIN password or pattern, then display the following text//
                textView.setText("Please enable lockscreen security in your device's Settings");
            } else {
                try {

                    generateKey();
                } catch (FingerprintException e) {
                    e.printStackTrace();
                }

                if (initCipher()) {
                    //If the cipher is initialized successfully, then create a CryptoObject instance//
                    cryptoObject = new FingerprintManager.CryptoObject(cipher);

                    // Here, I’m referencing the FingerprintHandler class that we’ll create in the next section. This class will be responsible
                    // for starting the authentication process (via the startAuth method) and processing the authentication process events//
                    FingerprintHandler helper = new FingerprintHandler(getActivity(),mListener);
                    helper.startAuth(fingerprintManager, cryptoObject);
                }
            }
        }
        return view;
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(String sendBackText);
    }


    //Create the generateKey method that we’ll use to gain access to the Android keystore and generate the encryption key//

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void generateKey() throws FingerprintException {
        try {
            // Obtain a reference to the Keystore using the standard Android keystore container identifier (“AndroidKeystore”)//
            keyStore = KeyStore.getInstance("AndroidKeyStore");

            //Generate the key//
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            //Initialize an empty KeyStore//
            keyStore.load(null);

            //Initialize the KeyGenerator//
            keyGenerator.init(new

                    //Specify the operation(s) this key can be used for//
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)

                    //Configure this key so that the user has to confirm their identity with a fingerprint each time they want to use it//
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());

            //Generate the key//
            keyGenerator.generateKey();

        } catch (KeyStoreException
                | NoSuchAlgorithmException
                | NoSuchProviderException
                | InvalidAlgorithmParameterException
                | CertificateException
                | IOException exc) {
            exc.printStackTrace();
            throw new FingerprintException(exc);
        }
    }

    //Create a new method that we’ll use to initialize our cipher//
    public boolean initCipher() {
        try {
            //Obtain a cipher instance and configure it with the properties required for fingerprint authentication//
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            //Return true if the cipher has been initialized successfully//
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {

            //Return false if cipher initialization failed//
            return false;
        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    public class FingerprintException extends Exception {
        public FingerprintException(Exception e) {
            super(e);
        }
    }


//FingerPrint Handler

    @RequiresApi(api = Build.VERSION_CODES.M)
    public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

        // You should use the CancellationSignal method whenever your app can no longer process user input, for example when your app goes
        // into the background. If you don’t use this method, then other apps will be unable to access the touch sensor, including the lockscreen!//

        private CancellationSignal cancellationSignal;
        private Context context;
        private OnFragmentInteractionListener onFragmentInteractionListener;

        public FingerprintHandler(Context mContext,OnFragmentInteractionListener monFragmentInteractionListener) {
            context = mContext;
            onFragmentInteractionListener = monFragmentInteractionListener;
        }

        //Implement the startAuth method, which is responsible for starting the fingerprint authentication process//

        @RequiresApi(api = Build.VERSION_CODES.M)
        public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {

            cancellationSignal = new CancellationSignal();
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
        }

        @Override
        //onAuthenticationError is called when a fatal error has occurred. It provides the error code and error message as its parameters//

        public void onAuthenticationError(int errMsgId, CharSequence errString) {

            //I’m going to display the results of fingerprint authentication as a series of toasts.
            //Here, I’m creating the message that’ll be displayed if an error occurs//

            Toast.makeText(context, "Authentication error\n" + errString, Toast.LENGTH_LONG).show();
        }

        @Override

        //onAuthenticationFailed is called when the fingerprint doesn’t match with any of the fingerprints registered on the device//

        public void onAuthenticationFailed() {
            Toast.makeText(context, "Authentication failed", Toast.LENGTH_LONG).show();
        }

        @Override

        //onAuthenticationHelp is called when a non-fatal error has occurred. This method provides additional information about the error,
        //so to provide the user with as much feedback as possible I’m incorporating this information into my toast//
        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
            Toast.makeText(context, "Authentication help\n" + helpString, Toast.LENGTH_LONG).show();
        }@Override

        //onAuthenticationSucceeded is called when a fingerprint has been successfully matched to one of the fingerprints stored on the user’s device//
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {

            Toast.makeText(context, "Verification Successfull!", Toast.LENGTH_LONG).show();
            onFragmentInteractionListener.onFragmentInteraction("Verification Successfull!");




        }

    }




}