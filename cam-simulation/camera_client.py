import cv2
import requests
import threading
import time
import os
import glob
import sys

# ==============================================================================
# CAMERA CLIENT - HỆ THỐNG CHẤM CÔNG NHẬN DIỆN KHUÔN MẶT (SCHOOL HRM)
# Kết nối trực tiếp tới Spring Boot Backend: http://localhost:8080/api/v1/attendance/scan-face
# Hỗ trợ 2 chế độ:
#   1. Webcam vật lý (OpenCV + DeepFace MTCNN)
#   2. Chế độ Giả lập Camera (--simulate hoặc tự động kích hoạt khi không có webcam)
#      - Nhấn phím 1..5 hoặc SPACE để quét khuôn mặt nhân viên mẫu từ dataset/
#      - Nhấn phím 'a' để bật/tắt tự động quét luân phiên mỗi 6 giây
#      - Nhấn phím 'q' để thoát
# ==============================================================================

API_URL = os.environ.get("HRM_FACE_API_URL", "http://localhost:8080/api/v1/attendance/scan-face")
DEVICE_ID = os.environ.get("HRM_CAMERA_DEVICE_ID", "CAM-GATE-AI-01")
COOLDOWN_SECONDS = 12.0

# Dữ liệu chia sẻ giữa các luồng
latest_frame = None
status_message = "SAN SANG CHAM CONG (SPACE: Quet | 1-5: Chon GV | A: Auto | Q: Thoat)"
status_color = (255, 255, 255)
detected_face_code = "UNKNOWN"
detected_emp_name = ""
current_face_box = None
is_processing = False
last_scan_by_code = {}


def clean_old_pkl_cache(dataset_dir="dataset"):
    """Tự động xóa các file cache .pkl cũ để tránh lỗi xung đột phiên bản/model"""
    pkl_files = glob.glob(os.path.join(dataset_dir, "*.pkl"))
    for file_path in pkl_files:
        try:
            os.remove(file_path)
            print(f"-> Đã dọn dẹp cache cũ: {os.path.basename(file_path)}")
        except Exception:
            pass


def ensure_sample_dataset(dataset_dir="dataset"):
    """Đảm bảo thư mục dataset có các ảnh mẫu gắn mã nhân viên chuẩn của School HRM"""
    os.makedirs(dataset_dir, exist_ok=True)
    base_img = os.path.join(dataset_dir, "ANH001.jpg")
    sample_codes = [
        "EMP-GV-001",
        "EMP-TBT-001",
        "EMP-VAN-T01",
        "EMP-NN1-T01",
        "EMP-KHTN-T01"
    ]
    if os.path.exists(base_img):
        import shutil
        for code in sample_codes:
            target = os.path.join(dataset_dir, f"{code}.jpg")
            if not os.path.exists(target):
                try:
                    shutil.copyfile(base_img, target)
                except Exception:
                    pass
    images = sorted(glob.glob(os.path.join(dataset_dir, "*.jpg")))
    return images


def send_attendance_to_server(emp_code, bypass_cooldown=False):
    """Gửi mã nhân viên nhận diện được lên Spring Boot API /api/v1/attendance/scan-face"""
    global status_message, status_color, detected_face_code, detected_emp_name

    now_ts = time.time()
    if not bypass_cooldown and emp_code in last_scan_by_code:
        if now_ts - last_scan_by_code[emp_code] < COOLDOWN_SECONDS:
            return

    detected_face_code = emp_code
    try:
        payload = {
            "employee_code": emp_code,
            "device_id": DEVICE_ID,
            "confidence": 99.1,
            "bypass_cooldown": bypass_cooldown
        }
        res = requests.post(API_URL, json=payload, timeout=3)
        data = res.json()

        st = data.get("status")
        if st == "SUCCESS":
            last_scan_by_code[emp_code] = now_ts
            emp_name = data.get("employeeName") or emp_code
            detected_emp_name = emp_name
            ev_type = data.get("type", "IN")
            ev_time = data.get("time", "")
            status_message = f"XAC NHAN: {emp_code} [{ev_type}] - {ev_time}"
            status_color = (0, 255, 0)
            print(f"[SUCCESS] {emp_code} ({emp_name}) -> {ev_type} lúc {ev_time}")
        elif st == "COOLDOWN":
            last_scan_by_code[emp_code] = now_ts
            status_message = data.get("message", f"Da xac nhan {emp_code} truoc do!")
            status_color = (0, 255, 255)
            print(f"[COOLDOWN] {status_message}")
        else:
            status_message = data.get("message", f"Loi cham cong: {emp_code}")
            status_color = (0, 165, 255)
            print(f"[WARN] {status_message}")
    except Exception as ex:
        status_message = f"LOI KET NOI SERVER (8080): {emp_code}"
        status_color = (0, 0, 255)
        print(f"[ERROR] Không thể kết nối tới {API_URL}: {ex}")


def recognize_face_thread():
    """Luồng nhận diện khuôn mặt bằng DeepFace khi sử dụng Webcam thật"""
    global is_processing, status_message, status_color, detected_face_code, current_face_box
    try:
        from deepface import DeepFace
    except Exception as ex:
        print(f"-> Không tải được DeepFace ({ex}), chuyển sang chế độ giả lập phím bấm.")
        return

    while True:
        if latest_frame is not None and not is_processing:
            is_processing = True
            try:
                frame_to_process = latest_frame.copy()
                dfs = DeepFace.find(
                    img_path=frame_to_process,
                    db_path="dataset",
                    model_name="VGG-Face",
                    detector_backend="mtcnn",
                    enforce_detection=False,
                    silent=True
                )

                if len(dfs) > 0 and not dfs[0].empty:
                    matched_row = dfs[0].iloc[0]
                    matched_path = matched_row["identity"]

                    x = int(matched_row.get("source_x", 120))
                    y = int(matched_row.get("source_y", 80))
                    w = int(matched_row.get("source_w", 220))
                    h = int(matched_row.get("source_h", 220))
                    current_face_box = (x, y, w, h)

                    emp_code = matched_path.replace("\\", "/").split("/")[-1].split(".")[0]
                    send_attendance_to_server(emp_code, bypass_cooldown=False)
                else:
                    detected_face_code = "UNKNOWN"
                    current_face_box = None
            except Exception:
                pass

            is_processing = False
            time.sleep(0.25)


def main():
    global latest_frame, current_face_box, detected_face_code, status_message, status_color

    script_dir = os.path.dirname(os.path.abspath(__file__))
    os.chdir(script_dir)
    dataset_dir = os.path.join(script_dir, "dataset")

    clean_old_pkl_cache(dataset_dir)
    dataset_images = ensure_sample_dataset(dataset_dir)

    force_sim = "--simulate" in sys.argv
    cap = None
    use_simulation = force_sim

    if not force_sim:
        cap = cv2.VideoCapture(0)
        if cap is None or not cap.isOpened():
            use_simulation = True
        else:
            ret, test_frame = cap.read()
            if not ret or test_frame is None:
                use_simulation = True

    if not use_simulation:
        print(f"-> Đã mở Webcam vật lý. Kết nối API: {API_URL}")
        thread = threading.Thread(target=recognize_face_thread, daemon=True)
        thread.start()
    else:
        print(f"-> Khởi chạy CHẾ ĐỘ GIẢ LẬP CAMERA AI (Simulation Mode). Kết nối API: {API_URL}")
        print("   + Nhấn SPACE hoặc phím 1..5 để quét khuôn mặt giáo viên và gửi điểm danh")
        print("   + Nhấn 'a' để Bật/Tắt chế độ tự động quét luân phiên")
        print("   + Nhấn 'q' để thoát")

    sim_index = 0
    auto_scan = False
    last_auto_time = time.time()

    while True:
        if not use_simulation and cap is not None:
            ret, frame = cap.read()
            if not ret or frame is None:
                use_simulation = True
                continue
            latest_frame = frame.copy()
        else:
            # Tạo khung hình giả lập từ ảnh trong dataset
            if dataset_images:
                img_path = dataset_images[sim_index % len(dataset_images)]
                emp_code_preview = os.path.splitext(os.path.basename(img_path))[0]
                raw_img = cv2.imread(img_path)
                if raw_img is not None:
                    frame = cv2.resize(raw_img, (640, 480))
                else:
                    frame = cv2.UMat(480, 640, cv2.CV_8UC3).get()
            else:
                emp_code_preview = "EMP-GV-001"
                frame = cv2.UMat(480, 640, cv2.CV_8UC3).get()

            current_face_box = (180, 75, 280, 290)
            detected_face_code = emp_code_preview

            # Tự động quét luân phiên nếu bật Auto Mode
            if auto_scan and (time.time() - last_auto_time >= 5.0):
                last_auto_time = time.time()
                send_attendance_to_server(emp_code_preview, bypass_cooldown=True)
                sim_index = (sim_index + 1) % max(1, len(dataset_images))

        # Vẽ khung nhận diện khuôn mặt
        if current_face_box is not None:
            x, y, w, h = current_face_box
            box_color = (0, 255, 0) if detected_face_code != "UNKNOWN" else (0, 0, 255)
            cv2.rectangle(frame, (x, y), (x + w, y + h), box_color, 2)
            cv2.putText(frame, f"FACE ID: {detected_face_code}", (x, max(25, y - 10)),
                        cv2.FONT_HERSHEY_SIMPLEX, 0.75, box_color, 2)

        # Header OSD
        cv2.rectangle(frame, (0, 0), (frame.shape[1], 34), (25, 25, 25), -1)
        mode_str = "SIMULATION MODE" if use_simulation else "LIVE WEBCAM"
        auto_str = " [AUTO: ON]" if auto_scan else ""
        cv2.putText(frame, f"SCHOOL HRM - {DEVICE_ID} ({mode_str}){auto_str}", (10, 23),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.58, (0, 255, 255), 2)

        # Thanh trạng thái dưới cùng
        cv2.rectangle(frame, (0, frame.shape[0] - 42), (frame.shape[1], frame.shape[0]), (0, 0, 0), -1)
        cv2.putText(frame, status_message, (10, frame.shape[0] - 14),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.56, status_color, 2)

        cv2.imshow("May Cham Cong Face ID - School HRM", frame)

        key = cv2.waitKey(30) & 0xFF
        if key == ord("q"):
            break
        elif key == ord(" "):
            # Nhấn SPACE để gửi chấm công ngay cho khuôn mặt hiện tại
            target_code = detected_face_code if detected_face_code != "UNKNOWN" else "EMP-GV-001"
            send_attendance_to_server(target_code, bypass_cooldown=True)
        elif key == ord("a"):
            auto_scan = not auto_scan
            last_auto_time = time.time()
            status_message = "DA BAT AUTO QUET LUAN PHIEN" if auto_scan else "DA TAT AUTO QUET"
            status_color = (0, 255, 255)
        elif ord("1") <= key <= ord("5"):
            idx = key - ord("1")
            if dataset_images and idx < len(dataset_images):
                sim_index = idx
                code = os.path.splitext(os.path.basename(dataset_images[sim_index]))[0]
                send_attendance_to_server(code, bypass_cooldown=True)

    if cap is not None:
        cap.release()
    cv2.destroyAllWindows()


if __name__ == "__main__":
    main()
