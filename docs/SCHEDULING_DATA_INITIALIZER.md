# Tài Liệu Kỹ Thuật: Khởi Tạo Dữ Liệu Mẫu Phân Hệ Thời Khóa Biểu & 10 Ban Chuyên Môn

Class chịu trách nhiệm: `com.kltn.school_hrm.shared.configuration.SchedulingDataInitializer`

---

## 1. Tổng quan Kiến trúc

- **Package**: `com.kltn.school_hrm.shared.configuration`
- **Mục đích**: Tự động kiểm tra và khởi tạo dữ liệu mẫu cho 10 Ban (Bộ môn) chuẩn Chương trình GDPT, mỗi ban gồm đúng **3 nhân sự (1 Trưởng bộ môn + 2 Giáo viên)**, cùng với dữ liệu Học vụ, Phân công giảng dạy và Thời khóa biểu mẫu.
- **Thứ tự thực thi**: `@Order(3)` cùng `@DependsOn({"roleDataInitializer", "dataInitializer"})`.
- **Tính an toàn (Idempotency)**: Tất cả phương thức `seed*` đều kiểm tra sự tồn tại trong Database (`existsBy*` hoặc `findBy*`) trước khi lưu, đảm bảo an toàn tuyệt đối khi restart ứng dụng nhiều lần.

---

## 2. Danh mục 10 Môn học & 10 Ban Chuyên môn (3 Người / Ban: 1 Trưởng + 2 Giáo viên)

> **Mật khẩu mặc định**:
> - Tất cả các Trưởng bộ môn mới: `Head@123` (riêng Trưởng ban Toán `mathhead`: `MathHead@123`)
> - Tất cả các Giáo viên: `Teacher@123`

| STT | Mã Ban (`Department`) | Tên Ban / Bộ môn | Mã Môn (`Subject`) | Tên Môn & Mô tả chuẩn | 1 Trưởng Bộ Môn (`HEAD_OF_DEPARTMENT`) | 2 Giáo Viên (`TEACHER`) |
| :-: | :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | `BAN-VAN` | **Bộ môn Ngữ văn** | `LIT` | **Ngữ văn**: Rèn luyện kỹ năng đọc, viết, nói, nghe và cảm thụ văn học. | `head_van` (`EMP-VAN-H01`)<br>ThS. Lê Thị Trưởng Ban Văn | 1. `gv_van1` (`EMP-VAN-T01`) - Cô Nguyễn Thị Ngữ Văn<br>2. `gv_van2` (`EMP-VAN-T02`) - Thầy Trần Văn Văn Học |
| **2** | `BAN-TOAN` | **Bộ môn Toán** | `MATH` | **Toán**: Học về số học, đại số, hình học, đo lường, thống kê và xác suất. | `mathhead` (`EMP-TBT-001`)<br>ThS. Trần Thị Trưởng Ban Toán | 1. `teacher` (`EMP-GV-001`) - Thầy Hoàng Văn Giáo Viên<br>2. `gv_toan2` (`EMP-TOAN-T02`) - Cô Phạm Thị Đại Số |
| **3** | `BAN-NN1` | **Bộ môn Ngoại ngữ 1** | `ENG` | **Ngoại ngữ 1**: Thường là tiếng Anh (hoặc các ngoại ngữ khác tùy điều kiện từng trường). | `head_nn1` (`EMP-NN1-H01`)<br>ThS. Phạm Văn Trưởng Ban Ngoại Ngữ | 1. `gv_nn1` (`EMP-NN1-T01`) - Cô Đặng Thị Tiếng Anh<br>2. `gv_nn2` (`EMP-NN1-T02`) - Thầy Vũ Văn Ngoại Ngữ |
| **4** | `BAN-GDCD` | **Bộ môn Giáo dục công dân** | `GDCD` | **Giáo dục công dân**: Giáo dục đạo đức, kỹ năng sống, kinh tế và pháp luật cơ bản. | `head_gdcd` (`EMP-GDCD-H01`)<br>ThS. Bùi Văn Trưởng Ban GDCD | 1. `gv_gdcd1` (`EMP-GDCD-T01`) - Cô Hoàng Thị Công Dân<br>2. `gv_gdcd2` (`EMP-GDCD-T02`) - Thầy Ngô Văn Pháp Luật |
| **5** | `BAN-SUDIA` | **Bộ môn Lịch sử và Địa lí** | `HIST_GEO` | **Lịch sử và Địa lí**: Đây là môn tích hợp gồm hai phân môn Lịch sử và Địa lí. | `head_sudia` (`EMP-SD-H01`)<br>ThS. Dương Văn Trưởng Ban Sử Địa | 1. `gv_sudia1` (`EMP-SD-T01`) - Thầy Lý Văn Lịch Sử<br>2. `gv_sudia2` (`EMP-SD-T02`) - Cô Mai Thị Địa Lí |
| **6** | `BAN-KHTN` | **Bộ môn Khoa học tự nhiên** | `KHTN` | **Khoa học tự nhiên**: Đây là môn tích hợp kiến thức của ba phân môn Vật lí, Hóa học và Sinh học. | `head_khtn` (`EMP-KHTN-H01`)<br>TS. Hoàng Văn Trưởng Ban KHTN | 1. `gv_khtn1` (`EMP-KHTN-T01`) - Thầy Trịnh Văn Vật Lí<br>2. `gv_khtn2` (`EMP-KHTN-T02`) - Cô Võ Thị Hóa Sinh |
| **7** | `BAN-CN` | **Bộ môn Công nghệ** | `TECH` | **Công nghệ**: Tìm hiểu về kỹ thuật, nông nghiệp, công nghiệp, điện tử và hướng nghiệp. | `head_cn` (`EMP-CN-H01`)<br>ThS. Đỗ Văn Trưởng Ban Công Nghệ | 1. `gv_cn1` (`EMP-CN-T01`) - Thầy Phan Văn Kỹ Thuật<br>2. `gv_cn2` (`EMP-CN-T02`) - Cô Lâm Thị Công Nghệ |
| **8** | `BAN-TIN` | **Bộ môn Tin học** | `IT` | **Tin học**: Học kỹ năng sử dụng máy tính, công nghệ thông tin, an toàn mạng và lập trình cơ bản. | `head_tin` (`EMP-TIN-H01`)<br>ThS. Đinh Văn Trưởng Ban Tin Học | 1. `gv_tin1` (`EMP-TIN-T01`) - Thầy Trương Văn Lập Trình<br>2. `gv_tin2` (`EMP-TIN-T02`) - Cô Châu Thị Tin Học |
| **9** | `BAN-GDTC` | **Bộ môn Giáo dục thể chất** | `PE` | **Giáo dục thể chất**: Rèn luyện thể lực và các môn thể thao tự chọn. | `head_gdtc` (`EMP-GDTC-H01`)<br>ThS. Lương Văn Trưởng Ban Thể Chất | 1. `gv_gdtc1` (`EMP-GDTC-T01`) - Thầy Hà Văn Thể Thao<br>2. `gv_gdtc2` (`EMP-GDTC-T02`) - Cô Tạ Thị Điền Kinh |
| **10** | `BAN-NGHETHUAT` | **Bộ môn Nghệ thuật** | `ART` | **Nghệ thuật**: Gồm hai nội dung là Âm nhạc và Mỹ thuật. | `head_nghethuat` (`EMP-NT-H01`)<br>ThS. Cao Thị Trưởng Ban Nghệ Thuật | 1. `gv_nt1` (`EMP-NT-T01`) - Cô Đoàn Thị Âm Nhạc<br>2. `gv_nt2` (`EMP-NT-T02`) - Thầy Huỳnh Văn Mỹ Thuật |

> **Đặc điểm nghiệp vụ được tự động gán**:
> 1. Mỗi Trưởng bộ môn được tự động gán làm `Department.manager` của ban tương ứng.
> 2. Cả 3 thành viên trong mỗi ban (1 Trưởng + 2 Giáo viên) đều được cấp chứng chỉ chuyên môn (`TeacherSubjectQualification`) của môn học thuộc ban đó, sẵn sàng tham gia đứng lớp hoặc dạy thay.

---

## 3. Khung chương trình học (`ClassSubject`) & Thời khóa biểu mẫu Lớp 10A1

Khởi tạo đầy đủ 10 môn học cho Lớp `10A1` trong `Học kỳ 1 (2025-2026)` (tổng **23 tiết/tuần**), khớp 100% giữa `ClassSubject`, `TeachingAssignment` và `TimetableEntry`:
- **Thứ 2**: Tiết 1-2 Toán, Tiết 3-4 Ngữ văn, Tiết 5 Giáo dục công dân
- **Thứ 3**: Tiết 1-2 Ngoại ngữ 1, Tiết 3-4 Khoa học tự nhiên (Tiết 4 có Cô Võ Thị Hóa Sinh dạy thay), Tiết 5 Công nghệ
- **Thứ 4**: Tiết 1-2 Toán, Tiết 3-4 Ngữ văn
- **Thứ 5**: Tiết 1-2 Lịch sử và Địa lí, Tiết 3-4 Tin học (Phòng LAB-IT1)
- **Thứ 6**: Tiết 1 Ngoại ngữ 1, Tiết 2 Khoa học tự nhiên (Phòng LAB-KHTN), Tiết 3-4 Giáo dục thể chất (Sân SAN-GDTC)
- **Thứ 7**: Tiết 1 Nghệ thuật
