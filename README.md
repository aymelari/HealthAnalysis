# Assignment System - Backend with Java + Spring Boot + AI Model Integration

This is the backend for the Assignment System platform, developed using **Java**, **Spring Boot**, and integrated with **machine learning models** through a **Python FastAPI** microservice.

## 🔧 Backend Overview

The backend handles:

- User input from the frontend
- Communication with machine learning models (image/text-based)
- Parsing and formatting AI predictions
- Serving results to the frontend in a user-friendly format

## 🧠 Model Integration

Originally, we attempted to integrate **ONNX models** directly into the Java environment using Java ONNX libraries, but faced several technical challenges:

- Complex image preprocessing in Java (compared to Python)
- Difficulties with tensor shapes, types, and silent preprocessing bugs
- Lack of debugging tools in Java for tensor/image pipelines

To address these issues, we transitioned to a **Python FastAPI service** to handle ML inference.

### 🧪 Final Setup

- Java Spring Boot backend acts as the **main application**.
- ML model inference is done through **HTTP API calls** to a Python FastAPI service.
- Data is exchanged in **JSON format** (e.g., base64 images, structured text).
- The backend parses the response and returns the results to the client.

## 🚀 Technologies Used

- **Java 17**
- **Spring Boot**
- **Python 3.10+**
- **FastAPI**
- **PyTorch** (or similar libraries for model inference)
- **OpenCV**, **Pillow**, **NumPy** (for preprocessing)

## 📈 Future Improvements

- Deploy Python API to a production server (Docker or cloud)
- Implement authentication between Java and Python services
- Improve input validation and error handling
- Scale prediction endpoints with load balancing

## 🤝 Contributors

- Aysu [@aymelari](https://github.com/aymelari)

---

**Note:** The Python services are currently running **locally**. Ensure the FastAPI server is running before testing predictions from the backend.
