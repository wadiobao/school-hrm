# Tài Liệu Kỹ Thuật: Khởi Tạo Dữ Liệu Mẫu Phân Hệ Thời Khóa Biểu & Học Vụ

Class chịu trách nhiệm: `com.kltn.school_hrm.shared.configuration.SchedulingDataInitializer`

---

## 1. Tổng quan Kiến trúc

- **Package**: `com.kltn.school_hrm.shared.configuration`
- **Mục đích**: Tự động kiểm tra và khởi tạo dữ liệu mẫu cho toàn bộ chu trình Học vụ, Phân công giảng dạy và Thời khóa biểu khi ứng dụng Spring Boot khởi động.
- **Thứ tự thực thi**: `@Order(2)` — Chạy ngay sau `DataInitializer` nền tảng (nơi tạo Roles, Departments, Positions cơ bản).
- **Tính an toàn (Idempotency)**: Tất cả phương thức `seed*` đều kiểm tra sự tồn tại trong Database (`existsBy*` hoặc `findBy*`) trước khi lưu, đảm bảo an toàn tuyệt đối khi restart ứng dụng nhiều lần.

---

## 2. Chi tiết 10 Khối Dữ liệu Khởi tạo

### 2.1. Tài khoản & Nhân sự mới (Users & Employees)

| Username | Password | Mã NV | Họ và tên | Chức vụ (Position) | Vai trò (Role) | Bộ phận |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `viceprincipal` | `VicePrincipal@123` | `EMP-VP-001` | **Trần Thị Phó Hiệu Trưởng** | `VICE_PRINCIPAL` | `BOARD_OF_DIRECTORS` | Ban Giám hiệu |
| `teachervan` | `Teacher@123` | `EMP-T-VAN-001` | **Lê Thị Ngữ Văn** | `TEACHER` | `TEACHER` | Ban Giám hiệu / Văn |
| `teacheranh` | `Teacher@123` | `EMP-T-ENG-001` | **Phạm Văn Tiếng Anh** | `TEACHER` | `TEACHER` | Bộ môn Tiếng Anh |
| `teacherly` | `Teacher@123` | `EMP-T-PHYS-001` | **Hoàng Văn Vật Lý** | `TEACHER` | `TEACHER` | Bộ môn Vật lý |

> **Lưu ý**: Tài khoản `viceprincipal` cùng với `hrmanager` là 2 tài khoản đại diện sở hữu thẩm quyền tạo, kiểm tra xung đột và công bố thời khóa biểu theo đúng yêu cầu nghiệp vụ.

---

### 2.2. Năm học & Học kỳ (Academic Year & Semesters)
- **Năm học**:
  - Tên: `Năm học 2025-2026`
  - Thời gian: `01/09/2025` $\rightarrow$ `30/06/2026`
  - Trạng thái: `ACTIVE`
- **Học kỳ trực thuộc**:
  - **Học kỳ 1**: `Học kỳ 1 (2025-2026)` (`01/09/2025` $\rightarrow$ `15/01/2026`), Trạng thái: `ACTIVE`
  - **Học kỳ 2**: `Học kỳ 2 (2025-2026)` (`16/01/2026` $\rightarrow$ `30/06/2026`), Trạng thái: `PLANNING`

---

### 2.3. Danh mục Môn học (Subjects)
Khởi tạo 8 môn học cốt lõi theo chương trình THPT, trạng thái `SubjectStatus.ACTIVE`:

| Mã môn (`code`) | Tên môn học (`name`) | Mô tả chi tiết |
| :--- | :--- | :--- |
| `MATH` | **Toán học** | Môn Toán chuẩn THPT |
| `LIT` | **Ngữ văn** | Môn Ngữ văn chuẩn THPT |
| `ENG` | **Tiếng Anh** | Môn Tiếng Anh hệ 10 năm |
| `PHYS` | **Vật lý** | Môn Vật lý thực nghiệm |
| `CHEM` | **Hóa học** | Môn Hóa học |
| `BIO` | **Sinh học** | Môn Sinh học |
| `IT` | **Tin học** | Môn Tin học ứng dụng |
| `PE` | **Giáo dục thể chất** | Môn Thể dục thể thao |

---

### 2.4. Danh mục Phòng học & Cơ sở vật chất (Classrooms)
Khởi tạo 6 địa điểm giảng dạy, trạng thái `ClassroomStatus.AVAILABLE`:

| Mã phòng (`code`) | Tên phòng học | Sức chứa | Loại phòng (`ClassroomType`) |
| :--- | :--- | :---: | :--- |
| `P101` | Phòng học 101 (Nhà A, Tầng 1) | 45 | `STANDARD` (Tiêu chuẩn / Lý thuyết) |
| `P102` | Phòng học 102 (Nhà A, Tầng 1) | 45 | `STANDARD` (Tiêu chuẩn / Lý thuyết) |
| `P103` | Phòng học 103 (Nhà A, Tầng 1) | 45 | `STANDARD` (Tiêu chuẩn / Lý thuyết) |
| `LAB-IT1` | Phòng máy tính 1 (Nhà B, Tầng 2) | 40 | `LAB` (Phòng thực hành) |
| `LAB-PHYS` | Phòng thí nghiệm Lý (Nhà B, Tầng 2) | 40 | `LAB` (Phòng thí nghiệm) |
| `SAN-GDTC` | Sân Thể thao ngoài trời | 100 | `GYM` (Khu thể chất) |

---

### 2.5. Danh sách Lớp học (School Classes)
Khởi tạo 3 lớp học thuộc Năm học 2025-2026, trạng thái `ClassStatus.ACTIVE`:

| Tên lớp | Khối | Sĩ số | Giáo viên Chủ nhiệm (GVCN) |
| :--- | :---: | :---: | :--- |
| **10A1** | 10 | 45 | Nguyễn Văn Giáo Viên (`EMP-TEACHER-001`) |
| **10A2** | 10 | 45 | Lê Thị Ngữ Văn (`EMP-T-VAN-001`) |
| **11A1** | 11 | 42 | Hoàng Văn Vật Lý (`EMP-T-PHYS-001`) |

---

### 2.6. Khung 9 Tiết học chuẩn (Time Slots)
Khởi tạo khung giờ biểu chuẩn gồm 5 tiết sáng và 4 tiết chiều:

- **Buổi sáng (`MORNING`)**:
  - Tiết 1: `07:00` – `07:45`
  - Tiết 2: `07:50` – `08:35`
  - *(Ra chơi 15 phút)*
  - Tiết 3: `08:50` – `09:35`
  - Tiết 4: `09:40` – `10:25`
  - Tiết 5: `10:30` – `11:15`
- **Buổi chiều (`AFTERNOON`)**:
  - Tiết 6: `13:00` – `13:45`
  - Tiết 7: `13:50` – `14:35`
  - *(Ra chơi 15 phút)*
  - Tiết 8: `14:50` – `15:35`
  - Tiết 9: `15:40` – `16:25`

---

### 2.7. Chuyên môn Giáo viên (Teacher Qualifications - BR-09)
Thiết lập dữ liệu chứng chỉ chuyên môn cho giáo viên phục vụ kiểm tra ràng buộc phân công:

| Giáo viên | Môn học chuyên môn | Bằng cấp / Chứng chỉ | Trạng thái |
| :--- | :--- | :--- | :--- |
| Nguyễn Văn Giáo Viên | `MATH` (Toán học) | Cử nhân Sư phạm Toán | `ACTIVE` |
| Lê Thị Ngữ Văn | `LIT` (Ngữ văn) | Cử nhân Sư phạm Ngữ văn | `ACTIVE` |
| Phạm Văn Tiếng Anh | `ENG` (Tiếng Anh) | Thạc sĩ TESOL, IELTS 8.0 | `ACTIVE` |
| Hoàng Văn Vật Lý | `PHYS` (Vật lý) | Cử nhân Sư phạm Vật lý | `ACTIVE` |

---

### 2.8. Định mức Giảng dạy (Teaching Norms - BR-07, BR-08)
- Vị trí: `PositionCode.TEACHER` (Giáo viên)
- Năm học: `2025-2026`
- Định mức chuẩn: **17 tiết / tuần**
- Tỷ lệ miễn giảm: 0%

---

### 2.9. Phân công Giảng dạy Lớp - Môn (Teaching Assignments - 1 Năm học)
Thực hiện phân công giáo viên cho lớp **10A1** (quy tắc: **1 giáo viên duy nhất cho 1 môn/lớp trong cả năm học**):

| Lớp | Môn học | Giáo viên phụ trách | Số tiết/tuần | Chương trình |
| :--- | :--- | :--- | :---: | :--- |
| **10A1** | Toán học | Nguyễn Văn Giáo Viên | 4.0 | `MOET` |
| **10A1** | Ngữ văn | Lê Thị Ngữ Văn | 4.0 | `MOET` |
| **10A1** | Tiếng Anh | Phạm Văn Tiếng Anh | 3.0 | `MOET` |
| **10A1** | Vật lý | Hoàng Văn Vật Lý | 2.0 | `MOET` |

---

### 2.10. Thời khóa biểu mẫu (Sample Timetable & Entries)
Tạo sẵn 1 thời khóa biểu chính thức đã công bố để kiểm thử giao diện:
- **Lớp**: `10A1`
- **Học kỳ**: `Học kỳ 1 (2025-2026)`
- **Phiên bản**: `v1`
- **Trạng thái**: `TimetableStatus.PUBLISHED`
- **Chi tiết phân bổ các tiết học**:
  - **Thứ 2 (Monday)**:
    - Tiết 1: Môn Toán (GV chính: Nguyễn Văn Giáo Viên, Phòng: `P101`)
    - Tiết 2: Môn Toán (GV chính: Nguyễn Văn Giáo Viên, Phòng: `P101`)
    - Tiết 3: Môn Ngữ văn (GV chính: Lê Thị Ngữ Văn, Phòng: `P101`)
    - Tiết 4: Môn Ngữ văn (GV chính: Lê Thị Ngữ Văn, Phòng: `P101`)
  - **Thứ 3 (Tuesday)**:
    - Tiết 1: Môn Tiếng Anh (GV chính: Phạm Văn Tiếng Anh, Phòng: `P101`)
    - Tiết 2: Môn Tiếng Anh (GV chính: Phạm Văn Tiếng Anh, Phòng: `P101`)
    - Tiết 3: Môn Vật lý (GV chính: Hoàng Văn Vật Lý, Phòng: `P101`)
    - **Tiết 4: Môn Vật lý — Có Giáo viên dạy thay mẫu: Hoàng Văn Vật Lý $\rightarrow$ Nguyễn Văn Giáo Viên dạy thay** *(hiển thị badge cảnh báo dạy thay màu cam trên lưới)*
  - **Thứ 4 (Wednesday)**:
    - Tiết 1: Môn Toán (GV: Nguyễn Văn Giáo Viên, Phòng: `P101`)
    - Tiết 2: Môn Toán (GV: Nguyễn Văn Giáo Viên, Phòng: `P101`)
    - Tiết 3: Môn Ngữ văn (GV: Lê Thị Ngữ Văn, Phòng: `P101`)
    - Tiết 4: Môn Ngữ văn (GV: Lê Thị Ngữ Văn, Phòng: `P101`)

---

## 3. Cách thức Tái sử dụng & Kiểm tra
1. Khi khởi động ứng dụng, console log sẽ hiển thị:
   ```text
   === SchedulingDataInitializer: Bắt đầu khởi tạo dữ liệu mẫu Học vụ & Thời khóa biểu ===
     [+] Đã tạo Nhân sự: Trần Thị Phó Hiệu Trưởng (EMP-VP-001) - username: viceprincipal
     ...
     [+] Đã tạo Thời khóa biểu mẫu (PUBLISHED v1) cho Lớp: 10A1 - Học kỳ: Học kỳ 1 (2025-2026)
   === SchedulingDataInitializer: Hoàn tất khởi tạo dữ liệu mẫu Học vụ & Thời khóa biểu ===
   ```
2. Đăng nhập với tài khoản `hrmanager` hoặc `viceprincipal` và truy cập `http://localhost:8080/scheduling`, chọn Lớp `10A1` để thấy ngay ma trận thời khóa biểu hoàn chỉnh.

