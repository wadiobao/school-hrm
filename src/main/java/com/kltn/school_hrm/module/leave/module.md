Toàn bộ kiến trúc của module **Leave (Quản lý nghỉ phép)** được thiết kế theo mô hình chuẩn hóa quy trình đăng ký, trừ quỹ phép và phê duyệt nghỉ phép. Dưới đây là vai trò chi tiết của từng class trong module:

---

### 1. Nhóm Entity (Mô hình dữ liệu - Database)

| Class | Vai trò | Mô tả chi tiết |
| :--- | :--- | :--- |
| [LeaveRequest](/src/main/java/com/kltn/school_hrm/module/leave/entity/LeaveRequest.java) | **Đơn xin nghỉ phép** | Lưu thông tin đơn xin nghỉ: nhân viên, loại phép (`ANNUAL`, `SICK`, `UNPAID`), khoảng thời gian nghỉ (`startDate`, `endDate`), số ngày nghỉ tính toán (`totalDays`), lý do và trạng thái đơn (`PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`). |
| [LeaveBalance](/src/main/java/com/kltn/school_hrm/module/leave/entity/LeaveBalance.java) | **Quỹ ngày phép theo năm** | Theo dõi hạn mức phép của từng nhân viên theo năm tài chính (`year`): tổng ngày được hưởng (`totalDays`), số ngày đã sử dụng (`usedDays`), số ngày đang chờ duyệt (`pendingDays`), và ngày phép còn lại (`remainingDays`). |
| [LeaveBalanceTransaction](/src/main/java/com/kltn/school_hrm/module/leave/entity/LeaveBalanceTransaction.java) | **Nhật ký biến động quỹ phép (Audit Ledger)** | Ghi lại lịch sử cộng/trừ ngày phép: cấp phép định kỳ đầu năm, trừ phép khi duyệt đơn, hoàn phép khi hủy đơn hoặc điều chỉnh từ HR. |
| [LeaveApproval](/src/main/java/com/kltn/school_hrm/module/leave/entity/LeaveApproval.java) | **Lịch sử phê duyệt đơn phép** | Lưu các bước duyệt, người duyệt (`approver`), thứ tự cấp duyệt, hành động (`APPROVE`, `REJECT`) và ý kiến/lý do duyệt. |

---

### 2. Nhóm Service & Job (Xử lý nghiệp vụ)

| Class / Interface | Vai trò | Mô tả chi tiết |
| :--- | :--- | :--- |
| [LeaveRequestService](/src/main/java/com/kltn/school_hrm/module/leave/service/LeaveRequestService.java) | **Quản lý vòng đời đơn nghỉ phép** | Xử lý tạo đơn mới, kiểm tra hợp lệ ngày nghỉ (bỏ qua ngày lễ và cuối tuần nhờ `LeaveDayCalculator`), kiểm tra số dư quỹ phép còn lại, hủy đơn và tra cứu đơn theo nhân viên/trạng thái. |
| [LeaveBalanceService](/src/main/java/com/kltn/school_hrm/module/leave/service/LeaveBalanceService.java) | **Quản lý & Cập nhật số dư ngày phép** | Tính toán, khởi tạo số dư phép hàng năm cho nhân viên, giữ chỗ ngày phép (`pendingDays`) khi nộp đơn và khấu trừ/hoàn trả ngày phép theo kết quả phê duyệt. |
| [LeaveBalanceTransactionService](/src/main/java/com/kltn/school_hrm/module/leave/service/LeaveBalanceTransactionService.java) | **Quản lý lịch sử giao dịch quỹ phép** | Truy vấn và ghi nhận chi tiết các giao dịch biến động phép phục vụ đối soát công và kiểm toán nhân sự. |
| [LeaveApprovalService](/src/main/java/com/kltn/school_hrm/module/leave/service/LeaveApprovalService.java) | **Xử lý phê duyệt đơn nghỉ phép** | Tiếp nhận quyết định phê duyệt/từ chối từ cấp quản lý hoặc kích hoạt quy trình đa cấp thông qua `ApprovalEngineService`. |
| [AnnualLeaveAccrualJob](/src/main/java/com/kltn/school_hrm/module/leave/job/AnnualLeaveAccrualJob.java) | **Job tự động tích lũy phép định kỳ** | Scheduled task chạy định kỳ để tự động khởi tạo hoặc cộng dồn phép thâm niên hàng năm cho nhân viên. |

---

### 3. Mối quan hệ tương tác giữa các Class trong hệ thống

```text
[Nhân viên gửi đơn xin nghỉ]
             │
             ▼
    [LeaveRequestService] ──────────► [LeaveDayCalculator] (Trừ T7, CN, Ngày lễ)
             │
             ├──────────────────────► [LeaveBalanceService] (Kiểm tra & giữ pendingDays)
             │                                    │
             ▼                                    ▼
       [LeaveRequest]                     [LeaveBalance]
             │                                    │
             ▼ (Tạo yêu cầu duyệt)                 │
    [LeaveApprovalService]                        │
             │                                    │
      (Duyệt thành công)                          ▼
             └──────────────────────► [LeaveBalanceTransaction] (Ghi nhận sổ cái trừ phép)
```
