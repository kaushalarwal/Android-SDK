package ottu.payment.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import okhttp3.ResponseBody;
import ottu.payment.R;
import ottu.payment.adapter.PaymentMethodAdapter;
import ottu.payment.adapter.SavedCardAdapter;
import ottu.payment.databinding.ActivityPaymentBinding;
import ottu.payment.model.DeleteCard.SendDeleteCard;
import ottu.payment.model.GenerateToken.CreatePaymentTransaction;
import ottu.payment.model.RedirectUrl.CreateRedirectUrl;
import ottu.payment.model.RedirectUrl.RespoRedirectUrl;
import ottu.payment.model.redirect.ResponceFetchTxnDetail;
import ottu.payment.model.submitCHD.Card_SubmitCHD;
import ottu.payment.model.submitCHD.SubmitCHDToOttoPG;
import ottu.payment.network.GetDataService;
import ottu.payment.network.RetrofitClientInstance;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static ottu.payment.network.RetrofitClientInstance.getRetrofitInstance;
import static ottu.payment.network.RetrofitClientInstance.getRetrofitInstancePg;
import static ottu.payment.util.Constant.Amount;
import static ottu.payment.util.Constant.ApiId;
import static ottu.payment.util.Constant.MerchantId;
import static ottu.payment.util.Constant.SessionId;
import static ottu.payment.util.Constant.savedCardSelected;
import static ottu.payment.util.Constant.selectedCardPos;
import static ottu.payment.util.Constant.selectedCardPosision;
import static ottu.payment.util.Constant.sessionId;
import static ottu.payment.util.Util.isNetworkAvailable;

public class PaymentActivity extends AppCompatActivity {

    ActivityPaymentBinding binding;
    private PaymentMethodAdapter adapterPaymentMethod;
    private SavedCardAdapter adapterSavedCard;

    private List<String> pg_codes;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        view();
        getTrnDetail();

    }


    private void view() {

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.gradiunt_blue));

        binding.rvSavedCards.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPaymentMethod.setLayoutManager(new LinearLayoutManager(this));
        binding.payNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (savedCardSelected){
                    SubmitCHDToOttoPG cardDetail = adapterSavedCard.getCardDetail();
                    Log.e("=========",cardDetail.toString());
                    payNow(cardDetail);
                }else {
                    if (selectedCardPos == 0) {
                        Card_SubmitCHD submitCHD = adapterPaymentMethod.getCardData();
                        if (submitCHD == null) {
                            Toast.makeText(PaymentActivity.this, getResources().getString(R.string.enter_carddetail), Toast.LENGTH_SHORT).show();
                        } else {
                            if (sessionId.equals("")) {
                                Toast.makeText(PaymentActivity.this, "Try again", Toast.LENGTH_SHORT).show();
                                return;
                            }

//                        CreatePaymentTransaction paymentTransaction = adapterPaymentMethod.getPaymentTrn(selectedCardPos);
//                        createTrx(paymentTransaction,paymentTransaction.getPg_codes().get(selectedCardPos));
                            SubmitCHDToOttoPG submitCHDToPG = new SubmitCHDToOttoPG(MerchantId, SessionId, "card", submitCHD);
                            payNow(submitCHDToPG);
                        }
                    } else if (selectedCardPos == 1) {
//                    CreatePaymentTransaction paymentTransaction = adapterPaymentMethod.getPaymentTrn(selectedCardPos);
//                    createTrx(paymentTransaction,paymentTransaction.getPg_codes().get(selectedCardPos));


                        CreateRedirectUrl redirectUrl = new CreateRedirectUrl(pg_codes.get(selectedCardPos), "mobile_sdk");
                        createRedirectUrl(redirectUrl, SessionId);
                    } else if (selectedCardPos == 2) {

                        CreateRedirectUrl redirectUrl = new CreateRedirectUrl(pg_codes.get(selectedCardPos), "mobile_sdk");
                        createRedirectUrl(redirectUrl, SessionId);
                    }
                }
            }
        });
        binding.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    public void setPayEnable(boolean isenble){
        binding.payNow.setEnabled(isenble);
        if (isenble){
            binding.payNow.setBackground(getResources().getDrawable(R.drawable.payenable));
        }else {
            binding.payNow.setBackground(getResources().getDrawable(R.drawable.buttondisable));
        }
    }

    private void payNow(SubmitCHDToOttoPG submitCHDToPG) {
        if (isNetworkAvailable(PaymentActivity.this)) {
            final ProgressDialog dialog = new ProgressDialog(PaymentActivity.this);
            dialog.setMessage("Please wait for a moment. Fetching data.");
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
            GetDataService apiendPoint = getRetrofitInstancePg();
            Call<JsonElement> register = apiendPoint.respoSubmitCHD(submitCHDToPG);
            register.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    dialog.dismiss();

                    if (response.isSuccessful()) {

                        try {
                            JSONObject jsonObject = new JSONObject(new Gson().toJson(response.body()));


                            if (jsonObject.has("status")) {
                                // got success
                                String status = jsonObject.getString("status");
                                if (status.equals("success")){

                                    Toast.makeText(PaymentActivity.this, "Payment Successfull", Toast.LENGTH_SHORT).show();
                                }else if (status.equals("failed")){
                                    Toast.makeText(PaymentActivity.this, "Payment Failed", Toast.LENGTH_SHORT).show();
                                }else if (status.equals("error")){
                                    Toast.makeText(PaymentActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                }else if (status.equals("3DS")){
                                    startActivity(new Intent(PaymentActivity.this,WebPaymentActivity.class)
                                            .putExtra("is3DS",true)
                                    .putExtra("html",jsonObject.getString("html"))
                                    .putExtra("reference_number",jsonObject.getString("reference_number"))
                                            .putExtra("ws_url",jsonObject.getString("ws_url")));
                                }

                            }else {
                                //got payment error

                                JSONObject cardFieldError = jsonObject.getJSONObject("card");
                                JSONArray cardGlobleError = jsonObject.getJSONArray("card");
                                JSONArray nonFieldErrors = jsonObject.getJSONArray("non_field_errors");
                                JSONArray merchantId = jsonObject.getJSONArray("merchant_id");
                                JSONArray payment_method = jsonObject.getJSONArray("payment_method");

                                
                                if (cardFieldError != null){
                                    Toast.makeText(PaymentActivity.this, "Card Filed Error", Toast.LENGTH_SHORT).show();
                                }
                                if (cardGlobleError != null){
                                    Toast.makeText(PaymentActivity.this, ""+cardGlobleError.get(0), Toast.LENGTH_SHORT).show();
                                }
                                if (nonFieldErrors != null){
                                    Toast.makeText(PaymentActivity.this, nonFieldErrors.getString(0), Toast.LENGTH_SHORT).show();
                                }
                                if (merchantId != null){
                                    Toast.makeText(PaymentActivity.this, merchantId.getString(0), Toast.LENGTH_SHORT).show();
                                }

                            }




                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }else {
                        Toast.makeText(PaymentActivity.this, "Please try again!" , Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    dialog.dismiss();
                    Toast.makeText(PaymentActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void getTrnDetail() {
        String apiId = null;
        String amount = null;
        if (getIntent().hasExtra("SessionId")) {
             apiId = getIntent().getStringExtra("ApiId");
             Amount = getIntent().getStringExtra("Amount");
             MerchantId = getIntent().getStringExtra("MerchantId");
            SessionId = getIntent().getStringExtra("SessionId");
             ApiId = apiId;
             binding.amountTextView.setText("Amount : "+Amount);
        }else {
            Toast.makeText(this, "No sessionid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        if (isNetworkAvailable(PaymentActivity.this)) {
            final ProgressDialog dialog = new ProgressDialog(PaymentActivity.this);
            dialog.setMessage("Please wait for a moment. Fetching data.");
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
            GetDataService apiendPoint = new RetrofitClientInstance().getRetrofitInstance();
            Call<ResponceFetchTxnDetail> register = apiendPoint.fetchTxnDetail(apiId,true);
            register.enqueue(new Callback<ResponceFetchTxnDetail>() {
                @Override
                public void onResponse(Call<ResponceFetchTxnDetail> call, Response<ResponceFetchTxnDetail> response) {
                    dialog.dismiss();

                    if (response.isSuccessful() && response.body() != null) {
                        showData(response.body());
                        sessionId = response.body().session_id;
                        pg_codes = response.body().pg_codes;
                        Log.e("=======",response.body().toString());
                    }else {
                        Toast.makeText(PaymentActivity.this, "Please try again!" , Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onFailure(Call<ResponceFetchTxnDetail> call, Throwable t) {
                    dialog.dismiss();
                    Toast.makeText(PaymentActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                }

            });
        }
    }

    public void showData(ResponceFetchTxnDetail body) {
        if (body != null){

            if (body.cards != null) {
                adapterSavedCard = new SavedCardAdapter(PaymentActivity.this,body.cards );
                binding.rvSavedCards.setAdapter(adapterSavedCard);
            }
            if (body.payment_methods != null) {
               adapterPaymentMethod =  new PaymentMethodAdapter(this,body );
                binding.rvPaymentMethod.setAdapter(adapterPaymentMethod);
            }
        }
    }


    private void createRedirectUrl(CreateRedirectUrl redirectUrl, String session_id) {

        if (isNetworkAvailable(PaymentActivity.this)) {
            final ProgressDialog dialog = new ProgressDialog(PaymentActivity.this);
            dialog.setMessage("Please wait for a moment. Fetching data.");
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
            GetDataService apiendPoint = getRetrofitInstance();
            Call<RespoRedirectUrl> register = apiendPoint.createRedirectUrl(session_id,redirectUrl);
            register.enqueue(new Callback<RespoRedirectUrl>() {
                @Override
                public void onResponse(Call<RespoRedirectUrl> call, Response<RespoRedirectUrl> response) {
                    dialog.dismiss();

                    if (response.isSuccessful() && response.body() != null) {

                        if (response.body().getRedirect_url() != null){
                            startActivity(new Intent(PaymentActivity.this,WebPaymentActivity.class)
                            .putExtra("RedirectUrl",response.body().getRedirect_url()));
                        }else {
                            Toast.makeText(PaymentActivity.this, response.body().getMessage() , Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        Log.e("=======",response.body().toString());
                    }else {
                        Toast.makeText(PaymentActivity.this, "Please try again!" , Toast.LENGTH_SHORT).show();
                        finish();
                    }

                }

                @Override
                public void onFailure(Call<RespoRedirectUrl> call, Throwable t) {
                    dialog.dismiss();
                    Toast.makeText(PaymentActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    public void notifySavedCardAdapter(){
        if (adapterSavedCard != null){
            selectedCardPosision = -1;
            adapterSavedCard.notifyDataSetChanged();
        }
    }
    public void notifyPaymentMethodAdapter(){

        if (adapterPaymentMethod != null){
//            binding.rvPaymentMethod.setAdapter(adapterPaymentMethod);
            selectedCardPos = -1;
            adapterPaymentMethod.notifyDataSetChanged();
        }
    }

    public void deleteCard(SendDeleteCard deleteCard, String token) {

        if (isNetworkAvailable(PaymentActivity.this)) {
            final ProgressDialog dialog = new ProgressDialog(PaymentActivity.this);
            dialog.setMessage("Please wait for a moment. Fetching data.");
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
            GetDataService apiendPoint = getRetrofitInstance();
            Call<ResponseBody> register = apiendPoint.deleteCard(token,deleteCard.customer_id,deleteCard.type);
            register.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    dialog.dismiss();


                    if (response.isSuccessful() && response.body() != null) {

                        if (response.isSuccessful()){
                            Toast.makeText(PaymentActivity.this, "Card Deleted" , Toast.LENGTH_SHORT).show();
                            onRestart();
                        }
                        Log.e("=======",response.body().toString());
                    }else {
                        Toast.makeText(PaymentActivity.this, "Please try again!" , Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    dialog.dismiss();
                }
            });
        }

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        selectedCardPos = -1;
        selectedCardPosision = -1;
        notifySavedCardAdapter();
        notifyPaymentMethodAdapter();
    }
}