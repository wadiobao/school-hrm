# ĐỊNH HƯỚNG KIẾN TRÚC TỰ ĐỘNG XẾP THỜI KHÓA BIỂU (SCH-08)

Tài liệu này cung cấp thiết kế kiến trúc, mô hình ràng buộc toán học và định hướng giải thuật để nâng cấp hệ thống xếp thời khóa biểu thủ công hiện tại thành hệ thống **Tự động xếp thời khóa biểu (Automatic Timetable Solver)** trong tương lai.

---

## 1. Bản chất bài toán (Problem Formulation)

Bài toán xếp thời khóa biểu trường học (School Timetabling Problem - STP) thuộc lớp bài toán **NP-Complete** trong khoa học máy tính và tối ưu hóa tổ hợp (Combinatorial Optimization).

- **Biến quyết định (Decision Variables)**:
  $$X_{c, s, t, r, p} \in \{0, 1\}$$
  Trong đó:
  - $c \in C$: Lớp học (SchoolClass)
  - $s \in S$: Môn học (Subject)
  - $t \in T$: Giáo viên (Teacher / Employee)
  - $r \in R$: Phòng học (Classroom)
  - $p \in P$: Tiết học trong tuần (DayOfWeek $\times$ TimeSlot)

Mục tiêu là tìm ma trận phân bổ gán giá trị 1 sao cho:
1. Thỏa mãn **100% Ràng buộc cứng (Hard Constraints)**.
2. Tối đa hóa điểm số / Tối thiểu hóa hình phạt của **Ràng buộc mềm (Soft Constraints)**.

---

## 2. Phân loại ràng buộc (Constraints Catalog)

### 2.1. Hard Constraints (Bắt buộc vi phạm = 0)
- **HC-01 (Lớp - Thời gian)**: Mỗi lớp học tại một thời điểm $p$ chỉ được học tối đa 1 môn:
  $$\forall c, p: \sum_{s, t, r} X_{c, s, t, r, p} \le 1$$
- **HC-02 (Giáo viên - Thời gian)**: Mỗi giáo viên tại một thời điểm $p$ chỉ dạy tối đa 1 lớp:
  $$\forall t, p: \sum_{c, s, r} X_{c, s, t, r, p} \le 1$$
- **HC-03 (Phòng học - Thời gian)**: Mỗi phòng học tại một thời điểm $p$ chỉ phục vụ tối đa 1 lớp:
  $$\forall r, p: \sum_{c, s, t} X_{c, s, t, r, p} \le 1$$
- **HC-04 (Hạn mức số tiết môn học)**: Tổng số tiết xếp cho môn $s$ của lớp $c$ phải bằng đúng `periodsPerWeek` trong `ClassSubject`:
  $$\forall c, s: \sum_{t, r, p} X_{c, s, t, r, p} = \text{periodsPerWeek}(c, s)$$
- **HC-05 (Chuyên môn giáo viên)**: Giáo viên $t$ được gán dạy môn $s$ phải có chứng chỉ chuyên môn (`TeacherSubjectQualification`):
  $$X_{c, s, t, r, p} = 0 \quad \text{nếu } (t, s) \notin \text{Qualifications}$$
- **HC-06 (Sức chứa phòng học)**: Sức chứa của phòng $r$ phải đáp ứng sĩ số lớp $c$:
  $$X_{c, s, t, r, p} = 0 \quad \text{nếu } \text{capacity}(r) < \text{capacity}(c)$$

### 2.2. Soft Constraints (Tối ưu hóa chất lượng TKB)
- **SC-01 (Giảm tiết trống của giáo viên)**: Hạn chế tình trạng giáo viên dạy Tiết 1 rồi trống Tiết 2, 3 và dạy lại Tiết 4.
- **SC-02 (Phân bổ môn học đều trong tuần)**: Các môn có $\ge 3$ tiết/tuần không nên dồn hết vào 1 ngày mà nên rải đều các thứ.
- **SC-03 (Tránh quá tải học sinh)**: Không xếp quá 2 tiết cùng một môn khó (Toán, Lý, Hóa) liên tục trong cùng một buổi.
- **SC-04 (Nguyện vọng giờ dạy)**: Tôn trọng khung giờ giáo viên đăng ký bận hoặc ưu tiên giảng dạy.

---

## 3. Kiến trúc hệ thống đề xuất

```text
┌────────────────────────────────────────────────────────┐
│               TimetableGenerationService               │
└──────────────────────────┬─────────────────────────────┘
                           │ 1. Chuẩn bị dữ liệu bài toán
                           ▼
┌────────────────────────────────────────────────────────┐
│                TimetableProblemBuilder                 │
│  - Load Classes, Subjects, Teachers, Rooms, TimeSlots  │
│  - Load Constraints & Parameters                       │
└──────────────────────────┬─────────────────────────────┘
                           │ 2. Mô hình hóa bài toán CSP
                           ▼
┌────────────────────────────────────────────────────────┐
│               SchedulingSolver (Engine)                │
│  Lựa chọn công nghệ:                                   │
│  A. Timefold Solver (OptaPlanner) (Java Native)        │
│  B. Google OR-Tools CP-SAT (via JNI / Microservice)    │
│  C. Heuristic Backtracking with Constraint Propagation  │
└──────────────────────────┬─────────────────────────────┘
                           │ 3. Tạo kết quả ứng viên
                           ▼
┌────────────────────────────────────────────────────────┐
│             Existing TimetableValidator                │
│  - Kiểm tra lại 100% bằng bộ Validator hiện có         │
│  - Đảm bảo tính nhất quán tuyệt đối                    │
└──────────────────────────┬─────────────────────────────┘
                           │ 4. Lưu bản nháp
                           ▼
┌────────────────────────────────────────────────────────┐
│            DRAFT Timetable (Version mới)               │
│  Người dùng có thể xem trước, tinh chỉnh trên UI       │
└────────────────────────────────────────────────────────┘
```

---

## 4. Lựa chọn công nghệ giải thuật cho Spring Boot

1. **Khuyến nghị số 1: Timefold Solver (Kế thừa OptaPlanner)**:
   - Viết hoàn toàn bằng Java, tích hợp mượt mà với Spring Boot (`timefold-solver-spring-boot-starter`).
   - Sử dụng giải thuật meta-heuristics (Tabu Search, Simulated Annealing) kết hợp Constraint Streams.
   - Thích hợp giải bài toán hàng ngàn tiết học chỉ trong vài chục giây.

2. **Khuyến nghị số 2: Google OR-Tools CP-SAT**:
   - Bộ giải CSP mạnh mẽ nhất thế giới hiện nay.
   - Có thể chạy dưới dạng service riêng (Python FastAPI / C++) hoặc gọi thư viện Java bindings.
