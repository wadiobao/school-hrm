Toàn bộ kiến trúc của module **Approval (Động cơ phê duyệt đa cấp - Approval Engine)** được thiết kế như một workflow engine độc lập, linh hoạt và có thể tái sử dụng cho nhiều nghiệp vụ khác nhau (nghỉ phép, điều chỉnh công, đề xuất tăng lương,...). Dưới đây là vai trò chi tiết của từng class trong module:

---

### 1. Nhóm Entity (Mô hình dữ liệu - Database)

| Class | Vai trò | Mô tả chi tiết |
| :--- | :--- | :--- |
| [ApprovalPolicy](/src/main/java/com/kltn/school_hrm/module/approval/entity/ApprovalPolicy.java) | **Chính sách phê duyệt nghiệp vụ** | Định nghĩa cấu hình quy trình duyệt cho từng loại đối tượng (`targetType: LEAVE_REQUEST, ATTENDANCE_CORRECTION,...`). Quy định số cấp duyệt tối đa, điều kiện áp dụng và trạng thái kích hoạt (`active`). |
| [ApprovalLevel](/src/main/java/com/kltn/school_hrm/module/approval/entity/ApprovalLevel.java) | **Cấu hình từng cấp duyệt** | Chi tiết một nấc duyệt trong policy: thứ tự bước (`stepOrder`), quy tắc xác định người duyệt (`approverRole`, `DIRECT_MANAGER`, `DEPARTMENT_HEAD`), và thời gian giới hạn xử lý (SLA timeout). |
| [ApprovalRequest](/src/main/java/com/kltn/school_hrm/module/approval/entity/ApprovalRequest.java) | **Phiên yêu cầu phê duyệt cụ thể** | Thể hiện một luồng duyệt đang chạy gắn liền với một bản ghi nghiệp vụ (`targetId`, `targetType`), người khởi tạo (`requester`), trạng thái chung (`PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`), và bước hiện tại (`currentStep`). |
| [ApprovalRequestStep](/src/main/java/com/kltn/school_hrm/module/approval/entity/ApprovalRequestStep.java) | **Tiến trình từng bước của yêu cầu** | Trạng thái của một bước duyệt cụ thể: ai là người được chỉ định duyệt (`assignedApprover`), đã hoàn thành hay chưa (`status`), thời điểm giao việc và kết thúc bước. |
| [ApprovalAction](/src/main/java/com/kltn/school_hrm/module/approval/entity/ApprovalAction.java) | **Nhật ký hành động duyệt (Audit Trail)** | Lưu lại vết của từng hành động duyệt: người thực hiện thao tác, hành động (`APPROVE`, `REJECT`, `REQUEST_CHANGE`), thời điểm và lý do/nhận xét (comment). |

---

### 2. Nhóm Service (Xử lý nghiệp vụ)

| Class / Interface | Vai trò | Mô tả chi tiết |
| :--- | :--- | :--- |
| [ApprovalEngineService](/src/main/java/com/kltn/school_hrm/module/approval/service/ApprovalEngineService.java) | **Động cơ điều phối luồng duyệt (Core Engine)** | Khởi tạo quy trình duyệt (`initiateApproval`), chuyển bước tiếp theo khi cấp trước duyệt xong (`approveStep`), hủy luồng khi bị từ chối (`rejectStep`), và thông báo callback về cho module nghiệp vụ tương ứng. |
| [ApproverResolverService](/src/main/java/com/kltn/school_hrm/module/approval/service/ApproverResolverService.java) | **Bộ định tuyến người phê duyệt** | Giải quyết logic xác định người duyệt thực tế dựa trên cấu hình: phân giải cấp trên trực tiếp (`directManager`), Trưởng bộ môn/phòng ban của người làm đơn, hoặc người dùng có vai trò quản trị cụ thể. |

---

### 3. Mối quan hệ tương tác giữa các Class trong hệ thống

```text
[Module nghiệp vụ (Leave/Attendance)]
             │
             ▼ (1. Khởi tạo quy trình)
   [ApprovalEngineService] ◄── [ApprovalPolicy] ──► [ApprovalLevel] (Cấu hình)
             │                     │
             │                     ▼
             │            [ApproverResolverService] (Xác định người duyệt thực tế)
             ▼
      [ApprovalRequest]
             │
             ▼ (1-n)
   [ApprovalRequestStep] (Bước 1 ──► Bước 2 ──► Bước 3)
             │
             ▼ (Ghi nhận thao tác: APPROVE / REJECT)
      [ApprovalAction] (Lưu vết kiểm toán)
```
