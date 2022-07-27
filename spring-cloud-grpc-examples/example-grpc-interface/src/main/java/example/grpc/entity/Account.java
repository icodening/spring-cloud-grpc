package example.grpc.entity;

import java.util.Date;

/**
 * @author icodening
 * @date 2022.07.27
 */
public class Account {

    private Long id;

    private String username;

    private String password;

    private Date createTime;

    public Long getId() {
        return id;
    }

    public Account setId(Long id) {
        this.id = id;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public Account setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public Account setPassword(String password) {
        this.password = password;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Account setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }
}
