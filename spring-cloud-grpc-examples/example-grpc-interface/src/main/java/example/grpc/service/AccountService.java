package example.grpc.service;

import example.grpc.entity.Account;
import org.springframework.cloud.grpc.annotation.GrpcClient;

import java.util.List;

/**
 * @author icodening
 * @date 2022.07.27
 */
@GrpcClient(application = "grpc-provider")
public interface AccountService {

    Account getAccount(Long id);

    List<Account> findAll();

}
