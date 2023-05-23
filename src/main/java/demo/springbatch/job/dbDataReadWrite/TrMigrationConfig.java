package demo.springbatch.job.dbDataReadWrite;

import demo.springbatch.core.domain.accounts.Accounts;
import demo.springbatch.core.domain.accounts.AccountsRepository;
import demo.springbatch.core.domain.orders.Orders;
import demo.springbatch.core.domain.orders.OrdersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * desc: 주문테이블 -> 정산테이블 데이터 이관하는 batch job
 * run : --spring.batch.job.names=trMigrationJob
 */
@Slf4j
@Configuration
public class TrMigrationConfig {

  @Autowired
  private OrdersRepository ordersRepository;

  @Autowired
  private AccountsRepository accountsRepository;

  @Autowired
  private JobBuilderFactory jobBuilderFactory;

  @Autowired
  private StepBuilderFactory stepBuilderFactory;

  @Bean
  public Job trMigrationJob(Step trMigrationStep){
    return jobBuilderFactory.get("trMigrationJob")
        .incrementer(new RunIdIncrementer()) // job 실행시 id 부여하는데 seq를 순차적으로 부여하기 위함
        .start(trMigrationStep)  // job안에 하나의 스텝을 만들어줌
        .build();
  }

  @JobScope  // job 하위에서 실행되기 때문에 해당 애너테이션 명시
  @Bean
  public Step trMigrationStep(ItemReader trOrderReader,
                              ItemProcessor trOrderProcessor,
                              ItemWriter trOrderWriter){
    return stepBuilderFactory.get("trMigrationStep")
        .<Orders, Accounts>chunk(5)//5개의 데이터 단위로 Orders타입의 데이터를 읽어서 Orders타입의 데이터로 write 처리
        .reader(trOrderReader)
//        .writer(new ItemWriter(){
//          @Override
//          public void write(List items) throws Exception {
//            items.forEach(System.out::println);
//          }
//        })
        .processor(trOrderProcessor)
        .writer(trOrderWriter)
        .build();
  }


  @Bean
  @StepScope
  // itemReader, Itemprocessor, itemWriter 로 작업을 진행
  public RepositoryItemReader<Orders> trOrderReader(){
    return new RepositoryItemReaderBuilder<Orders>()
        .name("trOrdersReader")
        .repository(ordersRepository)
        .methodName("findAll")
        .pageSize(5)
        .arguments(Arrays.asList())
        .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
        .build();
  }


  @StepScope
  @Bean
  public ItemProcessor<Orders, Accounts> trOrderProcessor(){
    return new ItemProcessor<Orders, Accounts>() {
      @Override
      public Accounts process(Orders item) throws Exception {
        return new Accounts(item);
      }
    };
  }

//  @StepScope
//  @Bean
//  public RepositoryItemWriter<Accounts> trOrderWriter(){
//    return new RepositoryItemWriterBuilder<Accounts>()
//        .repository(accountsRepository)
//        .methodName("save")
//        .build();
//  }

  @StepScope
  @Bean
  public ItemWriter<Accounts> trOrderWriter(){// repositoryitemwriter 사용하지 않고 직접 구현하는 경우
    return new ItemWriter<Accounts>(){
      @Override
      public void write(List<? extends Accounts> items) throws Exception {
        items.forEach(item->accountsRepository.save(item));
      }
    };
  }
}
