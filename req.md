# Multi-Level Approval — Thiết kế tổng quát

## 1. Mục tiêu

Refactor cơ chế `LeaveApproval` hiện tại thành một **Generic Multi-Level Approval Framework** có thể tái sử dụng cho nhiều nghiệp vụ:

* Leave Request
* Overtime Request
* Payroll
* Recruitment
* Purchase Request
* Contract
* Salary Adjustment
* ...

### Mục tiêu kiến trúc

```text
LeaveRequest
PayrollRequest
OvertimeRequest
RecruitmentRequest
       │
       ▼
ApprovalEngine
       │
       ├── ApprovalPolicy
       ├── ApprovalLevel
       ├── ApprovalRequest
       ├── ApprovalRequestStep
       └── ApprovalAction
```

> **Ghi chú:** Approval Engine chỉ chịu trách nhiệm về workflow phê duyệt. Nó không được biết nghiệp vụ cụ thể là Leave, Payroll hay Recruitment.

---

# 2. Vấn đề của `LeaveApproval` hiện tại

## 2.1. Tightly Coupled với Leave

Hiện tại:

```java
@ManyToOne
private LeaveRequest leaveRequest;
```

Điều này khiến `LeaveApproval` chỉ dùng được cho Leave.

Muốn dùng cho Payroll phải tạo:

```text
PayrollApproval
```

Muốn dùng cho Overtime lại tạo:

```text
OvertimeApproval
```

Dẫn tới nhiều code trùng lặp.

### Thiết kế mới

Dùng:

```text
businessType
businessId
```

Ví dụ:

```text
businessType = LEAVE_REQUEST
businessId   = 123
```

Hoặc:

```text
businessType = PAYROLL
businessId   = 456
```

> **Ghi chú:** Đây là trade-off có chủ ý. Database không có FK trực tiếp tới từng business table, nhưng Approval Engine có thể tái sử dụng cho nhiều module.

---

# 3. Mô hình tổng thể

```text
                 CONFIGURATION
                      │
                      ▼
              ┌─────────────────┐
              │ ApprovalPolicy  │
              └────────┬────────┘
                       │ 1:N
                       ▼
              ┌─────────────────┐
              │ ApprovalLevel   │
              └─────────────────┘


                 RUNTIME
                      │
                      ▼
              ┌─────────────────┐
              │ ApprovalRequest │
              └────────┬────────┘
                       │
              ┌────────┴─────────┐
              ▼                  ▼
   ┌────────────────────┐ ┌──────────────────┐
   │ ApprovalRequestStep│ │ ApprovalAction   │
   └────────────────────┘ └──────────────────┘
```

Có thể hiểu đơn giản:

```text
ApprovalPolicy
"Workflow này phải duyệt như thế nào?"

ApprovalLevel
"Từng cấp phải duyệt theo rule nào?"

ApprovalRequest
"Đơn cụ thể này đang chạy workflow nào?"

ApprovalRequestStep
"Trong đơn này, từng bước đang ở trạng thái nào và ai được assign?"

ApprovalAction
"Trong lịch sử, ai đã thực hiện hành động gì?"
```

---

# 4. `ApprovalPolicy`

## 4.1. Mục đích

`ApprovalPolicy` là **cấu hình workflow**.

Ví dụ:

```text
LEAVE_APPROVAL_POLICY v1
```

quy định:

```text
Level 1 → Direct Manager
Level 2 → Parent Department Manager
```

## 4.2. Fields

```java
@Entity
public class ApprovalPolicy extends BaseEntity {

    private String code;

    private String name;

    private String businessType;

    private Integer version;

    private Boolean isActive;

    private String description;
}
```

Ví dụ:

```text
code         = LEAVE_APPROVAL_POLICY
businessType = LEAVE_REQUEST
version      = 1
isActive     = true
```

---

# 5. `ApprovalLevel`

## 5.1. Mục đích

`ApprovalLevel` định nghĩa **một bước trong Policy**.

Ví dụ:

```text
Level 1
    Direct Manager

Level 2
    Parent Department Manager
```

## 5.2. Fields

```java
@Entity
public class ApprovalLevel extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ApprovalPolicy approvalPolicy;

    private Integer levelOrder;

    private String levelName;

    @Enumerated(EnumType.STRING)
    private ApproverType approverType;

    private String approverRole;

    private Long specificApproverId;

    private String conditionExpression;
}
```

---

# 6. ApproverType

```java
public enum ApproverType {

    DIRECT_MANAGER,

    PARENT_DEPARTMENT_MANAGER,

    SPECIFIC_ROLE,

    SPECIFIC_EMPLOYEE
}
```

Ví dụ:

| ApproverType                | Ý nghĩa                        |
| --------------------------- | ------------------------------ |
| `DIRECT_MANAGER`            | Manager trực tiếp của Employee |
| `PARENT_DEPARTMENT_MANAGER` | Manager của Department cha     |
| `SPECIFIC_ROLE`             | Một người có Role cụ thể       |
| `SPECIFIC_EMPLOYEE`         | Một User/Employee cụ thể       |

---

# 7. Lưu ý quan trọng: Rule và Approver thực tế là hai thứ khác nhau

Không nên nhầm:

```text
ApprovalLevel
    ↓
DIRECT_MANAGER
```

với:

```text
Nguyễn Văn A
```

`DIRECT_MANAGER` là **rule**.

Nguyễn Văn A là **kết quả resolution**.

Flow:

```text
ApprovalLevel
    │
    │ approverType = DIRECT_MANAGER
    ▼
Approver Resolver
    │
    ▼
Employee.department.manager
    │
    ▼
Nguyễn Văn A
```

> **Ghi chú:** `ApprovalLevel` không nên trở thành nơi lưu người duyệt thực tế của từng request.

---

# 8. `ApprovalRequest`

## 8.1. Mục đích

`ApprovalRequest` là **một workflow instance cụ thể**.

Ví dụ:

```text
LeaveRequest #100
```

được submit.

Hệ thống tạo:

```text
ApprovalRequest #500
```

## 8.2. Fields

```java
@Entity
public class ApprovalRequest extends BaseEntity {

    private String businessType;

    private Long businessId;

    private String businessRefCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ApprovalPolicy policy;

    private Integer policyVersion;

    private Integer currentLevel;

    @Enumerated(EnumType.STRING)
    private ApprovalStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User requester;

    @Version
    private Long version;
}
```

### Status

```java
public enum ApprovalStatus {

    PENDING,

    APPROVED,

    REJECTED,

    CANCELLED
}
```

> **Ghi chú:** `@Version` giúp xử lý trường hợp hai request approve cùng lúc. Không nên bỏ qua concurrency control khi approval là nghiệp vụ thực tế.

---

# 9. Tại sao cần `ApprovalRequestStep`?

Đây là entity được bổ sung sau khi đánh giá thiết kế ban đầu.

Không nên chỉ có:

```text
ApprovalRequest
    ↓
policySnapshotJson
```

mà nên có:

```text
ApprovalRequest
    │
    ├── ApprovalRequestStep Level 1
    │
    └── ApprovalRequestStep Level 2
```

## 9.1. Lý do

`ApprovalLevel` là configuration.

`ApprovalRequestStep` là runtime state.

Ví dụ:

```text
ApprovalLevel

Level 1
approverType = DIRECT_MANAGER
```

Khi LeaveRequest được submit:

```text
ApprovalRequestStep

Level 1
approverType = DIRECT_MANAGER
assignedApprover = Nguyễn Văn A
status = PENDING
```

Nếu Department Manager thay đổi sau đó:

```text
Nguyễn Văn A
        ↓
Nguyễn Văn C
```

thì request cũ vẫn giữ:

```text
assignedApprover = Nguyễn Văn A
```

---

# 10. Đề xuất `ApprovalRequestStep`

```java
@Entity
public class ApprovalRequestStep extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ApprovalRequest approvalRequest;

    private Integer levelOrder;

    private String levelName;

    @Enumerated(EnumType.STRING)
    private ApproverType approverType;

    @ManyToOne(fetch = FetchType.LAZY)
    private User assignedApprover;

    @Enumerated(EnumType.STRING)
    private ApprovalStatus status;

    private String conditionSnapshot;

    private LocalDateTime assignedAt;

    private LocalDateTime completedAt;
}
```

Có thể bổ sung:

```text
resolvedApproverType
resolvedApproverId
```

nếu muốn lưu rõ kết quả resolution.

---

# 11. `ApprovalAction`

## 11.1. Mục đích

`ApprovalAction` là **audit history**.

Không dùng nó để thay thế `ApprovalRequestStep`.

### Step

Cho biết:

```text
Level 1
Status = APPROVED
```

### Action

Cho biết:

```text
Nguyễn Văn A
APPROVE
09:30
comment = "Đồng ý"
```

Do đó:

```text
ApprovalRequestStep = Current State

ApprovalAction = History
```

---

# 12. `ApprovalAction`

```java
@Entity
public class ApprovalAction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ApprovalRequest approvalRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    private ApprovalRequestStep step;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User actor;

    private Integer level;

    @Enumerated(EnumType.STRING)
    private ApprovalActionType action;

    private String comment;

    private LocalDateTime actionAt;
}
```

---

# 13. ApprovalActionType

```java
public enum ApprovalActionType {

    SUBMIT,

    APPROVE,

    REJECT,

    CANCEL,

    FORWARD
}
```

Quy ước:

```text
SUBMIT
    level = null

APPROVE
    level != null

REJECT
    level != null

FORWARD
    level != null
```

> **Ghi chú:** `SUBMIT` là hành động đặc biệt vì nó xảy ra khi khởi tạo workflow, không phải hành động duyệt một level.

---

# 14. Policy Versioning

Không sửa trực tiếp Policy version cũ nếu nó đã được sử dụng.

Không nên:

```text
LEAVE_POLICY v1
```

sửa:

```text
>= 2 ngày
```

thành:

```text
>= 3 ngày
```

mà nên:

```text
LEAVE_POLICY v1
        │
        │ immutable
        ▼
LEAVE_POLICY v2
```

Kết quả:

```text
Request #100 → Policy v1

Request #101 → Policy v2
```

> **Ghi chú:** Policy version cũ nên được xem như immutable configuration để bảo toàn lịch sử.

---

# 15. Snapshot Policy

Snapshot cần giải quyết vấn đề:

> Policy hoặc cơ cấu tổ chức thay đổi sau khi request được submit thì request cũ phải chạy theo cấu hình nào?

Câu trả lời:

```text
Theo configuration tại thời điểm submit.
```

Ví dụ:

```text
2026-09-01

Policy v1
Level 1 → Manager
Level 2 → Principal
```

Request được tạo:

```text
ApprovalRequest #100
```

Sau đó:

```text
2026-09-02

Policy v2
Level 1 → Manager
Level 2 → HR Manager
```

Request #100 vẫn:

```text
Level 1 → Manager
Level 2 → Principal
```

---

# 16. Snapshot cái gì?

Không chỉ snapshot:

```text
Policy rule
```

mà nên snapshot cả:

```text
Approval Level
+
Approver Resolution
+
Condition
```

Ví dụ:

```text
ApprovalRequest #100

Step 1
------------------
level = 1
approverType = DIRECT_MANAGER
assignedApprover = Nguyễn Văn A

Step 2
------------------
level = 2
approverType = PARENT_DEPARTMENT_MANAGER
assignedApprover = Trần Văn B
condition = totalDays >= 2
```

> **Ghi chú quan trọng:** Snapshot runtime phải bảo đảm request cũ không bị ảnh hưởng bởi thay đổi Manager, Role, Department hoặc Policy sau này.

---

# 17. JSON Snapshot hay `ApprovalRequestStep`?

## Khuyến nghị

Dùng:

```text
ApprovalRequestStep
```

làm dữ liệu runtime chính.

Không cần phụ thuộc hoàn toàn vào:

```text
policySnapshotJson
```

### Vì sao?

Các query như:

```text
Ai đang duyệt?
Level hiện tại là ai?
Level nào đã approve?
Level nào pending?
```

đều dễ thực hiện bằng relational table.

Ví dụ:

```text
ApprovalRequest
       │
       ├── Step 1 → APPROVED
       │
       └── Step 2 → PENDING
```

thay vì phải parse JSON.

Nếu muốn giữ JSON để debug/audit đầy đủ:

```text
ApprovalRequest
    ├── policyVersion
    ├── policySnapshotJson   optional
    └── ApprovalRequestStep
```

Nhưng JSON không nên là source of truth cho runtime workflow.

---

# 18. Condition Expression

Ban đầu không nên triển khai SpEL tự do:

```text
totalDays >= 2
```

và evaluate trực tiếp từ database.

Cách này làm hệ thống phức tạp về:

```text
Security
Validation
Debugging
Testing
Versioning
```

## Nên bắt đầu bằng condition có cấu trúc

Ví dụ:

```text
conditionType = MIN_LEAVE_DAYS
conditionValue = 2
```

Hoặc:

```json
{
  "type": "MIN_LEAVE_DAYS",
  "value": 2
}
```

Sau này có thể mở rộng:

```text
MIN_LEAVE_DAYS
SALARY_THRESHOLD
EMPLOYEE_ROLE
DEPARTMENT
CONTRACT_TYPE
```

> **Ghi chú:** Đừng xây một expression engine hoàn chỉnh khi nghiệp vụ hiện tại chỉ cần vài loại condition cố định.

---

# 19. Approver Resolution

Khi workflow được initiate:

```text
ApprovalLevel
      │
      ▼
Approver Resolver
      │
      ▼
Actual Approver
```

Ví dụ:

### DIRECT_MANAGER

```text
LeaveRequest
    ↓
Employee
    ↓
Department
    ↓
Department.manager
```

### PARENT_DEPARTMENT_MANAGER

```text
Employee
    ↓
Department
    ↓
Parent Department
    ↓
Parent Department.manager
```

### SPECIFIC_ROLE

```text
Role
    ↓
Users
    ↓
Eligible Approver
```

### SPECIFIC_EMPLOYEE

```text
specificApproverId
```

---

# 20. `SPECIFIC_ROLE` cần quy định cardinality

Nếu Role có nhiều người:

```text
HR_MANAGER
    ├── A
    ├── B
    └── C
```

cần biết:

```text
A OR B OR C
```

hay:

```text
A AND B AND C
```

Nên có rule như:

```text
ANY_ONE
ALL
```

Ví dụ:

```text
SPECIFIC_ROLE
role = HR_MANAGER
mode = ANY_ONE
```

nghĩa là chỉ cần một HR Manager approve.

---

# 21. Transfer / Forward

Không nên đơn giản:

```sql
UPDATE approval_request_step
SET assigned_approver = B
```

mà không lưu lịch sử.

Ví dụ:

```text
Step 1
assignedApprover = A
```

A chuyển tiếp:

```text
ApprovalAction
actor = A
action = FORWARD
comment = "Đang nghỉ phép"
```

Sau đó:

```text
Step 1
assignedApprover = B
```

Lịch sử vẫn thể hiện:

```text
A từng được assign
A đã forward
B là người tiếp nhận hiện tại
```

> **Ghi chú:** Nếu sau này nghiệp vụ delegation/ủy quyền trở nên phức tạp, có thể tách thêm `ApprovalAssignment`. Hiện tại chưa cần.

---

# 22. ApprovalEngineService

Generic engine chịu trách nhiệm:

```text
initiate()
approve()
reject()
cancel()
getCurrentStep()
getCurrentApprovers()
```

Không chịu trách nhiệm:

```text
LeaveBalance
LeaveRequest status
Payroll calculation
Contract status
```

---

# 23. Service Architecture

```text
                    ┌─────────────────────┐
                    │ ApprovalEngineService│
                    └──────────┬──────────┘
                               │
             ┌─────────────────┼─────────────────┐
             ▼                 ▼                 ▼
       LeaveService      PayrollService    OvertimeService
             │                 │                 │
             ▼                 ▼                 ▼
     LeaveBalance        Payroll Logic     Overtime Logic
```

Generic Engine không được làm:

```text
ApprovalEngine
      ↓
LeaveBalanceService
```

vì như vậy generic engine lại phụ thuộc vào Leave.

---

# 24. LeaveRequestService vẫn là nơi orchestration nghiệp vụ

Ví dụ:

```java
@Transactional
public void approve(Long requestId, User currentUser) {

    LeaveRequest request = findRequest(requestId);

    ApprovalResult result =
            approvalEngineService.approve(
                    "LEAVE_REQUEST",
                    request.getId(),
                    currentUser
            );

    if (result.isFullyApproved()) {

        request.approve();

        leaveBalanceService.consume(
                request.getEmployee(),
                request.getTotalDays(),
                request.getStartDate(),
                "LEAVE_REQUEST",
                request.getId()
        );
    }
}
```

Flow:

```text
Controller
    ↓
LeaveRequestService
    ↓
ApprovalEngine
    ↓
ApprovalResult
    ↓
LeaveRequestService
    ├── LeaveRequest → APPROVED
    └── LeaveBalance → consume
```

---

# 25. Create Leave Request

```text
POST /leave-requests
        │
        ▼
LeaveRequestService
        │
        ├── Validate Employee
        ├── Validate LeaveType
        ├── Validate Dates
        ├── Calculate Leave Days
        ├── Check Overlap
        ├── Check Balance
        │
        ├── Create LeaveRequest(PENDING)
        │
        ├── Reserve pendingDays
        │
        └── ApprovalEngine.initiate()
```

Tất cả nên nằm trong:

```java
@Transactional
```

để tránh tình trạng:

```text
LeaveRequest created
    ↓
Balance reserved
    ↓
ApprovalRequest creation failed
```

---

# 26. Approval Initiate

```text
ApprovalEngine.initiate()
        │
        ├── Find active ApprovalPolicy
        │
        ├── Determine Policy version
        │
        ├── Evaluate conditions
        │
        ├── Resolve approvers
        │
        ├── Create ApprovalRequest
        │
        ├── Create ApprovalRequestStep
        │
        └── Create ApprovalAction(SUBMIT)
```

Ví dụ Leave 1 ngày:

```text
Policy

Level 1 → Direct Manager
Level 2 → >= 2 days
```

Request:

```text
totalDays = 1
```

kết quả:

```text
ApprovalRequest
    │
    └── Step 1 → Manager
```

Leave 3 ngày:

```text
ApprovalRequest
    │
    ├── Step 1 → Manager
    └── Step 2 → Parent Manager
```

---

# 27. Approval Flow

## Level 1 approve

```text
Manager
   │
   ▼
ApprovalEngine.approve()
   │
   ├── Validate current step
   ├── Validate actor
   ├── Create ApprovalAction(APPROVE)
   ├── Step 1 → APPROVED
   │
   └── Move currentLevel → 2
```

Request vẫn:

```text
ApprovalRequest = PENDING
LeaveRequest = PENDING
```

---

# 28. Final Approval

```text
Level 2
   │
   ▼
APPROVE
   │
   ├── Create ApprovalAction
   ├── Step 2 → APPROVED
   └── ApprovalRequest → APPROVED
```

Sau đó `LeaveRequestService`:

```text
LeaveRequest → APPROVED
LeaveBalance → consume
```

Ledger:

```text
LEAVE -3 days
```

---

# 29. Reject

Nếu Level 1 reject:

```text
ApprovalEngine
      │
      ├── ApprovalAction(REJECT)
      └── ApprovalRequest → REJECTED
```

Sau đó:

```text
LeaveRequest → REJECTED
LeaveBalance → release pendingDays
```

Nếu Level 2 reject:

```text
ApprovalEngine
      │
      ├── ApprovalAction(REJECT)
      └── ApprovalRequest → REJECTED
```

Sau đó:

```text
LeaveRequest → REJECTED
LeaveBalance → release pendingDays
```

> **Ghi chú:** ApprovalEngine chỉ trả về kết quả workflow. Business Service quyết định hậu quả nghiệp vụ.

---

# 30. Các trạng thái

## ApprovalRequest

```text
PENDING
   ├── APPROVED
   ├── REJECTED
   └── CANCELLED
```

Không nên cho:

```text
APPROVED → PENDING
REJECTED → PENDING
```

trong flow bình thường.

Nếu muốn reopen/re-submit, nên thiết kế thành business flow riêng.

---

# 31. Concurrency

Approval là nghiệp vụ cần kiểm soát concurrent update.

Ví dụ:

```text
Tab 1 → Manager APPROVE
Tab 2 → Manager APPROVE
```

Cả hai cùng đọc:

```text
currentLevel = 1
```

Nếu không kiểm soát có thể tạo duplicate action.

Nên dùng:

```java
@Version
private Long version;
```

trên `ApprovalRequest`.

Kết hợp:

```java
@Transactional
```

và unique constraint nếu cần.

> **Ghi chú:** Không nên giải quyết mọi concurrency bằng distributed lock ngay từ đầu. Optimistic locking thường đủ cho workflow approval thông thường.

---

# 32. Migration từ `LeaveApproval`

Không nên duy trì lâu dài hai source of truth:

```text
LeaveApproval
ApprovalRequest
```

Nếu cả hai cùng lưu:

```text
status
approver
level
```

có nguy cơ:

```text
LeaveApproval     = APPROVED
ApprovalRequest   = PENDING
```

## Migration strategy

### Phase 1

```text
LeaveApproval
      +
ApprovalEngine
```

Xây framework mới nhưng chưa xóa code cũ.

### Phase 2

Chuyển:

```text
LeaveService
```

sang:

```text
ApprovalEngine
```

### Phase 3

Dừng ghi:

```text
LeaveApproval
```

### Phase 4

Migrate dữ liệu lịch sử nếu cần.

### Phase 5

Xóa:

```text
LeaveApproval
LeaveApprovalService
LeaveApprovalRepository
```

---

# 33. Implementation Plan

## Phase 1 — Core Entity

* [ ] `ApprovalPolicy`
* [ ] `ApprovalLevel`
* [ ] `ApprovalRequest`
* [ ] `ApprovalRequestStep`
* [ ] `ApprovalAction`

Enums:

* [ ] `ApproverType`
* [ ] `ApprovalStatus`
* [ ] `ApprovalActionType`

---

## Phase 2 — Repository

* [ ] `ApprovalPolicyRepository`
* [ ] `ApprovalLevelRepository`
* [ ] `ApprovalRequestRepository`
* [ ] `ApprovalRequestStepRepository`
* [ ] `ApprovalActionRepository`

---

## Phase 3 — Policy Engine

* [ ] Find active policy
* [ ] Policy versioning
* [ ] Condition evaluation
* [ ] Approver resolution
* [ ] Create ApprovalRequest
* [ ] Create ApprovalRequestStep
* [ ] Snapshot resolved approvers
* [ ] Create SUBMIT action

---

## Phase 4 — Approval Engine

* [ ] `initiate()`
* [ ] `approve()`
* [ ] `reject()`
* [ ] `cancel()`
* [ ] `getCurrentStep()`
* [ ] `getCurrentApprovers()`
* [ ] Actor validation
* [ ] Optimistic locking
* [ ] Transaction handling

---

## Phase 5 — Leave Integration

* [ ] LeaveRequest → ApprovalEngine
* [ ] Remove hardcoded approval rule
* [ ] Reserve `pendingDays`
* [ ] Consume balance after final approval
* [ ] Release balance after rejection
* [ ] Update LeaveResponse
* [ ] Return approval status/current approver/history

---

## Phase 6 — Migration

* [ ] Test old/new flow
* [ ] Stop writing `LeaveApproval`
* [ ] Migrate historical data
* [ ] Remove `LeaveApprovalService`
* [ ] Remove `LeaveApproval`
* [ ] Remove old repository/query

---

# 34. Test Cases bắt buộc

## Leave 1 ngày

```text
Create
  ↓
Level 1
  ↓
Manager APPROVE
  ↓
ApprovalRequest APPROVED
  ↓
LeaveRequest APPROVED
  ↓
Balance consumed
```

---

## Leave >= 2 ngày

```text
Create
  ↓
Level 1
  ↓
Manager APPROVE
  ↓
Level 2
  ↓
Parent Manager APPROVE
  ↓
ApprovalRequest APPROVED
  ↓
LeaveRequest APPROVED
  ↓
Balance consumed
```

---

## Reject Level 1

```text
Create
  ↓
Manager REJECT
  ↓
ApprovalRequest REJECTED
  ↓
LeaveRequest REJECTED
  ↓
pendingDays released
```

---

## Reject Level 2

```text
Create
  ↓
Level 1 APPROVE
  ↓
Level 2 REJECT
  ↓
ApprovalRequest REJECTED
  ↓
LeaveRequest REJECTED
  ↓
pendingDays released
```

---

## Policy thay đổi

```text
Request #100
    ↓
Policy v1
```

Sau đó:

```text
Policy v2
```

Kiểm tra:

```text
Request #100 vẫn chạy theo v1
```

---

## Manager thay đổi

Submit:

```text
Manager = A
```

Sau đó đổi:

```text
Manager = B
```

Kiểm tra:

```text
Request cũ → A
Request mới → B
```

---

## Double Approve

```text
Tab 1 → APPROVE
Tab 2 → APPROVE
```

Kiểm tra:

```text
Không tạo workflow sai
Không consume balance 2 lần
Không tạo duplicate approval action
```

---

# 35. Nguyên tắc kiến trúc cần nhớ

### 1. Policy ≠ Request

```text
Policy = Rule
Request = Runtime
```

---

### 2. Level ≠ Step

```text
ApprovalLevel
= configuration

ApprovalRequestStep
= runtime instance
```

---

### 3. Step ≠ Action

```text
Step
= current state

Action
= immutable history
```

---

### 4. Rule ≠ Actual Approver

```text
DIRECT_MANAGER
        ↓
Resolution
        ↓
Nguyễn Văn A
```

---

### 5. ApprovalEngine ≠ LeaveService

```text
ApprovalEngine
= workflow

LeaveService
= Leave business logic
```

---

### 6. Policy version cũ phải immutable

```text
v1 → immutable
v2 → new configuration
```

---

### 7. Request phải giữ runtime snapshot

Thay đổi:

```text
Policy
Department
Manager
Role
```

không được làm thay đổi workflow của request cũ.

---

### 8. Không có hai source of truth

Không duy trì lâu dài:

```text
LeaveApproval
+
ApprovalRequest
```

cùng quản lý approval state.

---

# 36. Kiến trúc cuối cùng

```text
                         ┌───────────────────┐
                         │ ApprovalPolicy    │
                         │                   │
                         │ code              │
                         │ version           │
                         │ businessType      │
                         │ isActive          │
                         └─────────┬─────────┘
                                   │
                                   │ 1:N
                                   ▼
                         ┌───────────────────┐
                         │ ApprovalLevel     │
                         │                   │
                         │ levelOrder        │
                         │ approverType      │
                         │ condition         │
                         └─────────┬─────────┘
                                   │
                                   │ resolve
                                   ▼
┌──────────────┐          ┌───────────────────┐
│ LeaveRequest │─────────▶│ ApprovalRequest   │
└──────────────┘          │                   │
                          │ businessType      │
┌──────────────┐          │ businessId        │
│ Payroll      │─────────▶│ policyVersion     │
└──────────────┘          │ currentLevel      │
                          │ status            │
┌──────────────┐          └─────────┬─────────┘
│ Overtime     │                    │
└──────────────┘          ┌─────────┴─────────┐
                           │                   │
                           ▼                   ▼
                 ┌──────────────────┐  ┌──────────────────┐
                 │ RequestStep       │  │ ApprovalAction   │
                 │                  │  │                  │
                 │ level            │  │ actor            │
                 │ approver         │  │ action           │
                 │ status           │  │ comment          │
                 │ resolved info    │  │ actionAt         │
                 └──────────────────┘  └──────────────────┘
```

## Core principle

```text
                    "Policy nói phải duyệt thế nào"

                              ↓

                    "Request nói đơn này
                     đang chạy workflow nào"

                              ↓

                    "Step nói hiện tại
                     mỗi cấp đang thế nào"

                              ↓

                    "Action nói lịch sử
                     ai đã làm gì"
```

Đây là cấu trúc nên dùng làm **baseline để bắt đầu refactor `LeaveApproval`**.
