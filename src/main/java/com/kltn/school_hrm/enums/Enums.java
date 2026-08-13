package com.kltn.school_hrm.enums;


import lombok.Getter;

public class Enums {

    @Getter
    public enum RoleCode {
        SUPER_ADMIN("Quản trị hệ thống"),
        BOARD_OF_DIRECTORS("Ban Giám hiệu"),
        HR_MANAGER("Trưởng phòng Nhân sự"),
        HEAD_OF_DEPARTMENT("Trưởng Bộ môn / Head of Faculty"),
        TEACHER("Giáo viên"),
        STAFF("Nhân viên Hành chính");

        private final String label;
        RoleCode(String label) { this.label = label; }
    }

    @Getter
    public enum UserStatus {
        ACTIVE("Đang hoạt động"),
        INACTIVE("Tạm khóa"),
        SUSPENDED("Đình chỉ");

        private final String label;
        UserStatus(String label) { this.label = label; }
    }

    @Getter
    public enum TeacherType {
        EXPAT("Giáo viên Nước ngoài"),
        LOCAL("Giáo viên Việt Nam"),
        BILINGUAL("Giáo viên Song ngữ");

        private final String label;
        TeacherType(String label) { this.label = label; }
    }

    @Getter
    public enum EmployeeStatus {
        WORKING("Đang làm việc"),
        ON_LEAVE("Đang nghỉ phép"),
        RETIRED("Đã nghỉ hưu"),
        RESIGNED("Đã nghỉ việc");

        private final String label;
        EmployeeStatus(String label) { this.label = label; }
    }

    @Getter
    public enum WorkPermitStatus {
        NOT_STARTED("Chưa làm"),
        IN_PROGRESS("Đang xử lý"),
        ACTIVE("Còn hiệu lực"),
        EXPIRED("Hết hạn");

        private final String label;
        WorkPermitStatus(String label) { this.label = label; }
    }

    @Getter
    public enum DegreeType {
        DEGREE("Bằng cấp chuyên môn"),
        CERTIFICATE("Chứng chỉ sư phạm / Ngoại ngữ");

        private final String label;
        DegreeType(String label) { this.label = label; }
    }

    @Getter
    public enum Currency {
        VND("Việt Nam Đồng"),
        USD("US Dollar");

        private final String label;
        Currency(String label) { this.label = label; }
    }

    @Getter
    public enum ContractStatus {
        ACTIVE("Còn hiệu lực"),
        EXPIRED("Hết hạn"),
        TERMINATED("Đã chấm dứt");

        private final String label;
        ContractStatus(String label) { this.label = label; }
    }

    @Getter
    public enum Curriculum {
        IB("International Baccalaureate"),
        CAMBRIDGE("Cambridge International"),
        AMERICAN_AP("American Advanced Placement"),
        MOET("Chương trình Bộ GD&ĐT");

        private final String label;
        Curriculum(String label) { this.label = label; }
    }

    @Getter
    public enum TeachingLogType {
        REGULAR("Tiết dạy chính khóa"),
        SUBSTITUTE("Dạy thay"),
        MAKEUP("Dạy bù"),
        OVERTIME("Dạy vượt giờ");

        private final String label;
        TeachingLogType(String label) { this.label = label; }
    }

    @Getter
    public enum AttendanceStatus {
        PRESENT("Có mặt"),
        LATE("Đi trễ"),
        EARLY_LEAVE("Về sớm"),
        ABSENT("Vắng mặt");

        private final String label;
        AttendanceStatus(String label) { this.label = label; }
    }

    @Getter
    public enum LeaveType {
        ANNUAL("Nghỉ phép năm"),
        SICK("Nghỉ ốm"),
        MATERNITY("Nghỉ thai sản"),
        UNPAID("Nghỉ không lương");

        private final String label;
        LeaveType(String label) { this.label = label; }
    }

    @Getter
    public enum RequestStatus {
        PENDING("Chờ phê duyệt"),
        APPROVED("Đã phê duyệt"),
        REJECTED("Từ chối");

        private final String label;
        RequestStatus(String label) { this.label = label; }
    }

    @Getter
    public enum ComponentType {
        ALLOWANCE("Phụ cấp / Cộng"),
        DEDUCTION("Khoản khấu trừ");

        private final String label;
        ComponentType(String label) { this.label = label; }
    }

    @Getter
    public enum PayrollStatus {
        DRAFT("Bản nháp"),
        APPROVED("Đã duyệt"),
        PAID("Đã chi trả");

        private final String label;
        PayrollStatus(String label) { this.label = label; }
    }
}