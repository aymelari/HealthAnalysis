package org.example.healthanalysis.Service;

import ai.onnxruntime.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.example.healthanalysis.dto.MedicalScanRequestDto;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LungService {

    private static final String URL = "https://9517-185-146-113-28.ngrok-free.app/predict-lung/";
    private final RestTemplate restTemplate = new RestTemplate();
    private final MedicalScanService medicalScanService;

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
        medicalScanService.saveMedicalScan(medicalScanRequestDto, responseBody, "Lung", filePath);

        return responseBody;
    }
}
   /* private OrtSession session;
    private OrtEnvironment env;
    private MedicalScanService medicalScanService;

    public LungService(MedicalScanService medicalScanService) {
        this.medicalScanService = medicalScanService;
    }

    @PostConstruct
    public void init() throws Exception {
        env = OrtEnvironment.getEnvironment();
        try (var modelStream = getClass().getResourceAsStream("/lung.onnx")) {
            session = env.createSession(modelStream.readAllBytes());
        }
    }

    public String predict(MedicalScanRequestDto medicalScanRequestDto,String path) throws Exception {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(medicalScanRequestDto.getMultipartFile().getBytes()));
        float[] inputData = preprocessImage(image);

        long[] shape = {1, 224, 224, 3}; // NHWC
        OnnxTensor tensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(inputData), shape);

        try (OrtSession.Result results = session.run(Collections.singletonMap("input", tensor))) {
            float[][] output = (float[][]) results.get(0).getValue();
            float confidence = output[0][0];
            String prediction = confidence > 0.5 ? "cancerous" : "non-cancerous";
            String result=String.format("Prediction: %s | Confidence: %.2f", prediction, confidence);
            medicalScanService.saveMedicalScan(medicalScanRequestDto,result,"Lung cancer",path);
            return result;
        }
    }

    private float[] preprocessImage(BufferedImage image) {
        BufferedImage gray = toGrayscale(image);
        BufferedImage equalized = equalizeHistogram(gray);
        BufferedImage thresholded = applyThreshold(equalized, 128);
        BufferedImage resized = resizeImage(thresholded, 224, 224);
        return normalizeImage(resized);
    }

    private BufferedImage toGrayscale(BufferedImage image) {
        BufferedImage gray = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = gray.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return gray;
    }

    private BufferedImage equalizeHistogram(BufferedImage image) {
        int width = image.getWidth(), height = image.getHeight();
        int[] histogram = new int[256];
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                histogram[(image.getRGB(x, y) >> 16) & 0xFF]++;

        int total = width * height, cdf[] = new int[256];
        cdf[0] = histogram[0];
        for (int i = 1; i < 256; i++) cdf[i] = cdf[i - 1] + histogram[i];

        BufferedImage equalized = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++) {
                int pixel = (image.getRGB(x, y) >> 16) & 0xFF;
                int newPixel = (int) (cdf[pixel] * 255.0 / total);
                equalized.setRGB(x, y, (newPixel << 16) | (newPixel << 8) | newPixel);
            }

        return equalized;
    }

    private BufferedImage applyThreshold(BufferedImage image, int threshold) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < image.getHeight(); y++)
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = (image.getRGB(x, y) >> 16) & 0xFF;
                int newPixel = pixel < threshold ? 0 : pixel;
                result.setRGB(x, y, (newPixel << 16) | (newPixel << 8) | newPixel);
            }
        return result;
    }

    private BufferedImage resizeImage(BufferedImage image, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();
        return resized;
    }

    private float[] normalizeImage(BufferedImage image) {
        int width = image.getWidth(), height = image.getHeight();
        float[] floatArray = new float[3 * width * height];
        int index = 0;
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++) {
                int pixel = (image.getRGB(x, y) >> 16) & 0xFF;
                float normalized = pixel / 255.0f;
                floatArray[index++] = normalized;
                floatArray[index++] = normalized;
                floatArray[index++] = normalized;
            }
        return floatArray;
    }*/

