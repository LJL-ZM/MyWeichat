package com.example.myweixin_client;

public class LoginReq {
    private String username;
    private String password;

    public LoginReq(String username, String password){
        this.password = password;
        this.username = username;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
