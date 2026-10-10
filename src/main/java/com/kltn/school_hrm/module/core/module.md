Toàn bộ kiến trúc của module **Core (Hệ thống cốt lõi & Danh mục nền tảng)** đóng vai trò cung cấp nền tảng quản trị người dùng, phân quyền, cấu hình cơ cấu tổ chức và lịch ngày lễ chung cho toàn bộ các module khác. Dưới đây là vai trò chi tiết của từng class trong module:

---

### 1. Nhóm Entity (Mô hình dữ liệu - Database)

| Class | Vai trò | Mô tả chi tiết |
| :--- | :--- | :--- |
| [User](/src/main/java/com/kltn/school_hrm/module/core/entity/User.java) | **Tài khoản người dùng (Authentication & Identity)** | Lưu thông tin đăng nhập hệ thống: tên đăng nhập (`username`), mật khẩu đã mã hóa (`password`), email liên hệ, trạng thái tài khoản (`ACTIVE`, `INACTIVE`), và liên kết với vai trò (`Role`). |
| [Role](/src/main/java/com/kltn/school_hrm/module/core/entity/Role.java) | **Vai trò phân quyền hệ thống** | Định nghĩa các vai trò quản trị và nghiệp vụ: `SUPER_ADMIN`, `BOARD_OF_DIRECTORS`, `HR_MANAGER`, `HEAD_OF_DEPARTMENT`, `TEACHER`, `STAFF`. |
| [Department](/src/main/java/com/kltn/school_hrm/module/core/entity/Department.java) | **Phòng ban / Bộ môn** | Cấu trúc cơ cấu tổ chức của nhà trường: Phòng Hành chính - Nhân sự, Ban Giám hiệu, Khoa Ngoại ngữ, Tổ bộ môn Toán,... |
| [Position](/src/main/java/com/kltn/school_hrm/module/core/entity/Position.java) | **Vị trí công việc / Chức danh** | Danh mục chức danh chuyên môn: Hiệu trưởng, Trưởng bộ môn, Giáo viên giảng dạy, Chuyên viên nhân sự, Kế toán,... |
| [Holiday](/src/main/java/com/kltn/school_hrm/module/core/entity/Holiday.java) | **Lịch ngày nghỉ lễ quốc gia & trường học** | Danh mục ngày nghỉ lễ theo luật định và theo lịch nhà trường (`date`, `name`, `coefficient`). Dùng chung cho tính ngày phép (module Leave) và xác định ngày công/làm thêm ngày lễ (module Attendance). |

---

### 2. Nhóm Service (Xử lý nghiệp vụ)

| Class / Interface | Vai trò | Mô tả chi tiết |
| :--- | :--- | :--- |
| [UserService](/src/main/java/com/kltn/school_hrm/module/core/service/UserService.java) | **Quản lý tài khoản đăng nhập** | Cung cấp tạo tài khoản, đổi mật khẩu, gán vai trò quyền hạn và khóa/mở khóa tài khoản nhân viên. |
| [RoleService](/src/main/java/com/kltn/school_hrm/module/core/service/RoleService.java) | **Quản lý vai trò quyền hạn** | Tra cứu và thiết lập danh mục vai trò trong hệ thống. |
| [DepartmentService](/src/main/java/com/kltn/school_hrm/module/core/service/DepartmentService.java) | **Quản lý danh mục phòng ban** | Thêm, sửa, xóa, tìm kiếm danh mục phòng ban, tổ chức bộ môn. |
| [PositionService](/src/main/java/com/kltn/school_hrm/module/core/service/PositionService.java) | **Quản lý danh mục vị trí chức danh** | Cung cấp CRUD và phân loại vị trí công việc theo tiêu chuẩn trường học. |

---

### 3. Mối quan hệ tương tác giữa các Class trong hệ thống

```text
       [Role] ────────┐
                      ▼
[User] ◄────── [Employee] ──────► [Department]
                      │
                      ├─────────► [Position]
                      │
[Holiday] ────────────┴─────────► [Dùng chung cho Attendance & Leave]
```
