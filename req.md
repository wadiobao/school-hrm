# Thiết kế Database & Business Rules — Class Scheduling

Theo mô hình bạn đã chọn:

- Thời khóa biểu cố định theo tuần, từ thứ 2 đến thứ 6.
- Mỗi môn học trong một lớp chỉ có một giáo viên phụ trách trong một học kỳ.
- Một giáo viên có thể dạy nhiều lớp và nhiều môn nếu đủ điều kiện.
- Một lớp có nhiều môn học.
- Một tiết học gắn với một lớp, môn học, giáo viên, phòng học và khung giờ cụ thể.

Tôi đề xuất chia thành ba module: Academic Management, Teaching Assignment và Timetable Management. Thiết kế theo Modular Monolith, phù hợp với Spring Boot và cấu trúc domain/application/infrastructure mà bạn đang theo đuổi.

## 1. Phân biệt các khái niệm

Trước khi thiết kế database, cần phân biệt:

| Khái niệm           | Ý nghĩa                                     |
| ------------------- | ------------------------------------------- |
| Academic Year       | Năm học, ví dụ 2026–2027                    |
| Semester            | Học kỳ thuộc một năm học                    |
| SchoolClass         | Lớp học, ví dụ 10A1                         |
| Subject             | Môn học, ví dụ Toán                         |
| Teaching Assignment | Phân công giáo viên dạy một môn cho một lớp |
| Time Slot           | Khung giờ/tiết học trong ngày               |
| Timetable           | Thời khóa biểu của một lớp trong một học kỳ |
| Timetable Entry     | Một tiết học được xếp vào lịch              |

Điểm thiết kế: `TeachingAssignment` không phải là `TimetableEntry`.

Ví dụ:

- Phân công: Giáo viên A dạy Toán lớp 10A1, 5 tiết/tuần.
- Thời khóa biểu: Giáo viên A dạy Toán lớp 10A1 vào thứ 2 tiết 1, thứ 3 tiết 2...

Một phân công có thể tạo ra nhiều tiết học trong thời khóa biểu.

## 2. Database Schema

### 2.1. Academic Management

Các bảng dữ liệu nền tảng.

`academic_year`

| Column     | Type        | Constraint |
| ---------- | ----------- | ---------- |
| id         | BIGINT      | PK         |
| name       | VARCHAR(20) | UNIQUE     |
| start_date | DATE        | NOT NULL   |
| end_date   | DATE        | NOT NULL   |
| status     | VARCHAR(20) | NOT NULL   |

`semester`

| Column           | Type        | Constraint |
| ---------------- | ----------- | ---------- |
| id               | BIGINT      | PK         |
| academic_year_id | BIGINT      | FK         |
| name             | VARCHAR(30) | NOT NULL   |
| start_date       | DATE        | NOT NULL   |
| end_date         | DATE        | NOT NULL   |
| status           | VARCHAR(20) | NOT NULL   |

`school_class`

| Column              | Type        | Constraint       |
| ------------------- | ----------- | ---------------- |
| id                  | BIGINT      | PK               |
| name                | VARCHAR(50) | NOT NULL         |
| grade_level         | INT         | NOT NULL         |
| academic_year_id    | BIGINT      | FK               |
| homeroom_teacher_id | BIGINT      | FK → employee.id |
| capacity            | INT         | NULL             |
| status              | VARCHAR(20) | NOT NULL         |

`subject`

| Column      | Type         | Constraint |
| ----------- | ------------ | ---------- |
| id          | BIGINT       | PK         |
| code        | VARCHAR(30)  | UNIQUE     |
| name        | VARCHAR(100) | NOT NULL   |
| description | TEXT         | NULL       |
| status      | VARCHAR(20)  | NOT NULL   |

`class_subject`

| Column           | Type   | Constraint |
| ---------------- | ------ | ---------- |
| id               | BIGINT | PK         |
| class_id         | BIGINT | FK         |
| subject_id       | BIGINT | FK         |
| periods_per_week | INT    | NOT NULL   |
| semester_id      | BIGINT | FK         |

UNIQUE(class_id, subject_id, semester_id)

`class_subject` rất quan trọng. Nó định nghĩa chương trình học của từng lớp trong một học kỳ: lớp đó học môn gì và cần bao nhiêu tiết mỗi tuần.

Không nên để `periods_per_week` trong `Subject`, vì cùng một môn có thể có số tiết khác nhau giữa các khối lớp.

### 2.2. Teacher Allocation

`teacher_subject_qualification`

Lưu các môn mà giáo viên đủ điều kiện giảng dạy.

| Column              | Type        | Constraint       |
| ------------------- | ----------- | ---------------- |
| id                  | BIGINT      | PK               |
| teacher_id          | BIGINT      | FK → employee.id |
| subject_id          | BIGINT      | FK → subject.id  |
| qualification_level | VARCHAR(30) | NULL             |
| status              | VARCHAR(20) | NOT NULL         |

Ràng buộc:

```
UNIQUE (teacher_id, subject_id)
```

`teaching_assignment`

Lưu phân công giáo viên cho một môn học của một lớp trong một học kỳ.

| Column           | Type        | Constraint       |
| ---------------- | ----------- | ---------------- |
| id               | BIGINT      | PK               |
| class_subject_id | BIGINT      | FK               |
| teacher_id       | BIGINT      | FK → employee.id |
| assigned_by      | BIGINT      | FK → user.id     |
| assigned_at      | TIMESTAMP   | NOT NULL         |
| status           | VARCHAR(20) | NOT NULL         |

Ràng buộc:

```
UNIQUE (class_subject_id)
```

Ràng buộc này bảo đảm mỗi môn của một lớp trong một học kỳ chỉ có một giáo viên phụ trách tại một thời điểm.

Nếu cần thay giáo viên, nên cập nhật assignment có kiểm soát và lưu lịch sử thay đổi, không tạo thêm assignment đang hoạt động cho cùng `class_subject_id`.

### 2.3. Timetable Management

`time_slot`

| Column        | Type        | Constraint |
| ------------- | ----------- | ---------- |
| id            | BIGINT      | PK         |
| period_number | INT         | NOT NULL   |
| start_time    | TIME        | NOT NULL   |
| end_time      | TIME        | NOT NULL   |
| session       | VARCHAR(20) | NOT NULL   |

Ví dụ:

| Period | Session   | Start | End   |
| ------ | --------- | ----- | ----- |
| 1      | MORNING   | 07:00 | 07:45 |
| 2      | MORNING   | 07:50 | 08:35 |
| 3      | MORNING   | 08:45 | 09:30 |
| 1      | AFTERNOON | 13:00 | 13:45 |
| 2      | AFTERNOON | 13:50 | 14:35 |

Nếu có cả ca sáng và chiều, `period_number` có thể lặp lại giữa các ca. Vì vậy nên có:

```
UNIQUE (session, period_number)
```

`classroom`

| Column    | Type         | Constraint |
| --------- | ------------ | ---------- |
| id        | BIGINT       | PK         |
| code      | VARCHAR(30)  | UNIQUE     |
| name      | VARCHAR(100) | NOT NULL   |
| capacity  | INT          | NULL       |
| room_type | VARCHAR(30)  | NULL       |
| status    | VARCHAR(20)  | NOT NULL   |

`timetable`

Một bản thời khóa biểu của một lớp trong một học kỳ.

| Column       | Type        | Constraint   |
| ------------ | ----------- | ------------ |
| id           | BIGINT      | PK           |
| class_id     | BIGINT      | FK           |
| semester_id  | BIGINT      | FK           |
| version      | INT         | NOT NULL     |
| status       | VARCHAR(20) | NOT NULL     |
| created_by   | BIGINT      | FK → user.id |
| published_at | TIMESTAMP   | NULL         |

Ràng buộc:

```
UNIQUE (class_id, semester_id, version)
```

Không đặt `UNIQUE(class_id, semester_id)` vì một lớp có thể có nhiều phiên bản thời khóa biểu trong cùng học kỳ.

`timetable_entry`

Một dòng tương ứng với một tiết học cụ thể.

| Column                 | Type     | Constraint |
| ---------------------- | -------- | ---------- |
| id                     | BIGINT   | PK         |
| timetable_id           | BIGINT   | FK         |
| teaching_assignment_id | BIGINT   | FK         |
| day_of_week            | SMALLINT | NOT NULL   |
| time_slot_id           | BIGINT   | FK         |
| classroom_id           | BIGINT   | FK         |

Ví dụ:

```
TimetableEntry
----------------------------------------------------
Timetable | Assignment | Day | Slot | Classroom
----------------------------------------------------
1         | 10         | 1   | 1    | P101
1         | 11         | 1   | 2    | P101
1         | 12         | 2   | 1    | P102
```

Quy ước `day_of_week`:

```
1 = MONDAY
2 = TUESDAY
3 = WEDNESDAY
4 = THURSDAY
5 = FRIDAY
6 = SATURDAY
7 = SUNDAY
```

Nếu trường chỉ học từ thứ 2 đến thứ 6, validator sẽ giới hạn từ 1 đến 5.

## 3. Quan hệ giữa các bảng

Sơ đồ quan hệ logic:

```
AcademicYear
    |
    | 1 - N
    v
Semester
    |
    +--------------------+
    |                    |
    v                    v
ClassSubject          SchoolClass
    |                    |
    | 1 - 1              | 1 - N
    v                    v
TeachingAssignment   Timetable
    |                    |
    | N - 1              | 1 - N
    v                    v
Employee            TimetableEntry
                         |
            +------------+-------------+
            |            |             |
            v            v             v
      TimeSlot      Classroom    TeachingAssignment
```

Một số quan hệ bổ sung:

```
TeacherSubjectQualification
    N
    |
    +---- Employee
    |
    +---- Subject
```

Lưu ý về lịch sử phân công: Nếu cần lưu lịch sử giáo viên từng phụ trách môn học, không nên xóa assignment cũ. Có thể bổ sung:

`teaching_assignment_history`

| Field                  | Ý nghĩa             |
| ---------------------- | ------------------- |
| teaching_assignment_id | Phân công liên quan |
| old_teacher_id         | Giáo viên cũ        |
| new_teacher_id         | Giáo viên mới       |
| changed_by             | Người thay đổi      |
| changed_at             | Thời điểm thay đổi  |
| reason                 | Lý do               |

## 4. Business Rules

Đây là phần quan trọng hơn việc tạo các bảng. Database chỉ lưu được cấu trúc; Service phải đảm bảo các quy tắc nghiệp vụ.

### 4.1. Quy tắc Academic Structure

| ID    | Business Rule                                                                 |
| ----- | ----------------------------------------------------------------------------- |
| BR-01 | Ngày kết thúc năm học phải lớn hơn ngày bắt đầu.                              |
| BR-02 | Học kỳ phải nằm trong khoảng thời gian của năm học.                           |
| BR-03 | Các học kỳ trong cùng năm học không được chồng lấn nếu trường không cho phép. |
| BR-04 | Lớp học phải thuộc một năm học.                                               |
| BR-05 | Số tiết mỗi môn trong lớp phải lớn hơn 0.                                     |
| BR-06 | Một lớp không được khai báo trùng môn trong cùng học kỳ.                      |

### 4.2. Quy tắc Teacher Allocation

| ID    | Business Rule                                                                  |
| ----- | ------------------------------------------------------------------------------ |
| BR-07 | Giáo viên phải tồn tại trong Employee.                                         |
| BR-08 | Giáo viên phải có trạng thái làm việc hợp lệ.                                  |
| BR-09 | Giáo viên phải có chuyên môn phù hợp với môn được phân công.                   |
| BR-10 | Mỗi ClassSubject chỉ có một TeachingAssignment đang hoạt động.                 |
| BR-11 | Một giáo viên có thể phụ trách nhiều lớp.                                      |
| BR-12 | Tổng số tiết được phân công không được vượt giới hạn giảng dạy của giáo viên.  |
| BR-13 | Không được phân công vào học kỳ đã kết thúc hoặc không còn cho phép chỉnh sửa. |

Ví dụ kiểm tra tải giảng dạy:

```
Teacher A:
- 10A1 - Math: 5 periods/week
- 10A2 - Math: 5 periods/week
- 10A3 - Math: 5 periods/week

Total = 15 periods/week
Max workload = 18 periods/week

Result: VALID
```

Nên xây `TeacherWorkloadService` để tổng hợp tải giảng dạy từ các phân công đang hoạt động.

### 4.3. Quy tắc Timetable

| ID    | Business Rule                                                                     |
| ----- | --------------------------------------------------------------------------------- |
| BR-14 | Một lớp không được có hai môn cùng một ngày và tiết học.                          |
| BR-15 | Một giáo viên không được dạy hai lớp cùng một thời điểm.                          |
| BR-16 | Một phòng học không được được sử dụng bởi hai lớp cùng thời điểm.                 |
| BR-17 | Một tiết học phải tham chiếu đến TeachingAssignment hợp lệ.                       |
| BR-18 | Tổng số tiết đã xếp cho môn phải bằng số tiết yêu cầu mỗi tuần trước khi công bố. |
| BR-19 | Chỉ được xếp lịch vào những ngày và khung giờ trường cho phép.                    |
| BR-20 | Giáo viên không được xếp lịch vào thời gian đã đăng ký không thể giảng dạy.       |
| BR-21 | Sức chứa phòng phải phù hợp với sĩ số lớp nếu trường áp dụng kiểm tra sức chứa.   |
| BR-22 | Không được sửa trực tiếp thời khóa biểu đã công bố.                               |

## 5. Kiểm tra trùng lịch

Đây là ba điều kiện bắt buộc.

### 5.1. Trùng lớp

Một lớp chỉ có thể học một môn tại một thời điểm.

```
SELECT COUNT(*)
FROM timetable_entry te
JOIN timetable t ON t.id = te.timetable_id
WHERE t.id = :timetableId
  AND te.day_of_week = :day
  AND te.time_slot_id = :timeSlotId;
```

Nếu kết quả lớn hơn 0 thì lớp đã có tiết học tại vị trí đó.

### 5.2. Trùng giáo viên

```
SELECT COUNT(*)
FROM timetable_entry te
JOIN timetable t ON t.id = te.timetable_id
JOIN teaching_assignment ta
    ON ta.id = te.teaching_assignment_id
WHERE t.semester_id = :semesterId
  AND t.status IN ('DRAFT', 'REVIEW', 'PUBLISHED')
  AND ta.teacher_id = :teacherId
  AND te.day_of_week = :day
  AND te.time_slot_id = :timeSlotId;
```

Nếu kết quả lớn hơn 0 thì giáo viên đã có lịch dạy tại thời điểm đó.

### 5.3. Trùng phòng

```
SELECT COUNT(*)
FROM timetable_entry te
JOIN timetable t ON t.id = te.timetable_id
WHERE t.semester_id = :semesterId
  AND t.status IN ('DRAFT', 'REVIEW', 'PUBLISHED')
  AND te.classroom_id = :classroomId
  AND te.day_of_week = :day
  AND te.time_slot_id = :timeSlotId;
```

Nếu kết quả lớn hơn 0 thì phòng đã được sử dụng.

Lưu ý quan trọng: Ba câu query trên là logic minh họa. Khi triển khai thực tế, cần loại trừ chính bản ghi đang cập nhật, và chỉ kiểm tra các thời khóa biểu có hiệu lực hoặc đang được giữ chỗ. Đặc biệt, không nên kiểm tra riêng từng query rồi insert mà không kiểm soát đồng thời, vì hai request có thể cùng vượt qua kiểm tra.

## 6. State Machine cho Timetable

Tôi đề xuất vòng đời:

```
DRAFT
  |
  v
VALIDATING
  |
  +---- Validation Failed ----> DRAFT
  |
  v
REVIEW
  |
  +---- Rejected -------------> DRAFT
  |
  v
PUBLISHED
  |
  v
ARCHIVED
```

Enum:

```
public enum TimetableStatus {
    DRAFT,
    VALIDATING,
    REVIEW,
    PUBLISHED,
    ARCHIVED
}
```

Quy tắc:

- `DRAFT`: Được thêm, sửa, xóa các tiết học.
- `VALIDATING`: Hệ thống đang kiểm tra tính hợp lệ.
- `REVIEW`: Đã vượt qua validation, chờ người có quyền duyệt.
- `PUBLISHED`: Đã công bố, không được sửa trực tiếp.
- `ARCHIVED`: Phiên bản cũ, không còn được sử dụng.

Khi cần thay đổi thời khóa biểu đã công bố:

1. Tạo phiên bản mới.
2. Sao chép các tiết học từ phiên bản hiện tại.
3. Thực hiện điều chỉnh trên phiên bản mới.
4. Validate và phê duyệt.
5. Công bố phiên bản mới.
6. Chuyển phiên bản cũ sang `ARCHIVED`.

Ví dụ:

```
Timetable 10A1 - Semester 1

Version 1: PUBLISHED
Version 2: DRAFT

Sau khi Version 2 được công bố:

Version 1: ARCHIVED
Version 2: PUBLISHED
```

Nên có ràng buộc chỉ một phiên bản `PUBLISHED` cho mỗi lớp và học kỳ. Có thể sử dụng partial unique index nếu database hỗ trợ, hoặc kiểm soát trong transaction kết hợp locking.

## 7. Thiết kế Service trong Spring Boot

Tôi đề xuất phân chia như sau:

```
modules/
├── academic/
│   ├── academic-year/
│   ├── semester/
│   ├── school-class/
│   ├── subject/
│   └── classroom/
│
├── teaching/
│   ├── qualification/
│   ├── class-subject/
│   ├── assignment/
│   └── workload/
│
└── scheduling/
    ├── timetable/
    ├── timetable-entry/
    ├── validator/
    └── publication/
```

Các Service:

| Service                       | Trách nhiệm                     |
| ----------------------------- | ------------------------------- |
| `AcademicYearService`         | Quản lý năm học                 |
| `SemesterService`             | Quản lý học kỳ                  |
| `ClassSubjectService`         | Quản lý môn học của từng lớp    |
| `TeachingAssignmentService`   | Phân công giáo viên             |
| `TeacherWorkloadService`      | Tính tổng số tiết giảng dạy     |
| `TimetableService`            | Quản lý thời khóa biểu          |
| `TimetableEntryService`       | Thêm, sửa, xóa tiết học         |
| `TimetableValidator`          | Kiểm tra các điều kiện xếp lịch |
| `TimetablePublicationService` | Phê duyệt và công bố            |

Ví dụ validator:

```
public interface TimetableValidator {
    ValidationResult validate(Long timetableId);
}
```

Có thể chia thành các validator nhỏ:

```
public interface ScheduleConstraint {
    List<ValidationError> validate(Timetable timetable);
}
```

Các implementation:

```
ClassConflictConstraint
TeacherConflictConstraint
ClassroomConflictConstraint
SubjectPeriodConstraint
TeacherQualificationConstraint
TeacherWorkloadConstraint
```

Cách này giúp thêm các quy tắc mới mà không phải sửa một class validator quá lớn.

## 8. Các vấn đề cần xử lý khi triển khai thực tế

### 8.1. Không cho phép hai request cùng xếp một giáo viên

Ví dụ:

- Admin A xếp giáo viên X vào lớp 10A1, thứ 2 tiết 1.
- Admin B đồng thời xếp giáo viên X vào lớp 10A2, thứ 2 tiết 1.

Cả hai request đều kiểm tra thấy chưa trùng lịch và cùng insert.

Đây là race condition.

Giải pháp:

- Transaction.
- Pessimistic locking hoặc cơ chế serialize thao tác theo học kỳ.
- Kiểm tra lại xung đột ngay trước khi ghi dữ liệu.
- Sử dụng unique constraints phù hợp ở database.

Không nên chỉ dựa vào frontend để ngăn trùng lịch.

### 8.2. Giáo viên nghỉ việc hoặc thay đổi hợp đồng

Nếu giáo viên nghỉ việc giữa học kỳ:

1. HRM cập nhật trạng thái Employee.
2. Hệ thống xác định các TeachingAssignment bị ảnh hưởng.
3. Thông báo cho quản trị viên.
4. Phân công giáo viên thay thế.
5. Tạo phiên bản thời khóa biểu mới.
6. Công bố lại.

Không nên tự động xóa các tiết học cũ vì cần giữ lịch sử.

### 8.3. Thay đổi số tiết môn học

Ví dụ:

- Ban đầu Toán 10A1 có 5 tiết/tuần.
- Sau đó nhà trường điều chỉnh còn 4 tiết/tuần.

Cần cập nhật `ClassSubject`, sau đó kiểm tra và điều chỉnh `TimetableEntry`. Không được để thời khóa biểu công bố vẫn có 5 tiết nhưng chương trình học chỉ yêu cầu 4 tiết.

# 9. Giao diện UI nên thiết kế như thế nào?

Tôi đề xuất ba màn hình chính.

Màn hình 1 — Teacher Allocation

Phân công giáo viên cho các lớp và môn học

| Lớp  | Môn  | Giáo viên    | Tiết/tuần |
| ---- | ---- | ------------ | --------- |
| 10A1 | Toán | Nguyễn Văn A | 5         |
| 10A1 | Văn  | Trần Thị B   | 4         |
| 10A2 | Toán | Nguyễn Văn A | 5         |
| 10A2 | Văn  | Trần Thị B   | 4         |

Đã phân công

Màn hình 2 — Timetable Editor

Giao diện thời khóa biểu dạng lưới

| Tiết | Thứ 2 | Thứ 3 | Thứ 4 |
| ---- | ----- | ----- | ----- |
| 1    | Toán  | Văn   | Anh   |
| 2    | Văn   | Toán  | Toán  |
| 3    | Anh   | Toán  | Văn   |
| 4    | Toán  | Anh   | Toán  |
| 5    | Văn   | Văn   | Anh   |

Các ô có thể kéo thả để thay đổi vị trí. Backend phải validate lại sau mỗi thao tác lưu.

Màn hình 3 — Teacher Workload

Theo dõi tải giảng dạy của giáo viên

| Giáo viên    | Số tiết/tuần | Giới hạn |
| ------------ | ------------ | -------- |
| Nguyễn Văn A | 15           | 18       |
| Trần Thị B   | 20           | 18       |
| Lê Văn C     | 12           | 18       |

Vượt tải: Trần Thị B

# 10. Thứ tự triển khai database và backend

Tôi khuyên bạn triển khai theo thứ tự này:

| Thứ tự | Công việc                            | Ưu tiên                 |
| ------ | ------------------------------------ | ----------------------- |
| 1      | AcademicYear, Semester               | Cao                     |
| 2      | Subject, SchoolClass, Classroom      | Cao                     |
| 3      | TimeSlot                             | Cao                     |
| 4      | ClassSubject                         | Cao                     |
| 5      | TeacherSubjectQualification          | Trung bình              |
| 6      | TeachingAssignment                   | Cao                     |
| 7      | TeacherWorkloadService               | Trung bình              |
| 8      | Timetable, TimetableEntry            | Cao                     |
| 9      | TimetableValidator                   | Rất cao                 |
| 10     | Timetable State Machine, Publication | Cao                     |
| 11     | UI kéo thả                           | Sau khi backend ổn định |
| 12     | Automatic Scheduling                 | Để giai đoạn sau        |

Một số quyết định tôi khuyên chốt ngay:

1. Dùng `Employee` làm giáo viên, không tạo entity `Teacher` riêng.
2. `ClassSubject` quản lý số tiết yêu cầu mỗi tuần.
3. `TeachingAssignment` quản lý giáo viên được phân công.
4. `TimetableEntry` quản lý từng tiết học cụ thể.
5. Không cho phép sửa trực tiếp thời khóa biểu đã công bố.
6. Dùng `TimetableValidator` để kiểm soát xung đột tập trung.
7. Lưu lịch sử phân công và phiên bản thời khóa biểu.

Thiết kế này đủ để triển khai xếp thời khóa biểu thủ công trước, sau đó mở rộng sang tự động xếp lịch mà không phải thay đổi toàn bộ domain model.
