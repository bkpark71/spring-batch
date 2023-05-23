package demo.springbatch.conditionalStep;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * desc: step 결과에 따른 다음 step 분기 처리
 * run : --spring.batch.job.names=conditionalStepJob
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ConditionalStepJobConfig {

  @Autowired
  private JobBuilderFactory jobBuilderFactory;

  @Autowired
  private StepBuilderFactory stepBuilderFactory;

  @Bean
  public Job conditionalStepJob(
      Step conditionalStartStep,
      Step conditionalAllStep,
      Step conditionalFailStep,
      Step conditionalCompletedStep){
    return jobBuilderFactory.get("conditionalStepJob")
        .incrementer(new RunIdIncrementer()) // job 실행시 id 부여하는데 seq를 순차적으로 부여하기 위함
        .start(conditionalStartStep)
          .on("FAILED").to(conditionalFailStep)
        .from(conditionalStartStep)
          .on("COMPLETED").to(conditionalCompletedStep)
        .from(conditionalStartStep)  // fail 도 completed 도 아닌 경우
          .on("*").to(conditionalAllStep)
        .end()
        .build();
  }

  @JobScope  // job 하위에서 실행되기 때문에 해당 애너테이션 명시
  @Bean
  public Step conditionalStartStep(){
    return stepBuilderFactory.get("conditionalStartStep")
        .tasklet(new Tasklet() {
                   @Override
                   public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
//                     System.out.println("conditionalStartStep");
//                     return RepeatStatus.FINISHED;
                     throw new Exception("Start Step exception");
                   }
                 })
        .build();
  }

  @JobScope  // job 하위에서 실행되기 때문에 해당 애너테이션 명시
  @Bean
  public Step conditionalAllStep(){
    return stepBuilderFactory.get("conditionalAllStep")
        .tasklet(new Tasklet() {
          @Override
          public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
            System.out.println("conditionalAllStep");
            return RepeatStatus.FINISHED;
          }
        })
        .build();
  }


  @JobScope  // job 하위에서 실행되기 때문에 해당 애너테이션 명시
  @Bean
  public Step conditionalCompletedStep(){
    return stepBuilderFactory.get("conditionalCompletedStep")
        .tasklet(new Tasklet() {
          @Override
          public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
            System.out.println("conditionalCompletedStep");
            return RepeatStatus.FINISHED;
          }
        })
        .build();
  }
  @JobScope  // job 하위에서 실행되기 때문에 해당 애너테이션 명시
  @Bean
  public Step conditionalFailStep(){
    return stepBuilderFactory.get("conditionalFailStep")
        .tasklet(new Tasklet() {
          @Override
          public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
            System.out.println("conditionalFailStep");
            return RepeatStatus.FINISHED;
          }
        })
        .build();
  }

}
