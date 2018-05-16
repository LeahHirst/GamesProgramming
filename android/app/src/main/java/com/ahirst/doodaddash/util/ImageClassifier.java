package com.ahirst.doodaddash.util;

import android.content.res.AssetManager;
import android.graphics.Bitmap;

import java.util.List;

public class ImageClassifier {

    // Defaults
    private static final String MODEL_FILE = "file:///android_asset/tensorflow_inception_graph.pb";
    private static final String LABEL_FILE =
            "file:///android_asset/imagenet_comp_graph_label_strings.txt";
    private static final int INPUT_SIZE = 224;
    private static final int IMAGE_MEAN = 117;
    private static final float IMAGE_STD = 1;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "output";

    private Classifier mClassifier;

    public static class Builder {
        private AssetManager manager;
        private String modelFile;
        private String labelFile;
        private int inputSize;
        private int imageMean;
        private float imageStd;
        private String inputName;
        private String outputName;

        public Builder(AssetManager manager) {
            this.manager = manager;
        }

        public Builder setModelFile(String modelFile) {
            this.modelFile = modelFile;
            return this;
        }

        public Builder setLabelFile(String labelFile) {
            this.labelFile = labelFile;
            return this;
        }

        public Builder setInputSize(int inputSize) {
            this.inputSize = inputSize;
            return this;
        }

        public Builder setImageMean(int imageMean) {
            this.imageMean = imageMean;
            return this;
        }

        public Builder setImageStd(float imageStd) {
            this.imageStd = imageStd;
            return this;
        }

        public Builder setInputName(String inputName) {
            this.inputName = inputName;
            return this;
        }

        public Builder setOutputName(String outputName) {
            this.outputName = outputName;
            return this;
        }

        public ImageClassifier create() {
            return new ImageClassifier(this);
        }
    }

    public ImageClassifier(Builder builder) {

        // Set defaults if null
        if (builder.modelFile == null) builder.modelFile = MODEL_FILE;
        if (builder.labelFile == null) builder.labelFile = LABEL_FILE;
        if (builder.inputSize == 0) builder.inputSize = INPUT_SIZE;
        if (builder.imageMean == 0) builder.imageMean = IMAGE_MEAN;
        if (builder.imageStd < 1) builder.imageStd = IMAGE_STD;
        if (builder.inputName == null) builder.inputName = INPUT_NAME;
        if (builder.outputName == null) builder.outputName = OUTPUT_NAME;

        // Initiate the classifier
        mClassifier =
                TensorFlowImageClassifier.create(
                        builder.manager,
                        builder.modelFile,
                        builder.labelFile,
                        builder.inputSize,
                        builder.imageMean,
                        builder.imageStd,
                        builder.inputName,
                        builder.outputName
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
