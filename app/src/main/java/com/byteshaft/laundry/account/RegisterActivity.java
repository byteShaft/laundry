package com.byteshaft.laundry.account;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.byteshaft.laundry.R;
import com.byteshaft.laundry.utils.AppGlobals;
import com.byteshaft.laundry.utils.WebServiceHelpers;
import com.byteshaft.requests.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;


public class RegisterActivity extends Activity implements View.OnClickListener,
        HttpRequest.OnReadyStateChangeListener, HttpRequest.OnErrorListener {

    private Button mRegisterButton;
    private EditText mUsername;
    private EditText mEmailAddress;
    private EditText mPassword;
    private EditText mVerifyPassword;
    private EditText mPhoneNumber;

    private String mUsernameString;
    public static String mEmailAddressString;
    private String mVerifyPasswordString;
    private String mPhoneNumberString;
    private String mPasswordString;

    private HttpRequest request;
    private static RegisterActivity sInstance;

    public static RegisterActivity getInstance() {
        return sInstance;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        sInstance = this;
        mUsername = (EditText) findViewById(R.id.user_name);
        mEmailAddress = (EditText) findViewById(R.id.email);
        mPhoneNumber = (EditText) findViewById(R.id.phone);
        mPassword = (EditText) findViewById(R.id.password);
        mVerifyPassword = (EditText) findViewById(R.id.verify_password);
        mRegisterButton = (Button) findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.register_button:
                if (validateEditText()) {
                    registerUser(
                            mUsernameString,
                            mPasswordString,
                            mEmailAddressString,
                            mPhoneNumberString
                    );
                }
                break;
        }
    }

    private boolean validateEditText() {

        boolean valid = true;
        mPasswordString = mPassword.getText().toString();
        mVerifyPasswordString = mVerifyPassword.getText().toString();
        mEmailAddressString = mEmailAddress.getText().toString();
        mPhoneNumberString = mPhoneNumber.getText().toString();
        mUsernameString = mUsername.getText().toString();


        if (mPasswordString.trim().isEmpty() || mPasswordString.length() < 3) {
            mPassword.setError("enter at least 3 characters");
            valid = false;
        } else {
            mPassword.setError(null);
        }

        if (mVerifyPasswordString.trim().isEmpty() || mVerifyPasswordString.length() < 3 ||
                !mVerifyPasswordString.equals(mPasswordString)) {
            mVerifyPassword.setError("password does not match");
            valid = false;
        } else {
            mVerifyPassword.setError(null);
        }
        if (mPhoneNumberString.trim().isEmpty() || mPhoneNumberString.length() < 3) {
            mPhoneNumber.setError("please enter your phone number");
            valid = false;
        } else {
            mPhoneNumber.setError(null);
        }


        if (mEmailAddressString.trim().isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(mEmailAddressString).matches()) {
            mEmailAddress.setError("please provide a valid email");
            valid = false;
        } else {
            mEmailAddress.setError(null);
        }
        return valid;
    }

    private void registerUser(String username, String password, String email, String phoneNumner) {
        request = new HttpRequest(getApplicationContext());
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        request.open("POST", " http://178.62.87.25/api/user/register");
        request.send(getRegisterData(username, password, email, phoneNumner));
        WebServiceHelpers.showProgressDialog(RegisterActivity.this, "Registering User ");
    }


    private String getRegisterData(String username, String password, String email, String phoneNumner) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("full_name", username);
            jsonObject.put("email", email);
            jsonObject.put("phone_number", phoneNumner);
            jsonObject.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();

    }

    @Override
    public void onReadyStateChange(HttpRequest request, int readyState) {
        switch (readyState) {
            case HttpRequest.STATE_DONE:
                WebServiceHelpers.dismissProgressDialog();
                switch (request.getStatus()) {
                    case HttpRequest.ERROR_NETWORK_UNREACHABLE:
                        AppGlobals.alertDialog(RegisterActivity.this, "Login Failed!", "please check your internet connection");
                        break;
                    case HttpURLConnection.HTTP_BAD_REQUEST:
                        AppGlobals.alertDialog(RegisterActivity.this, "Registration Failed!", "EmailAddress is already in use");
                        break;
                    case HttpURLConnection.HTTP_CREATED:
                        System.out.println(request.getResponseText() + "working ");
                        Toast.makeText(getApplicationContext(), "Activation code has been sent to you! Please check your Email", Toast.LENGTH_SHORT).show();
                        try {
                            JSONObject jsonObject = new JSONObject(request.getResponseText());
                            String username = jsonObject.getString(AppGlobals.KEY_FULLNAME);
                            String userId = jsonObject.getString(AppGlobals.KEY_USER_ID);
                            String email = jsonObject.getString(AppGlobals.KEY_EMAIL);
                            String phoneNumber = jsonObject.getString(AppGlobals.KEY_PHONE_NUMBER);

                            //saving values
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_FULLNAME, username);
                            Log.i("user name", " " + AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_FULLNAME));
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_EMAIL, email);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_PHONE_NUMBER, phoneNumber);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_USER_ID, userId);
                            LoginActivity.getInstance().finish();
                            finish();
                            startActivity(new Intent(getApplicationContext(), CodeConfirmationActivity.class));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                }
        }

    }

    @Override
    public void onError(HttpRequest request, int readyS, short error, Exception exception) {
        System.out.println(request.getStatus());
        switch (request.getStatus()) {
            case HttpURLConnection.HTTP_UNAUTHORIZED:
        }
    }
}
