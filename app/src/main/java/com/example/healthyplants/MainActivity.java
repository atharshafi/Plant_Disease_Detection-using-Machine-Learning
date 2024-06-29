package com.example.healthyplants;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.healthyplants.ml.LiteModelDiseaseClassification12;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button btLoadImage;
    TextView tvResult, textView2;
    ImageView ivAddImage;
    ActivityResultLauncher<String> mgetContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivAddImage = findViewById(R.id.iv_add_image);
        tvResult = findViewById(R.id.result);
        btLoadImage = findViewById(R.id.bt_load_image);
        textView2 = findViewById(R.id.textView2);

        getSupportActionBar().hide();


        mgetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                Bitmap imageBitmap = null;
                try {
                    imageBitmap = UriToBitmap(result);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                //ivAddImage.setImageURI(result);
                ivAddImage.setImageBitmap(imageBitmap);
                outputGenerator(imageBitmap);
            }
        });




        btLoadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mgetContent.launch("image/*");
            }
        });

        tvResult.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + tvResult.getText().toString()));
                startActivity(intent);
            }
        });
    }



    private void outputGenerator(Bitmap imageBitmap) {

        try {
            LiteModelDiseaseClassification12 model = LiteModelDiseaseClassification12.newInstance(MainActivity.this);

            // Creates inputs for reference.
            TensorImage image = TensorImage.fromBitmap(imageBitmap);

            // Runs model inference and gets result.
            LiteModelDiseaseClassification12.Outputs outputs = model.process(image);
            List<Category> probability = outputs.getProbabilityAsCategoryList();

            int index = 0;
            float max = probability.get(0).getScore();
            for (int i = 0; i < probability.size(); i++) {
                if (max < probability.get(i).getScore()) {
                    max = probability.get(i).getScore();
                    index = i;
                }
            }

            Category output = probability.get(index);
            tvResult.setText(output.getLabel());
            textView2.setVisibility(View.VISIBLE);

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }

    }
    private Bitmap UriToBitmap(Uri result) throws IOException {
        return MediaStore.Images.Media.getBitmap(this.getContentResolver(), result);

    }
}