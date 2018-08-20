package com.zn.upctool;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.samples.vision.barcodereader.BarcodeCaptureActivity;
import com.google.android.gms.vision.barcode.Barcode;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UPCSearchActivity extends AppCompatActivity {

//    Products mProducts;

    public static final String UPCDATABASE_BASE_SEARCH_URL = "https://api.upcdatabase.org/search/";
    public static final String UPCDATABASE_BASE_UPC_SEARCH_URL = "https://api.upcdatabase.org/product/";

    public static final String UPCITEMDB_BASE_SEARCH_URL = "https://api.upcitemdb.com/prod/trial/search?s=";
    public static final String UPCITEMDB_BASE_UPC_SEARCH_URL = "https://api.upcitemdb.com/prod/trial/lookup?upc=";

    public static final int RC_BARCODE_CAPTURE = 1;

    @BindView(R.id.jsonResult)
    TextView mJsonResult;
    @BindView(R.id.query)
    EditText mQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upcsearch);
        ButterKnife.bind(this);

//        mProducts = initSem3();

        mQuery.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    String query = mQuery.getText().toString();
                    new GetProductsAsyncTask().execute(query);
                    return true;
                }
                return false;
            }
        });
    }

    public void getBarcodeFromCamera(View view) {
        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
        intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);

        startActivityForResult(intent, RC_BARCODE_CAPTURE);
    }

//    private Products initSem3() {
//        return new Products(BuildConfig.SEMANTICS_API_KEY, BuildConfig.SEMANTICS_API_SECRET);
//    }

    private class GetProductsAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPostExecute(String s) {
            mJsonResult.setText(s);
        }


        // https://api.upcitemdb.com/prod/trial/search?s=google%20pixel%202&match_mode=0&type=product
        @Override
        protected String doInBackground(String... strings) {
            String queryUrl = UPCITEMDB_BASE_UPC_SEARCH_URL + strings[0];
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(queryUrl)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                return response.body().string();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return "Error getting results";
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE && resultCode == CommonStatusCodes.SUCCESS) {
            if (data != null) {
                Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                mQuery.setText(barcode.displayValue);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
