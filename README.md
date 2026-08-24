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

