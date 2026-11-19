# Scheduling Module – Implementation & Fix Plan

## 1. Mục tiêu

Rà soát và hoàn thiện phân hệ phân bổ thời khóa biểu (`Scheduling`), bảo đảm:

- Không tạo được tiết học sai lớp, sai môn hoặc sai giáo viên.
- Số tiết được phân bổ đúng với chương trình học.
- Tải giảng dạy được tính chính xác theo học kỳ.
- Giáo viên dạy thay đáp ứng điều kiện nghiệp vụ.
- Phòng học và khung giờ đáp ứng các ràng buộc.
- Thời khóa biểu chỉ được công bố khi vượt qua toàn bộ kiểm tra.

**Nguyên tắc thực hiện:**

1. Không viết lại toàn bộ module Scheduling.
2. Tái sử dụng kiến trúc và các constraint hiện có.
3. Không phá vỡ API và dữ liệu hiện tại nếu không cần thiết.
4. Không tự ý thay đổi nghiệp vụ ngoài phạm vi yêu cầu.
5. Mỗi thay đổi phải có unit test hoặc integration test tương ứng.
6. Không đánh dấu hoàn thành nếu chưa chạy test xác nhận.

---

## 2. Phạm vi xử lý

| ID | Công việc | Mức độ |
|---|---|---|
| SCH-01 | Kiểm tra TeachingAssignment thuộc đúng lớp | Critical |
| SCH-02 | Kiểm tra số tiết môn học thừa/thiếu | Critical |
| SCH-03 | Sửa tính tải giảng dạy theo học kỳ | Critical |
| SCH-04 | Kiểm tra giáo viên dạy thay | High |
| SCH-05 | Kiểm tra điều kiện phòng học | High |
| SCH-06 | Kiểm tra khung giờ chồng lấn | High |
| SCH-07 | Bổ sung kiểm thử nghiệp vụ | Critical |
| SCH-08 | Đánh giá khả năng tự động xếp thời khóa biểu | Future |

---

# 3. Chi tiết triển khai

## SCH-01 – Kiểm tra TeachingAssignment thuộc đúng lớp

### Vấn đề

Một `Timetable` được tạo cho lớp A nhưng có thể nhận `TeachingAssignment` thuộc lớp B.

Điều này dẫn đến dữ liệu thời khóa biểu không nhất quán.

### File cần kiểm tra

- `module/scheduling/service/implement/TimetableEntryServiceImpl.java`
- `module/scheduling/entity/Timetable.java`
- `module/teaching/entity/TeachingAssignment.java`

### Yêu cầu xử lý

Khi tạo hoặc cập nhật `TimetableEntry`, phải xác nhận:

1. `Timetable` tồn tại.
2. `TeachingAssignment` tồn tại.
3. `TeachingAssignment.schoolClass` tồn tại.
4. `TeachingAssignment.schoolClass.id` bằng `Timetable.schoolClass.id`.
5. Giáo viên được phân công có trạng thái làm việc hợp lệ.
6. Phân công giảng dạy đang ở trạng thái `ACTIVE`.
7. Môn học của assignment phù hợp với chương trình học của lớp trong học kỳ.

Không chỉ kiểm tra trong `create()`. Phải áp dụng tương tự trong `update()`.

### Ví dụ logic

```java
if (!assignment.getSchoolClass().getId()
        .equals(timetable.getSchoolClass().getId())) {
    throw new BusinessException(
        "Phân công giảng dạy không thuộc lớp của thời khóa biểu"
    );
}
```

Đây chỉ là ví dụ logic. Cần kiểm tra null và các quan hệ entity trước khi triển khai.

### Test cases

- [ ] Tạo entry với assignment đúng lớp.
- [ ] Tạo entry với assignment khác lớp.
- [ ] Update entry sang assignment khác lớp.
- [ ] Assignment không tồn tại.
- [ ] Timetable không tồn tại.
- [ ] Assignment đã ngừng hoạt động.
- [ ] Giáo viên không còn trạng thái làm việc hợp lệ.

### Acceptance Criteria

- Không thể đưa assignment của lớp khác vào timetable.
- Không thể sử dụng assignment không hợp lệ.
- API trả về lỗi nghiệp vụ rõ ràng.
- Dữ liệu không hợp lệ không được lưu vào database.

---

## SCH-02 – Kiểm tra số tiết môn học thừa và thiếu

### Vấn đề

`SubjectPeriodConstraint` cần bảo đảm số tiết được phân bổ đúng với `ClassSubject.periodsPerWeek`.

Không chỉ kiểm tra thiếu tiết mà phải kiểm tra cả tiết thừa và môn không thuộc chương trình.

### File cần kiểm tra

- `module/scheduling/constraint/implement/SubjectPeriodConstraint.java`
- `module/academic/entity/ClassSubject.java`
- `module/scheduling/entity/TimetableEntry.java`
- `module/scheduling/repository/TimetableEntryRepository.java`

### Yêu cầu xử lý

Với mỗi môn học trong chương trình lớp:

```text
actualPeriods == requiredPeriods
```

Trong đó:

- `actualPeriods`: số tiết môn học đã được xếp.
- `requiredPeriods`: số tiết yêu cầu mỗi tuần trong `ClassSubject`.

Phải xử lý các trường hợp:

1. Số tiết thực tế nhỏ hơn số tiết yêu cầu.
2. Số tiết thực tế lớn hơn số tiết yêu cầu.
3. Môn học xuất hiện trong timetable nhưng không có trong ClassSubject.
4. Assignment thuộc môn học không nằm trong chương trình lớp.
5. Assignment thuộc sai học kỳ hoặc năm học.

### Quy tắc

Không được chỉ duyệt các `ClassSubject` rồi bỏ qua những entry không thuộc chương trình.

Nên kiểm tra theo hai chiều:

**Chiều 1 – Chương trình học:**

Mỗi môn trong `ClassSubject` phải được phân bổ đủ số tiết.

**Chiều 2 – Thời khóa biểu:**

Mỗi entry phải thuộc một môn hợp lệ trong chương trình học của lớp.

### Test cases

- [ ] Môn được xếp đủ tiết.
- [ ] Môn bị thiếu tiết.
- [ ] Môn bị thừa tiết.
- [ ] Entry sử dụng môn không thuộc chương trình.
- [ ] Assignment thuộc sai học kỳ.
- [ ] Lớp chưa có cấu hình ClassSubject.
- [ ] Timetable không có entry nào.

### Acceptance Criteria

- Phát hiện đầy đủ tiết thừa và thiếu.
- Không chấp nhận môn học ngoài chương trình.
- Validation trả về lỗi có tên môn học và số tiết chênh lệch.

---

## SCH-03 – Sửa tính tải giảng dạy theo học kỳ

### Vấn đề

`TeacherWorkloadServiceImpl` có nguy cơ cộng cả assignment không thuộc học kỳ đang tính.

Không được lấy tất cả assignment ACTIVE rồi mặc định cộng `weeklyContactHours` nếu không tìm thấy ClassSubject tương ứng.

### File cần kiểm tra

- `module/teaching/service/implement/TeacherWorkloadServiceImpl.java`
- `module/teaching/entity/TeachingAssignment.java`
- `module/academic/entity/ClassSubject.java`
- `module/academic/entity/Semester.java`

### Yêu cầu xử lý

1. Xác định học kỳ đang tính.
2. Lấy các assignment có hiệu lực trong phạm vi học kỳ đó.
3. Kiểm tra assignment thuộc đúng lớp và môn học.
4. Lấy số tiết yêu cầu từ `ClassSubject` tương ứng.
5. Chỉ tính những assignment hợp lệ.
6. Không tự động cộng `weeklyContactHours` khi không tìm thấy cấu hình chương trình học.
7. Trả về lỗi hoặc cảnh báo dữ liệu nếu assignment thiếu thông tin cần thiết.

### Quy tắc tính

```text
Teacher Workload
    = Tổng số tiết/tuần của các assignment hợp lệ
      trong học kỳ đang xét
```

Cần xác định rõ nguồn dữ liệu chuẩn để tránh trường hợp `weeklyContactHours` và `ClassSubject.periodsPerWeek` mâu thuẫn.

### Lưu ý thiết kế

Hiện tại `TeachingAssignment` chưa có quan hệ trực tiếp với `Semester`.

Cần xem xét mô hình năm học/học kỳ hiện tại trước khi quyết định:

- Bổ sung quan hệ học kỳ.
- Hoặc xác định phạm vi hiệu lực thông qua năm học và `ClassSubject`.

Không tự ý thêm quan hệ nếu mô hình hiện tại đã có cách xác định phạm vi tương đương.

### Test cases

- [ ] Giáo viên có assignment trong học kỳ hiện tại.
- [ ] Giáo viên có assignment ở học kỳ khác.
- [ ] Giáo viên có assignment đã ngừng hoạt động.
- [ ] Assignment không có ClassSubject tương ứng.
- [ ] Một giáo viên dạy nhiều lớp.
- [ ] Một giáo viên dạy nhiều môn.
- [ ] Giáo viên không có assignment.

### Acceptance Criteria

- Chỉ tính workload trong phạm vi học kỳ yêu cầu.
- Không cộng assignment ngoài phạm vi.
- Không âm thầm bỏ qua dữ liệu không hợp lệ.
- Kết quả tính tải có thể đối chiếu với các assignment nguồn.

---

## SCH-04 – Kiểm tra giáo viên dạy thay

### Vấn đề

Giáo viên dạy thay hiện được nhận theo ID nhưng cần kiểm tra thêm điều kiện nghiệp vụ.

### File cần kiểm tra

- `module/scheduling/service/implement/TimetableEntryServiceImpl.java`
- `module/scheduling/entity/TimetableEntry.java`
- `module/employee/entity/Employee.java`
- `module/teaching/entity/TeacherSubjectQualification.java`

### Yêu cầu xử lý

Khi thêm giáo viên dạy thay:

1. Giáo viên phải tồn tại.
2. Giáo viên phải có trạng thái làm việc hợp lệ.
3. Giáo viên phải có chuyên môn phù hợp với môn học.
4. Giáo viên thay thế không được trùng lịch.
5. Không được trùng với giáo viên chính nếu nghiệp vụ không cho phép.
6. Việc thay thế phải được ghi nhận rõ ràng.

Nếu nghiệp vụ cho phép ngoại lệ chuyên môn, cần có cơ chế phê duyệt thay vì bỏ qua validation.

### Test cases

- [ ] Giáo viên thay thế hợp lệ.
- [ ] Giáo viên không tồn tại.
- [ ] Giáo viên đã nghỉ việc.
- [ ] Giáo viên không có chuyên môn phù hợp.
- [ ] Giáo viên thay thế bị trùng lịch.
- [ ] Giáo viên thay thế trùng với giáo viên chính.

### Acceptance Criteria

Không thể thêm giáo viên thay thế không hợp lệ hoặc bị trùng lịch.

---

## SCH-05 – Kiểm tra điều kiện phòng học

### Vấn đề

Kiểm tra trùng phòng chưa đủ để bảo đảm phòng phù hợp với lớp và môn học.

### File cần kiểm tra

- `module/scheduling/constraint/implement/ClassroomConflictConstraint.java`
- Entity `Classroom`.
- Entity `Subject`.
- `module/scheduling/entity/TimetableEntry.java`

### Yêu cầu xử lý

Đánh giá và bổ sung các quy tắc:

1. Phòng phải tồn tại.
2. Phòng phải đang hoạt động.
3. Sức chứa phòng phải đáp ứng sĩ số lớp.
4. Phòng phải đáp ứng yêu cầu loại phòng của môn học.
5. Phòng không được trùng lịch.
6. Phòng phải được phép sử dụng trong khung giờ đó nếu có quy định.

### Thiết kế đề xuất

Có thể bổ sung loại phòng hoặc khả năng sử dụng:

```text
Classroom
    - capacity
    - status
    - classroomType
```

Ví dụ loại phòng:

```text
STANDARD
COMPUTER_LAB
SCIENCE_LAB
LIBRARY
MULTI_PURPOSE
```

Không bắt buộc sử dụng đúng các enum trên. Cần đối chiếu với mô hình Classroom hiện tại.

### Test cases

- [ ] Phòng đủ sức chứa.
- [ ] Phòng không đủ sức chứa.
- [ ] Phòng đã ngừng hoạt động.
- [ ] Phòng không phù hợp với môn học.
- [ ] Phòng bị trùng lịch.
- [ ] Không chỉ định phòng nếu nghiệp vụ cho phép.

### Acceptance Criteria

Không thể xếp lịch vào phòng không đáp ứng điều kiện bắt buộc.

---

## SCH-06 – Kiểm tra khung giờ chồng lấn

### Vấn đề

`TimeSlotServiceImpl` hiện kiểm tra giờ kết thúc sau giờ bắt đầu và trùng số thứ tự tiết trong cùng buổi.

Tuy nhiên, hai tiết khác số thứ tự vẫn có thể chồng lấn thời gian.

### File cần sửa

- `module/scheduling/service/implement/TimeSlotServiceImpl.java`
- `module/scheduling/entity/TimeSlot.java`
- `module/scheduling/repository/TimeSlotRepository.java`

### Quy tắc kiểm tra

Hai khoảng thời gian không được chồng lấn.

Công thức:

```text
newStart < existingEnd
AND
newEnd > existingStart
```

Nếu biểu thức trên đúng thì hai khoảng thời gian bị chồng lấn.

Ví dụ:

```text
Tiết 1: 07:00 - 07:45
Tiết 2: 07:30 - 08:15
```

Kết quả: Không hợp lệ.

Ví dụ hợp lệ:

```text
Tiết 1: 07:00 - 07:45
Tiết 2: 07:45 - 08:30
```

Hai tiết tiếp nối nhau, không chồng lấn.

### Yêu cầu bổ sung

- Kiểm tra khi tạo TimeSlot.
- Kiểm tra khi cập nhật TimeSlot.
- Không cho phép thời gian bắt đầu hoặc kết thúc null nếu nghiệp vụ yêu cầu bắt buộc.
- Xem xét việc thay đổi TimeSlot đã được sử dụng trong timetable đã công bố.

### Test cases

- [ ] Hai tiết không chồng lấn.
- [ ] Hai tiết chồng lấn một phần.
- [ ] Hai tiết trùng hoàn toàn.
- [ ] Hai tiết tiếp nối nhau.
- [ ] Update gây chồng lấn.
- [ ] Update không gây chồng lấn.
- [ ] TimeSlot đang được sử dụng trong timetable PUBLISHED.

### Acceptance Criteria

Không thể tạo hoặc cập nhật TimeSlot gây chồng lấn thời gian.

---

# 4. Kiểm thử tổng thể

Sau khi hoàn thành SCH-01 đến SCH-06, cần có kiểm thử tổng thể cho quy trình:

```text
Create Timetable
       |
       v
Create Teaching Assignment
       |
       v
Create Timetable Entries
       |
       v
Validate Timetable
       |
       v
Check Hard Constraints
       |
       +------ Invalid ------> Return to DRAFT
       |
       v
      REVIEW
       |
       v
    PUBLISHED
       |
       v
    ARCHIVED
```

### Các tình huống tích hợp

- [ ] Một lớp không có hai tiết trùng nhau.
- [ ] Một giáo viên không dạy hai lớp cùng thời điểm.
- [ ] Một phòng không phục vụ hai lớp cùng thời điểm.
- [ ] Số tiết từng môn đáp ứng chương trình học.
- [ ] Không có môn học ngoài chương trình.
- [ ] Không có assignment sai lớp.
- [ ] Không có assignment không hoạt động.
- [ ] Giáo viên dạy thay không bị trùng lịch.
- [ ] Khung giờ không chồng lấn.
- [ ] Không thể publish timetable không hợp lệ.
- [ ] Tạo version mới không làm thay đổi version cũ.
- [ ] Version PUBLISHED cũ được chuyển sang ARCHIVED khi publish version mới.

---

# 5. SCH-08 – Định hướng tự động xếp thời khóa biểu

Đây là giai đoạn mở rộng, không triển khai trước khi SCH-01 đến SCH-07 hoàn thành.

Hiện tại hệ thống chủ yếu hỗ trợ xếp lịch thủ công và validation. Có thể phát triển thêm chức năng tự động xếp lịch.

### 5.1. Hard Constraints

Các điều kiện bắt buộc:

- Một lớp không thể học hai môn cùng thời điểm.
- Một giáo viên không thể dạy hai lớp cùng thời điểm.
- Một phòng không thể phục vụ hai lớp cùng thời điểm.
- Số tiết phải đúng chương trình.
- Giáo viên phải đủ điều kiện giảng dạy.
- Phòng phải đáp ứng yêu cầu môn học.

### 5.2. Soft Constraints

Các điều kiện tối ưu:

- Hạn chế tiết trống của giáo viên.
- Hạn chế tiết trống của học sinh.
- Không dồn quá nhiều tiết cùng môn trong một ngày.
- Phân bổ các môn hợp lý trong tuần.
- Hạn chế tiết học quá sớm hoặc quá muộn.
- Cân bằng tải giảng dạy giữa các ngày.
- Tôn trọng thời gian giáo viên đăng ký không thể giảng dạy.

### 5.3. Kiến trúc đề xuất

```text
TimetableGenerationService
        |
        v
TimetableProblemBuilder
        |
        v
SchedulingSolver
        |
        v
Constraint Evaluation
        |
        v
Optimization
        |
        v
Generated Timetable
        |
        v
TimetableValidator
        |
        v
DRAFT Timetable
```

Có thể nghiên cứu các thuật toán:

- Backtracking.
- Constraint Satisfaction Problem (CSP).
- Local Search.
- Simulated Annealing.
- Google OR-Tools CP-SAT.

Không nên tự viết thuật toán tối ưu phức tạp trước khi có dữ liệu và bộ constraint ổn định.

---

# 6. Quy tắc triển khai cho AI Coding Agent

Khi thực hiện tài liệu này, phải tuân thủ:

1. Đọc toàn bộ file liên quan trước khi sửa.
2. Kiểm tra entity, repository, service, DTO và enum hiện tại.
3. Không đoán tên field hoặc method.
4. Không tạo thêm entity nếu có thể tận dụng mô hình hiện tại.
5. Không lặp lại business validation ở nhiều nơi.
6. Đặt logic nghiệp vụ trong service hoặc constraint phù hợp, không đưa vào controller.
7. Sử dụng `BusinessException` hoặc exception hiện có.
8. Sử dụng transaction phù hợp cho các thao tác tạo/cập nhật nhiều entity.
9. Không xóa dữ liệu lịch sử hoặc thay đổi timetable đã công bố ngoài quy trình versioning.
10. Viết test trước hoặc cùng lúc với implementation.
11. Chạy test sau mỗi task.
12. Không sửa các module ngoài phạm vi nếu chưa chứng minh được sự cần thiết.

Nếu phát hiện thiết kế hiện tại không đủ dữ liệu để thực hiện một yêu cầu, hãy báo cáo vấn đề và đề xuất thay đổi schema trước khi triển khai.

---

# 7. Thứ tự thực hiện

Thực hiện tuần tự, không làm tất cả trong một lần:

- [ ] SCH-01 – Assignment đúng lớp.
- [ ] SCH-02 – Kiểm tra số tiết môn học.
- [ ] SCH-03 – Tính workload theo học kỳ.
- [ ] SCH-04 – Kiểm tra giáo viên dạy thay.
- [ ] SCH-05 – Kiểm tra phòng học.
- [ ] SCH-06 – Kiểm tra khung giờ.
- [ ] SCH-07 – Kiểm thử tổng thể.
- [ ] SCH-08 – Nghiên cứu tự động xếp lịch.

## 8. Báo cáo sau mỗi task

Sau mỗi task, AI Coding Agent phải báo cáo:

```text
TASK:
SCH-XX

FILES MODIFIED:
- ...

PROBLEMS FOUND:
- ...

IMPLEMENTATION:
- ...

TESTS:
- Passed:
- Failed:
- Not implemented:

REMAINING ISSUES:
- ...

STATUS:
DONE / PARTIAL / BLOCKED
```

Chỉ chuyển sang task tiếp theo khi task hiện tại đã có implementation và test phù hợp.

## 9. Definition of Done

Phân hệ Scheduling được xem là hoàn thành giai đoạn xử lý khi:

- [ ] SCH-01 đến SCH-07 hoàn thành.
- [ ] Không còn lỗi toàn vẹn dữ liệu đã xác định.
- [ ] Các constraint đều có test.
- [ ] Các luồng create/update/validate/publish hoạt động đúng.
- [ ] Không làm hỏng các chức năng Teaching và Academic.
- [ ] Các test hiện tại không bị regression.
- [ ] Có báo cáo test thực tế.
- [ ] Không còn lỗi nghiệp vụ nghiêm trọng chưa được giải quyết.
