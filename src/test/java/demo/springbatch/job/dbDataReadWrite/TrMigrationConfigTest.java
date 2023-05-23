package demo.springbatch.job.dbDataReadWrite;

import demo.springbatch.core.domain.accounts.AccountsRepository;
import demo.springbatch.core.domain.orders.Orders;
import demo.springbatch.core.domain.orders.OrdersRepository;
import demo.springbatch.SpringBatchTestConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")  // mysql이 아니라 h2 로 테스트하려면 추가해줘야 함.
@SpringBatchTest
@SpringBootTest(classes = {SpringBatchTestConfig.class, TrMigrationConfig.class})
class TrMigrationConfigTest {
  @Autowired
  private JobLauncherTestUtils jobLauncherTestUtils;

  @Autowired
  private OrdersRepository ordersRepository;

  @Autowired
  private AccountsRepository accountsRepository;

  @AfterEach
  public void cleanupEach(){
    ordersRepository.deleteAll();
    accountsRepository.deleteAll();
  }

  @Test
  public void success_noData() throws Exception {
    //when
    JobExecution execution = jobLauncherTestUtils.launchJob();
    //then
    Assertions.assertEquals(execution.getExitStatus(), ExitStatus.COMPLETED);
    Assertions.assertEquals(0, accountsRepository.count()); // 데이터가 하나도 없는 상태에서 테스트 결과 확인
  }

  @Test
  public void success_existData() throws Exception {
    //given
    Orders order1 = new Orders(null, "kakao gift", 18000, new Date());
    Orders order2 = new Orders(null, "naver gift", 18000, new Date());

    ordersRepository.save(order1);
    ordersRepository.save(order2);
    //when
    JobExecution execution = jobLauncherTestUtils.launchJob();
    //then
    Assertions.assertEquals(execution.getExitStatus(), ExitStatus.COMPLETED);
    Assertions.assertEquals(2, accountsRepository.count()); // 데이터가 하나도 없는 상태에서 테스트 결과 확인
  }
}