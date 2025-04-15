package org.example.healthanalysis.Service;

import ai.onnxruntime.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

@Service
public class AlzheimerService {

    private final OrtEnvironment env;
    private final OrtSession session;
    private static final String[] CLASS_LABELS = {
            "No Impairment",
            "Very Mild Impairment",
            "Mild Impairment",
            "Moderate Impairment"
    };

    // Configuration parameters (verify with model author)
    private static final boolean USE_BGR = false;
    private static final boolean USE_I0MAGENET_NORMALIZATION = true;
    private static final boolean SAVE_DEBUG_IMAGE = true;
    private static final boolean USE_NORMALIZATION = false;

    @Autowired
    public AlzheimerService(ResourceLoader resourceLoader) throws OrtException, IOException {

        this.env = OrtEnvironment.getEnvironment();
        Resource resource = resourceLoader.getResource("classpath:alzheimer.onnx");
        Path modelPath = resource.getFile().toPath();

        OrtSession.SessionOptions options = new OrtSession.SessionOptions();
        this.session = env.createSession(modelPath.toString(), options);

        // Verify model input requirements
        System.out.println("Model expects input type: " + session.getInputInfo());
    }

    public String predictFromFile(MultipartFile file) throws IOException, OrtException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            throw new IOException("Unsupported image format");
        }
        System.out.println("Original image statistics:");
        printImageStats(image);
        // Convert to RGB and resize first
        image = ensureRGBFormat(image);
        image = resizeImage(image, 224, 224);

        // Save debug image after all preprocessing
        if (SAVE_DEBUG_IMAGE) {
            saveDebugImage(image);
        }

        float[] inputData = preprocessImage(image);
        logSamplePixels(inputData);

        long[] shape = {1, 224, 224, 3}; // NHWC format
        try (OnnxTensor inputTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(inputData), shape);
             OrtSession.Result output = session.run(Collections.singletonMap(
                     session.getInputNames().iterator().next(),
                     inputTensor
             ))) {
            return parseModelOutput(output);
        }
    }

    private void printImageStats(BufferedImage image) {
        int[] rgb = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        double avg = Arrays.stream(rgb).average().orElse(0);
        System.out.printf("Average pixel value: %.2f%n", avg);
    }

    private BufferedImage ensureRGBFormat(BufferedImage image) {
        // Always create new RGB image to prevent color space contamination
        BufferedImage rgbImage = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );
        rgbImage.createGraphics().drawImage(image, 0, 0, null);
        return rgbImage;
    }
    private void saveDebugImage(BufferedImage image) {
        try {
            File debugFile = File.createTempFile("preprocessed_", ".jpg");
            ImageIO.write(image, "jpg", debugFile);
            System.out.println("Debug image saved to: " + debugFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save debug image: " + e.getMessage());
        }
    }

    private float[] preprocessImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        float[] data = new float[height * width * 3];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int index = (y * width + x) * 3;

                // Simple 0-1 scaling without ImageNet normalization
                data[index] = ((rgb >> 16) & 0xFF) / 255.0f;  // R
                data[index + 1] = ((rgb >> 8) & 0xFF) / 255.0f;   // G
                data[index + 2] = (rgb & 0xFF) / 255.0f;         // B
            }
        }
        return data;
    }
    private void logSamplePixels(float[] data) {
        System.out.println("First 10 normalized pixels:");
        for (int i = 0; i < 10; i++) {
            System.out.printf("[%.3f, %.3f, %.3f]%n",
                    data[i*3], data[i*3+1], data[i*3+2]);
        }
    }

    private String parseModelOutput(OrtSession.Result output) throws OrtException {
        OnnxValue value = output.get(0);

        if (value instanceof OnnxTensor tensor) {
            Object tensorValue = tensor.getValue();

            if (tensorValue instanceof float[][] probabilities) {
                System.out.println("Model Output: " + Arrays.toString(probabilities[0]));
                return interpretProbabilities(probabilities[0]);
            }
        }
        throw new IllegalStateException("Unsupported output format");
    }

    private String interpretProbabilities(float[] probs) {
        int maxIndex = 0;
        for (int i = 1; i < probs.length; i++) {
            if (probs[i] > probs[maxIndex]) {
                maxIndex = i;
            }
        }
        System.out.printf("Prediction: %s (%.1f%%)%n",
                CLASS_LABELS[maxIndex], probs[maxIndex] * 100);

        // Confidence threshold check
        if (probs[maxIndex] < 0.6) {
            System.out.println("Low confidence prediction - verify input quality");
        }

        return CLASS_LABELS[maxIndex];
    }

    private BufferedImage resizeImage(BufferedImage original, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(original, 0, 0, width, height, null);
        g.dispose();
        return resized;
    }
}
