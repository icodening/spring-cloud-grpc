package example.grpc.service;

import example.grpc.entity.Account;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author icodening
 * @date 2022.07.27
 */
@Service
public class AccountServiceImpl implements AccountService {

    private final Map<Long, Account> accountMap;

    public AccountServiceImpl() {
        HashMap<Long, Account> accountHashMap = new HashMap<>();
        accountHashMap.put(1L, new Account().setId(1L).setUsername("ZhangSan").setPassword("123456").setCreateTime(new Date()));
        accountHashMap.put(2L, new Account().setId(2L).setUsername("LiSi").setPassword("456789").setCreateTime(new Date()));
        accountHashMap.put(3L, new Account().setId(3L).setUsername("WangWu").setPassword("789456").setCreateTime(new Date()));
        this.accountMap = accountHashMap;
    }

    @Override
    public Account getAccount(Long id) {
        return accountMap.get(id);
    }

    @Override
    public List<Account> findAll() {
        return new ArrayList<>(accountMap.values());
    }
}
