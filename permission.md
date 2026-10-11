# 1. Chức năng theo từng Role

## 1.1. `SUPER_ADMIN` — Quản trị hệ thống

Quản lý cấu hình kỹ thuật, tài khoản và phân quyền toàn hệ thống.

- Quản lý tài khoản: tạo, khóa, mở khóa, kích hoạt và vô hiệu hóa tài khoản.
- Phân quyền: gán role, thu hồi role, quản lý quyền truy cập.
- Cấu hình hệ thống: cấu hình tham số, trạng thái hoạt động và các thiết lập chung.
- Quản lý danh mục: quản lý các danh mục dùng chung nếu được phân quyền.
- Theo dõi hệ thống: xem audit log, lịch sử đăng nhập và các hoạt động quan trọng.
- Sao lưu và phục hồi: thực hiện hoặc giám sát các thao tác theo quyền được cấp.
- Quản lý lỗi hệ thống: theo dõi lỗi và trạng thái các tác vụ nền.

Lưu ý: không mặc định cho `SUPER_ADMIN` quyền sửa lương, duyệt nghỉ phép hoặc phê duyệt hợp đồng. Những thao tác đó thuộc nghiệp vụ và cần quyền riêng.

## 1.2. `BOARD_OF_DIRECTORS` — Ban Giám hiệu

Quản lý và giám sát hoạt động nhân sự ở cấp trường; phê duyệt các nghiệp vụ theo chính sách.

- Xem tổng quan nhân sự: số lượng nhân viên, cơ cấu nhân sự, tình trạng làm việc.
- Xem báo cáo: chấm công, nghỉ phép, biến động nhân sự và chi phí lương nếu được cấp quyền.
- Phê duyệt nghỉ phép: duyệt các yêu cầu được chuyển đến cấp Ban Giám hiệu.
- Phê duyệt nhân sự: phê duyệt tuyển dụng, bổ nhiệm, điều chuyển hoặc nghỉ việc nếu quy trình yêu cầu.
- Phê duyệt hợp đồng: phê duyệt ký mới, gia hạn hoặc chấm dứt hợp đồng theo thẩm quyền.
- Phê duyệt thay đổi lương: xem xét các đề xuất điều chỉnh lương thuộc thẩm quyền.
- Giám sát các bộ phận: xem báo cáo và tình hình nhân sự của các khoa, bộ môn và phòng ban.

Không mặc định Ban Giám hiệu được thay đổi dữ liệu chấm công hoặc sửa trực tiếp lịch sử lương.

## 1.3. `HR_MANAGER` — Trưởng phòng Nhân sự

Quản lý dữ liệu và quy trình nhân sự trên toàn trường.

- Quản lý hồ sơ nhân viên: tạo, cập nhật, tra cứu hồ sơ.
- Quản lý phòng ban: quản lý cơ cấu phòng ban, bộ môn và người phụ trách theo phạm vi được giao.
- Quản lý chức vụ: quản lý danh mục chức vụ.
- Quản lý hợp đồng: tạo hợp đồng, gia hạn, chấm dứt và theo dõi thời hạn.
- Quản lý lương: ghi nhận lịch sử lương và tạo đề xuất điều chỉnh lương.
- Quản lý chấm công: quản lý ca làm việc, phân công ca, kiểm tra và xử lý dữ liệu chấm công bất thường.
- Quản lý nghỉ phép: kiểm tra yêu cầu, theo dõi số dư phép và xử lý yêu cầu theo thẩm quyền.
- Quản lý phê duyệt: cấu hình quy trình phê duyệt nghiệp vụ nếu được cấp quyền.
- Báo cáo nhân sự: thống kê nhân sự, hợp đồng, nghỉ phép và chấm công.
- Quản lý ngày lễ và lịch làm việc: cấu hình ngày lễ, ngày làm việc chuẩn theo chính sách.

HR Manager không nhất thiết có quyền tự phê duyệt mọi đề xuất do chính mình tạo ra. Cần tách quyền tạo, kiểm tra và phê duyệt khi nghiệp vụ yêu cầu.

## 1.4. `HEAD_OF_DEPARTMENT` — Trưởng Bộ môn / Trưởng đơn vị

Quản lý nhân sự thuộc bộ môn hoặc đơn vị được giao phụ trách.

- Xem hồ sơ nhân viên: xem thông tin nhân sự thuộc đơn vị.
- Theo dõi chấm công: xem tình hình chấm công và các trường hợp đi muộn, về sớm.
- Xác nhận chấm công: xác nhận hoặc đề xuất điều chỉnh dữ liệu theo quy trình.
- Duyệt nghỉ phép cấp đơn vị: phê duyệt hoặc từ chối yêu cầu nghỉ phép theo thẩm quyền.
- Quản lý phân công: theo dõi lịch làm việc và phân công ca nếu chính sách cho phép.
- Đề xuất nhân sự: đề xuất tuyển dụng, điều chuyển, bổ nhiệm hoặc thay đổi nhân sự.
- Đề xuất làm thêm giờ: tạo hoặc xác nhận đề xuất làm thêm giờ.
- Xem báo cáo đơn vị: xem số lượng nhân sự, nghỉ phép và chấm công trong phạm vi quản lý.

Không được tự ý xem hồ sơ, lương hoặc dữ liệu của những đơn vị khác nếu không có quyền bổ sung.

## 1.5. `TEACHER` — Giáo viên

Sử dụng các chức năng cá nhân phục vụ công việc và theo dõi hồ sơ của mình.

- Xem hồ sơ cá nhân: thông tin liên hệ, chức vụ và đơn vị công tác.
- Xem hợp đồng: xem thông tin hợp đồng của bản thân theo chính sách bảo mật.
- Xem thông tin lương: xem phiếu lương hoặc thông tin lương cá nhân nếu hệ thống có module này.
- Xem chấm công: xem lịch sử vào/ra, số ngày công, đi muộn và về sớm.
- Gửi yêu cầu nghỉ phép: tạo yêu cầu nghỉ phép và theo dõi trạng thái.
- Hủy yêu cầu nghỉ phép: hủy yêu cầu đang chờ duyệt nếu quy định cho phép.
- Xem số dư phép: xem số ngày phép được cấp, đã sử dụng và còn lại.
- Gửi yêu cầu sửa chấm công: gửi đề nghị điều chỉnh khi thiếu lượt vào/ra hoặc có sai sót.
- Đăng ký làm thêm giờ: gửi đề xuất làm thêm giờ nếu nghiệp vụ áp dụng.
- Nhận thông báo: nhận thông báo về duyệt phép, hợp đồng và chấm công.

`TEACHER` không được tự sửa dữ liệu chấm công chính thức, tự duyệt nghỉ phép hoặc xem hồ sơ của giáo viên khác.

## 1.6. `STAFF` — Nhân viên Hành chính

Có các chức năng cá nhân tương tự giáo viên, đồng thời có thể có thêm các nghiệp vụ hành chính theo vị trí công việc.

- Xem hồ sơ cá nhân.
- Xem hợp đồng và thông tin lương cá nhân theo quyền được cấp.
- Xem lịch làm việc và lịch sử chấm công.
- Gửi, theo dõi và hủy yêu cầu nghỉ phép theo chính sách.
- Gửi yêu cầu điều chỉnh chấm công.
- Đăng ký hoặc đề xuất làm thêm giờ nếu áp dụng.
- Nhận thông báo cá nhân.
- Thực hiện nghiệp vụ hành chính chuyên môn nếu được cấp quyền riêng.

Không nên cho mọi `STAFF` quyền quản lý hồ sơ nhân sự toàn trường chỉ vì họ làm công việc hành chính. Nếu có nhân viên hành chính thuộc phòng Nhân sự, nên cấp thêm quyền nghiệp vụ phù hợp thay vì mặc định theo role chung.

# 2. Ma trận quyền chức năng

Quy ước:

- `CRUD`: tạo, xem, sửa, xóa trong phạm vi cho phép; dữ liệu lịch sử không nên xóa vật lý tùy tiện.
- `View`: chỉ xem.
- `Approve`: phê duyệt theo quy trình.
- `Own`: chỉ dữ liệu của bản thân.
- `Unit`: chỉ dữ liệu đơn vị phụ trách.
- `—`: không cấp quyền mặc định.

| Chức năng                     | SUPER_ADMIN     | BOARD_OF_DIRECTORS      | HR_MANAGER             | HEAD_OF_DEPARTMENT      | TEACHER           | STAFF             |
| ----------------------------- | --------------- | ----------------------- | ---------------------- | ----------------------- | ----------------- | ----------------- |
| Quản lý tài khoản, phân quyền | CRUD            | —                       | —                      | —                       | —                 | —                 |
| Cấu hình hệ thống             | CRUD            | View báo cáo            | View nghiệp vụ         | —                       | —                 | —                 |
| Hồ sơ nhân viên               | View kỹ thuật\* | View                    | CRUD                   | View — Unit             | View — Own        | View — Own        |
| Phòng ban, chức vụ            | CRUD danh mục\* | View                    | CRUD                   | View — Unit             | —                 | —                 |
| Hợp đồng                      | View kỹ thuật\* | Approve theo thẩm quyền | CRUD nghiệp vụ         | View hạn chế            | View — Own        | View — Own        |
| Lịch sử lương                 | View kỹ thuật\* | Approve theo thẩm quyền | Quản lý/đề xuất        | —                       | View — Own        | View — Own        |
| Ca làm việc                   | —               | View báo cáo            | CRUD                   | View/đề xuất            | View lịch cá nhân | View lịch cá nhân |
| Chấm công                     | —               | View báo cáo            | Quản lý/điều chỉnh     | View/xác nhận — Unit    | View — Own        | View — Own        |
| Nghỉ phép                     | —               | Approve cấp cao         | Quản lý theo quy trình | Approve — Unit          | Tạo/hủy — Own     | Tạo/hủy — Own     |
| Làm thêm giờ                  | —               | Approve theo thẩm quyền | Quản lý quy trình      | Đề xuất/xác nhận — Unit | Đề xuất — Own     | Đề xuất — Own     |
| Báo cáo nhân sự               | View hệ thống\* | View toàn trường        | View toàn trường       | View — Unit             | View cá nhân      | View cá nhân      |

\* Đây là quyền đề xuất, cần xác định rõ trong chính sách bảo mật. Có quyền quản trị kỹ thuật không đồng nghĩa được xem dữ liệu nhạy cảm như lương hoặc hồ sơ cá nhân.

# 3. Thiết kế phân quyền trong Spring Boot

Không nên chỉ dùng role để quyết định toàn bộ quyền truy cập. Nên kết hợp RBAC (Role-Based Access Control) với kiểm tra phạm vi dữ liệu.

Ví dụ:

- `TEACHER` có quyền `attendance:read` nhưng chỉ xem được chấm công của mình.
- `HEAD_OF_DEPARTMENT` có quyền `attendance:read` và `attendance:verify`, nhưng chỉ trong đơn vị được quản lý.
- `HR_MANAGER` có quyền quản lý chấm công trên toàn trường.
- `BOARD_OF_DIRECTORS` có quyền xem báo cáo và phê duyệt theo quy trình, nhưng không tự động có quyền sửa dữ liệu gốc.

Có thể thiết kế quyền theo dạng:

```
employee:read
employee:create
employee:update
contract:read
contract:create
contract:terminate
salary:read
salary:propose
salary:approve
attendance:read
attendance:verify
attendance:adjust
leave:create
leave:approve
leave:reject
shift:manage
report:read
account:manage
role:assign
```

Trong Spring Security, role có thể dùng để cấp quyền tổng quát; kiểm tra chủ sở hữu dữ liệu, đơn vị quản lý và người có quyền phê duyệt nên thực hiện thêm tại service hoặc bằng method security.

Khuyến nghị: Với dự án HRM của trường, hãy dùng 6 role trên làm nhóm vai trò ban đầu, sau đó định nghĩa quyền chi tiết và phạm vi dữ liệu riêng. Đặc biệt, cần tránh để `SUPER_ADMIN` tự động có quyền nghiệp vụ nhạy cảm và tránh để `HEAD_OF_DEPARTMENT` xem dữ liệu ngoài đơn vị.
