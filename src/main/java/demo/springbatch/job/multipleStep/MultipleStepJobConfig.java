package demo.springbatch.job.multipleStep;

import demo.springbatch.job.fileDataReadWrite.PlayerFieldSetMapper;
import demo.springbatch.job.fileDataReadWrite.dto.Player;
import demo.springbatch.job.fileDataReadWrite.dto.PlayerYears;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

/**
 * desc: 다중 step 사용하기, step to step 데이터 전달
 * run : --spring.batch.job.names=multipleStepJob
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MultipleStepJobConfig {

  @Autowired
  private JobBuilderFactory jobBuilderFactory;

  @Autowired
  private StepBuilderFactory stepBuilderFactory;

  @Bean
  public Job multipleStepJob(Step multipleStep1, Step multipleStep2, Step multipleStep3){
    return jobBuilderFactory.get("multipleStepJob")
        .incrementer(new RunIdIncrementer()) // job 실행시 id 부여하는데 seq를 순차적으로 부여하기 위함
        .start(multipleStep1)
        .next(multipleStep2)
        .next(multipleStep3)  // job안에 3개의 스텝을 만들어줌
        .build();
  }

  @JobScope  // job 하위에서 실행되기 때문에 해당 애너테이션 명시
  @Bean
  public Step multipleStep1(){
    return stepBuilderFactory.get("multipleStep1")
        .tasklet((contribution, chunkContext) -> {
          System.out.println("step1");
          return RepeatStatus.FINISHED;
        })
        .build();
  }

  @JobScope  // job 하위에서 실행되기 때문에 해당 애너테이션 명시
  @Bean
  public Step multipleStep2(){
    return stepBuilderFactory.get("multipleStep2")
        .tasklet((contribution, chunkContext) -> {
          System.out.println("step2");
          ExecutionContext executionContext = chunkContext   // 다음스텝으로 데이터를 전달하는 경우 , executionContext에 값을 담아서 전달함
              .getStepContext()
              .getStepExecution()
              .getJobExecution()
              .getExecutionContext();

          executionContext.put("somekey", "hello!");

          return RepeatStatus.FINISHED;
        })
        .build();
  }

  @JobScope  // job 하위에서 실행되기 때문에 해당 애너테이션 명시
  @Bean
  public Step multipleStep3(){
    return stepBuilderFactory.get("multipleStep3")
        .tasklet((contribution, chunkContext) -> {
          System.out.println("step3");
          ExecutionContext executionContext = chunkContext // 이전스텝에서 전달한 데이터를 executionContext에서 꺼내서 사용함
              .getStepContext()
              .getStepExecution()
              .getJobExecution()
              .getExecutionContext();

          System.out.println(executionContext.get("somekey"));

          return RepeatStatus.FINISHED;
        })
        .build();
  }
}
