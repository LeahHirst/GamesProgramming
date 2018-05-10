package com.ahirst.doodaddash.util;

import android.content.res.AssetManager;
import android.graphics.Bitmap;

import java.util.List;

public class ImageClassifier {

    private static final String MODEL_FILE = "file:///android_asset/tensorflow_inception_graph.pb";
    private static final String LABEL_FILE =
            "file:///android_asset/imagenet_comp_graph_label_strings.txt";
    private static final int INPUT_SIZE = 224;
    private static final int IMAGE_MEAN = 117;
    private static final float IMAGE_STD = 1;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "output";

    private Classifier mClassifier;

    public ImageClassifier(AssetManager manager) {
        mClassifier =
                TensorFlowImageClassifier.create(
                        manager,
                        MODEL_FILE,
                        LABEL_FILE,
                        INPUT_SIZE,
                        IMAGE_MEAN,
                        IMAGE_STD,
                        INPUT_NAME,
                        OUTPUT_NAME
                );
    }

    public String getMostLikely(Bitmap bitmap) {
        List<Classifier.Recognition> recognitions = mClassifier.recognizeImage(bitmap);

        if (recognitions != null && recognitions.size() > 0) {
            return recognitions.get(0).getTitle();
        } else {
            return "";
        }
    }

}
