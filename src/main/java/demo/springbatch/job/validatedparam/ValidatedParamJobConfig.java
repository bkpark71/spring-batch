package demo.springbatch.job.validatedparam;

import demo.springbatch.job.validatedparam.validator.FileParamValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.CompositeJobParametersValidator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * desc: 파일 이름 파라미터 전달 그리고 검증
 * run : --spring.batch.job.names=validatedParamJob -fileName=test.csv
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ValidatedParamJobConfig {
  @Autowired
  private JobBuilderFactory jobBuilderFactory;

  @Autowired
  private StepBuilderFactory stepBuilderFactory;

  @Bean
  public Job validatedParamJob(Step validatedParamStep){
    return jobBuilderFactory.get("validatedParamJob")
        .incrementer(new RunIdIncrementer()) // job 실행시 id 부여하는데 seq를 순차적으로 부여하기 위함
       // .validator(new FileParamValidator())  // csv 파일인지 검증
        .validator(multipleValidator())
        .start(validatedParamStep)  // job안에 하나의 스텝을 만들어줌, 주입받은 step을 호출해줌.
        .build();
  }

  private CompositeJobParametersValidator multipleValidator(){
    CompositeJobParametersValidator validator = new CompositeJobParametersValidator();
    validator.setValidators(Arrays.asList(new FileParamValidator())); // 여러 개의 validator를 리스트로 등록할 수 있다.

    return validator;
  }

  @JobScope  // job 하위에서 실행되기 때문에 해당 애너테이션 명시
  @Bean
  public Step validatedParamStep(Tasklet validatedParamTasklet){
    return stepBuilderFactory.get("validatedParamStep")
        .tasklet(validatedParamTasklet)  // itemReader, Itemprocessor, writer 가 있는데 읽고 쓸게 없는 단순한 스텝을 만드는 예제임,
        .build();                       // 주입받은 tasklet을 호출해줌
  }

  @StepScope // step 하위에서 실행되기 때문에 해당 애너테이션 명시
  @Bean
  public Tasklet validatedParamTasklet(@Value("#{jobParameters['fileName']}") String fileName){
    return new Tasklet() {
      @Override
      public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info(fileName);  // 배치 수행시 특정 파일을 핸들링하려면 파일명을 파라미터로 넘길 수 있다.
        log.info("validated param spring batch");
        return RepeatStatus.FINISHED;  // 원하는 작업이 끝난 후에 이 작업을 어떻게 할것인가 상태코드를 명시
      }
    };
  }

}
