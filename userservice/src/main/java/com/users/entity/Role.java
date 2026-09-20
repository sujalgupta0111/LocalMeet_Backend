package com.users.entity;

public enum Role {

    USER,
    ADMIN,
    MANAGER;

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public boolean isManager() {
        return this == MANAGER;
    }
}