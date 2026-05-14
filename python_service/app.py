from fastapi import FastAPI, File, UploadFile
from fastapi.responses import JSONResponse
import cv2
import numpy as np
import mediapipe as mp
import math
from mediapipe.tasks import python
from mediapipe.tasks.python.vision import FaceLandmarker, FaceLandmarkerOptions
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI(
    title="Yüz Analizi API",
    description="Yüz ölçümleri, burun kalkıklığı ve istatistiklere dayalı etiketleme döner."
)

# --- CORS ---
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# --- MediaPipe Modelini Yükle ---
# python_service klasörünün içinde olduğunu varsayıyoruz
model_path = "C:\\Users\\user\\Desktop\\yuzanalizi\\python_service\\face_landmarker.task"
base_options = python.BaseOptions(model_asset_path=model_path)
options = FaceLandmarkerOptions(
    base_options=base_options,
    output_face_blendshapes=False,
    output_facial_transformation_matrixes=False,
    num_faces=1
)
detector = FaceLandmarker.create_from_options(options)

# --- İstatistiksel Eşikler ---
FEATURE_STATS = {
    "nose_width": {"mean": 1.0, "std": 0.2},
    "nose_length": {"mean": 0.1397, "std": 0.0401},
    "lip_width": {"mean": 0.1987, "std": 0.0569},
    "upper_lip_height": {"mean": 0.02, "std": 0.005},
    "lower_lip_height": {"mean": 0.03, "std": 0.007},
    "eye_distance": {"mean": 0.3524, "std": 0.0920},
    "left_eye_width": {"mean": 0.1092, "std": 0.0303},
    "right_eye_width": {"mean": 0.1099, "std": 0.0301},
    "left_eye_height": {"mean": 0.05, "std": 0.01},
    "right_eye_height": {"mean": 0.05, "std": 0.01},
    "brow_distance": {"mean": 0.4501, "std": 0.1162},
    "cheek_width": {"mean": 0.5334, "std": 0.1336},
    "forehead_height": {"mean": 0.1893, "std": 0.0550},
    "burun_kalkiklik_orani": {"mean": 1.0, "std": 0.1}
}

# --- Yardımcı Fonksiyonlar ---
def euclidean_distance(p1, p2):
    return math.sqrt((p1.x - p2.x) ** 2 + (p1.y - p2.y) ** 2)

def classify_feature(value, mean, std, name):
    if name in ["left_eye_width", "right_eye_width"]:
        if value < 0.14:
            return f"Küçük {name}"
        elif value > 0.18:
            return f"Büyük {name}"
        else:
            return f"Orta {name}"

    elif name == "lip_width":
        if value < 0.23:
            return f"Küçük {name}"
        elif value > 0.27:
            return f"Büyük {name}"
        else:
            return f"Orta {name}"

    elif "lip_height" in name:
        low = mean * 0.8
        high = mean * 1.2
        if value < low:
            return f"Küçük {name}"
        elif value > high:
            return f"Büyük {name}"
        else:
            return f"Orta {name}"
    else:
        low = mean * 0.9
        high = mean * 1.1
        if value < low:
            return f"Küçük {name}"
        elif value > high:
            return f"Büyük {name}"
        else:
            return f"Orta {name}"

# --- Yüz Özelliklerini Ölç ---
def analyze_all_features(landmarks):
    nose_left = landmarks[93]
    nose_right = landmarks[323]
    nose_top = landmarks[6]
    nose_tip = landmarks[4]
    nose_bottom = landmarks[2]
    left_lip = landmarks[61]
    right_lip = landmarks[291]

    upper_lip_top = landmarks[13]
    upper_lip_bottom = landmarks[14]
    lower_lip_top = landmarks[14]
    lower_lip_bottom = landmarks[17]

    left_eye_top = landmarks[159]
    left_eye_bottom = landmarks[145]
    right_eye_top = landmarks[386]
    right_eye_bottom = landmarks[374]

    left_eye_outer = landmarks[33]
    right_eye_outer = landmarks[263]
    left_eye_inner = landmarks[133]
    right_eye_inner = landmarks[362]
    left_brow = landmarks[105]
    right_brow = landmarks[334]
    left_cheek = landmarks[234]
    right_cheek = landmarks[454]
    forehead_top = landmarks[10]
    chin_bottom = landmarks[152]

    # Hesaplamalar
    nose_width = euclidean_distance(nose_left, nose_right)
    nose_length = euclidean_distance(nose_top, nose_tip)
    lip_width = euclidean_distance(left_lip, right_lip)
    upper_lip_height = euclidean_distance(upper_lip_top, upper_lip_bottom)
    lower_lip_height = euclidean_distance(lower_lip_top, lower_lip_bottom)
    eye_distance = euclidean_distance(left_eye_outer, right_eye_outer)
    left_eye_width = euclidean_distance(left_eye_outer, left_eye_inner)
    right_eye_width = euclidean_distance(right_eye_outer, right_eye_inner)
    left_eye_height = euclidean_distance(left_eye_top, left_eye_bottom)
    right_eye_height = euclidean_distance(right_eye_top, right_eye_bottom)
    brow_distance = euclidean_distance(left_brow, right_brow)
    cheek_width = euclidean_distance(left_cheek, right_cheek)
    forehead_height = euclidean_distance(forehead_top, nose_top)

    # Burun kalkıklığı - yeni yöntem
    nose_to_lip = euclidean_distance(nose_tip, upper_lip_top)
    ratio = nose_to_lip / nose_length if nose_length > 0 else 0

    if ratio > 1.1:
        kalkiklik = "Kalkık burun"
    elif ratio < 0.9:
        kalkiklik = "Kalkık değil"
    else:
        kalkiklik = "Ortalama burun"

    # Altın oran hesaplamaları
    # Yüz yüksekliği: saç çizgisi (10) - çene (152)
    face_height = euclidean_distance(forehead_top, chin_bottom)
    # Yüz genişliği: yanaklar (234, 454)
    face_width = euclidean_distance(left_cheek, right_cheek)
    golden_ratio = 1.618
    altin_oran_orani = face_height / face_width if face_width > 0 else 0
    altin_oran_fark = abs(altin_oran_orani - golden_ratio)
    altin_oran_uygunluk_yuzdesi = max(0, 100 * (1 - altin_oran_fark / golden_ratio))

    if altin_oran_uygunluk_yuzdesi > 90:
        altin_oran_durum = "Altın orana çok uygun"
    elif altin_oran_uygunluk_yuzdesi > 70:
        altin_oran_durum = "Altın orana uygun"
    elif altin_oran_uygunluk_yuzdesi > 50:
        altin_oran_durum = "Orta seviyede altın oran"
    else:
        altin_oran_durum = "Altın orandan uzak"

    features = {
        "nose_width": round(nose_width, 4),
        "nose_length": round(nose_length, 4),
        "lip_width": round(lip_width, 4),
        "upper_lip_height": round(upper_lip_height, 4),
        "lower_lip_height": round(lower_lip_height, 4),
        "eye_distance": round(eye_distance, 4),
        "left_eye_width": round(left_eye_width, 4),
        "right_eye_width": round(right_eye_width, 4),
        "left_eye_height": round(left_eye_height, 4),
        "right_eye_height": round(right_eye_height, 4),
        "brow_distance": round(brow_distance, 4),
        "cheek_width": round(cheek_width, 4),
        "forehead_height": round(forehead_height, 4),
        "burun_kalkikligi": kalkiklik,
        "burun_kalkiklik_orani": round(ratio, 3),
        "altin_oran_orani": round(altin_oran_orani, 4),
        "altin_oran_uygunluk_yuzdesi": round(altin_oran_uygunluk_yuzdesi, 2),
        "altin_oran_durum": altin_oran_durum
    }

    labels = {}
    for key, val in features.items():
        if key not in ["burun_kalkikligi", "altin_oran_durum"]:
            if key in FEATURE_STATS:
                mean = FEATURE_STATS[key]["mean"]
                std = FEATURE_STATS[key]["std"]
                labels[key] = classify_feature(val, mean, std, key)

    labels["burun_kalkikligi"] = kalkiklik
    labels["altin_oran_durum"] = altin_oran_durum

    return {"olcumler": features, "etiketler": labels}

# --- API Endpoint ---
# --- API Endpoint ---
@app.post("/analyze-face")  # Adresi Python'un beklediği şekilde düzelttik
async def analyze_face_api(file: UploadFile = File(...)):
    # ... gerisi aynı kalsın
    try:
        img_bytes = await file.read()
        img_np = np.frombuffer(img_bytes, np.uint8)
        img_bgr = cv2.imdecode(img_np, cv2.IMREAD_COLOR)
        if img_bgr is None:
            return JSONResponse({"error": "Görsel okunamadı."}, status_code=400)

        img_rgb = cv2.cvtColor(img_bgr, cv2.COLOR_BGR2RGB)
        mp_image = mp.Image(image_format=mp.ImageFormat.SRGB, data=img_rgb)

        result = detector.detect(mp_image)
        if not result.face_landmarks:
            return JSONResponse({"error": "Yüz bulunamadı."}, status_code=404)

        landmarks = result.face_landmarks[0]
        feature_result = analyze_all_features(landmarks)

        return feature_result

    except Exception as e:
        return JSONResponse({"error": str(e)}, status_code=500)
 