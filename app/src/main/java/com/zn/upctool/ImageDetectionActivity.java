package com.zn.upctool;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.Descriptors;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ImageDetectionActivity extends AppCompatActivity {

    public static final int RC_LOAD_IMAGE = 1100;

    @BindView(R.id.iv_image)
    ImageView mImageView;
    @BindView(R.id.tv_image_detect_result)
    TextView mResult;

    Bitmap mImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detection);
        ButterKnife.bind(this);
    }

    public void getImageFromStorage(View view) {
        // Get image from user storage
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, RC_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RC_LOAD_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                // Load the image
                Uri imageUri = data.getData();
                Glide.with(this).asBitmap().load(imageUri).listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        mImage = resource;
                        new LoadImageResponse().execute();
                        return false;
                    }
                }).into(mImageView);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private String createAnnotateImageRequest(byte[] imageBytes) {
        try (ImageAnnotatorClient vision  = ImageAnnotatorClient.create()) {
            // Build the image annotation request
            List<AnnotateImageRequest> requests = new ArrayList<>();
            Image image = Image.parseFrom(imageBytes);
            Feature feat = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feat)
                    .setImage(image)
                    .build();
            requests.add(request);

            // Do label detection on the image file
            BatchAnnotateImagesResponse response = vision.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();

            for (AnnotateImageResponse res: responses) {
                if (res.hasError()) {
                    return "There was an errrrrrror";

                }

                for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
                    StringBuilder builder = new StringBuilder();
                    Map<Descriptors.FieldDescriptor, Object> fields = annotation.getAllFields();
                    return fields.toString();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Caught an error in IOException";
        }
        return "There is no spoon";
    }

    private byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return stream.toByteArray();
    }

    private class LoadImageResponse extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            return createAnnotateImageRequest(getBytesFromBitmap(mImage));
        }

        @Override
        protected void onPostExecute(String result) {
            mResult.setText(result);
        }
    }
}
