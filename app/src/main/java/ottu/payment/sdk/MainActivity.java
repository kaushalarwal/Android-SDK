package ottu.payment.sdk;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Arrays;

import ottu.payment.interfaces.OttuPaymentCallback;
import ottu.payment.interfaces.SendPaymentCallback;
import ottu.payment.model.GenerateToken.CreatePaymentTransaction;
import ottu.payment.model.RedirectUrl.CreateRedirectUrl;
import ottu.payment.model.redirect.ResponceFetchTxnDetail;
import ottu.payment.model.submitCHD.Card_SubmitCHD;
import ottu.payment.model.submitCHD.SubmitCHDToOttoPG;
import ottu.payment.sdk.network.GetDataService;
import ottu.payment.ui.OttoPaymentSdk;
import ottu.payment.ui.PaymentActivity;
import ottu.payment.ui.PaymentResultActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static ottu.payment.sdk.network.RetrofitClientInstance.getRetrofitInstance;


public class MainActivity extends AppCompatActivity implements OttuPaymentCallback {

    private EditText etLocalLan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isStoragePermissionGranted();
        EditText text = findViewById(R.id.etAmount);
        AppCompatButton pay = findViewById(R.id.pay);
        etLocalLan = findViewById(R.id.localLan);

        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String amount = text.getText().toString().trim();


                createTrx(Float.parseFloat(amount));


            }
        });
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {

                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    public void createTrx(float amount) {
        String[] listpg  = {"ottu_pg_kwd_tkn", "knet-test", "mpgs"};
        CreatePaymentTransaction paymentTransaction = new CreatePaymentTransaction("e_commerce"
                , Arrays.asList(listpg)
                ,String.valueOf(amount)
                ,"KWD"
                ,"https://postapp.knpay.net/disclose_ok/"
                ,"https://postapp.knpay.net/redirected/"
                ,"mani"
                ,"300");
        SendPaymentCallback paymentCallback = new SendPaymentCallback();
        paymentCallback.setSendPaymentCallback(this);


        if (isNetworkAvailable(MainActivity.this)) {
            final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage("Please wait for a moment. Fetching data.");
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
            GetDataService apiendPoint = getRetrofitInstance();
            Call<ResponceFetchTxnDetail> register = apiendPoint.createPaymentTxn(paymentTransaction);
            register.enqueue(new Callback<ResponceFetchTxnDetail>() {
                @Override
                public void onResponse(Call<ResponceFetchTxnDetail> call, Response<ResponceFetchTxnDetail> response) {
                    dialog.dismiss();

                    if (response.isSuccessful() && response.body() != null) {
                        OttoPaymentSdk ottuPaymentSdk = new OttoPaymentSdk(MainActivity.this);
                        ottuPaymentSdk.setApiId(response.body().session_id);
                        ottuPaymentSdk.setMerchantId("ksa.ottu.dev");
                        ottuPaymentSdk.setSessionId(response.body().session_id);
                        ottuPaymentSdk.setAmount(response.body().amount);
                        ottuPaymentSdk.setLocal(etLocalLan.getText().toString().trim());
                        ottuPaymentSdk.build();



                    }else {
                        Toast.makeText(MainActivity.this, "Please try again!" , Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onFailure(Call<ResponceFetchTxnDetail> call, Throwable t) {
                    dialog.dismiss();
                    Toast.makeText(MainActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }


    }

    public static boolean isNetworkAvailable(Activity activity) {
        ConnectivityManager manager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            // Network is present and connected
            isAvailable = true;
        }
        return isAvailable;
    }


    @Override
    public void onSuccess(String callback) {

        Toast.makeText(this, "Payment Success", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFail(String callback) {
        Toast.makeText(this, "Payment Fail", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_OK ){
            if (requestCode == 001){
                Toast.makeText(this, ""+data.getStringExtra("Result"), Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, ""+data.getStringExtra("Intent Null"), Toast.LENGTH_SHORT).show();
            }
        }
    }
}