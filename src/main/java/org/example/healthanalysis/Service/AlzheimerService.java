package org.example.healthanalysis.Service;



import lombok.RequiredArgsConstructor;
import org.example.healthanalysis.dto.MedicalScanRequestDto;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AlzheimerService {
    private final MedicalScanService medicalScanService;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String URL = "https://91ea-185-146-113-28.ngrok-free.app/predict/";

    public String callPredictAPI(MedicalScanRequestDto medicalScanRequestDto, String filePath) throws IOException {
        // Prepare data
        MultipartFile multipartFile = medicalScanRequestDto.getMultipartFile();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // 2. Prepare multipart body
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // Add the file part
        ByteArrayResource fileResource = new ByteArrayResource(multipartFile.getBytes()) {
            @Override
            public String getFilename() {
                return multipartFile.getOriginalFilename(); // Important for file recognition
            }
        };
        body.add("file", fileResource); // Key must match FastAPI's expected field name

        // 3. Create the request entity
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // 4. Send the request
        ResponseEntity<String> response = restTemplate.exchange(
                URL,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        // 5. Save results
        String responseBody = response.getBody();
        medicalScanService.saveMedicalScan(medicalScanRequestDto, responseBody, "Alzheimer", filePath);

        return responseBody;
    }
}

   /* private final MedicalScanService medicalScanService;
    private final OrtEnvironment env;
    private final OrtSession session;
    private static final String[] CLASS_LABELS = {
            "Mild Impairment",
            "Moderate Impairment",
            "No Impairment",
            "Very Mild Impairment"
    };

    // Updated configuration parameters for EfficientNetV2
    private static final boolean USE_BGR = false;
    private static final boolean SAVE_DEBUG_IMAGE = true;

    // EfficientNetV2 normalization constants
    private static final float[] MEAN = {0.485f, 0.456f, 0.406f};
    private static final float[] STD = {0.229f, 0.224f, 0.225f};

    @Autowired
    public AlzheimerService(ResourceLoader resourceLoader, UserRepository userRepository,
                            MedicalScanRepository medicalScanRepository, MedicalScanService medicalScanService)
            throws OrtException, IOException {
        this.medicalScanService = medicalScanService;
        this.env = OrtEnvironment.getEnvironment();
        Resource resource = resourceLoader.getResource("classpath:alzheimer.onnx");
        Path modelPath = resource.getFile().toPath();

        OrtSession.SessionOptions options = new OrtSession.SessionOptions();
        this.session = env.createSession(modelPath.toString(), options);

        // Verify model input requirements
        System.out.println("Model expects input type: " + session.getInputInfo());
    }

    public String predictFromFile(MedicalScanRequestDto medicalScanRequestDto, String filePath)
            throws IOException, OrtException {
        BufferedImage image = ImageIO.read(medicalScanRequestDto.getMultipartFile().getInputStream());
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
            String prediction = parseModelOutput(output);
            medicalScanService.saveMedicalScan(medicalScanRequestDto, prediction, "Alzheimer", filePath);
            return prediction;
        }
    }

    private void printImageStats(BufferedImage image) {
        int[] rgb = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        double avg = Arrays.stream(rgb).average().orElse(0);
        System.out.printf("Average pixel value: %.2f%n", avg);
    }

    private BufferedImage ensureRGBFormat(BufferedImage image) {
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
        float[] data = new float[3 * width * height]; // CHW format

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);

                // Extract RGB components and scale to [0, 1]
                float r = ((rgb >> 16) & 0xFF) / 255.0f;
                float g = ((rgb >> 8) & 0xFF) / 255.0f;
                float b = (rgb & 0xFF) / 255.0f;

                // Normalize to EfficientNetV2's mean and std values
                r = (r - MEAN[0]) / STD[0];
                g = (g - MEAN[1]) / STD[1];
                b = (b - MEAN[2]) / STD[2];

                // Store in CHW format: data[0] = R, data[1] = G, data[2] = B
                int idx = y * width + x;
                data[idx] = r;                      // Channel 0 (Red)
                data[width * height + idx] = g;     // Channel 1 (Green)
                data[2 * width * height + idx] = b; // Channel 2 (Blue)
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
    }*/
