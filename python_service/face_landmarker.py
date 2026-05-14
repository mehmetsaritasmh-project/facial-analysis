import cv2

import mediapipe as mp

import math



mp_face_mesh = mp.solutions.face_mesh



# Yardımcı fonksiyon: iki nokta arasındaki mesafe

def distance(p1, p2):

    return math.dist([p1.x, p1.y], [p2.x, p2.y])



# Ana analiz fonksiyonu

def analyze_face(image):

    results_data = {"olcumler": {}, "etiketler": {}}



    with mp_face_mesh.FaceMesh(

        static_image_mode=True,

        max_num_faces=1,

        refine_landmarks=True,

        min_detection_confidence=0.5

    ) as face_mesh:

        results = face_mesh.process(cv2.cvtColor(image, cv2.COLOR_BGR2RGB))



        if not results.multi_face_landmarks:

            return None  # yüz bulunamadı



        face_landmarks = results.multi_face_landmarks[0].landmark



        # --- Örnek ölçümler ---

        # Burun genişliği: sol burun kanadı (93) ile sağ burun kanadı (323)

        nose_width = distance(face_landmarks[93], face_landmarks[323])

        # Burun uzunluğu: burun ucu (1) ile üst dudak ortası (13)

        nose_length = distance(face_landmarks[1], face_landmarks[13])

        # Dudak genişliği: ağız köşeleri (61, 291)

        lip_width = distance(face_landmarks[61], face_landmarks[291])

        # Üst dudak yüksekliği: (13) ile (14)

        upper_lip_height = distance(face_landmarks[13], face_landmarks[14])

        # Alt dudak yüksekliği: (14) ile (17)

        lower_lip_height = distance(face_landmarks[14], face_landmarks[17])

        # Gözler arası mesafe: (133) ile (362)

        eye_distance = distance(face_landmarks[133], face_landmarks[362])

        # Sol göz genişliği: (33) ile (133)

        left_eye_width = distance(face_landmarks[33], face_landmarks[133])

        # Sağ göz genişliği: (362) ile (263)

        right_eye_width = distance(face_landmarks[362], face_landmarks[263])

        # Sol göz yüksekliği: (159) ile (145)

        left_eye_height = distance(face_landmarks[159], face_landmarks[145])

        # Sağ göz yüksekliği: (386) ile (374)

        right_eye_height = distance(face_landmarks[386], face_landmarks[374])

        # Kaş mesafesi: (70) ile (300)

        brow_distance = distance(face_landmarks[70], face_landmarks[300])

        # Yanak genişliği: (234) ile (454)

        cheek_width = distance(face_landmarks[234], face_landmarks[454])

        # Alın yüksekliği: (10) ile (152)

        forehead_height = distance(face_landmarks[10], face_landmarks[152])



        # Burun kalkıklığı (senin yeni tanımına göre)

        nose_tip = face_landmarks[1]

        upper_lip = face_landmarks[13]

        nose_base = face_landmarks[2]

        nose_length_calc = distance(nose_tip, nose_base)

        nose_to_lip = distance(nose_tip, upper_lip)

        burun_kalkikligi = nose_to_lip / (nose_length_calc + 1e-6)



        # --- Ölçümler dictionary ---

        results_data["olcumler"] = {

            "nose_width": nose_width,

            "nose_length": nose_length,

            "lip_width": lip_width,

            "upper_lip_height": upper_lip_height,

            "lower_lip_height": lower_lip_height,

            "eye_distance": eye_distance,

            "left_eye_width": left_eye_width,

            "right_eye_width": right_eye_width,

            "left_eye_height": left_eye_height,

            "right_eye_height": right_eye_height,

            "brow_distance": brow_distance,

            "cheek_width": cheek_width,

            "forehead_height": forehead_height,

            "burun_kalkikligi": burun_kalkikligi,

        }



        # --- Etiketler (örnek basit sınıflandırma) ---

        def classify(value, ref):

            if value < ref * 0.9:

                return "Küçük"

            elif value > ref * 1.1:

                return "Büyük"

            else:

                return "Orta"



        reference = {

            "nose_width": 0.3,

            "nose_length": 0.1,

            "lip_width": 0.11,

            "upper_lip_height": 0.005,

            "lower_lip_height": 0.03,

            "eye_distance": 0.19,

            "left_eye_width": 0.06,

            "right_eye_width": 0.06,

            "left_eye_height": 0.035,

            "right_eye_height": 0.035,

            "brow_distance": 0.18,

            "cheek_width": 0.29,

            "forehead_height": 0.20,

            "burun_kalkikligi": 1.0,  # referans oran

        }



        for key, val in results_data["olcumler"].items():

            results_data["etiketler"][key] = classify(val, reference[key])



    return results_data