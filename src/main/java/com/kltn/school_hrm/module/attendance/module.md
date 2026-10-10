
Toàn bộ kiến trúc của module **Attendance (Chấm công)** được thiết kế theo mô hình phân tách trách nhiệm (Separation of Concerns). Dưới đây là vai trò chi tiết của từng class trong module:

---

### 1. Nhóm Entity (Mô hình dữ liệu - Database)

| Class | Vai trò | Mô tả chi tiết |
| :--- | :--- | :--- |
| [AttendanceRawLog](/src/main/java/com/kltn/school_hrm/module/attendance/entity/AttendanceRawLog.java) | **Nhật ký quẹt thẻ thô (Immutable Log)** | Lưu từng lần quẹt thẻ độc lập (`eventTime`, `eventType: IN/OUT`, `source: WEB/FINGERPRINT/RFID`, `deviceId`). Dữ liệu này mang tính bất biến (append-only), không chỉnh sửa để làm bằng chứng kiểm toán. |
| [Shift](/src/main/java/com/kltn/school_hrm/module/attendance/entity/Shift.java) | **Định nghĩa Ca làm việc** | Chứa cấu hình quy định ca: giờ bắt đầu (`startTime`), giờ kết thúc (`endTime`), số phút ân hạn cho phép trễ (`graceMinutes`), thời gian nghỉ trưa (`breakMinutes`), và cờ đánh dấu ca qua đêm (`overNight`). |
| [EmployeeShiftAssignment](/src/main/java/com/kltn/school_hrm/module/attendance/entity/EmployeeShiftAssignment.java) | **Phân ca cho Nhân viên** | Xác định nhân viên nào làm việc ở ca (`Shift`) nào, vào thứ mấy trong tuần (`DayOfWeek`), và có hiệu lực từ ngày nào đến ngày nào (`effectiveFrom` → `effectiveTo`). |
| [AttendanceSession](/src/main/java/com/kltn/school_hrm/module/attendance/entity/AttendanceSession.java) | **Phiên làm việc cụ thể trong ngày** | Một ngày nhân viên có thể ra vào nhiều lần (vd: sáng làm, trưa quẹt ra nghỉ, chiều quẹt vào lại). Mỗi khoảng `checkIn` → `checkOut` này là một `Session` kèm số phút làm việc `workedMinutes`. |
| [AttendanceRecord](/src/main/java/com/kltn/school_hrm/module/attendance/entity/AttendanceRecord.java) | **Bản ghi chấm công tổng hợp theo ngày** | Kết quả tổng hợp cuối cùng của nhân viên trong một ngày (`workDate`): chứa danh sách các `AttendanceSession`, tổng số phút làm việc (`workedMinutes`), phút đi trễ (`lateMinutes`), phút về sớm (`earlyLeaveMinutes`), và trạng thái ngày công (`PRESENT`, `LATE`, `EARLY_LEAVE`, `INCOMPLETE`, `ABSENT`). |
| [Attendance](/src/main/java/com/kltn/school_hrm/module/attendance/entity/Attendance.java) | **Entity Legacy (Cũ)** | Bảng chấm công đơn giản kiểu cũ (chỉ có 1 lần check-in, 1 lần check-out). Được giữ lại phục vụ backward compatibility. |

---

### 2. Nhóm Service (Xử lý nghiệp vụ)

| Class / Interface | Vai trò | Mô tả chi tiết |
| :--- | :--- | :--- |
| [AttendanceRawLogService](/src/main/java/com/kltn/school_hrm/module/attendance/service/AttendanceRawLogService.java) | **Tiếp nhận & Quản lý log quẹt thẻ** | Ghi nhận sự kiện chấm công mới từ Web, Mobile, máy vân tay vào bảng `AttendanceRawLog`. Chống trùng lặp dữ liệu (`eventId`). |
| [AttendanceAggregationService](/src/main/java/com/kltn/school_hrm/module/attendance/service/AttendanceAggregationService.java) | **Xử lý gom nhóm & Khử trùng lặp log** | **Bộ não ghép cặp:** Gom tất cả raw logs trong ngày của nhân viên, loại bỏ log dư thừa (quẹt IN/OUT nhiều lần liên tiếp), ghép thành các `AttendanceSession` hoàn chỉnh, gắn vào `AttendanceRecord` rồi gọi calculation để chốt công. |
| [AttendanceCalculationService](/src/main/java/com/kltn/school_hrm/module/attendance/service/AttendanceCalculationService.java) | **Tính toán số liệu công & Trạng thái** | Thuần thuật toán (Business Rules): Nhận vào `AttendanceRecord` và `Shift` để tính số phút đi trễ (so với `startTime + graceMinutes`), về sớm (so với `endTime`), thời gian làm việc thực tế, trừ giờ nghỉ trưa, và xác định trạng thái (`LATE`, `PRESENT`, `INCOMPLETE`, v.v.). |
| [AttendanceRecordService](/src/main/java/com/kltn/school_hrm/module/attendance/service/AttendanceRecordService.java) | **Quản lý dữ liệu công đã tổng hợp** | Cung cấp các thao tác CRUD và truy vấn bảng công theo nhân viên, theo ngày/tháng để hiển thị cho HR và xuất báo cáo. |
| [ShiftService](/src/main/java/com/kltn/school_hrm/module/attendance/service/ShiftService.java) | **Quản lý danh mục Ca** | Tạo, sửa, xóa, tìm kiếm các ca làm việc trong hệ thống. |
| [EmployeeShiftAssignmentService](/src/main/java/com/kltn/school_hrm/module/attendance/service/EmployeeShiftAssignmentService.java) | **Quản lý lịch phân ca** | Thêm, sửa, hủy phân ca cho nhân viên. Validate chống việc phân công trùng lặp ca (overlap) trong cùng một thời gian. |
| [AttendanceService](/src/main/java/com/kltn/school_hrm/module/attendance/service/AttendanceService.java) | **Service Legacy (Cũ)** | Các hàm chấm công check-in / check-out đơn giản theo flow cũ. |

---

### 3. Mối quan hệ tương tác giữa các Class trong hệ thống

```text
[Thiết bị / Web / Máy vân tay]
             │
             ▼ (ghi nhận event)
   [AttendanceRawLogService] ──► [AttendanceRawLog]
                                         │
                                         ▼ (đọc raw logs)
                            [AttendanceAggregationService]
                                    │               │
         (tìm ca theo ngày)         │               │ (ghép cặp)
[EmployeeShiftAssignment] ◄────────┤               ▼
         │                          │      [AttendanceSession]
         ▼                          │               │
      [Shift]                       ▼               │
         │                 [AttendanceRecord] ◄─────┘
         │                          │
         └──────────┐  ┌────────────┘
                    ▼  ▼ (tính toán: late, early, workedMinutes)
             [AttendanceCalculationService]
                    │
                    ▼ (lưu kết quả hoàn chỉnh)
             [AttendanceRecordRepository] ──► Hiển thị báo cáo công cho HR
```
