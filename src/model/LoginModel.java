package model;

public class LoginModel {

    private String username;
    private String password;
    private Role   selectedRole;

    public LoginModel() {
        this.selectedRole = Role.STUDENT;
    }

    public String getUsername()         { return username; }
    public void   setUsername(String v) { this.username = v; }

    public String getPassword()         { return password; }
    public void   setPassword(String v) { this.password = v; }

    public Role getSelectedRole()       { return selectedRole; }
    public void setSelectedRole(Role r) { this.selectedRole = r; }

    public boolean validate() {
        return username != null && !username.trim().isEmpty()
            && password != null && !password.isEmpty();
    }

    public boolean authenticate() {
        return UserStore.getInstance()
                        .authenticate(username, password, selectedRole.name());
    }
}
