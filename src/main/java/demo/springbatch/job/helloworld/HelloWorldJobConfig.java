package demo.springbatch.job.helloworld;

import lombok.RequiredArgsConstructor;
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
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * desc: Hello world를 출력하는 batch job
 * run : --spring.batch.job.names=helloWorldJob
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class HelloWorldJobConfig {
  @Autowired
  private JobBuilderFactory jobBuilderFactory;

  @Autowired
  private StepBuilderFactory stepBuilderFactory;

  @Bean
  public Job helloWorldJob(){
    return jobBuilderFactory.get("helloWorldJob")
        .incrementer(new RunIdIncrementer()) // job 실행시 id 부여하는데 seq를 순차적으로 부여하기 위함
        .start(helloWorldStep())  // job안에 하나의 스텝을 만들어줌
        .build();
  }

  @JobScope  // job 하위에서 실행되기 때문에 해당 애너테이션 명시
  @Bean
  public Step helloWorldStep(){
    return stepBuilderFactory.get("helloWorldStep")
        .tasklet(helloWorldTasklet())  // itemReader, Itemprocessor, writer 가 있는데 읽고 쓸게 없는 단순한 스텝을 만드는 예제임
        .build();
  }

  @StepScope // step 하위에서 실행되기 때문에 해당 애너테이션 명시
  @Bean
  public Tasklet helloWorldTasklet(){
    return new Tasklet() {
      @Override
      public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("Helloworld spring batch");
        return RepeatStatus.FINISHED;  // 원하는 작업이 끝난 후에 이 작업을 어떻게 할것인가 상태코드를 명시
      }
    };
  }

}
