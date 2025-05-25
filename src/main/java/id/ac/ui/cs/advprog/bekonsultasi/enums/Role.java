package id.ac.ui.cs.advprog.bekonsultasi.enums;

import lombok.Getter;

@Getter
public enum Role {
    PACILIAN("PACILIAN"),
    CAREGIVER("CAREGIVER");
    
    private final String value;
    
    private Role(String value) {
        this.value = value;
    }
    
    public static boolean contains(String param) {
        for (Role role : Role.values()) {
            if (role.name().equals(param)) {
                return true;
            }
        }
        return false;
    }
}