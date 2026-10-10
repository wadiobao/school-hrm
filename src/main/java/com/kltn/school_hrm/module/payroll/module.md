Toàn bộ kiến trúc của module **Payroll (Tiền lương)** chịu trách nhiệm cấu hình thành phần lương, tổng hợp dữ liệu công và tính toán bảng lương chi tiết hàng tháng. Dưới đây là vai trò chi tiết của từng class trong module:

---

### 1. Nhóm Entity (Mô hình dữ liệu - Database)

| Class | Vai trò | Mô tả chi tiết |
| :--- | :--- | :--- |
| [SalaryComponent](/src/main/java/com/kltn/school_hrm/module/payroll/entity/SalaryComponent.java) | **Danh mục thành phần lương** | Định nghĩa các khoản cấu thành thu nhập: phụ cấp (ăn trưa, trách nhiệm, đi lại), trợ cấp, các khoản giảm trừ (bảo hiểm xã hội, y tế, thất nghiệp, thuế TNCN), và cờ chịu thuế (`taxable`). |
| [Payroll](/src/main/java/com/kltn/school_hrm/module/payroll/entity/Payroll.java) | **Bảng lương tổng hợp của nhân viên** | Bản ghi lương của nhân viên theo kỳ trả lương (`month`, `year`): lương cơ bản (`baseSalary`), tổng thu nhập (`grossSalary`), tổng các khoản khấu trừ (`totalDeductions`), lương thực nhận (`netSalary`), và trạng thái chi trả (`DRAFT`, `CONFIRMED`, `PAID`). |
| [PayrollDetail](/src/main/java/com/kltn/school_hrm/module/payroll/entity/PayrollDetail.java) | **Chi tiết từng khoản lương/khấu trừ** | Chi tiết các dòng thành phần gắn với một phiếu lương (`Payroll`): liên kết tới `SalaryComponent`, số tiền cụ thể (`amount`), và ghi chú tính toán. |

---

### 2. Nhóm Service (Xử lý nghiệp vụ)

| Class / Interface | Vai trò | Mô tả chi tiết |
| :--- | :--- | :--- |
| [SalaryComponentService](/src/main/java/com/kltn/school_hrm/module/payroll/service/SalaryComponentService.java) | **Quản lý cấu hình thành phần lương** | Cung cấp CRUD và thiết lập các thành phần lương áp dụng chung trong toàn trường hoặc cho từng nhóm vị trí công việc. |
| [PayrollService](/src/main/java/com/kltn/school_hrm/module/payroll/service/PayrollService.java) | **Tính toán & Xử lý kỳ lương** | **Bộ máy tính lương chính:** Thu thập dữ liệu ngày công thực tế (từ module Attendance), số giờ giảng dạy và vượt giờ (từ module Teaching), mức lương hiện tại (từ Contract/SalaryHistory), áp dụng công thức tính thu nhập trước/sau thuế và chốt bảng lương. |

---

### 3. Mối quan hệ tương tác giữa các Class trong hệ thống

```text
[Dữ liệu AttendanceRecord] ──┐
                             ├─► [PayrollService] ◄── [SalaryComponent] (Cấu hình phụ cấp/khấu trừ)
[Dữ liệu TeachingLog/Norm] ──┘         │
                                       ▼
                                   [Payroll] (Phiếu lương tổng hợp)
                                       │
                                       ▼ (1-n)
                                [PayrollDetail] (Chi tiết từng khoản)
```
