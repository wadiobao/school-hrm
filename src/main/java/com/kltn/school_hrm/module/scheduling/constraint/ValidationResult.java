package com.kltn.school_hrm.module.scheduling.constraint;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationResult {
    private boolean valid;
    @Builder.Default
    private List<ValidationError> errors = new ArrayList<>();

    public void addError(ValidationError error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
        this.valid = false;
    }

    public static ValidationResult success() {
        return ValidationResult.builder().valid(true).errors(new ArrayList<>()).build();
    }
}

