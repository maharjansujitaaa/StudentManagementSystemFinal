package model;

public class SignUpModel {

    private String fullName;
    private String email;
    private String username;
    private String password;
    private String confirmPassword;
    private Role   selectedRole;

    public SignUpModel() {
        this.selectedRole = Role.STUDENT;
    }

    public String getFullName()                { return fullName; }
    public void   setFullName(String v)        { this.fullName = v; }

    public String getEmail()                   { return email; }
    public void   setEmail(String v)           { this.email = v; }

    public String getUsername()                { return username; }
    public void   setUsername(String v)        { this.username = v; }

    public String getPassword()                { return password; }
    public void   setPassword(String v)        { this.password = v; }

    public String getConfirmPassword()         { return confirmPassword; }
    public void   setConfirmPassword(String v) { this.confirmPassword = v; }

    public Role getSelectedRole()              { return selectedRole; }
    public void setSelectedRole(Role r)        { this.selectedRole = r; }

    public String validate() {
        if (fullName == null || fullName.trim().isEmpty())
            return "Full name is required.";
        if (email == null || email.trim().isEmpty())
            return "Email address is required.";
        if (!email.contains("@") || !email.contains("."))
            return "Please enter a valid email address.";
        if (username == null || username.trim().isEmpty())
            return "Username is required.";
        if (username.trim().length() < 3)
            return "Username must be at least 3 characters.";
        if (!username.trim().matches("[a-zA-Z0-9_]+"))
            return "Username can only contain letters, numbers and underscores.";
        if (UserStore.getInstance().userExists(username))
            return "Username already taken. Please choose another.";
        if (password == null || password.isEmpty())
            return "Password is required.";
        if (password.length() < 6)
            return "Password must be at least 6 characters.";
        if (confirmPassword == null || confirmPassword.isEmpty())
            return "Please confirm your password.";
        if (!password.equals(confirmPassword))
            return "Passwords do not match.";
        return null;
    }

    public boolean register() {
        if (validate() != null) return false;

        UserStore.getInstance().registerUser(
            fullName,
            email,
            username.trim(),
            password,
            selectedRole.name()
        );

        return true;
    }
}
