package com.quetoquenana.userservice.dto;

import com.quetoquenana.userservice.model.Person;
import com.quetoquenana.userservice.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEmailInfo {
    private String username;
    private String personName;
    private String personLastname;

    public static UserEmailInfo from(User user) {
        if (user == null) return null;
        String username = user.getUsername();
        String name = null;
        String lastname = null;
        Person p = user.getPerson();
        if (p != null) {
            name = p.getName();
            lastname = p.getLastname();
        }
        return new UserEmailInfo(username, name, lastname);
    }

    /**
     * Convenience: build a lightweight User instance (used by default EmailService adapters).
     */
    public User toUser() {
        User u = new User();
        u.setUsername(this.username);
        Person p = new Person();
        p.setName(this.personName);
        p.setLastname(this.personLastname);
        u.setPerson(p);
        return u;
    }
}

