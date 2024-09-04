package com.soufianeTr.book_network.user;

import com.soufianeTr.book_network.book.Book;
import com.soufianeTr.book_network.history.BookTransactionHistory;
import com.soufianeTr.book_network.role.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "_user")
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails, Principal {
    @Id @GeneratedValue
    private Integer id;
    private String firstname;
    private String lastname;
    private LocalDate dateOfBirth;
    @Column(unique = true)
    private String email;
    private String password;
    @ManyToMany(fetch = FetchType.EAGER)
    private List<Role> roles;
    private boolean accountLocked;
    private boolean enabled;
    @OneToMany(mappedBy = "owner")
    private List<Book> books;

    @OneToMany(mappedBy = "user")
    private List<BookTransactionHistory> histories;
    @CreatedDate
    @Column(nullable = false,updatable = false)
    private LocalDateTime createdAt;
    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime lastModifiedDate;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.
                stream().
                map(role -> new SimpleGrantedAuthority(role.getName())).
        collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
    @Override
    public String getName() {
        return email;
    }
    public String fullName() {
        return firstname+" "+lastname;
    }
}
