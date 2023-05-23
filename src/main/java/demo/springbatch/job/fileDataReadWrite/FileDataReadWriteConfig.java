package demo.springbatch.job.fileDataReadWrite;

import demo.springbatch.core.domain.accounts.Accounts;
import demo.springbatch.core.domain.accounts.AccountsRepository;
import demo.springbatch.core.domain.orders.Orders;
import demo.springbatch.core.domain.orders.OrdersRepository;
import demo.springbatch.job.fileDataReadWrite.dto.Player;
import demo.springbatch.job.fileDataReadWrite.dto.PlayerYears;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.tools.cache.FlatFileCacheBacking;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * https://docs.spring.io/spring-batch/docs/current/reference/html/readersAndWriters.html#simpleDelimitedFileReadingExample
 * desc: 주문테이블 -> 정산테이블 데이터 이관하는 batch job
 * run : --spring.batch.job.names=fileReadWriteJob
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class FileDataReadWriteConfig {

  @Autowired
  private JobBuilderFactory jobBuilderFactory;

  @Autowired
  private StepBuilderFactory stepBuilderFactory;

  @Bean
  public Job fileReadWriteJob(Step fileReadWriteStep){
    return jobBuilderFactory.get("fileReadWriteJob")
        .incrementer(new RunIdIncrementer()) // job 실행시 id 부여하는데 seq를 순차적으로 부여하기 위함
        .start(fileReadWriteStep)  // job안에 하나의 스텝을 만들어줌
        .build();
  }

  @JobScope  // job 하위에서 실행되기 때문에 해당 애너테이션 명시
  @Bean
  public Step fileReadWriteStep(ItemReader playerItemReader,
                              ItemProcessor playerItemProcessor,
                              ItemWriter playerItemWriter){
    return stepBuilderFactory.get("fileReadWriteStep")
        .<Player, PlayerYears>chunk(5)//5개의 데이터 단위로 Player class 타입의 데이터를 읽어서 , write 처리
        .reader(playerItemReader)
//        .writer(new ItemWriter() {
//          @Override
//          public void write(List items) throws Exception {
//            items.forEach(System.out::println);
//          }
//        })
        .processor(playerItemProcessor)
        .writer(playerItemWriter)
        .build();
  }

  @StepScope
  @Bean
  public FlatFileItemReader<Player> playerItemReader(){
    return new FlatFileItemReaderBuilder<Player>()
        .name("playerItemReader")
        .resource(new FileSystemResource("Player.csv"))
        .lineTokenizer(new DelimitedLineTokenizer())
        .fieldSetMapper(new PlayerFieldSetMapper())
        .linesToSkip(1)
        .build();
  }

  @StepScope
  @Bean
  public ItemProcessor<Player, PlayerYears> playerItemProcessor(){
    return new ItemProcessor<Player, PlayerYears>() {
      @Override
      public PlayerYears process(Player item) throws Exception {
        return new PlayerYears(item);
      }
    };
  }

  @StepScope
  @Bean
  public FlatFileItemWriter<PlayerYears> playerItemWriter(){
    BeanWrapperFieldExtractor<PlayerYears> fieldExtractor = new BeanWrapperFieldExtractor<>();
    fieldExtractor.setNames(new String[]{"ID","lastName","position","yearsExperience"});
    fieldExtractor.afterPropertiesSet();

    DelimitedLineAggregator<PlayerYears> lineAggregator = new DelimitedLineAggregator<>();
    lineAggregator.setDelimiter(",");
    lineAggregator.setFieldExtractor(fieldExtractor);

    FileSystemResource outputResource = new FileSystemResource("Player_output.txt");

    return new FlatFileItemWriterBuilder<PlayerYears>()
        .name("playerItemWriter")
        .resource(outputResource)
        .lineAggregator(lineAggregator)
        .build();
  }

}
