package example.grpc.controller;

import example.grpc.entity.Account;
import example.grpc.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author icodening
 * @date 2022.07.27
 */
@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/{id}")
    public Object getAccount(@PathVariable(name = "id") Long id) {
        Account account = accountService.getAccount(id);
        if (account == null) {
            throw new RuntimeException("no such account for id [" + id + "]");
        }
        return account;
    }

    @GetMapping()
    public Object getAccount() {
        return accountService.findAll();
    }
}
