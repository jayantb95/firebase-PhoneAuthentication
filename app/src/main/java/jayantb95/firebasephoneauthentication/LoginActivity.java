package jayantb95.firebasephoneauthentication;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.stfalcon.smsverifycatcher.OnSmsCatchListener;
import com.stfalcon.smsverifycatcher.SmsVerifyCatcher;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private Button btnSendOtp;

    private EditText edtMobNum;
    private EditText edtOtp;

    private ProgressBar progressBarLogin;

    private TextView txtStatus;

    private SmsVerifyCatcher smsVerifyCatcher;

    private MyTimer myTimer;

    PhoneAuthProvider.ForceResendingToken mResendToken;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initialize();
    }

    private void initialize() {

        btnSendOtp = findViewById(R.id.btn_send_otp);
        edtMobNum = findViewById(R.id.edt_mobile_num);
        edtOtp = findViewById(R.id.edt_otp);
        progressBarLogin = findViewById(R.id.progress_bar_login);
        txtStatus = findViewById(R.id.txt_status);

        //init SmsVerifyCatcher
        smsVerifyCatcher = new SmsVerifyCatcher(this, new OnSmsCatchListener<String>() {
            @Override
            public void onSmsCatch(String message) {
                String code = parseCode(message);//Parse verification code
                //sets code in the editText(s)
                Log.d(TAG, "otp code: " + code);
                edtOtp.setText(code);
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                Toast.makeText(LoginActivity.this, "verification done" + phoneAuthCredential, Toast.LENGTH_LONG).show();
                Log.d(TAG, phoneAuthCredential.toString());

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(LoginActivity.this, "verification fail", Toast.LENGTH_LONG).show();
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // [START_EXCLUDE]
                    Toast.makeText(LoginActivity.this, "invalid mob no", Toast.LENGTH_LONG).show();
                    // [END_EXCLUDE]
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // [START_EXCLUDE]
                    Toast.makeText(LoginActivity.this, "quota over", Toast.LENGTH_LONG).show();
                    // [END_EXCLUDE]
                }
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);
                Toast.makeText(LoginActivity.this, "OTP sent to mobile number", Toast.LENGTH_LONG).show();
                mResendToken = token;
            }
        };

        listener();
    }

    private void listener() {
        btnSendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "send otp clicked");
                btnSendOtp.setEnabled(false);
                btnSendOtp.setClickable(false);

                //for showing countdown timer
                myTimer = new MyTimer(60000, 1000);
                myTimer.start();
                sendOtp();
            }
        });
    }

    private void sendOtp() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + edtMobNum.getText().toString(),        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                LoginActivity.this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    private String parseCode(String message) {
//        Pattern p = Pattern.compile("\\b\\d{4}\\b");  //if otp is of 4 digits.
        Pattern p = Pattern.compile("\\d{6}\\b");       //if otp is of 6 digits.
        Matcher m = p.matcher(message);
        String code = "";
        while (m.find()) {
            code = m.group(0);
        }
        return code;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        smsVerifyCatcher.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    class MyTimer extends CountDownTimer {

        private MyTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
            int progress = (int) (l / 1000);
            txtStatus.setVisibility(View.VISIBLE);
            txtStatus.setText(getResources().getText(R.string.txt_resend_otp_timer)
                    + String.valueOf(progress) + " seconds.");

            progressBarLogin.setProgress(progressBarLogin.getMax() - progress);
        }

        @Override
        public void onFinish() {
            progressBarLogin.setProgress(0);
            txtStatus.setText("");
            txtStatus.setVisibility(View.GONE);
            btnSendOtp.setText(getResources().getText(R.string.btn_resend_otp));
            btnSendOtp.setClickable(true);
            btnSendOtp.setEnabled(true);
        }
    }
}
