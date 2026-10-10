Toàn bộ kiến trúc của module **Teaching (Quản lý giảng dạy)** được thiết kế đặc thù cho môi trường trường học/giáo dục, quản lý phân công giảng dạy, định mức giờ dạy và nhật ký giảng dạy thực tế của giáo viên. Dưới đây là vai trò chi tiết của từng class trong module:

---

### 1. Nhóm Entity (Mô hình dữ liệu - Database)

| Class | Vai trò | Mô tả chi tiết |
| :--- | :--- | :--- |
| [TeachingNorm](/src/main/java/com/kltn/school_hrm/module/teaching/entity/TeachingNorm.java) | **Định mức giờ giảng dạy** | Quy định số giờ dạy tiêu chuẩn theo năm học/học kỳ (`academicYear`, `semester`), loại giáo viên (`TeacherType: EXPAT, LOCAL, BILINGUAL`) hoặc chức danh chuyên môn. Căn cứ để tính giờ dạy vượt định mức (overtime). |
| [TeachingAssignment](/src/main/java/com/kltn/school_hrm/module/teaching/entity/TeachingAssignment.java) | **Phân công môn học & lớp học** | Ghi nhận giáo viên được phân công dạy môn học nào (`subjectName`), lớp nào (`className`), năm học và số tiết theo kế hoạch đào tạo. |
| [TeachingLog](/src/main/java/com/kltn/school_hrm/module/teaching/entity/TeachingLog.java) | **Nhật ký giảng dạy thực tế** | Ghi nhận từng buổi dạy diễn ra: ngày dạy (`teachingDate`), số tiết dạy thực tế (`actualHours`), chủ đề/bài học, và trạng thái xác nhận hoàn thành buổi học. |

---

### 2. Nhóm Service (Xử lý nghiệp vụ)

| Class / Interface | Vai trò | Mô tả chi tiết |
| :--- | :--- | :--- |
| [TeachingAssignmentService](/src/main/java/com/kltn/school_hrm/module/teaching/service/TeachingAssignmentService.java) | **Quản lý kế hoạch phân công dạy học** | Tiếp nhận và quản lý thời khóa biểu/phân công giảng dạy của Ban Giám hiệu hoặc Trưởng bộ môn cho giáo viên từng học kỳ. |
| [TeachingService](/src/main/java/com/kltn/school_hrm/module/teaching/service/TeachingService.java) | **Tổng hợp giờ dạy & Tính toán thù lao** | **Xử lý chuyên môn giảng dạy:** Điểm danh/ghi nhận nhật ký giờ dạy, đối soát số giờ thực dạy với định mức chuẩn trong `TeachingNorm` để tính số giờ vượt định mức chuyển sang module Payroll chi trả. |

---

### 3. Mối quan hệ tương tác giữa các Class trong hệ thống

```text
[Ban giám hiệu / Bộ môn]
             │
             ▼
    [TeachingAssignment] ──► [Giáo viên nhận lớp & môn học]
             │
             ▼ (thực tế giảng dạy)
       [TeachingLog] ──────────► [TeachingService] ◄── [TeachingNorm] (Số tiết định mức chuẩn)
                                        │
                                        ▼ (Tính chênh lệch / vượt giờ)
                                [Chuyển dữ liệu sang Payroll]
```
