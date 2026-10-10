Toàn bộ kiến trúc của module **Employee (Hồ sơ & Vòng đời nhân sự)** quản lý toàn diện thông tin nhân viên, hợp đồng lao động, lịch sử lương, bằng cấp chứng chỉ và giấy tờ pháp lý (Visa/Giấy phép lao động cho giáo viên nước ngoài). Dưới đây là vai trò chi tiết của từng class trong module:

---

### 1. Nhóm Entity (Mô hình dữ liệu - Database)

| Class | Vai trò | Mô tả chi tiết |
| :--- | :--- | :--- |
| [Employee](/src/main/java/com/kltn/school_hrm/module/employee/entity/Employee.java) | **Hồ sơ nhân viên (Core Employee)** | Chứa toàn bộ thông tin định danh: mã nhân viên (`employeeCode`), họ tên, ngày sinh, CCCD/Hộ chiếu, liên kết phòng ban (`Department`), chức vụ (`Position`), tài khoản hệ thống (`User`), và loại giáo viên (`TeacherType`). |
| [Contract](/src/main/java/com/kltn/school_hrm/module/employee/entity/Contract.java) | **Hợp đồng lao động** | Lưu thông tin hợp đồng: số hợp đồng, loại hợp đồng (thử việc, xác định/không xác định thời hạn), ngày bắt đầu, ngày kết thúc, lương cơ bản theo hợp đồng và trạng thái hiệu lực. |
| [SalaryHistory](/src/main/java/com/kltn/school_hrm/module/employee/entity/SalaryHistory.java) | **Lịch sử điều chỉnh lương** | Lưu vết quá trình tăng lương/bậc lương qua các đợt đánh giá định kỳ: mức lương cũ, mức lương mới, ngày áp dụng, và lý do quyết định điều chỉnh. |
| [EmployeeStatusHistory](/src/main/java/com/kltn/school_hrm/module/employee/entity/EmployeeStatusHistory.java) | **Lịch sử trạng thái làm việc** | Nhật ký thay đổi trạng thái trong vòng đời làm việc: thử việc (`PROBATION`), chính thức (`WORKING`), tạm hoãn/đình chỉ (`SUSPENDED`), nghỉ hưu (`RETIRED`), nghỉ việc (`RESIGNED`). |
| [DegreeAndCertificate](/src/main/java/com/kltn/school_hrm/module/employee/entity/DegreeAndCertificate.java) | **Bằng cấp & Chứng chỉ chuyên môn** | Quản lý học vị, bằng sư phạm, chứng chỉ ngoại ngữ (IELTS, TESOL, CELTA,...) của giáo viên và nhân viên. |
| [WorkPermitAndVisa](/src/main/java/com/kltn/school_hrm/module/employee/entity/WorkPermitAndVisa.java) | **Visa & Giấy phép lao động (Expat Docs)** | Quản lý thời hạn và tính pháp lý của giấy phép lao động (Work Permit), thẻ tạm trú (TRC) hoặc Visa đối với chuyên gia/giáo viên nước ngoài. |

---

### 2. Nhóm Service (Xử lý nghiệp vụ)

| Class / Interface | Vai trò | Mô tả chi tiết |
| :--- | :--- | :--- |
| [EmployeeService](/src/main/java/com/kltn/school_hrm/module/employee/service/EmployeeService.java) | **Quản lý hồ sơ nhân viên** | Cung cấp các thao tác CRUD hồ sơ, tìm kiếm phân trang theo phòng ban/vị trí, lọc giáo viên theo chuyên môn và quản lý thông tin cá nhân. |
| [ContractService](/src/main/java/com/kltn/school_hrm/module/employee/service/ContractService.java) | **Quản lý hợp đồng lao động** | Ký kết hợp đồng mới, gia hạn hợp đồng, đóng hợp đồng cũ, cảnh báo hợp đồng sắp hết hạn để tái ký. |
| [SalaryHistoryService](/src/main/java/com/kltn/school_hrm/module/employee/service/SalaryHistoryService.java) | **Theo dõi & Ghi nhận diễn biến lương** | Xử lý việc tăng lương, lưu vết quyết định lương mới và cung cấp mức lương hiện hành chuẩn xác nhất cho module Payroll. |
| [EmployeeLifecycleService](/src/main/java/com/kltn/school_hrm/module/employee/service/EmployeeLifecycleService.java) | **Quản lý vòng đời nhân sự (Lifecycle Orchestrator)** | Điều phối luồng chuyển trạng thái: Tiếp nhận Onboarding -> Ký hợp đồng -> Đánh giá hết thử việc -> Đình chỉ/Khôi phục -> Xử lý thôi việc (Offboarding) và thu hồi quyền hệ thống. |

---

### 3. Mối quan hệ tương tác giữa các Class trong hệ thống

```text
[EmployeeLifecycleService]
        │
        ├─► [Employee] (Tạo hồ sơ nhân viên mới)
        │       │
        │       ├─► [Contract] (Tạo HĐLĐ thử việc / chính thức)
        │       │       └─► [SalaryHistory] (Đồng bộ mức lương ban đầu)
        │       │
        │       ├─► [DegreeAndCertificate] (Hồ sơ văn bằng)
        │       └─► [WorkPermitAndVisa] (Giấy phép lao động nếu là Expat)
        │
        └─► [EmployeeStatusHistory] (Ghi nhận trạng thái: PROBATION -> WORKING -> RESIGNED)
```
