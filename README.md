# school-hrm

quy tắc nghiệp vụ của employee lifecycle transitions:

| From      | To        | Cho phép? | Lý do                  |
| --------- | --------- | --------- | ---------------------- |
| PROBATION | WORKING   | Có        | Đạt thử việc           |
| PROBATION | RESIGNED  | Có        | Nghỉ trong thử việc    |
| WORKING   | SUSPENDED | Có        | Tạm đình chỉ           |
| SUSPENDED | WORKING   | Có        | Trở lại làm việc       |
| WORKING   | RESIGNED  | Có        | Nghỉ việc              |
| WORKING   | RETIRED   | Có        | Nghỉ hưu               |
| RESIGNED  | WORKING   | Không*    | Không tự kích hoạt lại |
| RETIRED   | WORKING   | Không*    | Không tự kích hoạt lại |
| RESIGNED  | RETIRED   | Không     | Không hợp lệ           |
| RETIRED   | RESIGNED  | Không     | Không hợp lệ           |

ảnh hưởng của employee status 
| Module         | Mức độ     | Lý do                                               |
| -------------- | ---------- | --------------------------------------------------- |
| **Employee**   | Rất cao    | Lifecycle nằm trực tiếp ở đây                       |
| **Contract**   | Rất cao    | Hợp đồng thường kích hoạt thay đổi trạng thái       |
| **User**       | Cao        | Nghỉ việc có thể disable tài khoản                  |
| **Department** | Cao        | Employee có thể là Manager                          |
| **Position**   | Thấp       | Position là master data, không bị xóa theo Employee |
| **Leave**      | Cao        | Quyết định quyền tạo và cộng phép                   |
| **Attendance** | Cao        | Quyết định có còn được tính công                    |
| **Payroll**    | Rất cao    | Phải tính theo khoảng thời gian làm việc thực tế    |
| **Role**       | Trung bình | Có thể giữ hoặc thu hồi quyền tùy chính sách        |

